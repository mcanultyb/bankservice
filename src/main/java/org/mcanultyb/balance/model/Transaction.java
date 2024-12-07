package org.mcanultyb.balance.model;


import java.util.concurrent.atomic.AtomicLong;

public final class Transaction {
  private static final AtomicLong ID_GENERATOR = new AtomicLong();
  private final int amountPence;
  private final long id;

  /**
   * Creates a transaction with the given value in pence. Auto generates unique transaction ID.
   * @param amountPence
   */
  public Transaction(final int amountPence) {
    this.amountPence =  amountPence;
    id = ID_GENERATOR.incrementAndGet();
  }

  public int getAmountPence() {
    return amountPence;
  }

  @Override
  public String toString() {
    return "TX ID="+id+" = "+amountPence+"p";
  }

  /**
   * Returns the auto generated unique ID for this transaction.
   * (unique up until Long runs out)
   */
  public long getID() {
    return id;
  }

}
