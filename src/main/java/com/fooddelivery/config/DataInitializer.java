package com.fooddelivery.config;

import com.fooddelivery.repository.CustomerRepository;
import com.fooddelivery.repository.DeliveryPersonRepository;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.repository.FoodOrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import com.fooddelivery.service.OperationsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final FoodItemRepository foodItemRepository;
    private final CustomerRepository customerRepository;
    private final DeliveryPersonRepository deliveryPersonRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final PaymentRepository paymentRepository;
    private final OperationsService operationsService;

    public DataInitializer(FoodItemRepository foodItemRepository,
                           CustomerRepository customerRepository,
                           DeliveryPersonRepository deliveryPersonRepository,
                           FoodOrderRepository foodOrderRepository,
                           PaymentRepository paymentRepository,
                           OperationsService operationsService) {
        this.foodItemRepository = foodItemRepository;
        this.customerRepository = customerRepository;
        this.deliveryPersonRepository = deliveryPersonRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.paymentRepository = paymentRepository;
        this.operationsService = operationsService;
    }

    @Override
    public void run(String... args) {
        boolean created = SampleDataSeeder.seedIfEmpty(
                foodItemRepository,
                customerRepository,
                deliveryPersonRepository,
                foodOrderRepository,
                paymentRepository);
        if (!created) {
            log.info("JSON store already contains food records. Existing data was left unchanged.");
        }
        operationsService.seedZonesIfEmpty();
    }
}
