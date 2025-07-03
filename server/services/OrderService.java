package server.services;

import java.sql.SQLException;

import common.models.Order;
import server.database.DatabaseManager;
import server.inventory.InventoryManager;

public class OrderService {
    private DatabaseManager dbManager;
    private InventoryManager inventoryManager;
    
    public OrderService() {
        this.dbManager = dbManager;
        this.inventoryManager = inventoryManager;
    }
    
    public boolean processOrder(Order order) throws SQLException {
        // Verify customer exists or create new one
        if (order.getCustomer().getId() == 0) {
            order.setCustomer(dbManager.addCustomer(order.getCustomer()));
        }
        
        // Update inventory with HQ fallback
        boolean inventoryUpdated = inventoryManager.updateInventoryForOrderWithHQFallback(order);
        if (!inventoryUpdated) {
            return false;
        }
        
        order.setStatus(Order.STATUS_COMPLETED);
        dbManager.addOrder(order);
        
        // Check for low stock after processing
        checkLowStockLevels();
        
        return true;
    }
    
    private void checkLowStockLevels() throws SQLException {
        // In a real application, this would send alerts to administrators
        inventoryManager.getDrinksBelowThreshold().forEach(drink -> 
            System.out.println("ALERT: " + drink.getName() + " is below threshold! Current stock: " 
                + drink.getQuantityAvailable() + ", Threshold: " + drink.getMinThreshold())
        );
    }
}