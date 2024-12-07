package org.mcanultyb.balance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.mcanultyb.balance.audit.AuditBatchEntry;
import org.mcanultyb.balance.model.Transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Testable
public class AuditBatchEntryTest {

  @Test
  public void testCountAndValue() {
    final List<Transaction> txList = new ArrayList<>();
    long expectedValue = 0;
    for (int i=0;i<55;i++) {
      final int val = (i+1);
      txList.add(new Transaction(val));
      expectedValue+=(val);
    }
    final AuditBatchEntry entry = new AuditBatchEntry(txList,txList.stream().collect(Collectors.summingInt(Transaction::getAmountPence)));
    assertEquals(expectedValue,entry.getBatchValue());
    assertEquals(txList.size(),entry.getBatchCount());
  }

  @Test
  public void testValueCheckInConstructorDetectsErrors() {
    final List<Transaction> txList = new ArrayList<>();
    txList.add(new Transaction(200));
    txList.add(new Transaction(800));
    try {
      final AuditBatchEntry entry = new AuditBatchEntry(txList,10); //should be 1000
      fail("Should have failed");
    }
    catch (final Exception e) {
      //expected
    }
  }

  @Test
  public void testGetCount() {
    final AuditBatchEntry e = new AuditBatchEntry(List.of(new Transaction(1)),1);
    assertEquals(1,e.getBatchCount());
  }

  @Test
  public void testGetValue() {
    final AuditBatchEntry e = new AuditBatchEntry(List.of(new Transaction(100)),100);
    assertEquals(100,e.getBatchValue());
  }
}
