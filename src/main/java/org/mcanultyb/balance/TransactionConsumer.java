package org.mcanultyb.balance;

import java.util.concurrent.TimeUnit;
import org.mcanultyb.balance.model.BalanceTracker;
import org.mcanultyb.balance.model.ConsumeOnlyQueue;
import org.mcanultyb.balance.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Reads transactions off a queue and processes them
 */
public class TransactionConsumer implements Runnable {

  private final ConsumeOnlyQueue queue;
  @Autowired private final BalanceTracker balanceTracker;

  public TransactionConsumer(final ConsumeOnlyQueue queue, final BalanceTracker tracker) {
    this.queue = queue;
    balanceTracker = tracker;

  }

    @Override
    public void run() {
      while (true) {
        try {
          getAndProcessNextTransaction();
        }
        catch (final Exception e) {
          e.printStackTrace();
          return;
        }
      }
    }

    Transaction getAndProcessNextTransaction() throws InterruptedException {
      final Transaction tx = queue.poll(100, TimeUnit.MILLISECONDS);
      if (tx != null) {
        balanceTracker.processTransaction(tx);
      }
      return tx;
    }
}
