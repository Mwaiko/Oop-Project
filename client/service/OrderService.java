package client.service;
import java.util.List;
interface HeadquartersService {
    void placeOrder(Order order, String requestingBranchId) throws HQOrderException;
}

interface InventoryService {
    boolean hasSufficientStock(List<OrderItem> items, String branchId);
    void reduceStock(List<OrderItem> items, String branchId);
}

class HQOrderException extends Exception {
    public HQOrderException(String message) {
        super(message);
    }
}
public class OrderService {
    private final String branchId;
    private final HeadquartersService hqService;
    private final InventoryService inventoryService;

    public OrderService(String branchId, HeadquartersService hqService, InventoryService inventoryService) {
        this.branchId = branchId;
        this.hqService = hqService;
        this.inventoryService = inventoryService;
    }

    /**
     * Places an order - tries local branch first, falls back to HQ if needed
     * @return true if order was successful (either locally or at HQ)
     */
    public boolean placeOrder(Order order) {
        // Try local branch first
        if (inventoryService.hasSufficientStock(order.getItems(), branchId)) {
            inventoryService.reduceStock(order.getItems(), branchId);
            return true;
        }
        
        // Fall back to HQ
        try {
            hqService.placeOrder(order, branchId);
            return true;
        } catch (HQOrderException e) {
            System.err.println("Failed to place HQ order: " + e.getMessage());
            return false;
        }
    }

    /**
     * Emergency order - places directly with HQ without checking local stock
     */
    public boolean placeEmergencyOrder(Order order) {
        try {
            hqService.placeOrder(order, branchId);
            return true;
        } catch (HQOrderException e) {
            System.err.println("Failed to place emergency HQ order: " + e.getMessage());
            return false;
        }
    }
}