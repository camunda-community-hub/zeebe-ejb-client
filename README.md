# Zeebe EJB Client

## Start process instances

curl for cmd.exe

```
curl --location --request POST "http://localhost:8080/zeebe-ejb-client/payment/command/start/" ^
--header "Content-Type: application/json" ^
--data-raw "{\"customerId\": \"12\", \"orderTotal\": 39.99, \"creditCardNumber\": \"1234123412341234\", \"cvc\": \"789\", \"expiryDate\": \"07/24\"}"
```