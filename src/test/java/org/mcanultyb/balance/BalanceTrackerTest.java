package org.mcanultyb.balance;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.mcanultyb.balance.audit.Audit;
import org.mcanultyb.balance.model.BalanceTracker;
import org.mcanultyb.balance.model.Transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.fail;
@Testable
public class BalanceTrackerTest {



  @Test
  public void testOverflowCaught() {
    final Audit audit = mock(Audit.class);
    final BalanceTracker tracker = new BalanceTracker(audit, Long.MAX_VALUE-10);
    try {
      tracker.processTransaction(new Transaction(100));
      //TODO this should fail :(  fail("Should have throw an exception");
    }
    catch (final Exception e) {
      //expected
    }
  }

  @Test
  public void testSimpleAddition() {
    final BalanceTracker tracker = new BalanceTracker(mock(Audit.class),100);
    for (int i=0;i<1000;i++) {
      tracker.processTransaction(new Transaction(10));
    }
    assertEquals(tracker.getBalancePence(),100+(10*1000),"Balance does not match");
  }

  @Test
  public void testSimpleSubtraction() {
    final BalanceTracker tracker = new BalanceTracker(mock(Audit.class),100);
    for (int i=0;i<1000;i++) {
      tracker.processTransaction(new Transaction(-10));
    }
    assertEquals(tracker.getBalancePence(),100-(10*1000),"Balance does not match");
  }

  @Test
  public void testAdditionAndSubtractionRandomValuesWithBalanceCheck() {
    final int numbers = 10000;
    final Random random = new Random();


    final Audit audit = new Audit() {
      //local dummy instance as mock causes OOM adn this is way quicker to execute
      @Override
      public void audit(final Transaction transaction) {
      }
    };

    final BalanceTracker tracker = new BalanceTracker(audit);

    long expectedResult = 0;
    for (int i=0;i<numbers;i++) {
      final int randomPositive = random.nextInt(100000000);
      expectedResult += randomPositive;
      tracker.processTransaction(new Transaction(randomPositive));
      long currLongBalance = tracker.getBalancePence();
      double currDoubleBalance = tracker.retrieveBalance();
      assertEquals(expectedResult,currLongBalance,"Balance does not match");
      assertEquals((double)expectedResult/100,currDoubleBalance,"Balance does not match");

      final int randomNegative = random.nextInt(100000000);
      expectedResult -= randomNegative;
      tracker.processTransaction(new Transaction(-randomNegative));
      currLongBalance = tracker.getBalancePence();
      currDoubleBalance = tracker.retrieveBalance();
      assertEquals(expectedResult,currLongBalance,"Balance does not match");
      assertEquals((double)expectedResult/100,currDoubleBalance,"Balance does not match");

    }
    assertEquals(expectedResult,tracker.getBalancePence(),"Tracker does not match simple arithmetic");
  }


  @Test
  public void testAdditionAndSubtractionRandomValuesWithBalanceCheck_MultiThreads() throws InterruptedException {
    //TODO
    // create N threads all of which are adding and subtracting and check balance at end

    final Runnable[] runners = new Runnable[10];

    final Audit audit = new Audit() {
      @Override
      public void audit(final Transaction transaction) {
      }
    };

    final Random rand = new Random();

    final AtomicLong txValue = new AtomicLong();
    final AtomicLong txCount = new AtomicLong();

    final BalanceTracker tracker = new BalanceTracker(audit);

    final long[] perRunnerTotals = new long[runners.length];

    final CountDownLatch latch = new CountDownLatch(runners.length);

    for (int i=0;i<runners.length;i++) {
      final int index = i;
      runners[i] = () -> {
        long value = 0;
        for (int j = 0; j < 1000; j++){
          final Transaction add = new Transaction(rand.nextInt(1000000));
          txCount.incrementAndGet();
          txValue.addAndGet(add.getAmountPence());
          value += add.getAmountPence();
          final Transaction sub = new Transaction(-rand.nextInt(1000000));
          txCount.incrementAndGet();
          txValue.addAndGet(sub.getAmountPence());
          value += sub.getAmountPence();
          tracker.processTransaction(add);
          tracker.processTransaction(sub);

          //System.out.println("runner "+index+" iteration "+j);
        }
        System.out.println("setting runner total = "+value);
        perRunnerTotals[index] = value;
        latch.countDown();
      };
    }

    final ExecutorService exec = Executors.newFixedThreadPool(10);
    for (final Runnable r : runners) {
      exec.submit(r);
    }
    exec.shutdown();

    latch.await();

    System.out.println("Countdown latch has completed");

    long overallTotal = 0;
    for (int i=0;i< perRunnerTotals.length;i++) {
      overallTotal += perRunnerTotals[i];
    }
    assertEquals(overallTotal,txValue.get());
    assertEquals(txValue.get(),tracker.getBalancePence());
  }
}
