package com.fooddelivery.config;

import com.fooddelivery.model.customer.Customer;
import com.fooddelivery.model.customer.RegisteredCustomer;
import com.fooddelivery.model.delivery.CardPayment;
import com.fooddelivery.model.delivery.CashPayment;
import com.fooddelivery.model.delivery.Delivery;
import com.fooddelivery.model.delivery.DeliveryPerson;
import com.fooddelivery.model.food.Beverage;
import com.fooddelivery.model.food.MainCourse;
import com.fooddelivery.model.order.OnlineOrder;
import com.fooddelivery.model.order.PickupOrder;
import com.fooddelivery.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@SuppressWarnings("null")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final FoodItemRepository foodItemRepository;
    private final CustomerRepository customerRepository;
    private final DeliveryPersonRepository deliveryPersonRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final PaymentRepository paymentRepository;

    public DataInitializer(FoodItemRepository foodItemRepository,
                           CustomerRepository customerRepository,
                           DeliveryPersonRepository deliveryPersonRepository,
                           FoodOrderRepository foodOrderRepository,
                           PaymentRepository paymentRepository) {
        this.foodItemRepository = foodItemRepository;
        this.customerRepository = customerRepository;
        this.deliveryPersonRepository = deliveryPersonRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (foodItemRepository.count() > 0) {
            log.info("Database already seeded with {} items. Skipping programmatic seed.", foodItemRepository.count());
            return;
        }

        log.info("Seeding Sri Lankan Food Delivery System mock data in LKR...");

        // 1. Food Items (Component 01)
        MainCourse biryani = new MainCourse(
                "Chicken Dum Biryani Special",
                "Fragrant kaima basmati rice steamed with marinated chicken leg, boiled egg, mint sambol, and raita",
                new BigDecimal("1850.00"), 25,
                "Sri Lankan Dum", false, "MEDIUM", "Full Clay Pot with Egg & Gravy"
        );

        MainCourse kottu = new MainCourse(
                "Jaffna Spiced Mutton Kottu",
                "Hand-shredded godamba roti stir-fried with roasted tender mutton, leeks, green chillies, and Jaffna curry gravy",
                new BigDecimal("2250.00"), 20,
                "Northern Sri Lankan", false, "EXTRA_HOT", "Large Plate (500g) with Gravy Dip"
        );

        MainCourse prawnCurry = new MainCourse(
                "Negombo Lagoon Prawn Curry",
                "Succulent fresh lagoon prawns slow-cooked in thick coconut milk, roasted curry powder, and wild goraka",
                new BigDecimal("2400.00"), 30,
                "Coastal Sri Lankan", false, "HOT", "350g Rich Seafood Pot with Roast Paan"
        );

        MainCourse friedRice = new MainCourse(
                "Vegetable Wok Fried Rice",
                "Aromatic jasmine rice tossed with crispy garden vegetables, golden cashews, accompanied by authentic hot chilli paste",
                new BigDecimal("1350.00"), 15,
                "Sri Lankan Fusion", true, "MEDIUM", "Standard Bowl (400g) with Chilli Paste"
        );

        Beverage ceylonTea = new Beverage(
                "Ceylon Nuwara Eliya Pekoe Tea",
                "Pure Ceylon single-estate high-grown orange pekoe brewed fresh and served with jaggery and fresh milk",
                new BigDecimal("450.00"), 5,
                "Single Tea Pot (350ml)", 350, false, 65, "LOW"
        );

        Beverage faluda = new Beverage(
                "Colombo Royal Faluda",
                "Creamy chilled milk layered with fragrant rose syrup, soaked basil seeds, vermicelli, jelly cubes, and vanilla ice cream",
                new BigDecimal("650.00"), 8,
                "Tall Sundae Glass (400ml)", 400, true, 0, "HIGH"
        );

        Beverage kingCoconut = new Beverage(
                "Fresh King Coconut (Thambili)",
                "Naturally sweet, mineral-rich chilled organic king coconut water cut fresh on order",
                new BigDecimal("350.00"), 3,
                "Fresh Nut Serving (450ml)", 450, true, 0, "ZERO"
        );

        foodItemRepository.saveAll(List.of(biryani, kottu, prawnCurry, friedRice, ceylonTea, faluda, kingCoconut));
        log.info("Seeded 7 Sri Lankan FoodItem entities.");

        // 2. Customers (Component 02)
        RegisteredCustomer eleanor = new RegisteredCustomer(
                "Eleanor Fernando", "eleanor.fernando@vanguard.lk", "+94-77-1234567",
                "No. 42, Ward Place, Colombo 07", 1200, "PLATINUM", "$2a$12$securehashLK1", "CARD"
        );

        RegisteredCustomer marcus = new RegisteredCustomer(
                "Marcus Perera", "marcus.perera@kandytech.lk", "+94-71-9876543",
                "No. 18, Peradeniya Road, Kandy", 650, "GOLD", "$2a$12$securehashLK2", "CASH"
        );

        Customer sophia = new Customer(
                "Sophia De Silva", "sophia.desilva@gmail.com", "+94-76-5551234",
                "No. 10, Church Street, Galle Fort"
        );

        RegisteredCustomer david = new RegisteredCustomer(
                "David Jayawardena", "david.j@jaffnadigital.lk", "+94-77-4448899",
                "No. 88, Hospital Road, Jaffna", 250, "SILVER", "$2a$12$securehashLK3", "CARD"
        );

        customerRepository.saveAll(List.of(eleanor, marcus, sophia, david));
        log.info("Seeded 4 Customer entities in Sri Lanka.");

        // 3. Couriers (Component 04 Part A)
        DeliveryPerson kasun = new DeliveryPerson("Kasun Bandara", "+94-77-8811223", "Honda Dio Scooter", "WP BDF-4491");
        kasun.setRating(new BigDecimal("4.95"));
        kasun.setAvailable(false);

        DeliveryPerson nuwan = new DeliveryPerson("Nuwan Pradeep", "+94-71-3344556", "TVS King Three-Wheeler", "WP QL-1892");
        nuwan.setRating(new BigDecimal("4.88"));
        nuwan.setAvailable(true);

        DeliveryPerson dinuka = new DeliveryPerson("Dinuka Silva", "+94-76-2233445", "Hero Eco 125 Motorcycle", "SP BD-8821");
        dinuka.setRating(new BigDecimal("4.90"));
        dinuka.setAvailable(true);

        deliveryPersonRepository.saveAll(List.of(kasun, nuwan, dinuka));
        log.info("Seeded 3 DeliveryPerson entities.");

        // 4. Orders, Line Items, Deliveries & Payments (Component 03 & 04)
        // Order 1: OnlineOrder
        OnlineOrder order1 = new OnlineOrder(
                eleanor,
                "No. 42, Ward Place, Colombo 07",
                "Security gate pass code #4401, deliver to 3rd floor apartment",
                new BigDecimal("350.00")
        );
        order1.setOrderNumber("ORD-20261018-1001");
        order1.addOrderItem(biryani, 1);
        order1.addOrderItem(ceylonTea, 1);
        order1.setOrderStatus("PREPARING");

        Delivery delivery1 = new Delivery(kasun);
        delivery1.setTrackingNumber("TRK-COL-2026-0001");
        delivery1.setDeliveryStatus("OUT_FOR_DELIVERY");
        delivery1.setDepartureTime(LocalDateTime.now().minusMinutes(20));
        order1.setDelivery(delivery1);

        foodOrderRepository.save(order1);

        CardPayment pay1 = new CardPayment(order1, order1.getTotalAmount(), "4242", "VISA", "AUTH-LK-94021", "pay_lk_1NpZ3uLkdIwHuV8K70");
        pay1.setPaymentStatus("AUTHORIZED");
        paymentRepository.save(pay1);

        // Order 2: PickupOrder
        PickupOrder order2 = new PickupOrder(marcus, "01:00 PM - 01:20 PM", 20, 2, "+94-71-9876543", false);
        order2.setOrderNumber("ORD-20261018-1002");
        order2.addOrderItem(kottu, 1);
        order2.addOrderItem(faluda, 1);
        order2.setOrderStatus("READY");

        foodOrderRepository.save(order2);

        CashPayment pay2 = new CashPayment(order2, order2.getTotalAmount(), new BigDecimal("3000.00"));
        pay2.setPaymentStatus("PENDING");
        paymentRepository.save(pay2);

        // Order 3: OnlineOrder
        OnlineOrder order3 = new OnlineOrder(
                sophia,
                "No. 10, Church Street, Galle Fort",
                "Leave at main reception of Dutch Villa",
                new BigDecimal("350.00")
        );
        order3.setOrderNumber("ORD-20261018-1003");
        order3.addOrderItem(prawnCurry, 1);
        order3.addOrderItem(kingCoconut, 1);
        order3.setOrderStatus("DELIVERED");

        Delivery delivery2 = new Delivery(dinuka);
        delivery2.setTrackingNumber("TRK-GAL-2026-0002");
        delivery2.setDeliveryStatus("DELIVERED");
        delivery2.setDepartureTime(LocalDateTime.now().minusMinutes(50));
        delivery2.setDeliveredTime(LocalDateTime.now().minusMinutes(15));
        order3.setDelivery(delivery2);

        foodOrderRepository.save(order3);

        CardPayment pay3 = new CardPayment(order3, order3.getTotalAmount(), "8819", "MASTERCARD", "AUTH-LK-33901", "pay_lk_2KqM8vPleJxKqL9W12");
        pay3.setPaymentStatus("COMPLETED");
        paymentRepository.save(pay3);

        // Order 4: PickupOrder
        PickupOrder order4 = new PickupOrder(david, "01:45 PM - 02:00 PM", 15, 1, "+94-77-4448899", true);
        order4.setOrderNumber("ORD-20261018-1004");
        order4.addOrderItem(friedRice, 1);
        order4.addOrderItem(faluda, 1);
        order4.setOrderStatus("PENDING");

        foodOrderRepository.save(order4);

        CardPayment pay4 = new CardPayment(order4, order4.getTotalAmount(), "1104", "AMEX", "AUTH-LK-77148", "pay_lk_3LtX9wQmeLyNrM0X34");
        pay4.setPaymentStatus("AUTHORIZED");
        paymentRepository.save(pay4);

        log.info("Sri Lankan mock data seed initialized successfully in LKR!");
    }
}
