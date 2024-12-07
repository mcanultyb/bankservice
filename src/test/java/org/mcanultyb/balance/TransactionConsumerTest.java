package org.mcanultyb.balance;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.mcanultyb.balance.audit.Audit;
import org.mcanultyb.balance.model.BalanceTracker;
import org.mcanultyb.balance.model.ConsumeOnlyQueue;
import org.mcanultyb.balance.model.Transaction;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
@Testable
public class TransactionConsumerTest {

  @Test
  public void testConsume() throws InterruptedException {
    final BalanceTracker tracker = new BalanceTracker(mock(Audit.class));
    final BlockingQueue<Transaction> queue = new ArrayBlockingQueue<>(1000);
    final int[] txProcessed = { 0 };
    final ConsumeOnlyQueue consumeOnlyQueue = new ConsumeOnlyQueue(queue);
    final TransactionConsumer consumer = new TransactionConsumer(consumeOnlyQueue,tracker) {
      @Override
      Transaction getAndProcessNextTransaction() throws InterruptedException {
        final Transaction tx = super.getAndProcessNextTransaction();
        if (tx != null) {
          txProcessed[0]++;
        }
        return tx;
      }
    };

    final Thread t = new Thread(consumer);
    t.start();

    //basically just make sure everything we publish to the queue is consumed...
    for (int i=0;i<1000;i++) {
      queue.add(new Transaction(0));
      Thread.sleep(30);
    }

    Thread.sleep(1000*30);
    //should be done
    assertEquals(1000,txProcessed[0],"Different number of transactions processed");

  }
}
