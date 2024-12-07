package org.mcanultyb.balance.model;

import java.util.Queue;

/**
 * Wraps a Queue and only allows the add() action, thereby preventing a producer from taking items
 * from the queue.
 */
public class PublishOnlyQueue {
  private final Queue<Transaction> queue;

  public PublishOnlyQueue(final Queue<Transaction> queue){
    this.queue = queue;
  }

  public void add(final Transaction t) {
    queue.add(t);
  }
}
