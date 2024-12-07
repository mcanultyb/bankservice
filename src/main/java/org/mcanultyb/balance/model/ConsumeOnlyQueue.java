package org.mcanultyb.balance.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Wraps a BlockingQueue and only allows the poll() action, thereby preventing a consumer from putting items
 * onto the queue.
 */
public class ConsumeOnlyQueue {
  private final BlockingQueue<Transaction> queue;

  public ConsumeOnlyQueue(final BlockingQueue<Transaction> queue) {
    this.queue = queue;
  }

  public Transaction poll(final long timeout, final TimeUnit unit) throws InterruptedException {
    return queue.poll(timeout,unit);
  }
}
