-- Insert sample products into the products table
-- This file should be run against the productdb database

USE productdb;

-- Insert sample products
INSERT INTO products (name, description, price, stock_quantity, category, image_url, active, created_at, updated_at) VALUES
('Laptop Pro 15"', 'High-performance laptop with 16GB RAM and 512GB SSD', 1299.99, 50, 'Electronics', 'https://example.com/images/laptop-pro.jpg', true, NOW(), NOW()),
('Wireless Mouse', 'Ergonomic wireless mouse with 2-year battery life', 29.99, 200, 'Electronics', 'https://example.com/images/mouse.jpg', true, NOW(), NOW()),
('Mechanical Keyboard', 'RGB backlit mechanical keyboard with Cherry MX switches', 149.99, 75, 'Electronics', 'https://example.com/images/keyboard.jpg', true, NOW(), NOW()),
('4K Monitor 27"', 'Ultra HD 4K monitor with HDR support', 399.99, 30, 'Electronics', 'https://example.com/images/monitor.jpg', true, NOW(), NOW()),
('USB-C Hub', '7-in-1 USB-C hub with HDMI, USB 3.0, and SD card reader', 49.99, 150, 'Electronics', 'https://example.com/images/usb-hub.jpg', true, NOW(), NOW()),
('Webcam HD', '1080p HD webcam with built-in microphone', 79.99, 100, 'Electronics', 'https://example.com/images/webcam.jpg', true, NOW(), NOW()),
('Gaming Headset', 'Surround sound gaming headset with noise cancellation', 129.99, 60, 'Electronics', 'https://example.com/images/headset.jpg', true, NOW(), NOW()),
('External SSD 1TB', 'Portable external SSD with USB 3.2 Gen 2', 119.99, 80, 'Electronics', 'https://example.com/images/ssd.jpg', true, NOW(), NOW()),
('Laptop Stand', 'Adjustable aluminum laptop stand for ergonomic setup', 39.99, 120, 'Accessories', 'https://example.com/images/laptop-stand.jpg', true, NOW(), NOW()),
('Desk Mat', 'Large desk mat with wrist support and smooth surface', 24.99, 200, 'Accessories', 'https://example.com/images/desk-mat.jpg', true, NOW(), NOW()),
('USB Flash Drive 64GB', 'High-speed USB 3.0 flash drive', 19.99, 300, 'Electronics', 'https://example.com/images/flash-drive.jpg', true, NOW(), NOW()),
('Cable Organizer', 'Cable management system with adhesive clips', 14.99, 250, 'Accessories', 'https://example.com/images/cable-organizer.jpg', true, NOW(), NOW()),
('Laptop Sleeve', 'Protective laptop sleeve with padding', 34.99, 100, 'Accessories', 'https://example.com/images/laptop-sleeve.jpg', true, NOW(), NOW()),
('Wireless Charger', 'Fast wireless charging pad for smartphones', 29.99, 150, 'Electronics', 'https://example.com/images/wireless-charger.jpg', true, NOW(), NOW()),
('Bluetooth Speaker', 'Portable Bluetooth speaker with 20-hour battery', 59.99, 90, 'Electronics', 'https://example.com/images/speaker.jpg', true, NOW(), NOW()),
('Tablet Stand', 'Adjustable tablet stand for viewing and typing', 24.99, 110, 'Accessories', 'https://example.com/images/tablet-stand.jpg', true, NOW(), NOW()),
('HDMI Cable 6ft', 'High-speed HDMI 2.0 cable with gold-plated connectors', 12.99, 400, 'Electronics', 'https://example.com/images/hdmi-cable.jpg', true, NOW(), NOW()),
('USB-C Cable', 'USB-C to USB-C cable with fast charging support', 15.99, 350, 'Electronics', 'https://example.com/images/usb-c-cable.jpg', true, NOW(), NOW()),
('Screen Cleaner Kit', 'Professional screen cleaning kit with microfiber cloth', 9.99, 500, 'Accessories', 'https://example.com/images/screen-cleaner.jpg', true, NOW(), NOW()),
('Laptop Cooling Pad', 'USB-powered laptop cooling pad with adjustable fan speed', 34.99, 70, 'Accessories', 'https://example.com/images/cooling-pad.jpg', true, NOW(), NOW());

-- Verify the inserts
SELECT COUNT(*) as total_products FROM products WHERE active = true;

