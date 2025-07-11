package common.models;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private Customer customer;
    private Branch branch;
    private List<OrderItem> items;
    private LocalDateTime orderDate;
    public static String OrderStatus;
    
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    
    public Order(Customer customer, Branch branch) {
        this.customer = customer;
        this.branch = branch;
        this.items = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
        this.OrderStatus = STATUS_PENDING;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public String getStatus() { return OrderStatus; }
    public void setStatus(String status) { this.OrderStatus = status; }
    
    // Helper methods
    public void addItem(Drink drink, int quantity) {
        OrderItem item = new OrderItem(drink, quantity);
        items.add(item);
    }
    
    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public static class OrderItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Drink drink;
        private int quantity;
        
        public OrderItem(Drink drink, int quantity) {
            this.drink = drink;
            this.quantity = quantity;
        }
        
        // Getters and setters
        public Drink getDrink() { return drink; }
        public void setDrink(Drink drink) { this.drink = drink; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        public BigDecimal getSubtotal() {
            return drink.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}