package client.service;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import common.models.*;

interface HeadquartersService {
    void placeOrder(Order order, String requestingBranchId) throws HQOrderException;
}

interface InventoryService {
    boolean hasSufficientStock(List<Order.OrderItem> items, String branchId);
    void reduceStock(List<Order.OrderItem> items, String branchId) throws InventoryException;
}

class HQOrderException extends Exception {
    public HQOrderException(String message) {
        super(message);
    }
    
    public HQOrderException(String message, Throwable cause) {
        super(message, cause);
    }
}

class InventoryException extends Exception {
    public InventoryException(String message) {
        super(message);
    }
    
    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class OrderService {
    private static final Logger logger = Logger.getLogger(OrderService.class.getName());
    
    private final String branchId;
    private final HeadquartersService hqService;
    private final InventoryService inventoryService;

    public OrderService(String branchId, HeadquartersService hqService, InventoryService inventoryService) {
        if (branchId == null || branchId.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch ID cannot be null or empty");
        }
        if (hqService == null) {
            throw new IllegalArgumentException("HeadquartersService cannot be null");
        }
        if (inventoryService == null) {
            throw new IllegalArgumentException("InventoryService cannot be null");
        }
        
        this.branchId = branchId.trim();
        this.hqService = hqService;
        this.inventoryService = inventoryService;
    }

    /**
     * Places an order - tries local branch first, falls back to HQ if needed
     * @param order The order to place
     * @return true if order was successful (either locally or at HQ)
     * @throws IllegalArgumentException if order is null or invalid
     */
    public boolean placeOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        if (order.getItems() == null || order.getItems().isEmpty()) {
            logger.warning("Order contains no items");
            return false;
        }
        
        // Try local branch first
        try {
            if (inventoryService.hasSufficientStock(order.getItems(), branchId)) {
                inventoryService.reduceStock(order.getItems(), branchId);
                logger.info("Order placed successfully at local branch: " + branchId);
                return true;
            }
        } catch (InventoryException e) {
            logger.log(Level.WARNING, "Failed to reduce stock at local branch, falling back to HQ", e);
        }
        
        // Fall back to HQ
        try {
            hqService.placeOrder(order, branchId);
            logger.info("Order placed successfully at HQ for branch: " + branchId);
            return true;
        } catch (HQOrderException e) {
            logger.log(Level.SEVERE, "Failed to place HQ order for branch: " + branchId, e);
            return false;
        }
    }

    /**
     * Emergency order - places directly with HQ without checking local stock
     * @param order The emergency order to place
     * @return true if order was successful
     * @throws IllegalArgumentException if order is null or invalid
     */
    public boolean placeEmergencyOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        if (order.getItems() == null || order.getItems().isEmpty()) {
            logger.warning("Emergency order contains no items");
            return false;
        }
        
        try {
            hqService.placeOrder(order, branchId);
            logger.info("Emergency order placed successfully at HQ for branch: " + branchId);
            return true;
        } catch (HQOrderException e) {
            logger.log(Level.SEVERE, "Failed to place emergency HQ order for branch: " + branchId, e);
            return false;
        }
    }
    
    /**
     * Gets the branch ID for this service
     * @return the branch ID
     */
    public String getBranchId() {
        return branchId;
    }
}