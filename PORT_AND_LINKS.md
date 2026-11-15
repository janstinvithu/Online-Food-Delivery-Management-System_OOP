# 🌐 System Access Matrix, Port & Localhost Links Guide

The **Online Food Delivery Management System** runs locally on **port `8080`**.
Equipped with clean Spring session-based role enforcement via `AuthInterceptor` (`CUSTOMER` vs `ADMIN`).

---

## 🔑 Pre-Configured Test Credentials

| Role | Name | Email | Password | Access / Tier Details |
| :--- | :--- | :--- | :--- | :--- |
| **ADMIN** | System Administrator | `admin@foodflow.lk` | `admin123` | Full Audit, Food CRUD, Fleet Dispatch, Statuses |
| **CUSTOMER** | Eleanor Fernando | `eleanor.fernando@vanguard.lk` | `password123` | **Platinum Tier** &bull; 1,200 pts &bull; **15% Auto Discount** |
| **CUSTOMER** | Marcus Perera | `marcus.perera@kandytech.lk` | `password123` | **Gold Tier** &bull; 650 pts &bull; **10% Auto Discount** |
| **CUSTOMER** | David Jayawardena | `david.j@jaffnadigital.lk` | `password123` | **Silver Tier** &bull; 250 pts &bull; 5% Auto Discount |
| **CUSTOMER** | Sophia De Silva | `sophia.desilva@gmail.com` | `password123` | Guest Checkout Account &bull; Galle Fort |

---

## 🛡️ Role & Route Access Matrix

### 1. Public Scope (Unauthenticated Permitted)
| Route | Link | Description |
| :--- | :--- | :--- |
| **Sign In** | [http://localhost:8080/login](http://localhost:8080/login) | Role-switched login page with dual toggle (Customer vs Admin) |
| **Register** | [http://localhost:8080/customers/new](http://localhost:8080/customers/new) | Create a new customer profile (Earn points & membership) |
| **Sign Out** | [http://localhost:8080/logout](http://localhost:8080/logout) | Session termination and redirect to login |
| **Assets** | `/static/**`, `/css/**`, `/js/**`, `/images/**` | Static stylesheets, JavaScript micro-interactions, SVG icons |

### 2. Customer Scope (Requires `'CUSTOMER'` Role)
| Route | Link | Description |
| :--- | :--- | :--- |
| **My Orders** | [http://localhost:8080/orders/my-orders](http://localhost:8080/orders/my-orders) | Order history, real-time rider tracking, and financial breakdown |
| **Order Details** | [http://localhost:8080/orders/my-orders/4](http://localhost:8080/orders/my-orders/4) | View one order, place or change its review, and edit a pending order |
| **Place Food Order** | [http://localhost:8080/orders/new](http://localhost:8080/orders/new) | Food cards, delivery zone, saved address, then Online Delivery or Pickup |
| **Saved Addresses** | [http://localhost:8080/orders/addresses](http://localhost:8080/orders/addresses) | Create, update, and delete a customer's delivery address |
| **Customer Profile** | [http://localhost:8080/customers/profile](http://localhost:8080/customers/profile) | View Loyalty Tier (Bronze/Silver/Gold/Platinum) and update address |
| **Checkout & Settle** | [http://localhost:8080/payments/checkout/1](http://localhost:8080/payments/checkout/1) | Settle with cash or card, update the payment, request a refund, and save a payment method |

### 3. Admin Scope (Requires `'ADMIN'` Role)
| Route | Link | Description |
| :--- | :--- | :--- |
| **Admin Dashboard** | [http://localhost:8080/](http://localhost:8080/) | Live KPI revenue cards (LKR), quick actions, and recent order stream |
| **Food Menu Catalog** | [http://localhost:8080/foods](http://localhost:8080/foods) | Food catalog, search, stock availability, and prices in LKR |
| **Add Food Item** | [http://localhost:8080/foods/new](http://localhost:8080/foods/new) | Create `MainCourse` (spiceLevel) or `Beverage` (volumeSize) |
| **Customer Audit Directory** | [http://localhost:8080/customers](http://localhost:8080/customers) | Full customer audit registry with Sri Lankan addresses and loyalty tiers |
| **Order Management Logs** | [http://localhost:8080/orders](http://localhost:8080/orders) | View, search, and filter all active orders |
| **Order Status Update** | [http://localhost:8080/orders/edit/1](http://localhost:8080/orders/edit/1) | Update order status, and view, reply to, update, or delete the customer's review |
| **Delivery Dispatch Board** | [http://localhost:8080/deliveries](http://localhost:8080/deliveries) | Active courier fleet status, tracking numbers, and live shipments |
| **Assign Delivery Courier** | [http://localhost:8080/deliveries/new](http://localhost:8080/deliveries/new) | Assign available fleet riders to pending online orders |
| **Courier Fleet** | [http://localhost:8080/deliveries/couriers](http://localhost:8080/deliveries/couriers) | Create, view, update, and delete delivery couriers. Edit a courier to manage shifts |
| **Delivery Zones** | [http://localhost:8080/deliveries/zones](http://localhost:8080/deliveries/zones) | Create, view, update, and delete an area and its delivery fee |
| **Delivery Checkpoints** | [http://localhost:8080/deliveries/1/checkpoints](http://localhost:8080/deliveries/1/checkpoints) | Add, correct, or remove a tracking step for one delivery |
| **Add Courier** | [http://localhost:8080/deliveries/couriers/new](http://localhost:8080/deliveries/couriers/new) | Add a courier name, phone, vehicle, plate, and rating |
| **Payment Auditing** | [http://localhost:8080/payments/1](http://localhost:8080/payments/1) | View a payment, update its status or card authorization, or request a refund |

---

## JSON data file

Orders, customers, food, deliveries, and payments are stored in `data/food-delivery-data.json` (created beside the project when the app starts). Dummy Sri Lankan records are written only when that file has no food items. Edits in the app are saved back to the same file and survive a restart. Backups are `data/food-delivery-data.json.bak` and `data/backups/`. No database, H2 console, or SQL Server profile is required.

---

## 🚀 How to Launch in IntelliJ IDEA
1. Open the project folder `c:\Online Food Delivery Management System` in IntelliJ IDEA.
2. Navigate to [`src/main/java/com/fooddelivery/FoodDeliveryApplication.java`](file:///c:/Online%20Food%20Delivery%20Management%20System/src/main/java/com/fooddelivery/FoodDeliveryApplication.java).
3. Click the green **Run ▶** icon (or press `Shift + F10`).
4. Once the console logs `Started FoodDeliveryApplication in ... seconds`, navigate to:
   - **Login Page**: [http://localhost:8080/login](http://localhost:8080/login)
