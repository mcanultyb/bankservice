package org.mcanultyb.balance.model;

import org.mcanultyb.balance.model.Transaction;

/**
 * Service to aggregate transactions tracking the overall balance for an account.
 */
public interface BankAccountService {

  /**
   * Proces a given transaction - this is to be called by the credit and debit generation threads.
   *
   * @param transaction transaction to process
   */
  void processTransaction(Transaction transaction);

  /**
   * Returns the current balance as a non decimal value ie: pence
   */
  long getBalancePence();

  /**
   * Retrieve the balance to the account
   */
  double retrieveBalance();
}
