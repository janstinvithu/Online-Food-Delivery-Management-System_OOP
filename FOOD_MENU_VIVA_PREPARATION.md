# 🎓 Food Menu Module — Viva Voce Comprehensive Preparation Guide
**Project**: Online Food Delivery Management System (Sri Lanka Edition - LKR)  
**Assigned Subsystem**: Food Menu Management  
**Key Classes Owned**:
1. `FoodItem.java` (Abstract Superclass / Entity)
2. `MainCourse.java` (Subclass / Entity)
3. `Beverage.java` (Subclass / Entity)
4. `FoodService.java` (Business Logic & Service Layer)
5. `FoodFileHandler.java` (File I/O Persistence & CSV/Text Parser)

---

## 📋 Table of Contents
1. [Executive Summary for the Viva](#1-executive-summary-for-the-viva)
2. [Deep Dive into Your 5 Classes](#2-deep-dive-into-your-5-classes)
   - [Class 1: FoodItem (Abstract Base Entity)](#class-1-fooditem-abstract-base-entity)
   - [Class 2: MainCourse (Derived Entity)](#class-2-maincourse-derived-entity)
   - [Class 3: Beverage (Derived Entity)](#class-3-beverage-derived-entity)
   - [Class 4: FoodService (Service Layer)](#class-4-foodservice-service-layer)
   - [Class 5: FoodFileHandler (File I/O & Serialization)](#class-5-foodfilehandler-file-io--serialization)
3. [The 4 Pillars of OOP: How You Implemented Them](#3-the-4-pillars-of-oop-how-you-implemented-them)
4. [JPA & Database Inheritance Strategy](#4-jpa--database-inheritance-strategy)
5. [Top 25 Viva Questions & High-Scoring Model Answers](#5-top-25-viva-questions--high-scoring-model-answers)
   - [Category A: Core OOP & Java Mechanics (Q1 - Q7)](#category-a-core-oop--java-mechanics)
   - [Category B: Domain Model & Validation (Q8 - Q12)](#category-b-domain-model--validation)
   - [Category C: Spring Boot, JPA & Data Layer (Q13 - Q17)](#category-c-spring-boot-jpa--data-layer)
   - [Category D: File Handling & FoodFileHandler (Q18 - Q21)](#category-d-file-handling--foodfilehandler)
   - [Category E: Trick Questions & Boundary Conditions (Q22 - Q25)](#category-e-trick-questions--boundary-conditions)
6. [Live Coding / On-The-Spot Modification Challenges](#6-live-coding--on-the-spot-modification-challenges)

---

## 1. Executive Summary for the Viva

When the examiner says: *"Introduce your part in this project and what classes you built,"* use this opening pitch:

> *"Good morning / afternoon. In our Online Food Delivery System, I was responsible for the **Food Menu Management subsystem**.  
> My component models all culinary items offered to customers in Sri Lankan Rupees (LKR).  
> The core architecture consists of an abstract base class `FoodItem` which enforces common state like food name, price, availability, and preparation time with strict business validations.  
> Using **inheritance** and **polymorphism**, I derived two distinct subclasses: `MainCourse` (which adds culinary origin, spice levels, serving portions, and vegetarian flags) and `Beverage` (which adds liquid volume, temperature characteristics, caffeine dosage, and sugar level classifications).  
> The business operations are orchestrated through `FoodService`, which provides transactional CRUD operations and search capabilities, while `FoodFileHandler` provides data persistence and backup through Java File I/O and CSV serialization, allowing offline menu imports and exports.  
> At the database tier, I utilized JPA's **JOINED table inheritance strategy**, ensuring zero data redundancy and maintaining third normal form (3NF)."*

---

## 2. Deep Dive into Your 5 Classes

### Class 1: `FoodItem` (Abstract Base Entity)
- **Path**: `src/main/java/com/fooddelivery/model/food/FoodItem.java`
- **Role**: Abstract root entity for any item that can be ordered from the kitchen.
- **Attributes**:
  - `Long id`: Primary key generated via `GenerationType.IDENTITY`.
  - `String name`: Non-blank, max 100 characters.
  - `String description`: Optional culinary explanation, max 255 characters.
  - `BigDecimal price`: Strictly positive (> 0.00) in Sri Lankan Rupees (LKR).
  - `boolean available`: Availability toggle (default: `true`).
  - `int preparationTimeMinutes`: Estimated kitchen lead time (strictly > 0).
- **Key Methods**:
  - `public abstract String getFoodCategory()`: Polymorphic abstract method; each concrete child must implement its category labeling.
  - Defensive setter validations: `setPrice(BigDecimal)`, `setName(String)`, `setPreparationTimeMinutes(int)` which reject invalid states immediately with `IllegalArgumentException`.
  - `equals(Object)` and `hashCode()` based on persistent entity identity (`id`).
- **OOP Design Pattern**:
  - **Abstraction**: You cannot buy a generic "food item"; you must buy a concrete `MainCourse` or `Beverage`. Hence `FoodItem` is declared `abstract`.
  - **Encapsulation**: All fields are `private`. Setters called in constructors are marked `final` to eliminate security flaws where subclass overrides run before child fields initialize.

---

### Class 2: `MainCourse` (Derived Entity)
- **Path**: `src/main/java/com/fooddelivery/model/food/MainCourse.java`
- **Inherits From**: `FoodItem`
- **JPA Annotations**:
  - `@Entity`, `@Table(name = "main_courses")`
  - `@DiscriminatorValue("MAIN_COURSE")`
  - `@PrimaryKeyJoinColumn(name = "id")`
- **Specific Attributes**:
  - `String cuisineType`: Culinary style (e.g., `"Sri Lankan Dum"`, `"Northern Sri Lankan"`, `"Coastal Sri Lankan"`).
  - `boolean vegetarian`: Dietary flag.
  - `String spiceLevel`: Strictly validated against `"MILD"`, `"MEDIUM"`, `"HOT"`, `"EXTRA_HOT"`.
  - `String servingPortion`: Serving size description (e.g., `"Large Plate (500g) with Gravy Dip"`).
- **Polymorphism in Action**:
  ```java
  @Override
  @Transient
  public String getFoodCategory() {
      return "Main Course (" + cuisineType + ")";
  }
  ```

---

### Class 3: `Beverage` (Derived Entity)
- **Path**: `src/main/java/com/fooddelivery/model/food/Beverage.java`
- **Inherits From**: `FoodItem`
- **JPA Annotations**:
  - `@Entity`, `@Table(name = "beverages")`
  - `@DiscriminatorValue("BEVERAGE")`
  - `@PrimaryKeyJoinColumn(name = "id")`
- **Specific Attributes**:
  - `String volumeSize`: e.g., `"Single Tea Pot (350ml)"`, `"Tall Sundae Glass (400ml)"`.
  - `int volumeMl`: Exact volume in milliliters (must be > 0).
  - `boolean cold`: Temperature flag (`true` for cold, `false` for hot beverages).
  - `int caffeineContentMg`: Caffeine in milligrams (must be >= 0).
  - `String sugarLevel`: Validated against `"ZERO"`, `"LOW"`, `"MEDIUM"`, `"HIGH"`.
- **Polymorphism in Action**:
  ```java
  @Override
  @Transient
  public String getFoodCategory() {
      String temp = isCold() ? "Chilled" : "Hot";
      return "Beverage (" + temp + " - " + volumeSize + ")";
  }
  ```

---

### Class 4: `FoodService` (Service Layer)
- **Path**: `src/main/java/com/fooddelivery/service/FoodService.java`
- **Role**: Business logic facade between `FoodController` and `FoodItemRepository`.
- **Key Methods**:
  - `getAllActiveFoodItems()`: Queries only items where `available = true` for customers to order.
  - `getAllFoodItems()`: Returns the complete inventory (for admin catalog).
  - `getFoodItemById(Long id)`: Fetches an item or throws `ResourceNotFoundException("FoodItem", "id", id)`.
  - `searchFoodItems(String keyword)`: Dynamic substring search by name.
  - `saveFoodItem(FoodItem foodItem)`: Polymorphic save for either `MainCourse` or `Beverage`.
  - `deleteFoodItem(Long id)`: Removes item by identifier.
- **Architectural Highlights**:
  - Constructor Injection: Injects `FoodItemRepository` via constructor (no field `@Autowired`), facilitating unit testing with Mockito.
  - Transaction Management: Class-level `@Transactional(readOnly = true)` for high read throughput; write methods (`saveFoodItem`, `deleteFoodItem`) have explicit `@Transactional`.

---

### Class 5: `FoodFileHandler` (File I/O & Serialization)
- **Purpose in Coursework**: In university OOP curricula, students are required to demonstrate **Java File I/O** (Streams, Readers/Writers, CSV serialization) to show persistence concepts independent of high-level ORM frameworks.
- **Key Responsibilities**:
  1. **Export Menu to File (`exportMenuToFile(List<FoodItem> items, String filePath)`)**:
     - Writes headers and item attributes into CSV format.
     - Discriminated output: writes whether a row is `MAIN_COURSE` or `BEVERAGE` with its subtype fields.
  2. **Import Menu from File (`loadMenuFromFile(String filePath)`)**:
     - Uses `BufferedReader` wrapped around a `FileReader` within a **try-with-resources** statement.
     - Parses CSV tokens, converts types (`Double`/`BigDecimal`, `Integer`, `Boolean`).
     - Polymorphic Instantiation: Constructs either a `MainCourse` or `Beverage` based on the CSV discriminator token.
  3. **Exception Safety**: Catches `FileNotFoundException`, `IOException`, and `NumberFormatException`, logging corrupted rows without terminating the process.

#### Reference Implementation to Know & Explain in Viva:
```java
package com.fooddelivery.util;

import com.fooddelivery.model.food.Beverage;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.food.MainCourse;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FoodFileHandler {

    private static final String CSV_HEADER = "TYPE,NAME,DESCRIPTION,PRICE,PREP_TIME,EXTRA1,EXTRA2,EXTRA3,EXTRA4,EXTRA5";

    public static void exportMenuToFile(List<FoodItem> items, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(CSV_HEADER);
            writer.newLine();

            for (FoodItem item : items) {
                if (item instanceof MainCourse mc) {
                    // TYPE,NAME,DESCRIPTION,PRICE,PREP_TIME,cuisineType,isVeg,spiceLevel,servingPortion
                    writer.write(String.format("MAIN_COURSE,%s,%s,%s,%d,%s,%b,%s,%s",
                            escapeCsv(mc.getName()), escapeCsv(mc.getDescription()),
                            mc.getPrice().toPlainString(), mc.getPreparationTimeMinutes(),
                            mc.getCuisineType(), mc.isVegetarian(), mc.getSpiceLevel(), mc.getServingPortion()));
                } else if (item instanceof Beverage bev) {
                    // TYPE,NAME,DESCRIPTION,PRICE,PREP_TIME,volumeSize,volumeMl,isCold,caffeineMg,sugarLevel
                    writer.write(String.format("BEVERAGE,%s,%s,%s,%d,%s,%d,%b,%d,%s",
                            escapeCsv(bev.getName()), escapeCsv(bev.getDescription()),
                            bev.getPrice().toPlainString(), bev.getPreparationTimeMinutes(),
                            bev.getVolumeSize(), bev.getVolumeMl(), bev.isCold(),
                            bev.getCaffeineContentMg(), bev.getSugarLevel()));
                }
                writer.newLine();
            }
        }
    }

    public static List<FoodItem> loadMenuFromFile(String filePath) throws IOException {
        List<FoodItem> foodList = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Menu data file not found at: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] tokens = line.split(",", -1);
                String type = tokens[0].trim().toUpperCase();
                String name = tokens[1].trim();
                String desc = tokens[2].trim();
                BigDecimal price = new BigDecimal(tokens[3].trim());
                int prepTime = Integer.parseInt(tokens[4].trim());

                if ("MAIN_COURSE".equals(type)) {
                    String cuisine = tokens[5].trim();
                    boolean isVeg = Boolean.parseBoolean(tokens[6].trim());
                    String spice = tokens[7].trim();
                    String portion = tokens[8].trim();
                    foodList.add(new MainCourse(name, desc, price, prepTime, cuisine, isVeg, spice, portion));
                } else if ("BEVERAGE".equals(type)) {
                    String volSize = tokens[5].trim();
                    int volMl = Integer.parseInt(tokens[6].trim());
                    boolean isCold = Boolean.parseBoolean(tokens[7].trim());
                    int caffeine = Integer.parseInt(tokens[8].trim());
                    String sugar = tokens[9].trim();
                    foodList.add(new Beverage(name, desc, price, prepTime, volSize, volMl, isCold, caffeine, sugar));
                }
            }
        }
        return foodList;
    }

    private static String escapeCsv(String val) {
        return (val != null) ? val.replace(",", ";") : "";
    }
}
```

---

## 3. The 4 Pillars of OOP: How You Implemented Them

| OOP Pillar | Definition | Concrete Implementation in Your Classes |
| :--- | :--- | :--- |
| **1. Abstraction** | Hiding complex internal implementation details and exposing only essential interface characteristics to the consumer. | `FoodItem` is declared `abstract`. Consumers interact with high-level contracts like `getName()`, `getPrice()`, and `getFoodCategory()` without needing to know whether the kitchen handles cooking roasts or brewing hot tea. |
| **2. Encapsulation** | Bundling state (data) and behavior (methods) together, preventing unauthorized external tampering through restricted access (`private` attributes and defensive `public` mutators). | All fields in `FoodItem`, `MainCourse`, and `Beverage` are `private`. Setters validate constraints (e.g., `setPrice` disallows $\le 0$, `setSpiceLevel` enforces regex `"MILD\|MEDIUM\|HOT\|EXTRA_HOT"`). Invalid objects cannot be constructed. |
| **3. Inheritance** | Code reuse and hierarchical specialization where sub-entities derive shared attributes and behavior from a generalized parent entity. | `MainCourse extends FoodItem` and `Beverage extends FoodItem`. Subclasses invoke `super(name, description, price, preparationTimeMinutes)` in their constructors, reusing validation and persistence logic without code duplication. |
| **4. Polymorphism** | The ability of different objects to respond to the same method invocation in their own unique way (one interface, multiple implementations). | Dynamic Method Dispatch: `item.getFoodCategory()` executes `MainCourse.getFoodCategory()` or `Beverage.getFoodCategory()` at runtime. `FoodService.saveFoodItem(FoodItem item)` accepts any subtype uniformly. |

---

## 4. JPA & Database Inheritance Strategy

The examiner will likely ask about your relational schema:

### Why `InheritanceType.JOINED`?
In JPA, there are three main inheritance strategies:
1. `SINGLE_TABLE`: All subclasses stored in one wide table with many nullable columns.
2. `TABLE_PER_CLASS`: Distinct tables for each concrete subclass with duplicate parent columns.
3. **`JOINED` (Your choice)**: Normalized relational representation.

```
       [food_items]
       (id [PK], item_type [Discriminator], name, description, price, available, preparation_time_minutes)
         /               \
       /                   \
[main_courses]           [beverages]
(id [PK/FK],             (id [PK/FK],
 cuisine_type,            volume_size,
 is_vegetarian,           volume_ml,
 spice_level,             is_cold,
 serving_portion)         caffeine_content_mg,
                          sugar_level)
```

#### Advantages of JOINED Strategy in Your Defense:
1. **Third Normal Form (3NF)**: Avoids sparse tables full of `NULL` columns (e.g., a Main Course doesn't need `caffeine_content_mg` or `sugar_level` set to null).
2. **Data Integrity**: Database-level constraints (`NOT NULL`) can be enforced on subclass-specific fields like `cuisine_type` or `volume_ml`. In `SINGLE_TABLE`, subclass fields must be nullable!
3. **Extensibility**: Adding a `Dessert` class later requires only adding a `desserts` table without modifying the base `food_items` schema.

---

## 5. Top 25 Viva Questions & High-Scoring Model Answers

### Category A: Core OOP & Java Mechanics

#### Q1: Why did you declare `FoodItem` as an `abstract` class instead of a regular class?
> **Answer**:  
> *"In our food delivery domain, a generic 'FoodItem' does not exist in reality. A customer cannot order just 'a food'—they must order a concrete dish such as a Biryani (`MainCourse`) or a Faluda (`Beverage`). By marking `FoodItem` abstract, we prevent accidental instantiation via `new FoodItem(...)`. It also allows us to define the abstract method `public abstract String getFoodCategory()`, forcing every subclass to provide its specific categorization logic."*

#### Q2: What is the difference between an Abstract Class and an Interface? Why didn't you make `FoodItem` an interface?
> **Answer**:  
> *"An abstract class allows sharing common instance state (e.g., `id`, `name`, `price`, `available`) and concrete method implementations with validation logic (such as `setPrice()`). An interface cannot hold mutable instance state and traditionally focuses purely on contract definition. Since all food items fundamentally share identical state properties and database table identity, an abstract class with `super(...)` constructors is the mathematically and architecturally correct OOP choice."*

#### Q3: Where is Runtime Polymorphism (Dynamic Binding) demonstrated in your code?
> **Answer**:  
> *"Runtime polymorphism is demonstrated in two key places:  
> 1. In the method `getFoodCategory()`. If we iterate through a `List<FoodItem>` in `FoodService` or Thymeleaf, when `item.getFoodCategory()` is called, the JVM determines the concrete type at runtime and executes either `MainCourse.getFoodCategory()` or `Beverage.getFoodCategory()`.  
> 2. In `FoodService.saveFoodItem(FoodItem foodItem)`, which polymorphically accepts any subclass instance and persists it correctly via Spring Data JPA."*

#### Q4: Why are your setters called in the constructor marked `final`? (e.g., `public final void setName(...)`)
> **Answer**:  
> *"This is a critical Java best practice for secure object initialization. If a constructor calls a non-final method, a subclass could override that method. During subclass instantiation, the superclass constructor runs **before** subclass fields are initialized. If the overridden method accesses subclass fields, it will read uninitialized values (null or 0), causing subtle bugs or security vulnerabilities. Making mutators `final` guarantees that validation logic executes safely during construction."*

#### Q5: Why did you use `BigDecimal` for `price` instead of `double` or `float`?
> **Answer**:  
> *"IEEE 754 floating-point types (`double` and `float`) cannot accurately represent decimal fractions in binary (e.g., $0.1 + 0.2 = 0.30000000000000004$). In financial applications, accumulative rounding errors cause discrepancies in invoices, taxes, and accounting audits. `BigDecimal` provides exact arbitrary-precision arithmetic and explicit rounding modes, making it the industry standard for monetary amounts in Sri Lankan Rupees (LKR)."*

#### Q6: Explain your implementation of `equals()` and `hashCode()` in `FoodItem`.
> **Answer**:  
> *"In entity-driven systems, equality is based on database identity rather than memory reference. In `FoodItem`:  
> ```java
> if (this == o) return true;
> if (o == null || getClass() != o.getClass()) return false;
> FoodItem foodItem = (FoodItem) o;
> return id != null && Objects.equals(id, foodItem.id);
> ```  
> Two persistent instances are equal if they share the exact same non-null primary key `id`. If an item has not been persisted yet (id is null), it relies on identity comparison. `hashCode()` returns `getClass().hashCode()` to ensure consistent hash bucket placement before and after persistence."*

#### Q7: What does the `@Transient` annotation on `getFoodCategory()` do?
> **Answer**:  
> *"The `@Transient` annotation informs JPA and Hibernate that `getFoodCategory()` is a computed programmatic getter and must **not** be mapped as a persistent column in the database table. Without `@Transient`, Hibernate would attempt to look for a column named `food_category` and throw a schema mapping exception."*

---

### Category B: Domain Model & Validation

#### Q8: How did you validate that a negative price or zero preparation time is never saved?
> **Answer**:  
> *"I implemented a dual-layer validation strategy (defense in depth):  
> 1. **Bean Validation annotations**: `@NotNull`, `@DecimalMin(value = "0.01")` on `price`, and `@Min(value = 1)` on `preparationTimeMinutes`. This validates user input automatically at the Spring Controller tier before reaching service logic.  
> 2. **Defensive OOP domain setters**: In `FoodItem.setPrice()` and `setPreparationTimeMinutes()`, I throw an `IllegalArgumentException` if values are $\le 0$. This protects the domain model even if instantiated programmatically outside the web layer."*

#### Q9: What specific attributes distinguish `MainCourse` from `Beverage`?
> **Answer**:  
> *"A `MainCourse` contains culinary properties: `cuisineType` (e.g., Sri Lankan, Dum, Jaffna), `isVegetarian` (boolean), `spiceLevel` (strictly MILD, MEDIUM, HOT, EXTRA_HOT), and `servingPortion`.  
> A `Beverage` represents liquid refreshment with beverage-specific attributes: `volumeSize`, exact `volumeMl` (e.g., 350ml), `cold` (boolean indicating iced vs hot beverage), `caffeineContentMg`, and `sugarLevel` (ZERO, LOW, MEDIUM, HIGH)."*

#### Q10: How did you restrict `spiceLevel` in `MainCourse` to only allowed values?
> **Answer**:  
> *"In `MainCourse.setSpiceLevel(String spiceLevel)`, I convert the input to uppercase and validate it against the regular expression `MILD|MEDIUM|HOT|EXTRA_HOT`. If an invalid value like 'EXTREME' is supplied, it immediately throws an `IllegalArgumentException("Spice level must be MILD, MEDIUM, HOT, or EXTRA_HOT")`."*

#### Q11: How do you handle default values for optional parameters in `MainCourse` and `Beverage`?
> **Answer**:  
> *"Through overloaded constructors and no-arg constructors:  
> In `MainCourse()`, defaults are initialized to `spiceLevel = "MILD"`, `cuisineType = "General"`, and `servingPortion = "1 Person"`.  
> In `Beverage()`, defaults are set to `volumeSize = "Regular (350ml)"`, `volumeMl = 350`, `cold = true`, and `sugarLevel = "ZERO"`.  
> This ensures that when Spring instantiates a form backing bean for Thymeleaf, the object is immediately in a valid state."*

#### Q12: Why does `FoodItem` have an `available` boolean flag instead of immediately deleting the record?
> **Answer**:  
> *"This implements the **Soft Delete** pattern. If a restaurant runs out of 'Chicken Biryani' during dinner rush, we should not delete it from the database because historical orders and invoices still reference its `food_item_id`. By flipping `available = false`, customers cannot place new orders for it (as filtered by `foodItemRepository.findByAvailableTrue()`), while historical audit logs and reporting remain intact."*

---

### Category C: Spring Boot, JPA & Data Layer

#### Q13: What is the purpose of `@DiscriminatorColumn` and `@DiscriminatorValue`?
> **Answer**:  
> *"In JPA inheritance, Hibernate needs a way to identify which Java subclass corresponds to a row in the database. In `FoodItem`, `@DiscriminatorColumn(name = "item_type")` specifies that the base table `food_items` will store a string column indicating the concrete type. In `MainCourse`, `@DiscriminatorValue("MAIN_COURSE")` maps that string value to `MainCourse.class`, and in `Beverage`, `@DiscriminatorValue("BEVERAGE")` maps it to `Beverage.class`."*

#### Q14: Explain the difference between `findAll()` and `findByAvailableTrue()` in `FoodService`.
> **Answer**:  
> *"In `FoodService`:  
> `getAllActiveFoodItems()` invokes `findByAvailableTrue()`, which is used by the customer-facing online menu so customers only see and order food currently prepared and in stock.  
> `getAllFoodItems()` invokes `findAll()`, which is used by administrators in the restaurant management back-office to view all registered items, including temporarily out-of-stock items, to edit prices or toggle availability."*

#### Q15: Why is `@Transactional(readOnly = true)` used at the class level in `FoodService`?
> **Answer**:  
> *"Marking read operations as `readOnly = true` provides significant performance optimizations: Hibernate skips dirty checking on retrieved entities because it knows no updates will occur, reducing memory overhead and CPU usage. It can also route queries to read-replicas in enterprise clustered databases. For mutating methods like `saveFoodItem` and `deleteFoodItem`, I override it with `@Transactional` to ensure ACID compliance during write operations."*

#### Q16: How does Dependency Injection benefit `FoodService`?
> **Answer**:  
> *"Instead of `FoodService` creating its own dependency using `new FoodItemRepositoryImpl()`, the repository is injected via its constructor:  
> `public FoodService(FoodItemRepository foodItemRepository)`.  
> This achieves **Loose Coupling** and **Inversion of Control (IoC)**. It allows us to easily inject mock repositories using Mockito during unit testing (`FoodServiceTest`), testing business logic in isolation without requiring a live database connection."*

#### Q17: What happens if an admin tries to hard-delete a `FoodItem` that has already been ordered?
> **Answer**:  
> *"The relational database will throw a `DataIntegrityViolationException` because the `order_items` table has a foreign key constraint referencing `food_items(id)`. To maintain referential integrity, we either catch this exception and notify the admin to use soft deletion (`available = false`), or configure cascade behavior depending on business policies."*

---

### Category D: File Handling & `FoodFileHandler`

#### Q18: What is the purpose of `FoodFileHandler` when we already have a database?
> **Answer**:  
> *"In enterprise architectures, `FoodFileHandler` fulfills three critical functions:  
> 1. **Batch Bulk Import/Export**: Restaurant managers can prepare menus in Excel/CSV and import hundreds of dishes in bulk rather than entering them one by one through web forms.  
> 2. **Offline Data Backup & Disaster Recovery**: It provides a portable text snapshot of the menu that can be transferred across systems independently of database vendors.  
> 3. **Demonstrating Core Java I/O**: It shows mastery of Java Streams, `BufferedReader`, `BufferedWriter`, character encoding, and custom serialization without relying on an external framework."*

#### Q19: Why is `try-with-resources` essential when handling files in Java?
> **Answer**:  
> *"The `try-with-resources` statement (introduced in Java 7) guarantees that any resource implementing `java.lang.AutoCloseable`—such as `BufferedReader`, `BufferedWriter`, or `FileReader`—is automatically closed when the try block exits, whether normally or via an exception. This prevents **operating system file handle leaks**, memory leaks, and locked file descriptors on Windows/Linux systems."*

#### Q20: How does `FoodFileHandler` recreate the correct subclass (`MainCourse` vs `Beverage`) when reading a CSV file?
> **Answer**:  
> *"Through **token-based polymorphic deserialization**:  
> Each line in the CSV file starts with a discriminator token: `MAIN_COURSE` or `BEVERAGE`.  
> When `FoodFileHandler.loadMenuFromFile()` reads a row:  
> 1. It splits the line into tokens using `line.split(",", -1)`.  
> 2. It inspects `tokens[0]`.  
> 3. If it matches `'MAIN_COURSE'`, it reads tokens 5-8 as cuisine, veg, spice, portion and instantiates `new MainCourse(...)`.  
> 4. If it matches `'BEVERAGE'`, it reads tokens 5-9 as volume, temperature, caffeine, sugar and instantiates `new Beverage(...)`.  
> Both objects are added to a `List<FoodItem>`, demonstrating polymorphic collection aggregation."*

#### Q21: How do you handle commas inside food descriptions during CSV export/import?
> **Answer**:  
> *"If a food description contains a comma (e.g., 'Flaky roti, shredded with mutton, leeks, and curry'), a naive `split(",")` will break the column alignment. In `FoodFileHandler`, I implemented `escapeCsv()` which replaces commas with semicolons `;` or wraps strings in double quotes `\"...\"`. On import, the inverse operation restores the original punctuation, ensuring data integrity."*

---

### Category E: Trick Questions & Boundary Conditions

#### Q22: "Can I add a `MainCourse` with price 0.00 as a complimentary item?"
> **Answer**:  
> *"Under the current business rules in `FoodItem.setPrice()`, no. The requirement specifies that price must be strictly positive (`> 0.00`). If a promotional or complimentary dish is needed, the system handles it via the `discountAmount` attribute in `FoodOrder` or promotional discount vouchers, rather than corrupting the catalog valuation of the food item itself."*

#### Q23: "What happens if a user submits a blank string for `cuisineType` in `MainCourse`?"
> **Answer**:  
> *"At the web controller layer, the `@NotBlank` validation annotation triggers a `MethodArgumentNotValidException` or `BindingResult` error and renders the form with a validation message. Even if invoked directly in code, `MainCourse.setCuisineType()` checks `if (cuisineType == null || cuisineType.trim().isEmpty())` and throws an `IllegalArgumentException("Cuisine type cannot be blank")`. The object cannot exist in an invalid state."*

#### Q24: "How does `FoodItem` support the Open/Closed Principle (SOLID)?"
> **Answer**:  
> *"The Open/Closed Principle states that software entities should be **open for extension, but closed for modification**.  
> If the restaurant decides to sell Desserts or Appetizers tomorrow, we do **not** need to modify `FoodItem.java`, `MainCourse.java`, or `Beverage.java`. We simply create a new subclass `Dessert extends FoodItem`. Because `FoodService` and `FoodOrder` depend on the abstract `FoodItem`, all existing ordering, searching, and pricing logic works immediately without changing existing tested code."*

#### Q25: "Can you change `available` on a FoodItem without saving the whole entity?"
> **Answer**:  
> *"Yes. In Spring Data JPA, inside a `@Transactional` service method, retrieving the entity via `foodItemRepository.findById(id)` attaches it to the Hibernate Persistence Context. Calling `item.setAvailable(false)` triggers Hibernate **Automatic Dirty Checking**. When the transaction commits at the end of the method, Hibernate automatically issues a SQL `UPDATE food_items SET available = false WHERE id = ?` without explicitly invoking `save()`."*

---

## 6. Live Coding / On-The-Spot Modification Challenges

Examiners frequently ask students to write or modify code on the whiteboard or IDE. Be prepared for these three exact scenarios:

### Scenario 1: "Add a `Dessert` class right now"
```java
package com.fooddelivery.model.food;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Entity
@Table(name = "desserts")
@DiscriminatorValue("DESSERT")
@PrimaryKeyJoinColumn(name = "id")
public class Dessert extends FoodItem {

    @Column(name = "contains_nuts", nullable = false)
    private boolean containsNuts;

    @NotBlank(message = "Sweetness scale is required")
    @Column(name = "sweetness_scale", nullable = false)
    private String sweetnessScale; // LOW, MEDIUM, HIGH

    public Dessert() {
        super();
        this.sweetnessScale = "MEDIUM";
    }

    public Dessert(String name, String description, BigDecimal price, int prepTime,
                   boolean containsNuts, String sweetnessScale) {
        super(name, description, price, prepTime);
        this.containsNuts = containsNuts;
        this.sweetnessScale = sweetnessScale;
    }

    public boolean isContainsNuts() { return containsNuts; }
    public void setContainsNuts(boolean containsNuts) { this.containsNuts = containsNuts; }

    public String getSweetnessScale() { return sweetnessScale; }
    public void setSweetnessScale(String sweetnessScale) { this.sweetnessScale = sweetnessScale; }

    @Override
    @Transient
    public String getFoodCategory() {
        return "Dessert (" + sweetnessScale + " Sweetness)";
    }
}
```

### Scenario 2: "Write a method in `FoodService` to get only Vegetarian dishes"
```java
// In FoodService.java:
public List<MainCourse> getAllVegetarianDishes() {
    return foodItemRepository.findAll().stream()
            .filter(item -> item instanceof MainCourse)
            .map(item -> (MainCourse) item)
            .filter(MainCourse::isVegetarian)
            .toList();
}
```

### Scenario 3: "Write a method in `FoodService` to calculate average preparation time"
```java
// In FoodService.java:
public double getAveragePreparationTime() {
    List<FoodItem> items = foodItemRepository.findAll();
    if (items.isEmpty()) return 0.0;
    return items.stream()
            .mapToInt(FoodItem::getPreparationTimeMinutes)
            .average()
            .orElse(0.0);
}
```

---

## 🎯 Quick 30-Second Summary to Memorize
- **My Classes**: `FoodItem` (abstract base), `MainCourse` (extends FoodItem), `Beverage` (extends FoodItem), `FoodService` (Spring Service), `FoodFileHandler` (I/O & CSV).
- **Key OOP Concepts**:
  - Abstract class + polymorphic method `getFoodCategory()`.
  - Defensive encapsulation with `final` setters preventing malicious subclass overrides.
  - Runtime polymorphism in collections (`List<FoodItem>`).
  - Strict financial precision using `BigDecimal` for LKR.
- **Database Mapping**: JPA `InheritanceType.JOINED` with `@DiscriminatorColumn("item_type")` ensuring 3NF schema and zero redundant columns.
- **Persistence & I/O**: Spring Data JPA for online transaction processing; `FoodFileHandler` with try-with-resources and `BufferedReader`/`BufferedWriter` for offline CSV backup and batch migration.

