import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private String customerId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime registrationDate;
    private String formattedRegistrationDate;
    private List<String> orderHistory;
    private double totalSpent;
    private CustomerStatus status;

    public enum CustomerStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }

    public Customer() {
        this.orderHistory = new ArrayList<>();
        this.registrationDate = LocalDateTime.now();
        this.formattedRegistrationDate = registrationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = CustomerStatus.ACTIVE;
        this.totalSpent = 0.0;
    }

    public Customer(String customerId, String name, String email, String phone, String address) {
        this();
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Getters
    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public String getFormattedRegistrationDate() {
        return formattedRegistrationDate;
    }

    public List<String> getOrderHistory() {
        return new ArrayList<>(orderHistory);
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    // Setters
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    // Business methods
    public void addOrder(String orderId, double orderAmount) {
        if (orderId != null && !orderId.trim().isEmpty() && orderAmount > 0) {
            orderHistory.add(orderId);
            totalSpent += orderAmount;
        }
    }

    public boolean hasOrderHistory() {
        return !orderHistory.isEmpty();
    }

    public int getTotalOrders() {
        return orderHistory.size();
    }

    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }

    public void activateCustomer() {
        this.status = CustomerStatus.ACTIVE;
    }

    public void deactivateCustomer() {
        this.status = CustomerStatus.INACTIVE;
    }

    public void suspendCustomer() {
        this.status = CustomerStatus.SUSPENDED;
    }

    public String getCustomerSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Customer ID: ").append(customerId != null ? customerId : "N/A").append("\n");
        summary.append("Name: ").append(name != null ? name : "N/A").append("\n");
        summary.append("Email: ").append(email != null ? email : "N/A").append("\n");
        summary.append("Phone: ").append(phone != null ? phone : "N/A").append("\n");
        summary.append("Address: ").append(address != null ? address : "N/A").append("\n");
        summary.append("Registration Date: ").append(formattedRegistrationDate != null ? formattedRegistrationDate : "N/A").append("\n");
        summary.append("Status: ").append(status).append("\n");
        summary.append("Total Orders: ").append(getTotalOrders()).append("\n");
        summary.append("Total Spent: KSh ").append(String.format("%.2f", totalSpent));
        return summary.toString();
    }

    @Override
    public String toString() {
        return String.format("Customer{ID='%s', Name='%s', Email='%s', Phone='%s', Status=%s, Orders=%d, TotalSpent=%.2f}",
                customerId != null ? customerId : "N/A",
                name != null ? name : "N/A",
                email != null ? email : "N/A",
                phone != null ? phone : "N/A",
                status, getTotalOrders(), totalSpent);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Customer customer = (Customer) obj;
        return customerId != null ? customerId.equals(customer.customerId) : customer.customerId == null;
    }

    @Override
    public int hashCode() {
        return customerId != null ? customerId.hashCode() : 0;
    }
}