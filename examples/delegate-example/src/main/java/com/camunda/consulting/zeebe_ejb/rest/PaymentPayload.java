package com.camunda.consulting.zeebe_ejb.rest;

public class PaymentPayload {

  private String customerId;
  private double orderTotal;
  private String creditCardNumber;
  private String expiryDate;
  private String cvc;

  public PaymentPayload(
      String customerId,
      double orderTotal,
      String creditCardNumber,
      String expiryDate,
      String cvc) {
    super();
    this.customerId = customerId;
    this.orderTotal = orderTotal;
    this.creditCardNumber = creditCardNumber;
    this.expiryDate = expiryDate;
    this.cvc = cvc;
  }

  public PaymentPayload() {
    super();
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public double getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(double orderTotal) {
    this.orderTotal = orderTotal;
  }

  public String getCreditCardNumber() {
    return creditCardNumber;
  }

  public void setCreditCardNumber(String creditCardNumber) {
    this.creditCardNumber = creditCardNumber;
  }

  public String getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(String expiryDate) {
    this.expiryDate = expiryDate;
  }

  public String getCvc() {
    return cvc;
  }

  public void setCvc(String cvc) {
    this.cvc = cvc;
  }

  @Override
  public String toString() {
    return String.format(
        "PaymentPayload [customerId=%s, orderTotal=%s, creditCardNumber=%s, expiryDate=%s, cvc=%s]",
        customerId, orderTotal, creditCardNumber, expiryDate, cvc);
  }
}
