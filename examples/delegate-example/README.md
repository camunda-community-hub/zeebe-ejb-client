# Example with JavaDelegate Implementation

## Starting a process instance

`POST http://localhost:8080/zeebe-ejb-delegate-example/payment/command/start/`

```
{
  "customerId": "15",
  "orderTotal": 39.99,
  "creditCardNumber": "1234123412341234",
  "cvc": "788",
  "expiryDate": "07/24"
}
```
