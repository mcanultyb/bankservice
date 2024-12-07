package org.mcanultyb.balance.audit;

import org.mcanultyb.balance.model.Transaction;

/**
 * Defines an audit mechanism that allows individual transactions to be audited
 */
public interface Audit {

  /**
   * Audits a single transaction
   */
  public void audit(final Transaction transaction);
}
