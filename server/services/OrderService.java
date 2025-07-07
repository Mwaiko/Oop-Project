package server.services;

import java.sql.SQLException;
import common.models.Order;
import server.database.DatabaseManager;
import server.inventory.InventoryManager;

public class OrderService {
    private DatabaseManager dbManager;
    private InventoryManager inventoryManager;

    // FIXED: Constructor now properly initializes the dependencies
    public OrderService() {
        this.dbManager = new DatabaseManager();
        this.inventoryManager = new InventoryManager();
    }

    // Alternative constructor that accepts dependencies (recommended for dependency injection)
    public OrderService(DatabaseManager dbManager, InventoryManager inventoryManager) {
        this.dbManager = dbManager;
        this.inventoryManager = inventoryManager;
    }

    public boolean processOrder(Order order) throws SQLException {
        // Add validation
        if (order == null) {
            System.err.println("Cannot process null order");
            return false;
        }

        if (order.getCustomer() == null) {
            System.err.println("Order must have a customer");
            return false;
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            System.err.println("Order must have at least one item");
            return false;
        }

        System.out.println("Processing order: " + order.getId() + " with " + order.getItems().size() + " items");

        try {
            int id = dbManager.getCustomer(order.getCustomer().getName());
            if (id == -1) {
                order.setCustomer(dbManager.addCustomer(order.getCustomer()));
                System.out.println("Created new customer: " + order.getCustomer().getName());
            }

            boolean inventoryUpdated = inventoryManager.updateInventoryForOrderWithHQFallback(order);
            if (!inventoryUpdated) {
                System.err.println("Failed to update inventory for order: " + order.getId());
                return false;
            }

            System.out.println("Inventory updated successfully for order: " + order.getId());

            // Set order status and save to database
            order.setStatus(Order.STATUS_COMPLETED);
            System.out.println("Updated Customer" + order.getCustomer().getId());
            dbManager.addOrder(order);

            System.out.println("Order saved to database: " + order.getId());

            // Check for low stock after processing
            checkLowStockLevels();

            return true;

        } catch (SQLException e) {
            System.err.println("Database error processing order " + order.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error processing order " + order.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void checkLowStockLevels() throws SQLException {
        try {
            // In a real application, this would send alerts to administrators
            inventoryManager.getDrinksBelowThreshold().forEach(drink ->
                    System.out.println("ALERT: " + drink.getName() + " is below threshold! Current stock: "
                            + drink.getQuantityAvailable() + ", Threshold: " + drink.getMinThreshold())
            );
        } catch (Exception e) {
            System.err.println("Error checking low stock levels: " + e.getMessage());
        }
    }
}