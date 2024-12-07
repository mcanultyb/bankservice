package org.mcanultyb.balance.model;

import java.util.concurrent.atomic.AtomicLong;
import org.mcanultyb.balance.audit.Audit;

public final class BalanceTracker implements BankAccountService {
  private final AtomicLong balance;

  private final Audit audit;

  /**
   * Creates a BalanceTracker which will automatically audit all transactions to the provided Audit implementation and sets the
   * initial balance to the value provided
   * @param audit
   * @param initialBalance
   */
  public BalanceTracker(final Audit audit, final long initialBalance) {
    this.audit = audit;
    balance = new AtomicLong(initialBalance);
  }

  /**
   * Creates a BalanceTracker which will automatically audit all transactions to the provided Audit implementation and sets the
   * initial balance 0
   * @param audit
   */
  public BalanceTracker(final Audit audit) {
    this(audit,0L);
  }

  public void processTransaction(final Transaction tx) {
    if (balance.get() + tx.getAmountPence() > Long.MAX_VALUE) {
      //TODO this is unreliable :(
      throw new IllegalStateException("This would cause an overflow - cannot process the request");
    }
    final long newBalance = balance.addAndGet(tx.getAmountPence());
    audit.audit(tx);
  }

  @Override
  public long getBalancePence() {
    return balance.get();
  }

  @Override
  public double retrieveBalance() {
    final long currentValue = balance.get();
    return ((double)currentValue) / 100;
  }
}

