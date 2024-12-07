package org.mcanultyb.balance;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.TimerTask;
import org.mcanultyb.balance.model.PublishOnlyQueue;
import org.mcanultyb.balance.model.Transaction;
import org.mcanultyb.balance.model.TransactionType;

/**
 * TimerTask that will generate a single transaction of a given type with a random value and publish it to the queue
 *
 */
public class TransactionGeneratorTask extends TimerTask {

  private final TransactionType type;
  private final Random rand;
  private final PublishOnlyQueue queue;
  private final int minValueInclusive;
  private final int randRange;
  private final int maxValueExclusive;

  private long iterations;
  private double avgPerSec;

  private final long firstExecTime = System.currentTimeMillis();

  /**
   * TimerTask that will generate a single transaction of a given type with a random value and publish it to the queue
   * @param type type of transaction - each task only generates a single type
   * @param rand random number generator
   * @param queue queue to publish the transaction to
   */
  public TransactionGeneratorTask(final TransactionType type, final Random rand, final PublishOnlyQueue queue, final int minValueIncl, final int maxValueExcl) {
    this.type = type;
    this.rand = rand;
    this.queue = queue;
    minValueInclusive = minValueIncl;
    maxValueExclusive = maxValueExcl;
    randRange = maxValueExcl-minValueIncl+1; //cater for rand.nextInt exclusive end (ie make sure we can generate up to max
  }

  public void run() {
    // for timing purposes - to show this (on average) will hit 25 per sec)
    iterations++;
    final long elapsedTimeSecs = (System.currentTimeMillis() - firstExecTime)/1000;
    avgPerSec =  ((double) iterations) / elapsedTimeSecs;

    if (elapsedTimeSecs % 10 == 0) {
      System.out.println(String.format(this + " " +
                         LocalDateTime.ofInstant(Instant.ofEpochMilli(scheduledExecutionTime()), ZoneId.systemDefault()) +
                         " @ " + LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()),
                         ZoneId.systemDefault()) + " "+ type.name() + " " +
                         Thread.currentThread() + " TGT run() - avgPerSec=%.2f", avgPerSec));
    }

    //the actual work
    final int amount = rand.nextInt(randRange) + minValueInclusive;
    final Transaction transaction = new Transaction(type.apply(amount));
    queue.add(transaction);
  }
}
