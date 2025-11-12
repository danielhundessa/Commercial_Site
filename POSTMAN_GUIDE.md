# Postman API Guide - Order Service

This guide explains how to test the order service endpoints using Postman.

## Base URL
- **Gateway Service**: `http://localhost:8090`
- **Order Service (Direct)**: `http://localhost:6060`

> **Note**: All requests should go through the Gateway at port 8090 unless testing directly.

## Required Headers
All cart and order endpoints require the `X-User-ID` header:
```
X-User-ID: <your-user-id>
```

Example: `X-User-ID: user123`

---

## Step-by-Step: Creating an Order

### Step 1: Get Available Products
First, you need to see what products are available to add to your cart.

**Request:**
```
GET http://localhost:8090/api/products
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Product Name",
    "description": "Product Description",
    "price": 29.99,
    "stockQuantity": 100,
    "category": "Electronics",
    "imageUrl": "https://example.com/image.jpg",
    "active": true
  }
]
```

**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8090/api/products`
- Headers: None required

---

### Step 2: Add Items to Cart
Add products to your cart before creating an order.

**Request:**
```
POST http://localhost:8090/api/cart/items
```

**Headers:**
```
X-User-ID: user123
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "productId": "1",
  "quantity": 2
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "productId": "1",
  "quantity": 2,
  "price": 59.98,
  "subtotal": 59.98
}
```

**Postman Setup:**
- Method: `POST`
- URL: `http://localhost:8090/api/cart/items`
- Headers:
  - `X-User-ID`: `user123`
  - `Content-Type`: `application/json`
- Body (raw JSON):
  ```json
  {
    "productId": "1",
    "quantity": 2
  }
  ```

**Add Multiple Items:**
Repeat this step with different `productId` values to add multiple items to your cart.

---

### Step 3: View Cart (Optional)
Check what's in your cart before placing the order.

**Request:**
```
GET http://localhost:8090/api/cart
```

**Headers:**
```
X-User-ID: user123
```

**Response (200 OK):**
```json
{
  "userId": "user123",
  "items": [
    {
      "id": 1,
      "productId": "1",
      "quantity": 2,
      "price": 59.98,
      "subtotal": 59.98
    },
    {
      "id": 2,
      "productId": "2",
      "quantity": 1,
      "price": 19.99,
      "subtotal": 19.99
    }
  ],
  "totalAmount": 79.97
}
```

**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8090/api/cart`
- Headers:
  - `X-User-ID`: `user123`

---

### Step 4: Create Order
Create an order from the items in your cart. This will clear your cart after successful order creation.

**Request:**
```
POST http://localhost:8090/api/orders
```

**Headers:**
```
X-User-ID: user123
```

**Response (201 Created):**
```json
{
  "id": 1,
  "totalAmount": 79.97,
  "status": "CONFIRMED",
  "items": [
    {
      "id": 1,
      "productId": "1",
      "quantity": 2,
      "price": 59.98,
      "subtotal": 119.96
    },
    {
      "id": 2,
      "productId": "2",
      "quantity": 1,
      "price": 19.99,
      "subtotal": 19.99
    }
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

**Postman Setup:**
- Method: `POST`
- URL: `http://localhost:8090/api/orders`
- Headers:
  - `X-User-ID`: `user123`

**Error Response (400 Bad Request):**
If your cart is empty, you'll get a 400 Bad Request response.

---

## Additional Cart Endpoints

### Remove Item from Cart
**Request:**
```
DELETE http://localhost:8090/api/cart/items/{cartItemId}
```

**Headers:**
```
X-User-ID: user123
```

**Postman Setup:**
- Method: `DELETE`
- URL: `http://localhost:8090/api/cart/items/1` (replace 1 with actual cart item ID)
- Headers:
  - `X-User-ID`: `user123`

---

### Clear Entire Cart
**Request:**
```
DELETE http://localhost:8090/api/cart
```

**Headers:**
```
X-User-ID: user123
```

**Postman Setup:**
- Method: `DELETE`
- URL: `http://localhost:8090/api/cart`
- Headers:
  - `X-User-ID`: `user123`

---

## Complete Postman Collection Example

### Collection Structure:
1. **Get Products** - `GET /api/products`
2. **Add to Cart** - `POST /api/cart/items`
3. **Get Cart** - `GET /api/cart`
4. **Create Order** - `POST /api/orders`
5. **Remove from Cart** - `DELETE /api/cart/items/{id}`
6. **Clear Cart** - `DELETE /api/cart`

### Environment Variables (Optional)
Create a Postman environment with:
- `base_url`: `http://localhost:8090`
- `user_id`: `user123`

Then use `{{base_url}}` and `{{user_id}}` in your requests.

---

## Troubleshooting

### Common Issues:

1. **400 Bad Request when creating order**
   - Make sure your cart has items in it
   - Verify the `X-User-ID` header is set correctly

2. **404 Not Found**
   - Ensure all services are running:
     - Eureka Server (port 8761)
     - Config Server (port 8888)
     - Gateway Service (port 8090)
     - Order Service (port 6060)
     - Product Service
     - User Service

3. **Product not found when adding to cart**
   - Verify the product ID exists by calling `GET /api/products` first
   - Ensure the product is active

4. **Connection refused**
   - Check that the Gateway service is running on port 8090
   - Verify Eureka service discovery is working

---

## Quick Test Flow

1. **Get Products**: `GET http://localhost:8090/api/products`
2. **Add Item 1**: `POST http://localhost:8090/api/cart/items` with `{"productId": "1", "quantity": 2}`
3. **Add Item 2**: `POST http://localhost:8090/api/cart/items` with `{"productId": "2", "quantity": 1}`
4. **View Cart**: `GET http://localhost:8090/api/cart`
5. **Create Order**: `POST http://localhost:8090/api/orders`

All requests should include: `X-User-ID: user123` header












