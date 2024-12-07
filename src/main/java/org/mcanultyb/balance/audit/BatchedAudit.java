package org.mcanultyb.balance.audit;

import java.util.List;
import org.mcanultyb.balance.audit.AuditBatchEntry;

/**
 * Defines an auditor that accepts batches of audit entries
 */
public interface BatchedAudit{

   /**
    * Audit the provided entries
    * @param entries
    */
   void audit(List<AuditBatchEntry> entries);
}
