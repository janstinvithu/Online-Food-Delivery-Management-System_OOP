package com.fooddelivery.model.food;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class Beverage extends FoodItem {

    @NotBlank(message = "Volume size is required")
    private String volumeSize; // e.g. Small (250ml), Regular (350ml), Large (500ml)

    @Min(value = 1, message = "Volume ml must be positive")
    private int volumeMl;

    private boolean cold;

    @Min(value = 0, message = "Caffeine content cannot be negative")
    private int caffeineContentMg;

    @NotBlank(message = "Sugar level is required")
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

    public void setVolumeSize(String volumeSize) {
        if (volumeSize == null || volumeSize.trim().isEmpty()) {
            throw new IllegalArgumentException("Volume size cannot be blank");
        }
        this.volumeSize = volumeSize.trim();
    }

    public int getVolumeMl() {
        return volumeMl;
    }

    public void setVolumeMl(int volumeMl) {
        if (volumeMl <= 0) {
            throw new IllegalArgumentException("Volume must be strictly positive");
        }
        this.volumeMl = volumeMl;
    }

    public boolean isCold() {
        return cold;
    }

    public void setCold(boolean cold) {
        this.cold = cold;
    }

    public int getCaffeineContentMg() {
        return caffeineContentMg;
    }

    public void setCaffeineContentMg(int caffeineContentMg) {
        if (caffeineContentMg < 0) {
            throw new IllegalArgumentException("Caffeine content cannot be negative");
        }
        this.caffeineContentMg = caffeineContentMg;
    }

    public String getSugarLevel() {
        return sugarLevel;
    }

    public void setSugarLevel(String sugarLevel) {
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
    public String getFoodCategory() {
        return (cold ? "Chilled" : "Hot") + " Beverage (" + volumeSize + ")";
    }
}
