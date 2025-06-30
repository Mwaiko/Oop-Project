package server.inventory;

import common.models.Drink;
import common.models.Order;
import server.database.DatabaseManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryManager {
    private DatabaseManager dbManager;
    
    public InventoryManager() {
        this.dbManager = dbManager;
    }
    
    public void initializeSampleDrinks() {
        if (dbManager.getAllDrinks().isEmpty()) {
            // Coca Cola (your original example)
            // Sodas
            dbManager.addDrink("Coca Cola", "Coca Cola", 50, new BigDecimal("50"), 50);
            dbManager.addDrink("Sprite", "Coca Cola", 150, new BigDecimal("80"), 40);
            dbManager.addDrink("Fanta Orange", "Coca Cola", 120, new BigDecimal("80"), 30);
            dbManager.addDrink("Fanta Pineapple", "Coca Cola", 100, new BigDecimal("80"), 30);

            // Water
            dbManager.addDrink("Still Water 500ml", "Aquamist", 300, new BigDecimal("50"), 100);
            dbManager.addDrink("Sparkling Water 500ml", "Aquamist", 150, new BigDecimal("75"), 50);

            // Juice
            dbManager.addDrink("Orange Juice", "Freshly", 80, new BigDecimal("120"), 20);
            dbManager.addDrink("Mango Juice", "Freshly", 70, new BigDecimal("120"), 20);
            dbManager.addDrink("Mixed Berry Juice", "Freshly", 60, new BigDecimal("130"), 15);

            // Energy Drinks
            dbManager.addDrink("Power Energy", "PowerBoost", 100, new BigDecimal("150"), 25);
            dbManager.addDrink("Red Bull", "Red Bull", 80, new BigDecimal("200"), 20);
        }
    }
    
    public boolean updateInventoryForOrder(Order order) {
        // Check if we have enough stock
        for (Order.OrderItem item : order.getItems()) {
            Drink drink = dbManager.getDrink(item.getDrink().getId());
            if (drink.getQuantityAvailable() < item.getQuantity()) {
                return false; // Not enough stock
            }
        }
        
        // Update stock levels
        for (Order.OrderItem item : order.getItems()) {
            Drink drink = dbManager.getDrink(item.getDrink().getId());
            drink.setQuantityAvailable(drink.getQuantityAvailable() - item.getQuantity());
            dbManager.updateDrink(drink);
        }
        
        return true;
    }
    
    public List<Drink> getDrinksBelowThreshold() {
        return dbManager.getAllDrinks().stream()
                .filter(Drink::isBelowThreshold)
                .collect(Collectors.toList());
    }
    
    public void restockDrink(int drinkId, int quantity) {
        Drink drink = dbManager.getDrink(drinkId);
        drink.setQuantityAvailable(drink.getQuantityAvailable() + quantity);
        dbManager.updateDrink(drink);
    }
    
    public List<Drink> getAllDrinks() {
        return dbManager.getAllDrinks();
    }
    
    public Drink getDrink(int id) {
        return dbManager.getDrink(id);
    }
}