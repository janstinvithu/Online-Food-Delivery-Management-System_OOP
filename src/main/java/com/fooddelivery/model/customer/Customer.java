package com.fooddelivery.model.customer;

import com.fooddelivery.model.order.FoodOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Entity
@Table(name = "customers")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "customer_type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("GUEST")
public class Customer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Customer full name is required")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone", nullable = false, length = 25)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<FoodOrder> orders = new ArrayList<>();

    public Customer() {
        this.createdAt = LocalDateTime.now();
    }

    public Customer(String fullName, String email, String phone, String address) {
        this();
        setFullName(fullName);
        setEmail(email);
        setPhone(phone);
        setAddress(address);
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public final void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or blank");
        }
        this.fullName = fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public final void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPhone() {
        return phone;
    }

    public final void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be blank");
        }
        this.phone = phone.trim();
    }

    public String getAddress() {
        return address;
    }

    public final void setAddress(String address) {
        this.address = (address != null) ? address.trim() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<FoodOrder> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public void addOrder(FoodOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (!orders.contains(order)) {
            orders.add(order);
            order.setCustomer(this);
        }
    }

    public void removeOrder(FoodOrder order) {
        if (order != null && orders.remove(order)) {
            order.setCustomer(null);
        }
    }

    @Transient
    public boolean isRegisteredCustomer() {
        return false;
    }

    @Transient
    public double getDiscountRate() {
        return 0.00;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return id != null && Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
