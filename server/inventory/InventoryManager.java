package server.inventory;

import common.models.Branch;
import common.models.Drink;
import common.models.Order;
import server.database.DatabaseManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InventoryManager {
    private DatabaseManager dbManager;
    private StockManager stockManager;

    public InventoryManager() {
        dbManager = new DatabaseManager();
        stockManager = new StockManager();
    }

    public void initializeSampleDrinks() throws SQLException {
        try {
            List<Drink> alldrinks = dbManager.getAllDrinks();
            if (alldrinks.isEmpty()) {
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
            }else {
                System.out.println("Initializing Drinks in Prices");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
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
     * This method will transfer stock from HQ to the branch if the branch doesn't have enough stock.
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

        if (!canFulfillOrder(order, branchId, hqBranchId)) {
            return true;
        }

        // Process each item in the order
        for (Order.OrderItem item : order.getItems()) {
            int drinkId = item.getDrink().getId();
            int requiredQuantity = item.getQuantity();
            int branchStock = getStockLevel(branchId, drinkId);

            if (branchStock >= requiredQuantity) {
                // Branch has enough stock
                stockManager.reduceStock(branchId, drinkId, requiredQuantity);
                System.out.println("Fulfilled " + requiredQuantity + " units of " + item.getDrink().getName() + " from branch " + branchId);
            } else {
                // Need to get stock from HQ
                int shortfall = requiredQuantity - branchStock;

                // Use all available stock from branch first
                if (branchStock > 0) {
                    stockManager.reduceStock(branchId, drinkId, branchStock);
                    System.out.println("Used " + branchStock + " units of " + item.getDrink().getName() + " from branch " + branchId);
                }

                // Transfer needed stock from HQ to branch
                transferStockFromHQToBranch(hqBranchId, branchId, drinkId, shortfall);

                // Now deduct the transferred stock from branch
                stockManager.reduceStock(branchId, drinkId, shortfall);
                System.out.println("Transferred and used " + shortfall + " units of " + item.getDrink().getName() + " from HQ to branch " + branchId);
            }
        }

        return true;
    }

    /**
     * Check if the order can be fulfilled using branch and HQ stock combined
     */
    private boolean canFulfillOrder(Order order, int branchId, int hqBranchId) throws SQLException {
        for (Order.OrderItem item : order.getItems()) {
            int drinkId = item.getDrink().getId();
            int requiredQuantity = item.getQuantity();
            int branchStock = getStockLevel(branchId, drinkId);
            int hqStock = getStockLevel(hqBranchId, drinkId);
            int totalAvailable = branchStock + hqStock;

            if (totalAvailable < requiredQuantity) {
                System.err.println("Not enough total stock for " + item.getDrink().getName() +
                        ". Required: " + requiredQuantity +
                        ", Available at branch: " + branchStock +
                        ", Available at HQ: " + hqStock +
                        ", Total available: " + totalAvailable);
                return false;
            }
        }
        return true;
    }

    /**
     * Transfer stock from HQ to a branch
     */
    private void transferStockFromHQToBranch(int hqBranchId, int branchId, int drinkId, int quantity) throws SQLException {
        // Remove from HQ
        stockManager.reduceStock(hqBranchId, drinkId, quantity);

        // Add to branch (you may need to implement addStock method in StockManager)
        // For now, we'll assume the stock is transferred and will be deducted immediately
        stockManager.addStock(branchId, drinkId, quantity, getMinThresholdForDrink(drinkId));

        System.out.println("Transferred " + quantity + " units of drink ID " + drinkId + " from HQ (branch " + hqBranchId + ") to branch " + branchId);
    }

    /**
     * Get minimum threshold for a drink (helper method)
     */
    private int getMinThresholdForDrink(int drinkId) throws SQLException {
        Drink drink = dbManager.getDrink(drinkId);
        return drink != null ? drink.getMinThreshold() : 5; // Default threshold
    }

    /**
     * Get stock level for a drink at a specific branch
     */
    private int getStockLevel(int branchId, int drinkId) throws SQLException {
        try {
            System.out.println("Getting Stock Level for Branch " + branchId + " and Drink " + drinkId);
            java.util.Map<String, Object> stock = stockManager.getStockByBranch(branchId).stream()
                    .filter(s -> ((Integer)s.get("drink_id")) == drinkId)
                    .findFirst().orElse(null);
            if (stock != null) {
                return (Integer) stock.get("quantity");
            }
        } catch (Exception e) {
            System.err.println("Error getting stock level for branch " + branchId + ", drink " + drinkId + ": " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get the headquarters branch ID
     */
    private int getHeadquartersBranchId() throws SQLException {
        return dbManager.getBranchId(common.models.Branch.HEADQUARTERS);
    }

    /**
     * Alternative method name for better clarity - this is the main method to use
     */
    public boolean processOrderWithStockTransfer(Order order) throws SQLException {
        return updateInventoryForOrderWithHQFallback(order);
    }
    public static void main(String [] args){
        try{
            new InventoryManager().initializeSampleDrinks();
        }catch (SQLException e){
            e.printStackTrace();
        }

    }
}
