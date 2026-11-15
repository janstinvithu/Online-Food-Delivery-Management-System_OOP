-- =============================================================================
-- ONLINE FOOD DELIVERY MANAGEMENT SYSTEM - MICROSOFT SQL SERVER & SSMS SETUP
-- Target Database Engine: Microsoft SQL Server 2017+ / 2019 / 2022 / Express
-- Target Tool: SQL Server Management Studio (SSMS)
-- =============================================================================

USE master;
GO

-- 1. Create Database if it does not already exist
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = N'food_delivery_db')
BEGIN
    CREATE DATABASE food_delivery_db;
    PRINT 'Database [food_delivery_db] created successfully.';
END
ELSE
BEGIN
    PRINT 'Database [food_delivery_db] already exists. Continuing...';
END
GO

USE food_delivery_db;
GO

-- -----------------------------------------------------------------------------
-- Clean Drop existing tables in reverse dependency order
-- -----------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.cash_payments', N'U') IS NOT NULL DROP TABLE dbo.cash_payments;
IF OBJECT_ID(N'dbo.card_payments', N'U') IS NOT NULL DROP TABLE dbo.card_payments;
IF OBJECT_ID(N'dbo.payments', N'U') IS NOT NULL DROP TABLE dbo.payments;
IF OBJECT_ID(N'dbo.order_items', N'U') IS NOT NULL DROP TABLE dbo.order_items;
IF OBJECT_ID(N'dbo.pickup_orders', N'U') IS NOT NULL DROP TABLE dbo.pickup_orders;
IF OBJECT_ID(N'dbo.online_orders', N'U') IS NOT NULL DROP TABLE dbo.online_orders;
IF OBJECT_ID(N'dbo.food_orders', N'U') IS NOT NULL DROP TABLE dbo.food_orders;
IF OBJECT_ID(N'dbo.deliveries', N'U') IS NOT NULL DROP TABLE dbo.deliveries;
IF OBJECT_ID(N'dbo.delivery_persons', N'U') IS NOT NULL DROP TABLE dbo.delivery_persons;
IF OBJECT_ID(N'dbo.customers', N'U') IS NOT NULL DROP TABLE dbo.customers;
IF OBJECT_ID(N'dbo.beverages', N'U') IS NOT NULL DROP TABLE dbo.beverages;
IF OBJECT_ID(N'dbo.main_courses', N'U') IS NOT NULL DROP TABLE dbo.main_courses;
IF OBJECT_ID(N'dbo.food_items', N'U') IS NOT NULL DROP TABLE dbo.food_items;
GO

-- -----------------------------------------------------------------------------
-- Component 01: FoodItem Hierarchy (Joined Table Inheritance)
-- -----------------------------------------------------------------------------
CREATE TABLE dbo.food_items (
    id BIGINT IDENTITY(1, 1) PRIMARY KEY,
    item_type VARCHAR(31) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255) NULL,
    price DECIMAL(10, 2) NOT NULL,
    available BIT NOT NULL DEFAULT 1,
    preparation_time_minutes INT NOT NULL,
    CONSTRAINT chk_food_price CHECK (price > 0.00),
    CONSTRAINT chk_prep_time CHECK (preparation_time_minutes > 0)
);
GO

CREATE TABLE dbo.main_courses (
    id BIGINT PRIMARY KEY,
    cuisine_type NVARCHAR(50) NOT NULL,
    is_vegetarian BIT NOT NULL DEFAULT 0,
    spice_level VARCHAR(20) NOT NULL,
    serving_portion NVARCHAR(100) NOT NULL,
    CONSTRAINT fk_main_course_food FOREIGN KEY (id) REFERENCES dbo.food_items(id) ON DELETE CASCADE,
    CONSTRAINT chk_spice_level CHECK (spice_level IN ('MILD', 'MEDIUM', 'HOT', 'EXTRA_HOT'))
);
GO

