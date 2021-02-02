# test_akka_rest_api

## Endpoints
### Get customer's info by id
GET /customers/:id
`curl http://rs/1lhost:8080/customer`

## Add customer
POST /customers
`curl -d '{"id":1, "name":"Nick"}' -H "Content-Type: application/json" -X POST http://localhost:8080/customers`
