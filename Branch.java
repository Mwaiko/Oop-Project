import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Branch implements Serializable {
    private static final long serialVersionUID = 1L;

    private String branchId;
    private String branchName;
    private String location;
    private String address;
    private String phone;
    private String email;
    private String managerId;
    private String managerName;

    private Map<String, Drink> inventory;
    private List<Order> orders;
    private List<Customer> customers;

    private double totalSales;
    private int totalOrdersProcessed;
    private LocalDateTime lastUpdated;
    private String formattedLastUpdated;

    private BranchStatus status;
    private int stockThreshold;

    public enum BranchStatus {
        OPEN, CLOSED, MAINTENANCE
    }

    public Branch() {
        this.inventory = new HashMap<>();
        this.orders = new ArrayList<>();
        this.customers = new ArrayList<>();
        this.totalSales = 0.0;
        this.totalOrdersProcessed = 0;
        this.lastUpdated = LocalDateTime.now();
        this.formattedLastUpdated = lastUpdated.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = BranchStatus.OPEN;
        this.stockThreshold = 10; // Default minimum stock threshold
    }

    public Branch(String branchId, String branchName, String location, String address,
                  String phone, String email, String managerId, String managerName) {
        this();
        this.branchId = branchId;
        this.branchName = branchName;
        this.location = location;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.managerId = managerId;
        this.managerName = managerName;
    }

    // Getters
    public String getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getLocation() {
        return location;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getManagerId() {
        return managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public Map<String, Drink> getInventory() {
        return new HashMap<>(inventory);
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public List<Customer> getCustomers() {
        return new ArrayList<>(customers);
    }

    public double getTotalSales() {
        return totalSales;
    }

    public int getTotalOrdersProcessed() {
        return totalOrdersProcessed;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public String getFormattedLastUpdated() {
        return formattedLastUpdated;
    }

    public BranchStatus getStatus() {
        return status;
    }

    public int getStockThreshold() {
        return stockThreshold;
    }

    // Setters
    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public void setStatus(BranchStatus status) {
        this.status = status;
        updateLastModified();
    }

    public void setStockThreshold(int stockThreshold) {
        this.stockThreshold = Math.max(0, stockThreshold);
    }

    // Inventory Management Methods
    public void addDrink(Drink drink) {
        if (drink != null && drink.getDrinkId() != null) {
            inventory.put(drink.getDrinkId(), drink);
            updateLastModified();
        }
    }

    public void removeDrink(String drinkId) {
        if (drinkId != null) {
            inventory.remove(drinkId);
            updateLastModified();
        }
    }

    public Drink getDrink(String drinkId) {
        return inventory.get(drinkId);
    }

    public boolean hasDrink(String drinkId) {
        return inventory.containsKey(drinkId);
    }

    public void updateDrinkStock(String drinkId, int newStock) {
        Drink drink = inventory.get(drinkId);
        if (drink != null) {
            drink.setStockQuantity(newStock);
            updateLastModified();
        }
    }

    public void addDrinkStock(String drinkId, int quantity) {
        Drink drink = inventory.get(drinkId);
        if (drink != null && quantity > 0) {
            drink.addStock(quantity);
            updateLastModified();
        }
    }

    // Order Management Methods
    public void addOrder(Order order) {
        if (order != null) {
            orders.add(order);
            if (order.getStatus() == Order.OrderStatus.COMPLETED) {
                totalSales += order.getTotalCost();
                totalOrdersProcessed++;
            }
            updateLastModified();
        }
    }

    public boolean processOrder(Order order) {
        if (order == null || !order.canBeProcessed()) {
            return false;
        }

        boolean success = order.processOrder();
        if (success) {
            totalSales += order.getTotalCost();
            totalOrdersProcessed++;
            updateLastModified();
        }
        return success;
    }

    // Customer Management Methods
    public void addCustomer(Customer customer) {
        if (customer != null && !customers.contains(customer)) {
            customers.add(customer);
            updateLastModified();
        }
    }

    public Customer getCustomer(String customerId) {
        for (Customer customer : customers) {
            if (customer.getCustomerId() != null && customer.getCustomerId().equals(customerId)) {
                return customer;
            }
        }
        return null;
    }

    // Stock Management and Alerts
    public List<Drink> getDrinksBelowThreshold() {
        List<Drink> lowStockDrinks = new ArrayList<>();
        for (Drink drink : inventory.values()) {
            if (drink.isStockBelowThreshold(stockThreshold)) {
                lowStockDrinks.add(drink);
            }
        }
        return lowStockDrinks;
    }

    public boolean hasLowStockItems() {
        return !getDrinksBelowThreshold().isEmpty();
    }

    public String getLowStockAlert() {
        List<Drink> lowStockDrinks = getDrinksBelowThreshold();
        if (lowStockDrinks.isEmpty()) {
            return "No low stock items.";
        }

        StringBuilder alert = new StringBuilder();
        alert.append("LOW STOCK ALERT for ").append(branchName).append(" (").append(location).append("):\n");
        for (Drink drink : lowStockDrinks) {
            alert.append(String.format("- %s %s: %d units remaining (Threshold: %d)\n",
                    drink.getBrand(), drink.getName(), drink.getStockQuantity(), stockThreshold));
        }
        return alert.toString();
    }

    // Reporting Methods
    public String getSalesReport() {
        StringBuilder report = new StringBuilder();
        report.append("SALES REPORT - ").append(branchName).append(" (").append(location).append(")\n");
        report.append("=".repeat(50)).append("\n");
        report.append("Total Sales: KSh ").append(String.format("%.2f", totalSales)).append("\n");
        report.append("Total Orders Processed: ").append(totalOrdersProcessed).append("\n");
        report.append("Total Customers: ").append(customers.size()).append("\n");
        report.append("Total Drinks in Inventory: ").append(inventory.size()).append("\n");
        report.append("Last Updated: ").append(formattedLastUpdated).append("\n");
        report.append("Status: ").append(status).append("\n");

        if (hasLowStockItems()) {
            report.append("\n").append(getLowStockAlert());
        }

        return report.toString();
    }

    public String getInventoryReport() {
        StringBuilder report = new StringBuilder();
        report.append("INVENTORY REPORT - ").append(branchName).append(" (").append(location).append(")\n");
        report.append("=".repeat(50)).append("\n");

        if (inventory.isEmpty()) {
            report.append("No drinks in inventory.\n");
        } else {
            for (Drink drink : inventory.values()) {
                report.append(String.format("- %s %s: %d units @ KSh %.2f each\n",
                        drink.getBrand(), drink.getName(), drink.getStockQuantity(), drink.getPrice()));
            }
        }

        return report.toString();
    }

    // Utility Methods
    private void updateLastModified() {
        this.lastUpdated = LocalDateTime.now();
        this.formattedLastUpdated = lastUpdated.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public boolean isOpen() {
        return status == BranchStatus.OPEN;
    }

    public void openBranch() {
        this.status = BranchStatus.OPEN;
        updateLastModified();
    }

    public void closeBranch() {
        this.status = BranchStatus.CLOSED;
        updateLastModified();
    }

    public void setMaintenanceMode() {
        this.status = BranchStatus.MAINTENANCE;
        updateLastModified();
    }

    public String getBranchSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Branch ID: ").append(branchId != null ? branchId : "N/A").append("\n");
        summary.append("Name: ").append(branchName != null ? branchName : "N/A").append("\n");
        summary.append("Location: ").append(location != null ? location : "N/A").append("\n");
        summary.append("Address: ").append(address != null ? address : "N/A").append("\n");
        summary.append("Manager: ").append(managerName != null ? managerName : "N/A").append("\n");
        summary.append("Status: ").append(status).append("\n");
        summary.append("Total Sales: KSh ").append(String.format("%.2f", totalSales)).append("\n");
        summary.append("Orders Processed: ").append(totalOrdersProcessed).append("\n");
        summary.append("Customers: ").append(customers.size()).append("\n");
        summary.append("Inventory Items: ").append(inventory.size());
        return summary.toString();
    }

    @Override
    public String toString() {
        return String.format("Branch{ID='%s', Name='%s', Location='%s', Status=%s, Sales=%.2f, Orders=%d}",
                branchId != null ? branchId : "N/A",
                branchName != null ? branchName : "N/A",
                location != null ? location : "N/A",
                status, totalSales, totalOrdersProcessed);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Branch branch = (Branch) obj;
        return branchId != null ? branchId.equals(branch.branchId) : branch.branchId == null;
    }

    @Override
    public int hashCode() {
        return branchId != null ? branchId.hashCode() : 0;
    }
}