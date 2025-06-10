import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private List<Drink> drinks;
    private Map<String, Integer> drinkQuantities;
    private double totalCost;

    private String branchLocation;
    private String branchId;

    private LocalDateTime orderTimestamp;
    private String formattedTimestamp;

    private OrderStatus status;

    public enum OrderStatus {
        PENDING, PROCESSING, COMPLETED, CANCELLED, INSUFFICIENT_STOCK
    }

    public Order() {
        this.drinks = new ArrayList<>();
        this.drinkQuantities = new HashMap<>();
        this.orderTimestamp = LocalDateTime.now();
        this.formattedTimestamp = orderTimestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = OrderStatus.PENDING;
        this.totalCost = 0.0;
    }

    public Order(String orderId, String customerId, String customerName,
                 String customerEmail, String customerPhone, String branchLocation, String branchId) {
        this();
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.branchLocation = branchLocation;
        this.branchId = branchId;
    }

    // Getters
    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public List<Drink> getDrinks() {
        return new ArrayList<>(drinks);
    }

    public Map<String, Integer> getDrinkQuantities() {
        return new HashMap<>(drinkQuantities);
    }

    public double getTotalCost() {
        return totalCost;
    }

    public String getBranchLocation() {
        return branchLocation;
    }

    public String getBranchId() {
        return branchId;
    }

    public LocalDateTime getOrderTimestamp() {
        return orderTimestamp;
    }

    public String getFormattedTimestamp() {
        return formattedTimestamp;
    }

    public OrderStatus getStatus() {
        return status;
    }

    // Setters
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public void setBranchLocation(String branchLocation) {
        this.branchLocation = branchLocation;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    // Business methods
    public void addDrink(Drink drink, int quantity) {
        // Added null checks and validation
        if (drink == null || drink.getDrinkId() == null || quantity <= 0) {
            return;
        }

        boolean drinkExists = false;
        for (Drink existingDrink : drinks) {
            if (existingDrink.getDrinkId().equals(drink.getDrinkId())) {
                // Fixed potential null pointer exception
                int currentQuantity = drinkQuantities.getOrDefault(drink.getDrinkId(), 0);
                drinkQuantities.put(drink.getDrinkId(), currentQuantity + quantity);
                drinkExists = true;
                break;
            }
        }

        if (!drinkExists) {
            drinks.add(drink);
            drinkQuantities.put(drink.getDrinkId(), quantity);
        }

        calculateTotalCost();
    }

    public void removeDrink(String drinkId) {
        if (drinkId == null) {
            return;
        }

        drinks.removeIf(drink -> drink.getDrinkId() != null && drink.getDrinkId().equals(drinkId));
        drinkQuantities.remove(drinkId);
        calculateTotalCost();
    }

    public void updateDrinkQuantity(String drinkId, int newQuantity) {
        if (drinkId == null) {
            return;
        }

        if (newQuantity <= 0) {
            removeDrink(drinkId);
        } else {
            // Only update if the drink exists in the order
            if (drinkQuantities.containsKey(drinkId)) {
                drinkQuantities.put(drinkId, newQuantity);
                calculateTotalCost();
            }
        }
    }

    /**
     * Calculates the total cost of the order
     * Fixed to handle potential null values and match the Drink class method name
     */
    private void calculateTotalCost() {
        totalCost = 0.0;
        for (Drink drink : drinks) {
            if (drink != null && drink.getDrinkId() != null) {
                int quantity = drinkQuantities.getOrDefault(drink.getDrinkId(), 0);
                // Using the correct method name from your Drink class
                totalCost += drink.Calculatecost(quantity);
            }
        }
    }

    public int getDrinkQuantity(String drinkId) {
        return drinkQuantities.getOrDefault(drinkId, 0);
    }

    public boolean hasItems() {
        return !drinks.isEmpty();
    }

    public int getTotalItemCount() {
        return drinkQuantities.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean canBeProcessed() {
        if (!hasItems()) {
            return false;
        }

        for (Drink drink : drinks) {
            if (drink != null && drink.getDrinkId() != null) {
                int requiredQuantity = drinkQuantities.getOrDefault(drink.getDrinkId(), 0);
                // Using the correct method name from your Drink class
                if (!drink.hasEnoughstock(requiredQuantity)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean processOrder() {
        if (!canBeProcessed()) {
            status = OrderStatus.INSUFFICIENT_STOCK;
            return false;
        }

        status = OrderStatus.PROCESSING;

        try {
            // Reduce stock for each drink
            boolean allReduced = true;
            for (Drink drink : drinks) {
                if (drink != null && drink.getDrinkId() != null) {
                    int quantity = drinkQuantities.getOrDefault(drink.getDrinkId(), 0);
                    if (!drink.reduceStock(quantity)) {
                        allReduced = false;
                        break;
                    }
                }
            }

            if (allReduced) {
                status = OrderStatus.COMPLETED;
                return true;
            } else {
                // If we couldn't reduce stock for some items, mark as insufficient stock
                status = OrderStatus.INSUFFICIENT_STOCK;
                return false;
            }
        } catch (Exception e) {
            // If any error occurs during processing, mark as insufficient stock
            status = OrderStatus.INSUFFICIENT_STOCK;
            return false;
        }
    }

    public void cancelOrder() {
        status = OrderStatus.CANCELLED;
    }

    public String getOrderSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Order ID: ").append(orderId != null ? orderId : "N/A").append("\n");
        summary.append("Customer: ").append(customerName != null ? customerName : "N/A").append("\n");
        summary.append("Branch: ").append(branchLocation != null ? branchLocation : "N/A").append("\n");
        summary.append("Date: ").append(formattedTimestamp != null ? formattedTimestamp : "N/A").append("\n");
        summary.append("Status: ").append(status).append("\n");
        summary.append("Items:\n");

        for (Drink drink : drinks) {
            if (drink != null && drink.getDrinkId() != null) {
                int quantity = drinkQuantities.getOrDefault(drink.getDrinkId(), 0);
                // Using the correct method name from your Drink class
                double itemCost = drink.Calculatecost(quantity);
                summary.append(String.format("  - %s %s (x%d) - KSh %.2f\n",
                        drink.getBrand() != null ? drink.getBrand() : "Unknown Brand",
                        drink.getName() != null ? drink.getName() : "Unknown Name",
                        quantity, itemCost));
            }
        }

        summary.append(String.format("Total Cost: KSh %.2f", totalCost));
        return summary.toString();
    }

    @Override
    public String toString() {
        return String.format("Order{ID='%s', Customer='%s', Branch='%s', Items=%d, Total=%.2f, Status=%s}",
                orderId != null ? orderId : "N/A",
                customerName != null ? customerName : "N/A",
                branchLocation != null ? branchLocation : "N/A",
                getTotalItemCount(), totalCost, status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Order order = (Order) obj;
        return orderId != null ? orderId.equals(order.orderId) : order.orderId == null;
    }

    @Override
    public int hashCode() {
        return orderId != null ? orderId.hashCode() : 0;
    }
}