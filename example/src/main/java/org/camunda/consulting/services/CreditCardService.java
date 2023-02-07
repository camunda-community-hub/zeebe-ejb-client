package org.camunda.consulting.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.time.LocalDate;

@Stateless
public class CreditCardService {

  private static final Logger LOG = LoggerFactory.getLogger(CreditCardService.class);

  public void chargeAmount(String cardNumber, String cvc, String expiryDate, Double amount) {
    if (!validateExpiryDate(expiryDate)) {
      throw new RuntimeException("Invalid expiry date: " + expiryDate + "\nExpiry date must be in the future.");
    }

    LOG.info("charging card {} that expires on {} and has cvc {} with amount of {}",
        cardNumber, expiryDate, cvc, amount);

    LOG.info("payment completed");
  }

  boolean validateExpiryDate(String expiryDate) {
    if (expiryDate.length() != 5) {
      return false;
    }
    try {
      int month = Integer.parseInt(expiryDate.substring(0, 2));
      int year = Integer.parseInt(expiryDate.substring(3, 5)) + 2000;
      LocalDate now = LocalDate.now();
      if (month < 1 || month > 12 || year < now.getYear()) {
        return false;
      }
      if (year > now.getYear() ||
          (year == now.getYear() && month >= now.getMonthValue())) {
        return true;
      } else {
        return false;
      }
    } catch (NumberFormatException|IndexOutOfBoundsException e) {
      return false;
    }
  }

}