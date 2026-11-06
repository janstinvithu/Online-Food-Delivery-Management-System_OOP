# Online Food Delivery Management System (Sri Lanka Edition - LKR)

Production-ready, enterprise-grade Spring Boot 3 & Spring Data JPA full-stack implementation tailored for **IntelliJ IDEA**, configured with a crisp **Light Theme UI** and **Sri Lankan Rupee (LKR)** currency formatting.

---

## ⚡ Quick Access & Port Links

The application runs locally on **Port `8080`**. Detailed documentation is available in [PORT_AND_LINKS.md](PORT_AND_LINKS.md).

| Module / Screen | URL Link | Description |
| :--- | :--- | :--- |
| **🚀 Main Dashboard** | [http://localhost:8080/](http://localhost:8080/) | Central overview & LKR metrics |
| **🍲 Food Menu Catalog** | [http://localhost:8080/foods](http://localhost:8080/foods) | Sri Lankan menu & item stock |
| **➕ Add Food Item** | [http://localhost:8080/foods/new](http://localhost:8080/foods/new) | Create MainCourse or Beverage |
| **👥 Customer Directory** | [http://localhost:8080/customers](http://localhost:8080/customers) | Loyalty points & member tiers |
| **📝 Register Customer** | [http://localhost:8080/customers/new](http://localhost:8080/customers/new) | Member account & guest registration |
| **📦 Order Logs & History** | [http://localhost:8080/orders](http://localhost:8080/orders) | All active & completed orders |
| **🛵 Place Food Order** | [http://localhost:8080/orders/new](http://localhost:8080/orders/new) | Online Delivery & Counter Pickup checkout |
| **🛵 Delivery Dispatch Board** | [http://localhost:8080/deliveries](http://localhost:8080/deliveries) | Active courier fleet & tracking |
| **📍 Assign Courier** | [http://localhost:8080/deliveries/new](http://localhost:8080/deliveries/new) | Dispatch rider to online delivery |
| **💳 Settle Payment** | [http://localhost:8080/payments/1](http://localhost:8080/payments/1) | Cash tender/change & Card validation |
| **🗄️ H2 Database Console** | [http://localhost:8080/h2-console](http://localhost:8080/h2-console) | In-memory relational database browser |

---

## 🏗️ Architectural Overview & Design Patterns

- **Framework**: Spring Boot 3.2.4 on Java 17/21 (LTS).
- **Persistence**: Spring Data JPA & Hibernate 6 with Relational SQL.
- **Frontend Layer**: Semantic HTML5, Thymeleaf Template Engine, and Vanilla CSS (Zero external CSS frameworks).
- **Light Theme Design System**:
  - Background: Crisp Light Slate (`#F8FAFC`)
  - Surfaces: Pure White Cards (`#FFFFFF`) with subtle borders (`#E2E8F0`)
  - Primary Accent: Electric Blue (`#2563EB` / Hover `#1D4ED8`)
  - Typography: Dark Slate (`#0F172A`) with high legibility (`Inter`, `system-ui`)
- **Locale & Currency**: Sri Lanka (`LKR` / `Rs. - formatted as "LKR #,##0.00"` via dedicated [`CurrencyUtil`](src/main/java/com/fooddelivery/common/util/CurrencyUtil.java)).

---

## 🧩 OOP Inheritance & Domain Hierarchy

```mermaid
classDiagram
    class FoodItem {
        <<Abstract>>
        +String name
        +BigDecimal price
        +int preparationTimeMinutes
        +boolean available
    }
    class MainCourse {
        +String cuisineType
        +String spiceLevel
        +String servingPortion
        +boolean vegetarian
    }
    class Beverage {
        +String volumeSize
        +int volumeMl
        +String sugarLevel
        +int caffeineContentMg
        +boolean cold
    }
    FoodItem <|-- MainCourse : Joined Table
    FoodItem <|-- Beverage : Joined Table

    class Customer {
        +String fullName
        +String email
        +String phone
        +String address
    }
    class RegisteredCustomer {
        +int loyaltyPoints
        +String membershipTier
        +double getDiscountRate()
        +BigDecimal calculateDiscount(subtotal)
    }
    Customer <|-- RegisteredCustomer : Single Table

    class FoodOrder {
        <<Abstract>>
        +String orderNumber
        +String orderStatus
        +BigDecimal subtotalAmount
        +BigDecimal discountAmount
        +BigDecimal totalAmount
        +recalculateTotal()
        +cancelOrder()
    }
    class OnlineOrder {
        +String deliveryAddress
        +BigDecimal deliveryFee
    }
    class PickupOrder {
        +String pickupTimeSlot
        +int counterNumber
        +boolean curbsidePickup
    }
    FoodOrder <|-- OnlineOrder : Joined Table
    FoodOrder <|-- PickupOrder : Joined Table

    class Payment {
        <<Abstract>>
        +BigDecimal amount
        +String paymentStatus
        +String transactionReference
    }
    class CashPayment {
        +BigDecimal amountTendered
        +BigDecimal changeDue
    }
    class CardPayment {
        +String cardLastFour
        +String cardType
        +String authorizationCode
    }
    Payment <|-- CashPayment : Joined Table
    Payment <|-- CardPayment : Joined Table

    FoodOrder "1" *-- "many" OrderItem : Composition
    OrderItem --> FoodItem : References
    FoodOrder "1" o-- "1" Payment : Associated
    OnlineOrder "1" <--> "1" Delivery : One-to-One
    Delivery "many" --> "1" DeliveryPerson : Assigned Courier
```

### Module Specifications:
1. **Component 01 - Food Menu Management**:
   - JPA Joined Table: `FoodItem` (abstract base) -> `MainCourse` (`spiceLevel`, `servingPortion`, `cuisineType`) and `Beverage` (`volumeSize`, `volumeMl`, `sugarLevel`, `caffeineContentMg`).
   - Routes: Catalog (`/foods`), Add Item (`/foods/new`), Edit Item (`/foods/edit/{id}`), Delete (`/foods/delete/{id}`).
2. **Component 02 - Customer Management**:
   - JPA Single Table: `Customer` (Guest) -> `RegisteredCustomer` (`loyaltyPoints`, dynamic discount tiers: Platinum 15%, Gold 10%, Silver 5%, Bronze 0%).
   - Routes: Directory (`/customers`), Register (`/customers/new`), Edit Profile (`/customers/edit/{id}`), Delete (`/customers/delete/{id}`).
3. **Component 03 - Food Order Management**:
   - JPA Joined Table: `FoodOrder` -> `OnlineOrder` (LKR 350.00 standard delivery fee) and `PickupOrder` (counter slot & curbside flag).
   - Dynamic Financial Logic: Automatically calculates line subtotals, computes loyalty discounts, adds fulfillment fees, and calculates net totals.
   - Routes: Checkout (`/orders/new`), Order Logs (`/orders`), Status Management (`/orders/edit/{id}`), Cancellation (`/orders/{id}/cancel`).
4. **Component 04 - Delivery & Payment Management**:
   - Associations: OneToOne between `OnlineOrder` and `Delivery`; ManyToOne between `Delivery` and `DeliveryPerson`.
   - Polymorphic Payments: `CashPayment` (tendered vs change due) and `CardPayment` (4-digit card mask & authorization tokens).
   - Routes: Dispatch Board (`/deliveries`), Assign Courier (`/deliveries/new` & `/deliveries/assign/{id}`), Settle Payment (`/payments/{orderId}`).

---

## 🚀 Running the Project in IntelliJ IDEA

### 1. Open Project in IntelliJ IDEA
1. Open IntelliJ IDEA -> `File` -> `Open...` -> Select `c:\Online Food Delivery Management System`.
2. IntelliJ will detect Maven (`pom.xml`) and auto-import dependencies.
3. Verify Project SDK is set to **JDK 17 or JDK 21**:
   - `File` -> `Project Structure...` -> `Project` -> Set SDK to JDK 17+.

### 2. Run the Application
- Locate `src/main/java/com/fooddelivery/FoodDeliveryApplication.java`.
- Click the green **Run** triangle or press `Shift + F10`.
- Alternatively, run via terminal:
  ```powershell
  mvn spring-boot:run
  ```

### 3. Access Live Application URLs
- **Main Dashboard**: [http://localhost:8080/](http://localhost:8080/)
- **Food Menu Catalog**: [http://localhost:8080/foods](http://localhost:8080/foods)
- **Add Dish / Beverage**: [http://localhost:8080/foods/new](http://localhost:8080/foods/new)
- **Customer Directory**: [http://localhost:8080/customers](http://localhost:8080/customers)
- **Register Customer**: [http://localhost:8080/customers/new](http://localhost:8080/customers/new)
- **Order Logs**: [http://localhost:8080/orders](http://localhost:8080/orders)
- **Place Food Order**: [http://localhost:8080/orders/new](http://localhost:8080/orders/new)
- **Delivery Dispatch Board**: [http://localhost:8080/deliveries](http://localhost:8080/deliveries)
- **Assign Courier**: [http://localhost:8080/deliveries/new](http://localhost:8080/deliveries/new)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:fooddeliverydb`, User: `sa`, Password: `password`)

---

## 🗄️ Database Scripts & Automated Seeding

- [`schema.sql`](src/main/resources/db/migration/schema.sql): Complete DDL with primary keys, foreign keys, check constraints, cascading rules, and performance indexes.
- [`data.sql`](src/main/resources/db/migration/data.sql): Realistic Sri Lankan mock records (Chicken Dum Biryani, Jaffna Mutton Kottu, Ceylon Pekoe Tea, Colombo/Jaffna addresses, and prices in LKR).
- [`DataInitializer.java`](src/main/java/com/fooddelivery/config/DataInitializer.java): Programmatic safety fallback ensuring sample data is always hydrated.