CREATE TABLE dbo.beverages (
    id BIGINT PRIMARY KEY,
    volume_size NVARCHAR(50) NOT NULL,
    volume_ml INT NOT NULL,
    is_cold BIT NOT NULL DEFAULT 1,
    caffeine_content_mg INT NOT NULL DEFAULT 0,
    sugar_level VARCHAR(20) NOT NULL,
    CONSTRAINT fk_beverage_food FOREIGN KEY (id) REFERENCES dbo.food_items(id) ON DELETE CASCADE,
    CONSTRAINT chk_volume CHECK (volume_ml > 0),
    CONSTRAINT chk_caffeine CHECK (caffeine_content_mg >= 0),
    CONSTRAINT chk_sugar_level CHECK (sugar_level IN ('ZERO', 'LOW', 'MEDIUM', 'HIGH'))
);
GO

-- -----------------------------------------------------------------------------
-- Component 02: Customer Hierarchy (Single Table Inheritance)
-- -----------------------------------------------------------------------------
CREATE TABLE dbo.customers (
    id BIGINT IDENTITY(1, 1) PRIMARY KEY,
    customer_type VARCHAR(31) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    email NVARCHAR(120) NOT NULL UNIQUE,
    phone NVARCHAR(25) NOT NULL,
    address NVARCHAR(255) NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    loyalty_points INT NOT NULL DEFAULT 0,
    membership_tier VARCHAR(20) NULL,
    password_hash NVARCHAR(255) NULL,
    preferred_payment_method VARCHAR(50) NULL,
    CONSTRAINT chk_customer_type CHECK (customer_type IN ('GUEST', 'REGISTERED')),
    CONSTRAINT chk_loyalty_pts CHECK (loyalty_points >= 0),
    CONSTRAINT chk_tier CHECK (membership_tier IS NULL OR membership_tier IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM'))
);
GO

-- -----------------------------------------------------------------------------
-- Component 04 (Part A): Delivery Couriers & Shipments
-- -----------------------------------------------------------------------------
CREATE TABLE dbo.delivery_persons (
    id BIGINT IDENTITY(1, 1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    phone NVARCHAR(25) NOT NULL UNIQUE,
    vehicle_type NVARCHAR(50) NOT NULL,
    vehicle_plate_number NVARCHAR(30) NOT NULL,
    is_available BIT NOT NULL DEFAULT 1,
    rating DECIMAL(3, 2) NOT NULL DEFAULT 5.00,
    CONSTRAINT chk_rating CHECK (rating >= 1.00 AND rating <= 5.00)
);
GO

CREATE TABLE dbo.deliveries (
    id BIGINT IDENTITY(1, 1) PRIMARY KEY,
    tracking_number VARCHAR(36) NOT NULL UNIQUE,
    delivery_status VARCHAR(30) NOT NULL,
    departure_time DATETIME2 NULL,
    delivered_time DATETIME2 NULL,
    delivery_person_id BIGINT NULL,
    CONSTRAINT fk_delivery_person FOREIGN KEY (delivery_person_id) REFERENCES dbo.delivery_persons(id) ON DELETE SET NULL
);
GO

-- -----------------------------------------------------------------------------
-- Component 03: Food Orders & Items
-- -----------------------------------------------------------------------------
CREATE TABLE dbo.food_orders (
    id BIGINT IDENTITY(1, 1) PRIMARY KEY,
    order_number VARCHAR(36) NOT NULL UNIQUE,
    order_type VARCHAR(31) NOT NULL,
    order_status VARCHAR(30) NOT NULL,
    order_date_time DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    subtotal_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    customer_id BIGINT NOT NULL,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES dbo.customers(id)
);
GO

CREATE TABLE dbo.online_orders (
    id BIGINT PRIMARY KEY,
    delivery_address NVARCHAR(255) NOT NULL,
    special_delivery_instructions NVARCHAR(255) NULL,
    estimated_delivery_time DATETIME2 NULL,
    delivery_fee DECIMAL(10, 2) NOT NULL DEFAULT 350.00,
    delivery_id BIGINT NULL,
    CONSTRAINT fk_online_order_food_order FOREIGN KEY (id) REFERENCES dbo.food_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_online_order_delivery FOREIGN KEY (delivery_id) REFERENCES dbo.deliveries(id) ON DELETE SET NULL
);
GO

CREATE TABLE dbo.pickup_orders (
    id BIGINT PRIMARY KEY,
    pickup_time_slot NVARCHAR(50) NOT NULL,
    pickup_window_minutes INT NOT NULL DEFAULT 15,
    counter_number INT NOT NULL DEFAULT 1,
    pickup_contact_phone NVARCHAR(25) NOT NULL,
    is_curbside_pickup BIT NOT NULL DEFAULT 0,
    CONSTRAINT fk_pickup_order_food_order FOREIGN KEY (id) REFERENCES dbo.food_orders(id) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.order_items (
    id BIGINT IDENTITY(1, 1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    food_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES dbo.food_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_food FOREIGN KEY (food_item_id) REFERENCES dbo.food_items(id)
);
GO

-- -----------------------------------------------------------------------------
-- Component 04 (Part B): Payments (Cash & Card)
-- -----------------------------------------------------------------------------
CREATE TABLE dbo.payments (
    id BIGINT IDENTITY(1, 1) PRIMARY KEY,
    payment_type VARCHAR(31) NOT NULL,
    order_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    payment_timestamp DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    transaction_reference VARCHAR(64) NOT NULL UNIQUE,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES dbo.food_orders(id) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.card_payments (
    id BIGINT PRIMARY KEY,
    card_last_four VARCHAR(4) NOT NULL,
    card_type VARCHAR(20) NOT NULL,
    authorization_code VARCHAR(50) NOT NULL,
    gateway_transaction_id VARCHAR(100) NOT NULL,
    CONSTRAINT fk_card_payment_payment FOREIGN KEY (id) REFERENCES dbo.payments(id) ON DELETE CASCADE,
    CONSTRAINT chk_card_last_four CHECK (LEN(card_last_four) = 4)
);
GO

CREATE TABLE dbo.cash_payments (
    id BIGINT PRIMARY KEY,
    amount_tendered DECIMAL(10, 2) NOT NULL,
    change_due DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_cash_payment_payment FOREIGN KEY (id) REFERENCES dbo.payments(id) ON DELETE CASCADE
);
GO

PRINT 'All tables created successfully in [food_delivery_db].';
GO

-- -----------------------------------------------------------------------------
-- Realistic Mock Seed Data (Sri Lanka Edition in LKR)
-- -----------------------------------------------------------------------------
SET IDENTITY_INSERT dbo.food_items ON;
INSERT INTO dbo.food_items (id, item_type, name, description, price, available, preparation_time_minutes) VALUES 
(1, 'MAIN_COURSE', N'Chicken Dum Biryani Special', N'Fragrant kaima basmati rice steamed with marinated chicken leg, boiled egg, mint sambol, and raita', 1850.00, 1, 25),
(2, 'MAIN_COURSE', N'Jaffna Spiced Mutton Kottu', N'Hand-shredded godamba roti stir-fried with roasted tender mutton, leeks, green chillies, and Jaffna curry gravy', 2250.00, 1, 20),
(3, 'MAIN_COURSE', N'Negombo Lagoon Prawn Curry', N'Succulent fresh lagoon prawns slow-cooked in thick coconut milk, roasted curry powder, and wild goraka', 2400.00, 1, 30),
(4, 'MAIN_COURSE', N'Vegetable Wok Fried Rice', N'Aromatic jasmine rice tossed with crispy garden vegetables, golden cashews, accompanied by authentic hot chilli paste', 1350.00, 1, 15),
(5, 'BEVERAGE', N'Ceylon Nuwara Eliya Pekoe Tea', N'Pure Ceylon single-estate high-grown orange pekoe brewed fresh and served with jaggery and fresh milk', 450.00, 1, 5),
(6, 'BEVERAGE', N'Colombo Royal Faluda', N'Creamy chilled milk layered with fragrant rose syrup, soaked basil seeds, vermicelli, jelly cubes, and rich vanilla ice cream', 650.00, 1, 8),
(7, 'BEVERAGE', N'Fresh King Coconut (Thambili)', N'Naturally sweet, mineral-rich chilled organic king coconut water cut fresh on order', 350.00, 1, 3);
SET IDENTITY_INSERT dbo.food_items OFF;
GO

INSERT INTO dbo.main_courses (id, cuisine_type, is_vegetarian, spice_level, serving_portion) VALUES
(1, N'Sri Lankan Dum', 0, 'MEDIUM', N'Full Clay Pot with Hard-boiled Egg & Gravy'),
(2, N'Northern Sri Lankan', 0, 'EXTRA_HOT', N'Large Plate (500g) with Extra Spicy Curry Dip'),
(3, N'Coastal Sri Lankan', 0, 'HOT', N'350g Rich Seafood Pot with 2 Roast Paan slices'),
(4, N'Sri Lankan Fusion', 1, 'MEDIUM', N'Standard Bowl (400g) with Katta Sambol & Chilli Paste');
GO

INSERT INTO dbo.beverages (id, volume_size, volume_ml, is_cold, caffeine_content_mg, sugar_level) VALUES
(5, N'Single Tea Pot (350ml)', 350, 0, 65, 'LOW'),
(6, N'Tall Sundae Glass (400ml)', 400, 1, 0, 'HIGH'),
(7, N'Fresh Nut Serving (450ml)', 450, 1, 0, 'ZERO');
GO

SET IDENTITY_INSERT dbo.customers ON;
INSERT INTO dbo.customers (id, customer_type, full_name, email, phone, address, created_at, loyalty_points, membership_tier, password_hash, preferred_payment_method) VALUES
(1, 'REGISTERED', N'Eleanor Fernando', N'eleanor.fernando@vanguard.lk', N'+94-77-1234567', N'No. 42, Ward Place, Colombo 07', '2026-01-10 10:15:00', 1200, 'PLATINUM', N'$2a$12$securehashLK1', 'CARD'),
(2, 'REGISTERED', N'Marcus Perera', N'marcus.perera@kandytech.lk', N'+94-71-9876543', N'No. 18, Peradeniya Road, Kandy', '2026-02-14 14:30:00', 650, 'GOLD', N'$2a$12$securehashLK2', 'CASH'),
(3, 'GUEST', N'Sophia De Silva', N'sophia.desilva@gmail.com', N'+94-76-5551234', N'No. 10, Church Street, Galle Fort', '2026-03-01 18:20:00', 0, NULL, NULL, NULL),
(4, 'REGISTERED', N'David Jayawardena', N'david.j@jaffnadigital.lk', N'+94-77-4448899', N'No. 88, Hospital Road, Jaffna', '2026-03-12 11:45:00', 250, 'SILVER', N'$2a$12$securehashLK3', 'CARD');
SET IDENTITY_INSERT dbo.customers OFF;
GO

SET IDENTITY_INSERT dbo.delivery_persons ON;
INSERT INTO dbo.delivery_persons (id, name, phone, vehicle_type, vehicle_plate_number, is_available, rating) VALUES
(1, N'Kasun Bandara', N'+94-77-8811223', N'Honda Dio Scooter', N'WP BDF-4491', 0, 4.95),
(2, N'Nuwan Pradeep', N'+94-71-3344556', N'TVS King Three-Wheeler', N'WP QL-1892', 1, 4.88),
(3, N'Dinuka Silva', N'+94-76-2233445', N'Hero Eco 125 Motorcycle', N'SP BD-8821', 1, 4.90);
SET IDENTITY_INSERT dbo.delivery_persons OFF;
GO

SET IDENTITY_INSERT dbo.deliveries ON;
INSERT INTO dbo.deliveries (id, tracking_number, delivery_status, departure_time, delivered_time, delivery_person_id) VALUES
(1, 'TRK-COL-2026-0001', 'OUT_FOR_DELIVERY', '2026-10-18 12:45:00', NULL, 1),
(2, 'TRK-GAL-2026-0002', 'DELIVERED', '2026-10-18 11:20:00', '2026-10-18 11:55:00', 3);
SET IDENTITY_INSERT dbo.deliveries OFF;
GO

SET IDENTITY_INSERT dbo.food_orders ON;
INSERT INTO dbo.food_orders (id, order_number, order_type, order_status, order_date_time, subtotal_amount, discount_amount, total_amount, customer_id) VALUES
(1, 'ORD-20261018-1001', 'ONLINE', 'PREPARING', '2026-10-18 12:30:00', 2300.00, 345.00, 2305.00, 1),
(2, 'ORD-20261018-1002', 'PICKUP', 'READY', '2026-10-18 12:35:00', 2900.00, 290.00, 2610.00, 2),
(3, 'ORD-20261018-1003', 'ONLINE', 'DELIVERED', '2026-10-18 11:05:00', 2750.00, 0.00, 3100.00, 3),
(4, 'ORD-20261018-1004', 'PICKUP', 'PENDING', '2026-10-18 13:00:00', 2000.00, 100.00, 1900.00, 4);
SET IDENTITY_INSERT dbo.food_orders OFF;
GO

INSERT INTO dbo.online_orders (id, delivery_address, special_delivery_instructions, estimated_delivery_time, delivery_fee, delivery_id) VALUES
(1, N'No. 42, Ward Place, Colombo 07', N'Security gate pass code #4401, deliver to 3rd floor apartment', '2026-10-18 13:15:00', 350.00, 1),
(3, N'No. 10, Church Street, Galle Fort', N'Leave at main reception of Dutch Villa', '2026-10-18 11:55:00', 350.00, 2);
GO

INSERT INTO dbo.pickup_orders (id, pickup_time_slot, pickup_window_minutes, counter_number, pickup_contact_phone, is_curbside_pickup) VALUES
(2, N'01:00 PM - 01:20 PM', 20, 2, N'+94-71-9876543', 0),
(4, N'01:45 PM - 02:00 PM', 15, 1, N'+94-77-4448899', 1);
GO

SET IDENTITY_INSERT dbo.order_items ON;
INSERT INTO dbo.order_items (id, order_id, food_item_id, quantity, unit_price, subtotal) VALUES
(1, 1, 1, 1, 1850.00, 1850.00),
(2, 1, 5, 1, 450.00, 450.00),
(3, 2, 2, 1, 2250.00, 2250.00),
(4, 2, 6, 1, 650.00, 650.00),
(5, 3, 3, 1, 2400.00, 2400.00),
(6, 3, 7, 1, 350.00, 350.00),
(7, 4, 4, 1, 1350.00, 1350.00),
(8, 4, 6, 1, 650.00, 650.00);
SET IDENTITY_INSERT dbo.order_items OFF;
GO

SET IDENTITY_INSERT dbo.payments ON;
INSERT INTO dbo.payments (id, payment_type, order_id, amount, payment_status, payment_timestamp, transaction_reference) VALUES
(1, 'CARD', 1, 2305.00, 'AUTHORIZED', '2026-10-18 12:31:10', 'TXN-CARD-LK9901'),
(2, 'CASH', 2, 2610.00, 'PENDING', '2026-10-18 12:35:40', 'TXN-CASH-LK7710'),
(3, 'CARD', 3, 3100.00, 'COMPLETED', '2026-10-18 11:06:15', 'TXN-CARD-LK8831'),
(4, 'CARD', 4, 1900.00, 'AUTHORIZED', '2026-10-18 13:00:55', 'TXN-CARD-LK6629');
SET IDENTITY_INSERT dbo.payments OFF;
GO

INSERT INTO dbo.card_payments (id, card_last_four, card_type, authorization_code, gateway_transaction_id) VALUES
(1, '4242', 'VISA', 'AUTH-LK-94021', 'pay_lk_1NpZ3uLkdIwHuV8K70'),
(3, '8819', 'MASTERCARD', 'AUTH-LK-33901', 'pay_lk_2KqM8vPleJxKqL9W12'),
(4, '1104', 'AMEX', 'AUTH-LK-77148', 'pay_lk_3LtX9wQmeLyNrM0X34');
GO

INSERT INTO dbo.cash_payments (id, amount_tendered, change_due) VALUES
(2, 3000.00, 390.00);
GO

PRINT 'Sri Lankan mock seed data in LKR successfully inserted into [food_delivery_db].';
GO
