package org.mcanultyb.balance;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.mcanultyb.balance.model.PublishOnlyQueue;
import org.mcanultyb.balance.model.Transaction;
import org.mcanultyb.balance.model.TransactionType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testable
public class TransactionGeneratorTaskTest {


  @Test
  public void testNumberOfTransactionsPublishedToQueueMatches() {
    final BlockingQueue<Transaction> queue = new ArrayBlockingQueue(100000);
    final PublishOnlyQueue publishOnlyQueue = new PublishOnlyQueue(queue);
    final TransactionGeneratorTask task = new TransactionGeneratorTask(TransactionType.CREDIT, new Random(), publishOnlyQueue, 1, 10);
    for (int i=0;i<100000;i++) {
      task.run();
    }
    assertEquals(100000,queue.size());
    final List<Transaction> outOfRange = queue.stream().filter(t -> t.getAmountPence() > 10 || t.getAmountPence() < 1).collect(Collectors.toList());
    assertTrue(outOfRange.isEmpty());
  }
}
