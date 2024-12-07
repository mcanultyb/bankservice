package org.mcanultyb.balance.audit;

import java.util.List;
import java.util.stream.Collectors;
import org.mcanultyb.balance.model.Transaction;

/**
 * Represents a batch of audit information to be sent to an audit system that accepts batches.
 * Contains a summary of a number of transactions ie: the number of transactions that are included
 * in this entry, and the absolute value of those transactions.
 */
public final class AuditBatchEntry {
  private final int count;
  private final int absValue;
  private final List<Transaction> content;

  public AuditBatchEntry(final List<Transaction> content, final int absValue) {
    count = content.size();
    this.absValue = absValue; //could actually just calculate this dynamically but its already been calculated by code calling this
    final long[] calculatedAbs = { 0 };
    content.stream().forEach(t -> calculatedAbs[0] += Math.abs(t.getAmountPence()));
    if (calculatedAbs[0] != absValue) {
      throw new IllegalArgumentException("Sum of values in content does not match absValue");
    }
    this.content = content; //for debugging purposes
  }

  public int getBatchCount() {
    return count;
  }

  public long getBatchValue() {
    return absValue;
  }

  @Override
  public String toString() {
    return "AuditBatchEntry count="+count+" value="+absValue+" tx="+content;
  }

}