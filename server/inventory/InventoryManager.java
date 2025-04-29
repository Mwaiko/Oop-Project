package server.inventory;

import common.models.Drink;
import common.models.Order;
import server.database.DatabaseManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryManager {
    private DatabaseManager dbManager;
    
    public InventoryManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    public void initializeSampleDrinks() {
        if (dbManager.getAllDrinks().isEmpty()) {
            // Sodas
            dbManager.addDrink(new Drink(0, "Coca Cola", "Coca Cola", new BigDecimal("80"), 200, 50));
            dbManager.addDrink(new Drink(0, "Sprite", "Coca Cola", new BigDecimal("80"), 150, 40));
            dbManager.addDrink(new Drink(0, "Fanta Orange", "Coca Cola", new BigDecimal("80"), 120, 30));
            dbManager.addDrink(new Drink(0, "Fanta Pineapple", "Coca Cola", new BigDecimal("80"), 100, 30));
            
            // Water
            dbManager.addDrink(new Drink(0, "Still Water 500ml", "Aquamist", new BigDecimal("50"), 300, 100));
            dbManager.addDrink(new Drink(0, "Sparkling Water 500ml", "Aquamist", new BigDecimal("75"), 150, 50));
            
            // Juice
            dbManager.addDrink(new Drink(0, "Orange Juice", "Freshly", new BigDecimal("120"), 80, 20));
            dbManager.addDrink(new Drink(0, "Mango Juice", "Freshly", new BigDecimal("120"), 70, 20));
            dbManager.addDrink(new Drink(0, "Mixed Berry Juice", "Freshly", new BigDecimal("130"), 60, 15));
            
            // Energy Drinks
            dbManager.addDrink(new Drink(0, "Power Energy", "PowerBoost", new BigDecimal("150"), 100, 25));
            dbManager.addDrink(new Drink(0, "Red Bull", "Red Bull", new BigDecimal("200"), 80, 20));
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