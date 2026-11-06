# 🌐 System Access Matrix, Port & Localhost Links Guide

The **Online Food Delivery Management System** runs locally on **port `8080`**.
Equipped with clean Spring session-based role enforcement via `AuthInterceptor` (`CUSTOMER` vs `ADMIN`).

---

## 🔑 Pre-Configured Test Credentials

| Role | Name | Email | Password | Access / Tier Details |
| :--- | :--- | :--- | :--- | :--- |
| **ADMIN** | System Administrator | `admin@foodflow.lk` | `admin123` | Full Audit, Food CRUD, Fleet Dispatch, Statuses |
| **CUSTOMER** | Eleanor Fernando | `eleanor.fernando@vanguard.lk` | `password123` | **Platinum Tier** &bull; 1,200 pts &bull; **10% Auto Discount** |
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
| **Place Food Order** | [http://localhost:8080/orders/new](http://localhost:8080/orders/new) | Order Online Delivery or Pickup with automatic tier discount |
| **Customer Profile** | [http://localhost:8080/customers/profile](http://localhost:8080/customers/profile) | View Loyalty Tier (Bronze/Silver/Gold/Platinum) and update address |
| **Checkout & Settle** | [http://localhost:8080/payments/checkout/1](http://localhost:8080/payments/checkout/1) | Settle order with Cash on delivery or Card in Sri Lankan Rupee (LKR) |

### 3. Admin Scope (Requires `'ADMIN'` Role)
| Route | Link | Description |
| :--- | :--- | :--- |
| **Admin Dashboard** | [http://localhost:8080/](http://localhost:8080/) | Live KPI revenue cards (LKR), quick actions, and recent order stream |
| **Food Menu Catalog** | [http://localhost:8080/foods](http://localhost:8080/foods) | Food catalog, search, stock availability, and prices in LKR |
| **Add Food Item** | [http://localhost:8080/foods/new](http://localhost:8080/foods/new) | Create `MainCourse` (spiceLevel) or `Beverage` (volumeSize) |
| **Customer Audit Directory** | [http://localhost:8080/customers](http://localhost:8080/customers) | Full customer audit registry with Sri Lankan addresses and loyalty tiers |
| **Order Management Logs** | [http://localhost:8080/orders](http://localhost:8080/orders) | View all active orders, fulfillment types, and payment states |
| **Order Status Update** | [http://localhost:8080/orders/edit/1](http://localhost:8080/orders/edit/1) | Lifecycle status updater (`PREPARING`, `READY`, `DELIVERED`, `CANCELLED`) |
| **Delivery Dispatch Board** | [http://localhost:8080/deliveries](http://localhost:8080/deliveries) | Active courier fleet status, tracking numbers, and live shipments |
| **Assign Delivery Courier** | [http://localhost:8080/deliveries/new](http://localhost:8080/deliveries/new) | Assign available fleet riders to pending online orders |
| **Payment Auditing** | [http://localhost:8080/payments/1](http://localhost:8080/payments/1) | Inspect polymorphic cash tender & change due / card authorization |

---

## 🗄️ Database Options & SSMS (SQL Server Management Studio)

The system supports both Microsoft SQL Server (managed in SSMS) and in-memory H2.

### 🔷 Option 1: Microsoft SQL Server & SSMS (Active Database)
The database **`food_delivery_db`** is pre-created and populated with authentic Sri Lankan records in your local Microsoft SQL Server (`localhost\SQLEXPRESS`).

1. **Connect via SQL Server Management Studio (SSMS)**:
   - **Server Type**: Database Engine
   - **Server Name**: `localhost\SQLEXPRESS` (or `.` / `localhost`)
   - **Authentication**: **Windows Authentication** (or **SQL Server Authentication** with `sa` / `Password123!`)
   - **Database**: Expand **Databases** &rarr; Select `food_delivery_db`
2. **Execute T-SQL Master Setup Script in SSMS**:
   - File path: [`src/main/resources/db/mssql/setup_ssms_food_delivery.sql`](file:///c:/Online%20Food%20Delivery%20Management%20System/src/main/resources/db/mssql/setup_ssms_food_delivery.sql)
   - Open this `.sql` file in SSMS and press **Execute (F5)** to rebuild or query tables at any time.
3. **Run Application with SQL Server**:
   In [`src/main/resources/application.properties`](file:///c:/Online%20Food%20Delivery%20Management%20System/src/main/resources/application.properties), set:
   ```properties
   spring.profiles.active=sqlserver
   ```

### 🔶 Option 2: Default Dev Profile (In-Memory H2)
- **Profile Setting**: `spring.profiles.active=dev`
- **H2 Web Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL**: `jdbc:h2:mem:food_delivery_db`
- **Username**: `sa` | **Password**: *(leave blank)*

---

## 🚀 How to Launch in IntelliJ IDEA
1. Open the project folder `c:\Online Food Delivery Management System` in IntelliJ IDEA.
2. Navigate to [`src/main/java/com/fooddelivery/FoodDeliveryApplication.java`](file:///c:/Online%20Food%20Delivery%20Management%20System/src/main/java/com/fooddelivery/FoodDeliveryApplication.java).
3. Click the green **Run ▶** icon (or press `Shift + F10`).
4. Once the console logs `Started FoodDeliveryApplication in ... seconds`, navigate to:
   - **Login Page**: [http://localhost:8080/login](http://localhost:8080/login)
