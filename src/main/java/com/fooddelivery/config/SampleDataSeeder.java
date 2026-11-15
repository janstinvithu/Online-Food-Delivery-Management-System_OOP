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
import com.fooddelivery.repository.CustomerRepository;
import com.fooddelivery.repository.DeliveryPersonRepository;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.repository.FoodOrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SampleDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(SampleDataSeeder.class);

    private SampleDataSeeder() {
    }

    public static boolean seedIfEmpty(FoodItemRepository foodItemRepository,
                                      CustomerRepository customerRepository,
                                      DeliveryPersonRepository deliveryPersonRepository,
                                      FoodOrderRepository foodOrderRepository,
                                      PaymentRepository paymentRepository) {
        if (foodItemRepository.count() > 0) {
            return false;
        }
        log.info("Seeding dummy Sri Lankan food-delivery records into the JSON store");

        MainCourse biryani = new MainCourse(
                "Chicken Dum Biryani Special",
                "Fragrant kaima basmati rice steamed with marinated chicken leg, boiled egg, mint sambol, and raita",
                new BigDecimal("1850.00"), 25,
                "Sri Lankan Dum", false, "MEDIUM", "Full Clay Pot with Hard-boiled Egg & Gravy");
        MainCourse kottu = new MainCourse(
                "Jaffna Spiced Mutton Kottu",
                "Hand-shredded godamba roti stir-fried with roasted tender mutton, leeks, green chillies, and Jaffna curry gravy",
                new BigDecimal("2250.00"), 20,
                "Northern Sri Lankan", false, "EXTRA_HOT", "Large Plate (500g) with Extra Spicy Curry Dip");
        MainCourse prawnCurry = new MainCourse(
                "Negombo Lagoon Prawn Curry",
                "Succulent fresh lagoon prawns slow-cooked in thick coconut milk, roasted curry powder, and wild goraka",
                new BigDecimal("2400.00"), 30,
                "Coastal Sri Lankan", false, "HOT", "350g Rich Seafood Pot with 2 Roast Paan slices");
        MainCourse friedRice = new MainCourse(
                "Vegetable Wok Fried Rice",
                "Aromatic jasmine rice tossed with crispy garden vegetables, golden cashews, accompanied by authentic hot chilli paste",
                new BigDecimal("1350.00"), 15,
                "Sri Lankan Fusion", true, "MEDIUM", "Standard Bowl (400g) with Katta Sambol & Chilli Paste");
        Beverage ceylonTea = new Beverage(
                "Ceylon Nuwara Eliya Pekoe Tea",
                "Pure Ceylon single-estate high-grown orange pekoe brewed fresh and served with jaggery and fresh milk",
                new BigDecimal("450.00"), 5,
                "Single Tea Pot (350ml)", 350, false, 65, "LOW");
        Beverage faluda = new Beverage(
                "Colombo Royal Faluda",
                "Creamy chilled milk layered with fragrant rose syrup, soaked basil seeds, vermicelli, jelly cubes, and rich vanilla ice cream",
                new BigDecimal("650.00"), 8,
                "Tall Sundae Glass (400ml)", 400, true, 0, "HIGH");
        Beverage kingCoconut = new Beverage(
                "Fresh King Coconut (Thambili)",
                "Naturally sweet, mineral-rich chilled organic king coconut water cut fresh on order",
                new BigDecimal("350.00"), 3,
                "Fresh Nut Serving (450ml)", 450, true, 0, "ZERO");
        foodItemRepository.saveAll(List.of(biryani, kottu, prawnCurry, friedRice, ceylonTea, faluda, kingCoconut));

        RegisteredCustomer eleanor = new RegisteredCustomer(
                "Eleanor Fernando", "eleanor.fernando@vanguard.lk", "+94-77-1234567",
                "No. 42, Ward Place, Colombo 07", 1200, "PLATINUM", "$2a$12$securehashLK1", "CARD");
        eleanor.restoreCreatedAt(LocalDateTime.of(2026, 1, 10, 10, 15));
        RegisteredCustomer marcus = new RegisteredCustomer(
                "Marcus Perera", "marcus.perera@kandytech.lk", "+94-71-9876543",
                "No. 18, Peradeniya Road, Kandy", 650, "GOLD", "$2a$12$securehashLK2", "CASH");
        marcus.restoreCreatedAt(LocalDateTime.of(2026, 2, 14, 14, 30));
        Customer sophia = new Customer(
                "Sophia De Silva", "sophia.desilva@gmail.com", "+94-76-5551234",
                "No. 10, Church Street, Galle Fort");
        sophia.restoreCreatedAt(LocalDateTime.of(2026, 3, 1, 18, 20));
        RegisteredCustomer david = new RegisteredCustomer(
                "David Jayawardena", "david.j@jaffnadigital.lk", "+94-77-4448899",
                "No. 88, Hospital Road, Jaffna", 250, "SILVER", "$2a$12$securehashLK3", "CARD");
        david.restoreCreatedAt(LocalDateTime.of(2026, 3, 12, 11, 45));
        customerRepository.saveAll(List.of(eleanor, marcus, sophia, david));

        DeliveryPerson kasun = new DeliveryPerson("Kasun Bandara", "+94-77-8811223", "Honda Dio Scooter", "WP BDF-4491");
        kasun.setRating(new BigDecimal("4.95"));
        kasun.setAvailable(false);
        DeliveryPerson nuwan = new DeliveryPerson("Nuwan Pradeep", "+94-71-3344556", "TVS King Three-Wheeler", "WP QL-1892");
        nuwan.setRating(new BigDecimal("4.88"));
        DeliveryPerson dinuka = new DeliveryPerson("Dinuka Silva", "+94-76-2233445", "Hero Eco 125 Motorcycle", "SP BD-8821");
        dinuka.setRating(new BigDecimal("4.90"));
        deliveryPersonRepository.saveAll(List.of(kasun, nuwan, dinuka));

        OnlineOrder order1 = new OnlineOrder(
                eleanor, "No. 42, Ward Place, Colombo 07",
                "Security gate pass code #4401, deliver to 3rd floor apartment", new BigDecimal("350.00"));
        order1.setOrderNumber("ORD-20261018-1001");
        order1.setOrderDateTime(LocalDateTime.of(2026, 10, 18, 12, 30));
        order1.addOrderItem(biryani, 1);
        order1.addOrderItem(ceylonTea, 1);
        order1.setOrderStatus("PREPARING");
        order1.setEstimatedDeliveryTime(LocalDateTime.of(2026, 10, 18, 13, 15));
        Delivery delivery1 = new Delivery(kasun);
        delivery1.setTrackingNumber("TRK-COL-2026-0001");
        delivery1.setDeliveryStatus("OUT_FOR_DELIVERY");
        delivery1.setDepartureTime(LocalDateTime.of(2026, 10, 18, 12, 45));
        order1.setDelivery(delivery1);
        foodOrderRepository.save(order1);
        CardPayment pay1 = new CardPayment(order1, order1.getTotalAmount(), "4242", "VISA", "AUTH-LK-94021", "pay_lk_1NpZ3uLkdIwHuV8K70");
        pay1.setTransactionReference("TXN-CARD-LK9901");
        pay1.setPaymentTimestamp(LocalDateTime.of(2026, 10, 18, 12, 31, 10));
        pay1.setPaymentStatus("AUTHORIZED");
        paymentRepository.save(pay1);

        PickupOrder order2 = new PickupOrder(marcus, "01:00 PM - 01:20 PM", 20, 2, "+94-71-9876543", false);
        order2.setOrderNumber("ORD-20261018-1002");
        order2.setOrderDateTime(LocalDateTime.of(2026, 10, 18, 12, 35));
        order2.addOrderItem(kottu, 1);
        order2.addOrderItem(faluda, 1);
        order2.setOrderStatus("READY");
        foodOrderRepository.save(order2);
        CashPayment pay2 = new CashPayment(order2, order2.getTotalAmount(), new BigDecimal("3000.00"));
        pay2.setTransactionReference("TXN-CASH-LK7710");
        pay2.setPaymentTimestamp(LocalDateTime.of(2026, 10, 18, 12, 35, 40));
        pay2.setPaymentStatus("PENDING");
        paymentRepository.save(pay2);

        OnlineOrder order3 = new OnlineOrder(
                sophia, "No. 10, Church Street, Galle Fort",
                "Leave at main reception of Dutch Villa", new BigDecimal("350.00"));
        order3.setOrderNumber("ORD-20261018-1003");
        order3.setOrderDateTime(LocalDateTime.of(2026, 10, 18, 11, 5));
        order3.addOrderItem(prawnCurry, 1);
        order3.addOrderItem(kingCoconut, 1);
        order3.setOrderStatus("DELIVERED");
        order3.setEstimatedDeliveryTime(LocalDateTime.of(2026, 10, 18, 11, 55));
        Delivery delivery2 = new Delivery(dinuka);
        delivery2.setTrackingNumber("TRK-GAL-2026-0002");
        delivery2.setDepartureTime(LocalDateTime.of(2026, 10, 18, 11, 20));
        delivery2.setDeliveryStatus("DELIVERED");
        delivery2.setDeliveredTime(LocalDateTime.of(2026, 10, 18, 11, 55));
        order3.setDelivery(delivery2);
        foodOrderRepository.save(order3);
        CardPayment pay3 = new CardPayment(order3, order3.getTotalAmount(), "8819", "MASTERCARD", "AUTH-LK-33901", "pay_lk_2KqM8vPleJxKqL9W12");
        pay3.setTransactionReference("TXN-CARD-LK8831");
        pay3.setPaymentTimestamp(LocalDateTime.of(2026, 10, 18, 11, 6, 15));
        pay3.setPaymentStatus("COMPLETED");
        paymentRepository.save(pay3);

        PickupOrder order4 = new PickupOrder(david, "01:45 PM - 02:00 PM", 15, 1, "+94-77-4448899", true);
        order4.setOrderNumber("ORD-20261018-1004");
        order4.setOrderDateTime(LocalDateTime.of(2026, 10, 18, 13, 0));
        order4.addOrderItem(friedRice, 1);
        order4.addOrderItem(faluda, 1);
        order4.setOrderStatus("PENDING");
        foodOrderRepository.save(order4);
        CardPayment pay4 = new CardPayment(order4, order4.getTotalAmount(), "1104", "AMEX", "AUTH-LK-77148", "pay_lk_3LtX9wQmeLyNrM0X34");
        pay4.setTransactionReference("TXN-CARD-LK6629");
        pay4.setPaymentTimestamp(LocalDateTime.of(2026, 10, 18, 13, 0, 55));
        pay4.setPaymentStatus("AUTHORIZED");
        paymentRepository.save(pay4);

        log.info("Dummy JSON data is ready: 7 foods, 4 customers, 3 couriers, 4 orders");
        return true;
    }
}
