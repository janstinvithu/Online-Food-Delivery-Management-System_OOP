-- =============================================================================
-- ONLINE FOOD DELIVERY MANAGEMENT SYSTEM - SRI LANKA EDITION (LKR)
-- Automated Mock Data Seed Script (DML)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Component 01: FoodItem Hierarchy (Authentic Sri Lankan Menu in LKR)
-- -----------------------------------------------------------------------------
INSERT INTO food_items (id, item_type, name, description, price, available, preparation_time_minutes) VALUES 
(1, 'MAIN_COURSE', 'Chicken Dum Biryani Special', 'Fragrant kaima basmati rice steamed with marinated chicken leg, boiled egg, mint sambol, and raita', 1850.00, TRUE, 25),
(2, 'MAIN_COURSE', 'Jaffna Spiced Mutton Kottu', 'Hand-shredded godamba roti stir-fried with roasted tender mutton, leeks, green chillies, and Jaffna curry gravy', 2250.00, TRUE, 20),
(3, 'MAIN_COURSE', 'Negombo Lagoon Prawn Curry', 'Succulent fresh lagoon prawns slow-cooked in thick coconut milk, roasted curry powder, and wild goraka', 2400.00, TRUE, 30),
(4, 'MAIN_COURSE', 'Vegetable Wok Fried Rice', 'Aromatic jasmine rice tossed with crispy garden vegetables, golden cashews, accompanied by authentic hot chilli paste', 1350.00, TRUE, 15),
(5, 'BEVERAGE', 'Ceylon Nuwara Eliya Pekoe Tea', 'Pure Ceylon single-estate high-grown orange pekoe brewed fresh and served with jaggery and fresh milk', 450.00, TRUE, 5),
(6, 'BEVERAGE', 'Colombo Royal Faluda', 'Creamy chilled milk layered with fragrant rose syrup, soaked basil seeds, vermicelli, jelly cubes, and rich vanilla ice cream', 650.00, TRUE, 8),
(7, 'BEVERAGE', 'Fresh King Coconut (Thambili)', 'Naturally sweet, mineral-rich chilled organic king coconut water cut fresh on order', 350.00, TRUE, 3);

INSERT INTO main_courses (id, cuisine_type, is_vegetarian, spice_level, serving_portion) VALUES
(1, 'Sri Lankan Dum', FALSE, 'MEDIUM', 'Full Clay Pot with Hard-boiled Egg & Gravy'),
(2, 'Northern Sri Lankan', FALSE, 'EXTRA_HOT', 'Large Plate (500g) with Extra Spicy Curry Dip'),
(3, 'Coastal Sri Lankan', FALSE, 'HOT', '350g Rich Seafood Pot with 2 Roast Paan slices'),
(4, 'Sri Lankan Fusion', TRUE, 'MEDIUM', 'Standard Bowl (400g) with Katta Sambol & Chilli Paste');

INSERT INTO beverages (id, volume_size, volume_ml, is_cold, caffeine_content_mg, sugar_level) VALUES
(5, 'Single Tea Pot (350ml)', 350, FALSE, 65, 'LOW'),
(6, 'Tall Sundae Glass (400ml)', 400, TRUE, 0, 'HIGH'),
(7, 'Fresh Nut Serving (450ml)', 450, TRUE, 0, 'ZERO');

-- -----------------------------------------------------------------------------
-- Component 02: Customer Hierarchy (Sri Lankan Addresses & Phone Numbers)
-- -----------------------------------------------------------------------------
INSERT INTO customers (id, customer_type, full_name, email, phone, address, created_at, loyalty_points, membership_tier, password_hash, preferred_payment_method) VALUES
(1, 'REGISTERED', 'Eleanor Fernando', 'eleanor.fernando@vanguard.lk', '+94-77-1234567', 'No. 42, Ward Place, Colombo 07', TIMESTAMP '2026-01-10 10:15:00', 1200, 'PLATINUM', '$2a$12$securehashLK1', 'CARD'),
(2, 'REGISTERED', 'Marcus Perera', 'marcus.perera@kandytech.lk', '+94-71-9876543', 'No. 18, Peradeniya Road, Kandy', TIMESTAMP '2026-02-14 14:30:00', 650, 'GOLD', '$2a$12$securehashLK2', 'CASH'),
(3, 'GUEST', 'Sophia De Silva', 'sophia.desilva@gmail.com', '+94-76-5551234', 'No. 10, Church Street, Galle Fort', TIMESTAMP '2026-03-01 18:20:00', 0, NULL, NULL, NULL),
(4, 'REGISTERED', 'David Jayawardena', 'david.j@jaffnadigital.lk', '+94-77-4448899', 'No. 88, Hospital Road, Jaffna', TIMESTAMP '2026-03-12 11:45:00', 250, 'SILVER', '$2a$12$securehashLK3', 'CARD');

-- -----------------------------------------------------------------------------
-- Component 04 (Part A): Delivery Couriers (Sri Lankan Vehicles & Registration)
-- -----------------------------------------------------------------------------
INSERT INTO delivery_persons (id, name, phone, vehicle_type, vehicle_plate_number, is_available, rating) VALUES
(1, 'Kasun Bandara', '+94-77-8811223', 'Honda Dio Scooter', 'WP BDF-4491', FALSE, 4.95),
(2, 'Nuwan Pradeep', '+94-71-3344556', 'TVS King Three-Wheeler', 'WP QL-1892', TRUE, 4.88),
(3, 'Dinuka Silva', '+94-76-2233445', 'Hero Eco 125 Motorcycle', 'SP BD-8821', TRUE, 4.90);

