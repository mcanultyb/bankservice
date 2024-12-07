package org.mcanultyb.balance;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.mcanultyb.balance.audit.AuditBatchEntry;
import org.mcanultyb.balance.audit.BatchAuditConsole;
import org.mcanultyb.balance.model.Transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
@Testable
public class BatchAuditConsoleTest {

  @Test
  public void testSingleBatch() {
    final String expected = "{"+System.lineSeparator()
        + "\tsubmissions: {"+System.lineSeparator()
        + "\t\tbatches: ["+System.lineSeparator()
        + "\t\t{"+System.lineSeparator()
        + "\t\t\ttotalValueOfAllTransactions: 500"+System.lineSeparator()
        + "\t\t\tcountOfTransactions: 1"+System.lineSeparator()
        + "\t\t}"+System.lineSeparator()
        + "\t\t]"+System.lineSeparator()
        + "\t}"+System.lineSeparator()
        + "}"+System.lineSeparator(); //trailing line separator as it does a println()

    final List<AuditBatchEntry> entries = new ArrayList<>();
    entries.add(new AuditBatchEntry(List.of(new Transaction(500)), 500));

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(baos);
    final BatchAuditConsole batchAudit = new BatchAuditConsole(ps);
    batchAudit.audit(entries);
    ps.close();
    assertEquals(expected,baos.toString());
  }

  @Test
  public void testMultipleBatch() {
    final String expected = "{"+System.lineSeparator()
        + "\tsubmissions: {"+System.lineSeparator()
        + "\t\tbatches: ["+System.lineSeparator()
        + "\t\t{"+System.lineSeparator()
        + "\t\t\ttotalValueOfAllTransactions: 1000"+System.lineSeparator()
        + "\t\t\tcountOfTransactions: 2"+System.lineSeparator()
        + "\t\t},"+System.lineSeparator()
        + "\t\t{"+System.lineSeparator()
        + "\t\t\ttotalValueOfAllTransactions: 999"+System.lineSeparator()
        + "\t\t\tcountOfTransactions: 3"+System.lineSeparator()
        + "\t\t},"+System.lineSeparator()
        + "\t\t{"+System.lineSeparator()
        + "\t\t\ttotalValueOfAllTransactions: 888"+System.lineSeparator()
        + "\t\t\tcountOfTransactions: 4"+System.lineSeparator()
        + "\t\t},"+System.lineSeparator()
        + "\t\t{"+System.lineSeparator()
        + "\t\t\ttotalValueOfAllTransactions: 777"+System.lineSeparator()
        + "\t\t\tcountOfTransactions: 3"+System.lineSeparator()
        + "\t\t},"+System.lineSeparator()
        + "\t\t{"+System.lineSeparator()
        + "\t\t\ttotalValueOfAllTransactions: 666"+System.lineSeparator()
        + "\t\t\tcountOfTransactions: 3"+System.lineSeparator()
        + "\t\t}"+System.lineSeparator()
        + "\t\t]"+System.lineSeparator()
        + "\t}"+System.lineSeparator()
        + "}"+System.lineSeparator(); //trailing line separator as it does a println()

    final List<AuditBatchEntry> entries = new ArrayList<>();
    entries.add(new AuditBatchEntry(List.of(new Transaction(1000),new Transaction(0)),1000));
    entries.add(new AuditBatchEntry(List.of(new Transaction(999),new Transaction(0),new Transaction(0)),999));
    entries.add(new AuditBatchEntry(List.of(new Transaction(888),new Transaction(0),new Transaction(0),new Transaction(0)),888));
    entries.add(new AuditBatchEntry(List.of(new Transaction(777),new Transaction(0),new Transaction(0)),777));
    entries.add(new AuditBatchEntry(List.of(new Transaction(666),new Transaction(0),new Transaction(0)),666));


    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(baos);
    final BatchAuditConsole batchAudit = new BatchAuditConsole(ps);
    batchAudit.audit(entries);
    ps.close();
    assertEquals(expected,baos.toString());
  }
}
