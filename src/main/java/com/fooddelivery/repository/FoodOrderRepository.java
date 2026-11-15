package com.fooddelivery.repository;

import com.fooddelivery.model.order.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {

    Optional<FoodOrder> findByOrderNumber(String orderNumber);

    List<FoodOrder> findByOrderStatus(String orderStatus);

    @Query("SELECT DISTINCT o FROM FoodOrder o " +
           "JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.foodItem " +
           "WHERE UPPER(o.orderStatus) = UPPER(:orderStatus) " +
           "ORDER BY o.orderDateTime DESC")
    List<FoodOrder> findByOrderStatusWithDetails(@Param("orderStatus") String orderStatus);

    @Query("SELECT DISTINCT o FROM FoodOrder o " +
           "JOIN FETCH o.customer c " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.foodItem " +
           "WHERE LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY o.orderDateTime DESC")
    List<FoodOrder> searchOrdersWithDetails(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT o FROM FoodOrder o " +
           "JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.foodItem " +
           "WHERE o.orderDateTime BETWEEN :startDate AND :endDate " +
           "ORDER BY o.orderDateTime DESC")
    List<FoodOrder> findOrdersByDateRangeWithDetails(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT o FROM FoodOrder o " +
           "JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.foodItem " +
           "WHERE o.orderStatus NOT IN ('DELIVERED', 'CANCELLED') " +
           "ORDER BY o.orderDateTime ASC")
    List<FoodOrder> findActiveOrdersWithDetails();

    @Query("SELECT COUNT(o) FROM FoodOrder o WHERE o.orderStatus = :orderStatus")
    long countByOrderStatus(@Param("orderStatus") String orderStatus);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.00) FROM FoodOrder o WHERE o.orderStatus = 'DELIVERED'")
    BigDecimal calculateDeliveredRevenue();

    @Query("SELECT COALESCE(SUM(o.discountAmount), 0.00) FROM FoodOrder o")
    BigDecimal calculateTotalDiscountsGiven();

    @Query("SELECT DISTINCT o FROM FoodOrder o " +
           "JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.foodItem " +
           "ORDER BY o.orderDateTime DESC")
    List<FoodOrder> findAllWithDetails();

    @Query("SELECT o FROM FoodOrder o " +
           "JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.foodItem " +
           "WHERE o.id = :id")
    Optional<FoodOrder> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT o FROM FoodOrder o " +
           "JOIN FETCH o.customer " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.foodItem " +
           "WHERE o.customer.id = :customerId " +
           "ORDER BY o.orderDateTime DESC")
    List<FoodOrder> findByCustomerIdWithDetails(@Param("customerId") Long customerId);
}
