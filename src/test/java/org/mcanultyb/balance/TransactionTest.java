package org.mcanultyb.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.mcanultyb.balance.model.Transaction;

@Testable
public class TransactionTest {

  @Test
  public void verifyTransactionValue_SingleThread() {
    final Random random = new Random();
    for (int i=0;i<100000;i++) {
      int val = random.nextInt(50000000);
      final boolean negate = random.nextBoolean();
      if (negate) {
        val = -val;
      }
      final Transaction t = new Transaction(val);
      assertEquals(t.getAmountPence(), val, "Value doesn't match constructor value");
    }
  }

  @Test
  public void verifyTransactionID_SingleThread() {
    final Random random = new Random();
    final Set<Long> previousIDs = new HashSet<>();
    for (int i=0;i<100000;i++) {
      final Transaction t = new Transaction(0);
      final boolean notPreviouslyPresent = previousIDs.add(t.getID());
      assertTrue(notPreviouslyPresent,"Transaction ID "+t.getID()+" has been used before");
    }
  }

  @Test
  public void verifyTransactionID_MultipleThreads() throws InterruptedException {
    final Random random = new Random();
    final Runnable[] runnables = new Runnable[3];
    final Map<Long,Transaction> ids = new ConcurrentHashMap<>();
    final AtomicInteger doneCount = new AtomicInteger();

    for (int k=0;k<runnables.length;k++){
      runnables[k] = () -> {
        //create a number of transactions
        for (int i=0;i<100000;i++) {
          final Transaction t = new Transaction(0);
          final boolean previouslyPresent = ids.put(Long.valueOf(t.getID()),t) != null;
          assertFalse(previouslyPresent,"ID "+t.getID()+" should not be present in 'this' list of ids");
        }
        System.out.println("Thread completed");
        doneCount.incrementAndGet();
      };
    }
    //start some threads to run the above
    final Thread[] threads = new Thread[runnables.length];
    for (int i=0;i<runnables.length;i++){
      threads[i] = new Thread(runnables[i]);
      threads[i].start();
    }

    //wait for them all to finish

    int done = 0;
    while ((done = doneCount.get()) != runnables.length) {
      try {
        System.out.println("Waiting for "+(runnables.length-done)+" to complete");
        Thread.sleep(200);
      }
      catch (final Exception e) {
        // TODO probably could use CountdownLatch
      }

    }
    assertEquals(ids.size(),runnables.length * 100000,"Should be same number of IDs as the # of threads x iterations (# of transactions)");
  }
}
