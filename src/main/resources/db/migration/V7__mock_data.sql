INSERT INTO users (username, email, password, role, created_at) VALUES
('user1', 'user1@example.com', '$2a$10$RqqZD97vp6v0puYXCXzSY.4HWaSktVKaejtb1cA7audkkp0ih3pte', 'USER', NOW()),
('user2', 'user2@example.com', '$2a$10$RqqZD97vp6v0puYXCXzSY.4HWaSktVKaejtb1cA7audkkp0ih3pte', 'USER', NOW());


INSERT INTO categories (name, description, is_active) VALUES
('Electronics', 'Electronic devices, gadgets and accessories', true),
('Clothing', 'Men and women clothing', true),
('Books', 'Physical and digital books', true);


INSERT INTO products (name, description, price, stock_quantity, category_id, is_active, created_at) VALUES
('MacBook Pro M2', 'Apple MacBook Pro with M2 chip, 16GB RAM, 512GB SSD', 1999.99, 50, 1, true, NOW()),
('iPhone 15 Pro', 'Apple iPhone 15 Pro 256GB', 1099.99, 100, 1, true, NOW()),
('Sony WH-1000XM5', 'Wireless noise cancelling headphones', 349.99, 30, 1, true, NOW()),
('Cotton T-Shirt', 'Comfortable 100% cotton t-shirt', 19.99, 200, 2, true, NOW()),
('The Pragmatic Programmer', 'Your journey to mastery', 39.99, 45, 3, true, NOW());


INSERT INTO orders (user_id, status, total_amount, note, created_at) VALUES
(2, 'DELIVERED', 2349.98, 'Please leave at the front door', NOW()),
(2, 'PENDING', 19.99, 'Call before delivery', NOW());


INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 1999.99),
(1, 3, 1, 349.99),
(2, 4, 1, 19.99);
