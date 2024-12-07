package org.mcanultyb.balance;

import java.util.Queue;
import java.util.Random;
import org.mcanultyb.balance.model.Transaction;
import org.mcanultyb.balance.model.TransactionType;

public final class TransactionGenerator implements Runnable{
  private static final int MIN_TRANSACTION_VALUE = 20000; //£200.00
  private static final int MAX_TRANSACTION_VALUE = 50000000; //£500,000.00

  private static final int TRANSACTION_VALUE_RANGE = MAX_TRANSACTION_VALUE-MIN_TRANSACTION_VALUE+1; //cater for rand.nextInt exclusive end

  private final TransactionType type;
  private final Random rand;

  private final Queue queue;


  TransactionGenerator(final TransactionType type, final Random rand, final Queue<Transaction> queue) {
    this.type = type;
    this.rand = rand;
    this.queue = queue;
  }

  private int getRandom() {
    //alternative mechanism for getting random value - both do the same I believe
    int result = 0;
    while ( (result = rand.nextInt(MAX_TRANSACTION_VALUE+1)) < MIN_TRANSACTION_VALUE);
    return result;
  }

  public void run() {
    long startTime = System.nanoTime();
    long generated = 0;
    int sleepTime = 40; //assuming 25 per sec and assuming no time to actually do the work to start with...
    final int targetPerSec = 25;
    long totalProduced = 0;
    long iterations = 0;
    while (true) {
      try {
        final int amount = getRandom(); //rand.nextInt(TRANSACTION_VALUE_RANGE) + MIN_TRANSACTION_VALUE;
        final Transaction transaction = new Transaction(type.apply(amount));
        generated++;
        totalProduced ++;
        final long now = System.nanoTime();
        final long elapsed = now - startTime;
        if (elapsed > 1000000000) { //ns > 1s
          iterations++;
          //work out average # created per second
          final double perSec = ((double)totalProduced / iterations);
          System.out.println(String.format("%s >> transactions generated per sec avg = %.2f",type.name(),perSec));
          if (perSec < targetPerSec && sleepTime > 1) {
            sleepTime -= 1;
            //System.out.println(type.name()+ ">> reduced sleep time to " + sleepTime + " to speed it up a bit");

          } else if (perSec > targetPerSec) {
            sleepTime += 1;
            //System.out.println(type.name()+" >> increased sleep time to " + sleepTime + " to slow it down a bit");
          }
          generated = 0;
          startTime = System.nanoTime();
        }
        queue.add(transaction);


        Thread.sleep(sleepTime);
      } catch (final Exception e) {
        e.printStackTrace();
        return; //probably interrupted
      }
    }
  }

}