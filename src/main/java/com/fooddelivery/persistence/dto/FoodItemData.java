package com.fooddelivery.persistence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodItemData {

    private Long id;
    private String type;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean available;
    private int preparationTimeMinutes;
    private String cuisineType;
    private Boolean vegetarian;
    private String spiceLevel;
    private String servingPortion;
    private String volumeSize;
    private Integer volumeMl;
    private Boolean cold;
    private Integer caffeineContentMg;
    private String sugarLevel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public int getPreparationTimeMinutes() { return preparationTimeMinutes; }
    public void setPreparationTimeMinutes(int preparationTimeMinutes) { this.preparationTimeMinutes = preparationTimeMinutes; }
    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }
    public Boolean getVegetarian() { return vegetarian; }
    public void setVegetarian(Boolean vegetarian) { this.vegetarian = vegetarian; }
    public String getSpiceLevel() { return spiceLevel; }
    public void setSpiceLevel(String spiceLevel) { this.spiceLevel = spiceLevel; }
    public String getServingPortion() { return servingPortion; }
    public void setServingPortion(String servingPortion) { this.servingPortion = servingPortion; }
    public String getVolumeSize() { return volumeSize; }
    public void setVolumeSize(String volumeSize) { this.volumeSize = volumeSize; }
    public Integer getVolumeMl() { return volumeMl; }
    public void setVolumeMl(Integer volumeMl) { this.volumeMl = volumeMl; }
    public Boolean getCold() { return cold; }
    public void setCold(Boolean cold) { this.cold = cold; }
    public Integer getCaffeineContentMg() { return caffeineContentMg; }
    public void setCaffeineContentMg(Integer caffeineContentMg) { this.caffeineContentMg = caffeineContentMg; }
    public String getSugarLevel() { return sugarLevel; }
    public void setSugarLevel(String sugarLevel) { this.sugarLevel = sugarLevel; }
}
