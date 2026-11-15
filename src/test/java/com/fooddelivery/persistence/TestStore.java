package com.fooddelivery.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.repository.CustomerRepository;
import com.fooddelivery.repository.DeliveryPersonRepository;
import com.fooddelivery.repository.DeliveryRepository;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.repository.FoodOrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import com.fooddelivery.repository.impl.FileCustomerRepository;
import com.fooddelivery.repository.impl.FileDeliveryPersonRepository;
import com.fooddelivery.repository.impl.FileDeliveryRepository;
import com.fooddelivery.repository.impl.FileFoodItemRepository;
import com.fooddelivery.repository.impl.FileFoodOrderRepository;
import com.fooddelivery.repository.impl.FilePaymentRepository;
import com.fooddelivery.service.OrderService;
import java.nio.file.Path;

public final class TestStore {

    public final FileDataStore files;
    public final DataStoreManager manager;
    public final CustomerRepository customers;
    public final FoodItemRepository foods;
    public final FoodOrderRepository orders;
    public final DeliveryRepository deliveries;
    public final DeliveryPersonRepository couriers;
    public final PaymentRepository payments;
    public final OrderService orderService;

    private TestStore(FileDataStore files, DataStoreManager manager,
                      CustomerRepository customers, FoodItemRepository foods, FoodOrderRepository orders,
                      DeliveryRepository deliveries, DeliveryPersonRepository couriers, PaymentRepository payments) {
        this.files = files;
        this.manager = manager;
        this.customers = customers;
        this.foods = foods;
        this.orders = orders;
        this.deliveries = deliveries;
        this.couriers = couriers;
        this.payments = payments;
        this.orderService = new OrderService(orders, foods, customers, payments);
    }

    public static TestStore open(Path directory) {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setDataDirectory(directory.toString());
        properties.setFileName("food-delivery-data.json");
        DataValidator validator = new DataValidator();
        FileDataStore files = new FileDataStore(properties, new ObjectMapper(), validator);
        DataStoreManager manager = new DataStoreManager(files);
        DomainMapper mapper = new DomainMapper();
        AggregateWriter writer = new AggregateWriter(mapper);
        CustomerRepository customers = new FileCustomerRepository(manager, mapper, writer);
        FoodItemRepository foods = new FileFoodItemRepository(manager, mapper, writer);
        FoodOrderRepository orders = new FileFoodOrderRepository(manager, mapper, writer);
        DeliveryRepository deliveries = new FileDeliveryRepository(manager, mapper, writer);
        DeliveryPersonRepository couriers = new FileDeliveryPersonRepository(manager, mapper, writer);
        PaymentRepository payments = new FilePaymentRepository(manager, mapper, writer);
        return new TestStore(files, manager, customers, foods, orders, deliveries, couriers, payments);
    }
}
