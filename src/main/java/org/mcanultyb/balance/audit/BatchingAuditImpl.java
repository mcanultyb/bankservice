package org.mcanultyb.balance.audit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.mcanultyb.balance.model.Transaction;

/**
 * Implementation of Audit that will collect transactions into batches for forwarding to the provided BatchedAudit impl.
 */
public class BatchingAuditImpl implements Audit {

  private final BatchedAudit audit; //underlying audit implementation
  private final List<Transaction> auditEntries = new ArrayList<>();
  private final int batchSize;
  private final int maxBatchValue;


  private static final Comparator<Transaction> HIGHEST_ABS_FIRST_COMPARATOR = new Comparator<Transaction>() {

    @Override
    public int compare(final Transaction t1, final Transaction t2) {
      final int a1 = Math.abs(t1.getAmountPence());
      final int a2 = Math.abs(t2.getAmountPence());
      return Integer.compare(a1, a2);
    }
  }.reversed();


  /**
   * @param audit the underlying BatchedAudit impl that we will pass batches to
   * @param trigger the number of transactions we must collect before triggering a submission to the underlying BatchedAudit impl.  Batch will not be sent until this is reached.
   * @param maxBatchEntryValue the maximum value that we can send in any given item within a batch.
   */
  public BatchingAuditImpl(final BatchedAudit audit, final int trigger, final int maxBatchEntryValue) {
    if (trigger <= 0) {
      throw new IllegalArgumentException("trigger must be >= 1");
    }
    batchSize = trigger;
    maxBatchValue = maxBatchEntryValue;
    this.audit = audit;
  }

  @Override
  public void audit(final Transaction transaction) {
    final List<Transaction> toSend = new ArrayList<>();
    synchronized (auditEntries) {
      auditEntries.add(transaction);
      if (auditEntries.size() == batchSize) {
        System.out.println("triggered batch audit submission");
        toSend.addAll(auditEntries);
        auditEntries.clear();
      }
    }
    if (!toSend.isEmpty()) {
      final List<AuditBatchEntry> entries = createBatch(toSend); //could be done async
      audit.audit(entries);
    }
  }

  private List<AuditBatchEntry> createBatch(final List<Transaction> transactions) {
    int totalTxValue = 0;
    for (final Transaction t : transactions) {
      totalTxValue += Math.abs(t.getAmountPence()); //just to make sure this adds up to total value in batches
    }

    final List<AuditBatchEntry> entries = batchTransactions(transactions);
    int totalBatchValue = 0;
    int totalBatchCount = 0;

    for (final AuditBatchEntry x : entries) {
      totalBatchCount += x.getBatchCount();
      totalBatchValue += x.getBatchValue();
    }

    if (totalBatchCount != batchSize) {
      throw new IllegalStateException("Number of transactions in all batches (" + totalBatchCount + ") does not match batch size (" + batchSize + ")");
    }

    if (totalBatchValue != totalTxValue) {
      throw new IllegalStateException("Value of transactions in all batches (" + totalBatchValue + ") does not match total values of transactions (" + totalTxValue + ")");
    }

    System.out.println("NUMBER OF BATCHES = " + entries.size());
    return entries;
  }

  private List<AuditBatchEntry> batchTransactions(final List<Transaction> transactions) {
    //each batch must not exceed abs value of 1,000,000
    final List<Transaction> sorted = new ArrayList<>(transactions);
    sorted.sort(HIGHEST_ABS_FIRST_COMPARATOR);

    //sanity checking range to make sure it makes sense
    System.out.println("RANGE = " + Math.abs(sorted.get(0).getAmountPence()) + " TO " + Math.abs(sorted.get(sorted.size() - 1).getAmountPence()));
    final List<AuditBatchEntry> batches = new ArrayList<>();
    AuditBatchEntry ab = null;
    while (!sorted.isEmpty() && (ab = getNextBatch(sorted)) != null) {
      batches.add(ab);
    }
    return batches;
  }

  private final AuditBatchEntry getNextBatch(final List<Transaction> sorted) {
    //brute force really - just keep going down to ever-smaller transactions
    //to find something that will 'fit' in this batch
    //tried taking biggest first, then working up from smallest to fill gaps
    //but this gives more batches overall (around 300 vs 250-260)
    //tried not sorting it and that was worse.
    final List<Transaction> currentBatch = new ArrayList<>();
    int currentAbsValue = 0;
    for (final Transaction t : sorted) {
      final int absValue = Math.abs(t.getAmountPence());
      if (absValue <= (maxBatchValue - currentAbsValue)) {
        currentBatch.add(t);
        currentAbsValue += absValue;
        if (currentAbsValue == maxBatchValue) {
          break;
        }
      }
    }
    sorted.removeAll(currentBatch);
    return new AuditBatchEntry(currentBatch, currentAbsValue);
  }
}
