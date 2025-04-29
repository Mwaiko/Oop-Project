package common.models;

import java.io.Serializable;
import java.math.BigDecimal;

public class Drink implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String name;
    private String brand;
    private BigDecimal price;
    private int quantityAvailable;
    private int minThreshold;
    
    public Drink(int id, String name, String brand, BigDecimal price, int quantityAvailable, int minThreshold) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.quantityAvailable = quantityAvailable;
        this.minThreshold = minThreshold;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public int getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(int quantityAvailable) { this.quantityAvailable = quantityAvailable; }
    
    public int getMinThreshold() { return minThreshold; }
    public void setMinThreshold(int minThreshold) { this.minThreshold = minThreshold; }
    
    public boolean isBelowThreshold() {
        return quantityAvailable < minThreshold;
    }
    
    @Override
    public String toString() {
        return name + " (" + brand + ") - Ksh " + price;
    }
}