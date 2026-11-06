package com.fooddelivery.model.food;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Entity
@Table(name = "beverages")
@DiscriminatorValue("BEVERAGE")
@PrimaryKeyJoinColumn(name = "id")
public class Beverage extends FoodItem {

    @NotBlank(message = "Volume size is required")
    @Column(name = "volume_size", nullable = false, length = 30)
    private String volumeSize; // e.g. Small (250ml), Regular (350ml), Large (500ml)

    @Min(value = 1, message = "Volume ml must be positive")
    @Column(name = "volume_ml", nullable = false)
    private int volumeMl;

    @Column(name = "is_cold", nullable = false)
    private boolean cold;

    @Min(value = 0, message = "Caffeine content cannot be negative")
    @Column(name = "caffeine_content_mg", nullable = false)
    private int caffeineContentMg;

    @NotBlank(message = "Sugar level is required")
    @Column(name = "sugar_level", nullable = false, length = 20)
    private String sugarLevel; // ZERO, LOW, MEDIUM, HIGH

    public Beverage() {
        super();
        this.volumeSize = "Regular (350ml)";
        this.volumeMl = 350;
        this.cold = true;
        this.caffeineContentMg = 0;
        this.sugarLevel = "ZERO";
    }

    public Beverage(String name, String description, BigDecimal price, int preparationTimeMinutes,
                    String volumeSize, int volumeMl, boolean cold, int caffeineContentMg, String sugarLevel) {
        super(name, description, price, preparationTimeMinutes);
        setVolumeSize(volumeSize);
        setVolumeMl(volumeMl);
        setCold(cold);
        setCaffeineContentMg(caffeineContentMg);
        setSugarLevel(sugarLevel);
    }

    public String getVolumeSize() {
        return volumeSize;
    }

    public final void setVolumeSize(String volumeSize) {
        if (volumeSize == null || volumeSize.trim().isEmpty()) {
            throw new IllegalArgumentException("Volume size cannot be blank");
        }
        this.volumeSize = volumeSize.trim();
    }

    public int getVolumeMl() {
        return volumeMl;
    }

    public final void setVolumeMl(int volumeMl) {
        if (volumeMl <= 0) {
            throw new IllegalArgumentException("Volume must be strictly positive");
        }
        this.volumeMl = volumeMl;
    }

    public boolean isCold() {
        return cold;
    }

    public final void setCold(boolean cold) {
        this.cold = cold;
    }

    public int getCaffeineContentMg() {
        return caffeineContentMg;
    }

    public final void setCaffeineContentMg(int caffeineContentMg) {
        if (caffeineContentMg < 0) {
            throw new IllegalArgumentException("Caffeine content cannot be negative");
        }
        this.caffeineContentMg = caffeineContentMg;
    }

    public String getSugarLevel() {
        return sugarLevel;
    }

    public final void setSugarLevel(String sugarLevel) {
        if (sugarLevel == null || sugarLevel.trim().isEmpty()) {
            throw new IllegalArgumentException("Sugar level cannot be blank");
        }
        String upper = sugarLevel.trim().toUpperCase();
        if (!upper.matches("ZERO|LOW|MEDIUM|HIGH")) {
            throw new IllegalArgumentException("Sugar level must be ZERO, LOW, MEDIUM, or HIGH");
        }
        this.sugarLevel = upper;
    }

    @Override
    @Transient
    public String getFoodCategory() {
        return (cold ? "Chilled" : "Hot") + " Beverage (" + volumeSize + ")";
    }
}
