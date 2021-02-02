# test_akka_rest_api

## Endpoints
### Get customer's info by id
GET /customers/:id
`curl http://localhost:8080/customers/1`

### Add customer
POST /customers
`curl -d '{"id":1, "name":"Nick"}' -H "Content-Type: application/json" -X POST http://localhost:8080/customers`