INSERT INTO deliveries (id, tracking_number, delivery_status, departure_time, delivered_time, delivery_person_id) VALUES
(1, 'TRK-COL-2026-0001', 'OUT_FOR_DELIVERY', TIMESTAMP '2026-10-18 12:45:00', NULL, 1),
(2, 'TRK-GAL-2026-0002', 'DELIVERED', TIMESTAMP '2026-10-18 11:20:00', TIMESTAMP '2026-10-18 11:55:00', 3);

-- -----------------------------------------------------------------------------
-- Component 03: Orders (Online & Pickup) in LKR
-- -----------------------------------------------------------------------------
-- Order 1: Online Order by Eleanor Fernando (Platinum: 15% discount on food subtotal LKR 2,300.00 -> Discount LKR 345.00 + Delivery LKR 350.00 = Total LKR 2,305.00)
INSERT INTO food_orders (id, order_number, order_type, order_status, order_date_time, subtotal_amount, discount_amount, total_amount, customer_id) VALUES
(1, 'ORD-20261018-1001', 'ONLINE', 'PREPARING', TIMESTAMP '2026-10-18 12:30:00', 2300.00, 345.00, 2305.00, 1),
(2, 'ORD-20261018-1002', 'PICKUP', 'READY', TIMESTAMP '2026-10-18 12:35:00', 2900.00, 290.00, 2610.00, 2),
(3, 'ORD-20261018-1003', 'ONLINE', 'DELIVERED', TIMESTAMP '2026-10-18 11:05:00', 2750.00, 0.00, 3100.00, 3),
(4, 'ORD-20261018-1004', 'PICKUP', 'PENDING', TIMESTAMP '2026-10-18 13:00:00', 2000.00, 100.00, 1900.00, 4);

INSERT INTO online_orders (id, delivery_address, special_delivery_instructions, estimated_delivery_time, delivery_fee, delivery_id) VALUES
(1, 'No. 42, Ward Place, Colombo 07', 'Security gate pass code #4401, deliver to 3rd floor apartment', TIMESTAMP '2026-10-18 13:15:00', 350.00, 1),
(3, 'No. 10, Church Street, Galle Fort', 'Leave at main reception of Dutch Villa', TIMESTAMP '2026-10-18 11:55:00', 350.00, 2);

INSERT INTO pickup_orders (id, pickup_time_slot, pickup_window_minutes, counter_number, pickup_contact_phone, is_curbside_pickup) VALUES
(2, '01:00 PM - 01:20 PM', 20, 2, '+94-71-9876543', FALSE),
(4, '01:45 PM - 02:00 PM', 15, 1, '+94-77-4448899', TRUE);

INSERT INTO order_items (id, order_id, food_item_id, quantity, unit_price, subtotal) VALUES
-- Order 1: Chicken Dum Biryani (1850.00) + Ceylon Tea (450.00) = Subtotal 2300.00
(1, 1, 1, 1, 1850.00, 1850.00),
(2, 1, 5, 1, 450.00, 450.00),
-- Order 2: Jaffna Mutton Kottu (2250.00) + Colombo Royal Faluda (650.00) = Subtotal 2900.00
(3, 2, 2, 1, 2250.00, 2250.00),
(4, 2, 6, 1, 650.00, 650.00),
-- Order 3: Negombo Prawn Curry (2400.00) + King Coconut (350.00) = Subtotal 2750.00
(5, 3, 3, 1, 2400.00, 2400.00),
(6, 3, 7, 1, 350.00, 350.00),
-- Order 4: Veg Wok Fried Rice (1350.00) + Colombo Faluda (650.00) = Subtotal 2000.00
(7, 4, 4, 1, 1350.00, 1350.00),
(8, 4, 6, 1, 650.00, 650.00);

-- -----------------------------------------------------------------------------
-- Component 04 (Part B): Payments (Cash & Card) in LKR
-- -----------------------------------------------------------------------------
INSERT INTO payments (id, payment_type, order_id, amount, payment_status, payment_timestamp, transaction_reference) VALUES
(1, 'CARD', 1, 2305.00, 'AUTHORIZED', TIMESTAMP '2026-10-18 12:31:10', 'TXN-CARD-LK9901'),
(2, 'CASH', 2, 2610.00, 'PENDING', TIMESTAMP '2026-10-18 12:35:40', 'TXN-CASH-LK7710'),
(3, 'CARD', 3, 3100.00, 'COMPLETED', TIMESTAMP '2026-10-18 11:06:15', 'TXN-CARD-LK8831'),
(4, 'CARD', 4, 1900.00, 'AUTHORIZED', TIMESTAMP '2026-10-18 13:00:55', 'TXN-CARD-LK6629');

INSERT INTO card_payments (id, card_last_four, card_type, authorization_code, gateway_transaction_id) VALUES
(1, '4242', 'VISA', 'AUTH-LK-94021', 'pay_lk_1NpZ3uLkdIwHuV8K70'),
(3, '8819', 'MASTERCARD', 'AUTH-LK-33901', 'pay_lk_2KqM8vPleJxKqL9W12'),
(4, '1104', 'AMEX', 'AUTH-LK-77148', 'pay_lk_3LtX9wQmeLyNrM0X34');

INSERT INTO cash_payments (id, amount_tendered, change_due) VALUES
(2, 3000.00, 390.00);
