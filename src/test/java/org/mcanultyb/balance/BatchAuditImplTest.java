package org.mcanultyb.balance;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.mcanultyb.balance.audit.AuditBatchEntry;
import org.mcanultyb.balance.audit.BatchedAudit;
import org.mcanultyb.balance.audit.BatchingAuditImpl;
import org.mcanultyb.balance.model.Transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@Testable
public class BatchAuditImplTest {

  @Test
  public void testBatchSizeExactNumberOfEntries() {
    //make sure that if we audit <number of transactions per submission> * N then we get N submissions
    final BatchedAudit underlying = mock(BatchedAudit.class);
    final BatchingAuditImpl impl = new BatchingAuditImpl(underlying, 50, Integer.MAX_VALUE);

    for (int i=0;i<50*1000;i++){
      impl.audit(new Transaction(1));
    }

    verify(underlying,times(1000)).audit(anyList());
  }

  @Test
  public void testBatchSizeLastBatchIncomplete() {
    final BatchedAudit underlying = mock(BatchedAudit.class);
    final BatchingAuditImpl impl = new BatchingAuditImpl(underlying,50,Integer.MAX_VALUE);

    for (int i=0;i<50*10;i++){ //10 complete batches
      impl.audit(new Transaction(1));
    }
    //and one incomplete
    impl.audit(new Transaction(1));

    //should not get a call to audit() for the last single one
    verify(underlying,times(10)).audit(anyList());
  }

  @Test
  public void testBatchSizeIncompleteFirstBatch() {
    //make sure that if we audit (<number of transactions per submission> * N) -1 then we get N-1 submissions
    final BatchedAudit underlying = mock(BatchedAudit.class);
    final BatchingAuditImpl impl = new BatchingAuditImpl(underlying,50,Integer.MAX_VALUE);

    for (int i=0;i<49;i++){
      impl.audit(new Transaction(1));
    }
    verify(underlying,times(0)).audit(anyList());
  }

  @Test
  public void testTransactionsFitsIntoSingleBatchByValuePositives() {
    doTestTransactionsFitsIntoSingleBatchByValue(true,false);
  }

  @Test
  public void testTransactionsFitsIntoSingleBatchByValueNegatives() {
    doTestTransactionsFitsIntoSingleBatchByValue(false,true);
  }

  @Test
  public void testTransactionsFitsIntoSingleBatchByValueMixed() {
    doTestTransactionsFitsIntoSingleBatchByValue(false,false);
  }


  @Test
  public void testTransactionsFitsIntoMultipleBatchesByValuePositives() {
    doTestTransactionsFitsIntoMultipleBatchesByValue(true,false);
  }

  @Test
  public void testTransactionsFitsIntoMultipleBatchesByValueNegatives() {
    doTestTransactionsFitsIntoMultipleBatchesByValue(false,true);
  }

  @Test
  public void testTransactionsFitsIntoMultipleBatchesByValueMixed() {
    doTestTransactionsFitsIntoMultipleBatchesByValue(false,false);
  }

  private void doTestTransactionsFitsIntoMultipleBatchesByValue(final boolean positivesOnly, final boolean negativesOnly) {
    final int calls[] = {0};
    final BatchedAudit underlying = new BatchedAudit() {

      @Override
      public void audit(final List<AuditBatchEntry> entries) {
        calls[0]++;
        assertEquals(entries.size(),2); //should be 2, one with value 30 & count 3, the other with value 2 & count 2
        assertEquals(5,entries.stream().collect(Collectors.summingInt(AuditBatchEntry::getBatchCount)));
        assertEquals(50,entries.stream().collect(Collectors.summingLong(AuditBatchEntry::getBatchValue)));
        System.out.println(entries);
      }
    };

    //5 tx per batch
    final BatchingAuditImpl impl = new BatchingAuditImpl(underlying,5,30); //should get triggered once, with 2 entries in batch, 30 and 20

    impl.audit(new Transaction(positivesOnly ? 10 : negativesOnly ? -10 : 10));
    impl.audit(new Transaction(positivesOnly ? 10 : negativesOnly ? -10 : -10));
    impl.audit(new Transaction(positivesOnly ? 10 : negativesOnly ? -10 : 10));
    impl.audit(new Transaction(positivesOnly ? 10 : negativesOnly ? -10 : -10));
    impl.audit(new Transaction(positivesOnly ? 10 : negativesOnly ? -10 : 10)); //5 x 10 = max value 50

    assertEquals(1,calls[0],"Incorrect number of calls to audit");
  }

  private void doTestTransactionsFitsIntoSingleBatchByValue(final boolean positivesOnly, final boolean negativesOnly) {
    final BatchedAudit underlying = new BatchedAudit() {
      private boolean called;
      @Override
      public void audit(final List<AuditBatchEntry> entries) {
        if (called){
          fail("Should only be called once - should all fit into a single batch");
        }
        called = true;
        assertEquals(entries.size(),1);
        assertEquals(entries.get(0).getBatchCount(),5);
        assertEquals(entries.get(0).getBatchValue(),50);
      }
    };

    //5 tx per batch
    final BatchingAuditImpl impl = new BatchingAuditImpl(underlying,5,50);

    impl.audit(new Transaction(positivesOnly ? 10 : negativesOnly ? -10 : 10));
    impl.audit(new Transaction(positivesOnly ? 10 : negativesOnly ? -10 : -10));
    impl.audit(new Transaction(positivesOnly ? 10 : negativesOnly ? -10 : 10));
    impl.audit(new Transaction(positivesOnly ? 10 : negativesOnly ? -10 : -10));
    impl.audit(new Transaction(positivesOnly ? 10 : negativesOnly ? -10 : 10)); //5 x 10 = max value 50

  }
}
