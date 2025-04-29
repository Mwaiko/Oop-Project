package server.database;

import common.models.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DatabaseManager {
    // In-memory database (in a real app, this would connect to a real database)
    private Map<Integer, Drink> drinks = new ConcurrentHashMap<>();
    private Map<Integer, Customer> customers = new ConcurrentHashMap<>();
    private Map<Integer, Branch> branches = new ConcurrentHashMap<>();
    private Map<Integer, Order> orders = new ConcurrentHashMap<>();
    
    private int nextDrinkId = 1;
    private int nextCustomerId = 1;
    private int nextBranchId = 1;
    private int nextOrderId = 1;
    
    // Drink operations
    public synchronized Drink addDrink(Drink drink) {
        if (drink.getId() == 0) {
            drink.setId(nextDrinkId++);
        }
        drinks.put(drink.getId(), drink);
        return drink;
    }
    
    public Drink getDrink(int id) {
        return drinks.get(id);
    }
    
    public List<Drink> getAllDrinks() {
        return new ArrayList<>(drinks.values());
    }
    
    public void updateDrink(Drink drink) {
        drinks.put(drink.getId(), drink);
    }
    
    public void deleteDrink(int id) {
        drinks.remove(id);
    }
    
    // Customer operations
    public synchronized Customer addCustomer(Customer customer) {
        if (customer.getId() == 0) {
            customer.setId(nextCustomerId++);
        }
        customers.put(customer.getId(), customer);
        return customer;
    }
    
    public Customer getCustomer(int id) {
        return customers.get(id);
    }
    
    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }
    
    public void updateCustomer(Customer customer) {
        customers.put(customer.getId(), customer);
    }
    
    public void deleteCustomer(int id) {
        customers.remove(id);
    }
    
    // Branch operations
    public synchronized Branch addBranch(Branch branch) {
        if (branch.getId() == 0) {
            branch.setId(nextBranchId++);
        }
        branches.put(branch.getId(), branch);
        return branch;
    }
    
    public Branch getBranch(int id) {
        return branches.get(id);
    }
    
    public Branch getBranchByName(String name) {
        return branches.values().stream()
                .filter(b -> b.getName().equals(name) || b.getLocation().equals(name))
                .findFirst()
                .orElse(null);
    }
    
    public List<Branch> getAllBranches() {
        return new ArrayList<>(branches.values());
    }
    
    public void updateBranch(Branch branch) {
        branches.put(branch.getId(), branch);
    }
    
    public void deleteBranch(int id) {
        branches.remove(id);
    }
    
    // Order operations
    public synchronized Order addOrder(Order order) {
        if (order.getId() == 0) {
            order.setId(nextOrderId++);
        }
        orders.put(order.getId(), order);
        return order;
    }
    
    public Order getOrder(int id) {
        return orders.get(id);
    }
    
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }
    
    public List<Order> getOrdersByBranch(String branchName) {
        return orders.values().stream()
                .filter(o -> o.getBranch().getLocation().equals(branchName))
                .collect(Collectors.toList());
    }
    
    public List<Order> getOrdersByCustomer(int customerId) {
        return orders.values().stream()
                .filter(o -> o.getCustomer().getId() == customerId)
                .collect(Collectors.toList());
    }
    
    public void updateOrder(Order order) {
        orders.put(order.getId(), order);
    }
    
    public void deleteOrder(int id) {
        orders.remove(id);
    }
    
    // Sales analytics
    public BigDecimal getTotalSalesByBranch(String branchName) {
        return orders.values().stream()
                .filter(o -> o.getBranch().getLocation().equals(branchName) && 
                         o.getStatus().equals(Order.STATUS_COMPLETED))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalSales() {
        return orders.values().stream()
                .filter(o -> o.getStatus().equals(Order.STATUS_COMPLETED))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // For demo purposes, initialize sample customers
    public void initializeSampleCustomers() {
        if (customers.isEmpty()) {
            addCustomer(new Customer(0, "John Doe", "0722123456", "john@example.com"));
            addCustomer(new Customer(0, "Jane Smith", "0733987654", "jane@example.com"));
            addCustomer(new Customer(0, "Alice Johnson", "0711555666", "alice@example.com"));
        }
    }
}