package server.inventory;

import common.models.Drink;
import common.models.Order;
import server.database.DatabaseManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryManager {
    private DatabaseManager dbManager;
    
    public InventoryManager() {
        dbManager = new DatabaseManager();
    }
    
    public void initializeSampleDrinks() throws SQLException {
        if (dbManager.getAllDrinks().isEmpty()) {
            // Sodas
            dbManager.addDrink("Coca Cola", "Coca Cola", 50, new BigDecimal("50"), 10);
            dbManager.addDrink("Sprite", "Coca Cola", 150, new BigDecimal("80"), 15);
            dbManager.addDrink("Fanta Orange", "Coca Cola", 120, new BigDecimal("80"), 15);
            dbManager.addDrink("Fanta Pineapple", "Coca Cola", 100, new BigDecimal("80"), 15);

            // Water
            dbManager.addDrink("Still Water 500ml", "Aquamist", 300, new BigDecimal("50"), 50);
            dbManager.addDrink("Sparkling Water 500ml", "Aquamist", 150, new BigDecimal("75"), 25);

            // Juice
            dbManager.addDrink("Orange Juice", "Freshly", 80, new BigDecimal("120"), 10);
            dbManager.addDrink("Mango Juice", "Freshly", 70, new BigDecimal("120"), 10);
            dbManager.addDrink("Mixed Berry Juice", "Freshly", 60, new BigDecimal("130"), 10);

            // Energy Drinks
            dbManager.addDrink("Power Energy", "PowerBoost", 100, new BigDecimal("150"), 20);
            dbManager.addDrink("Red Bull", "Red Bull", 80, new BigDecimal("200"), 15);
        }
    }
    
    public boolean updateInventoryForOrder(Order order) throws SQLException {
        // Check if we have enough stock
        for (Order.OrderItem item : order.getItems()) {
            Drink drink = dbManager.getDrink(item.getDrink().getId());
            if (drink == null) {
                System.err.println("Drink not found with ID: " + item.getDrink().getId());
                return false;
            }
            if (drink.getQuantityAvailable() < item.getQuantity()) {
                System.err.println("Not enough stock for " + drink.getName() + 
                    ". Available: " + drink.getQuantityAvailable() + 
                    ", Required: " + item.getQuantity());
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
    
    public List<Drink> getDrinksBelowThreshold() throws SQLException {
        return dbManager.getAllDrinks().stream()
                .filter(Drink::isBelowThreshold)
                .collect(Collectors.toList());
    }
    
    public void restockDrink(int drinkId, int quantity) throws SQLException {
        Drink drink = dbManager.getDrink(drinkId);
        if (drink != null) {
            drink.setQuantityAvailable(drink.getQuantityAvailable() + quantity);
            dbManager.updateDrink(drink);
        } else {
            throw new SQLException("Drink not found with ID: " + drinkId);
        }
    }
    
    public List<Drink> getAllDrinks() throws SQLException{
            return dbManager.getAllDrinks();
        
        
    }
    
    public Drink getDrink(int id) throws SQLException {
        return dbManager.getDrink(id);
    }
    
    public Drink addDrink(String name, String brand, int quantity, BigDecimal price, int minThreshold) throws SQLException {
        return dbManager.addDrink(name, brand, quantity, price, minThreshold);
    }
    
    public void updateDrink(Drink drink) throws SQLException {
        dbManager.updateDrink(drink);
    }
    
    public void deleteDrink(int id) throws SQLException {
        dbManager.deleteDrink(id);
    }

    /**
     * Update inventory for an order, first trying the branch, then falling back to headquarters if needed.
     * @param order The order to process
     * @return true if the order can be fulfilled, false otherwise
     */
    public boolean updateInventoryForOrderWithHQFallback(Order order) throws SQLException {
        if (order.getBranch() == null) {
            System.err.println("Order does not have a branch specified.");
            return false;
        }
        int branchId = order.getBranch().getId();
        int hqBranchId = getHeadquartersBranchId();
        StockManager stockManager = new StockManager();
        // First, check if branch has enough for all items
        boolean branchHasAll = true;
        for (Order.OrderItem item : order.getItems()) {
            if (!stockManager.isStockAvailable(branchId, item.getDrink().getId(), item.getQuantity())) {
                branchHasAll = false;
                break;
            }
        }
        if (branchHasAll) {
            // Deduct from branch stock
            for (Order.OrderItem item : order.getItems()) {
                stockManager.reduceStock(branchId, item.getDrink().getId(), item.getQuantity());
            }
            return true;
        }
        // If not, check if HQ can fulfill the missing items
        for (Order.OrderItem item : order.getItems()) {
            int branchAvailable = getStockLevel(branchId, item.getDrink().getId());
            int needed = item.getQuantity();
            int missing = needed - branchAvailable;
            if (missing > 0) {
                // Check HQ
                if (!stockManager.isStockAvailable(hqBranchId, item.getDrink().getId(), missing)) {
                    System.err.println("Not enough stock for " + item.getDrink().getName() + " at branch or HQ.");
                    return false;
                }
            }
        }
        // Deduct what is available from branch, rest from HQ
        for (Order.OrderItem item : order.getItems()) {
            int branchAvailable = getStockLevel(branchId, item.getDrink().getId());
            int needed = item.getQuantity();
            int missing = needed - branchAvailable;
            if (branchAvailable > 0) {
                stockManager.reduceStock(branchId, item.getDrink().getId(), Math.min(branchAvailable, needed));
            }
            if (missing > 0) {
                stockManager.reduceStock(hqBranchId, item.getDrink().getId(), missing);
                // Optionally, transfer the missing stock to the branch (simulate delivery)
                // stockManager.addStock(branchId, item.getDrink().getId(), missing, 5); // 5 as default minThreshold
            }
        }
        return true;
    }

    // Helper to get stock level for a drink at a branch
    private int getStockLevel(int branchId, int drinkId) throws SQLException {
        StockManager stockManager = new StockManager();
        try {
            java.util.Map<String, Object> stock = stockManager.getStockByBranch(branchId).stream()
                .filter(s -> ((Integer)s.get("drink_id")) == drinkId)
                .findFirst().orElse(null);
            if (stock != null) {
                return (Integer) stock.get("quantity");
            }
        } catch (Exception e) {
            System.err.println("Error getting stock level: " + e.getMessage());
        }
        return 0;
    }

    // Helper to get HQ branch id
    private int getHeadquartersBranchId() throws SQLException {
        return dbManager.getBranchId(common.models.Branch.HEADQUARTERS);
    }
}