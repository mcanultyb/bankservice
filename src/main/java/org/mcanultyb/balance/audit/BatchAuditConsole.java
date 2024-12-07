package org.mcanultyb.balance.audit;

import java.io.PrintStream;
import java.util.List;

/**
 * Implementation of a BatchedAudit that just writes the audit data to System.out or provided PrintStream
 */
public class BatchAuditConsole implements BatchedAudit{
  private final PrintStream out;

  /**
   * Creates a BatchAuditConsole which will output to the provided PrintStream
   * @param out the PrintStream to write output to
   */
  public BatchAuditConsole(final PrintStream out) {
    this.out = out;
  }

  /**
   * Creates a BatchAuditConsole which will output to stdout
   */
  public BatchAuditConsole() {
    this(System.out);
  }

  @Override
  public void audit(final List<AuditBatchEntry> entries) {
    final StringBuilder sb = new StringBuilder(1024 * 1024);
    sb.append("{").append(System.lineSeparator());
    sb.append("\tsubmissions: {").append(System.lineSeparator());
    sb.append("\t\tbatches: [").append(System.lineSeparator());
    addBatches(sb, entries);
    //append each one, comma separated
    sb.append("\t\t]").append(System.lineSeparator());
    sb.append("\t}").append(System.lineSeparator());
    sb.append("}");
    out.println(sb);
  }

  private void addBatches(final StringBuilder sb, final List<AuditBatchEntry> entries) {
    for (int i = 0; i < entries.size(); i++) {
      final AuditBatchEntry b = entries.get(i);
      sb.append("\t\t{").append(System.lineSeparator());
      sb.append("\t\t\ttotalValueOfAllTransactions: ").append(b.getBatchValue()).append(System.lineSeparator());
      sb.append("\t\t\tcountOfTransactions: ").append(b.getBatchCount()).append(System.lineSeparator());
      sb.append("\t\t}");
      if (i < entries.size() - 1) {
        sb.append(",");
      }
      sb.append(System.lineSeparator());
    }
  }
}
