package org.mcanultyb.balance.model;

/**
 * The types of transaction that this system supports
 */
public enum TransactionType {
  CREDIT,
  DEBIT {
    @Override
    public int apply(final int absValue) {
      return -absValue;
    }
  };

  /**
   * Applies any conversion to the transaction value (absValue) specific to the transaction type.
   * TODO arguably redundant, only used by generator to negate debit tx values
   * @param amount
   * @return
   */
  public int apply(final int absValue) {
    return absValue;
  }
}
