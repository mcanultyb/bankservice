package org.mcanultyb.balance;

import org.mcanultyb.balance.model.BalanceTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

  @Autowired private BalanceTracker balanceTracker;

  @GetMapping
  /**
   * Returns the current account balance as a decimal
   */
  public String getBalance() {
    return String.valueOf(balanceTracker.retrieveBalance());
  }
}
