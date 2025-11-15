# JMeter Order Creation Test Setup Guide

This guide explains how to set up and run the JMeter test for creating orders.

## Prerequisites

1. **Products in Database**: Before running the JMeter test, ensure products are inserted into the database
2. **Services Running**: Order service and Product service must be running
3. **JMeter Installed**: Download from https://jmeter.apache.org/download_jmeter.cgi

## Step 1: Insert Products into Database

The JMeter test uses product IDs from 1 to 20. You need to insert these products first.

### Option A: Using MySQL Command Line

```bash
# Connect to MySQL
mysql -u springstudent -p

# Run the SQL file
source product-service/src/main/resources/db/insert-products.sql

# Or directly:
mysql -u springstudent -p productdb < product-service/src/main/resources/db/insert-products.sql
```

### Option B: Using MySQL Workbench or DBeaver

1. Open your MySQL client
2. Connect to `productdb` database
3. Open and execute `product-service/src/main/resources/db/insert-products.sql`

### Verify Products

```sql
USE productdb;
SELECT id, name, price, stock_quantity FROM products WHERE active = true;
```

You should see 20 products with IDs from 1 to 20.

## Step 2: Run JMeter Test

### Option A: GUI Mode (for testing/debugging)

1. Open JMeter
2. File → Open → Select `bulk_order_test.jmx`
3. Click the green "Play" button
4. Monitor results in "View Results Tree" and "Summary Report"

### Option B: Command Line (for performance testing)

```bash
# Run test in non-GUI mode
jmeter -n -t bulk_order_test.jmx -l results.jtl -e -o report/

# View HTML report
# Open report/index.html in your browser
```

## Test Plan Configuration

The test plan is configured as follows:

- **Threads**: 10 concurrent users
- **Ramp-up**: 10 seconds
- **Loop Count**: 100 iterations per thread
- **Total Orders**: 10 threads × 100 loops = 1000 orders

### Test Flow

For each iteration:
1. **Add Item 1 to Cart**: Adds a random product (ID 1-20) with quantity 1-5
2. **Add Item 2 to Cart**: Adds another random product (ID 1-20) with quantity 1-3
3. **Create Order**: Creates an order from the cart items

### User IDs

Each thread uses a unique user ID format: `user-{threadNum}-{randomString}`

## Troubleshooting

### Issue: "Cart is empty" errors

**Cause**: Products don't exist in the database or product IDs are invalid.

**Solution**:
1. Verify products are inserted: `SELECT COUNT(*) FROM products WHERE active = true;`
2. Ensure product IDs 1-20 exist
3. Check product service is running and accessible

### Issue: 400 Bad Request when adding to cart

**Cause**: Product ID doesn't exist or product is inactive.

**Solution**:
1. Check product exists: `SELECT * FROM products WHERE id = {productId};`
2. Verify product is active: `SELECT * FROM products WHERE id = {productId} AND active = true;`

### Issue: Connection refused

**Cause**: Order service is not running on port 6060.

**Solution**:
1. Verify order service is running: `curl http://localhost:6060/actuator/health`
2. Check service logs for errors
3. Ensure gateway/eureka is running if using service discovery

### Issue: Slow performance

**Solution**:
1. Disable "View Results Tree" listener during performance tests
2. Reduce number of threads or loops
3. Increase ramp-up time

## Customizing the Test

### Change Number of Threads

1. Open `bulk_order_test.jmx` in JMeter
2. Select "Order Creation Thread Group"
3. Change "Number of Threads" value

### Change Product ID Range

1. Open `bulk_order_test.jmx` in JMeter
2. Select "Add Item 1 to Cart" or "Add Item 2 to Cart"
3. In Body Data, change `${__Random(1,20)}` to your desired range
4. Ensure products with those IDs exist in database

### Add More Items to Cart

1. Right-click "Add Item 2 to Cart"
2. Copy → Paste
3. Rename to "Add Item 3 to Cart"
4. Adjust product ID range if needed

## Monitoring Results

### View Results Tree
- Shows detailed request/response for each sample
- Useful for debugging
- **Disable for performance tests** (slows down execution)

### Summary Report
- Shows aggregate statistics:
  - Sample count
  - Average/Median/Min/Max response time
  - Error percentage
  - Throughput

### Command Line Results

After running in non-GUI mode:
- `results.jtl`: CSV file with all sample results
- `report/`: HTML report with charts and statistics

## Expected Results

With 20 products in database:
- **Success Rate**: Should be close to 100%
- **Average Response Time**: < 500ms per request
- **Order Creation**: Should succeed after items are added to cart

## Next Steps

After successful test runs:
1. Monitor Kafka topics: `orders.created`
2. Check Camunda process instances
3. Verify orders in orderdb database
4. Check notification service for order confirmations




