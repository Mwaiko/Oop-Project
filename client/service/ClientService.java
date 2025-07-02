package client.service;

import client.network.ClientNetworkManager;
import common.models.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientService implements ClientNetworkManager.MessageHandler {
    private ClientNetworkManager networkManager;
    private List<Drink> currentInventory;
    private String branchName;

    // Listeners for UI updates
    public interface InventoryUpdateListener {
        void onInventoryUpdated(List<Drink> drinks);
    }

    public interface OrderStatusListener {
        void onOrderStatusReceived(String status);
    }

    private List<InventoryUpdateListener> inventoryListeners;
    private List<OrderStatusListener> orderStatusListeners;

    public ClientService(String branchName) {
        this.branchName = branchName;
        this.currentInventory = new CopyOnWriteArrayList<>();
        this.inventoryListeners = new ArrayList<>();
        this.orderStatusListeners = new ArrayList<>();
    }

    // Connect to headquarters server
    public boolean connectToHeadquarters(String serverAddress, int port) {
        try {
            networkManager = new ClientNetworkManager(serverAddress, port, branchName);
            networkManager.setMessageHandler(this);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to headquarters: " + e.getMessage());
            return false;
        }
    }

    // Disconnect from server
    public void disconnect() {
        if (networkManager != null) {
            try {
                networkManager.close();
            } catch (IOException e) {
                System.err.println("Error disconnecting: " + e.getMessage());
            }
        }
    }

    // Submit order to headquarters
    public boolean submitOrder(Order order) {
        if (networkManager == null || !networkManager.isConnected()) {
            System.err.println("Not connected to headquarters server");
            return false;
        }

        try {
            boolean success = networkManager.sendOrder(order);
            if (success) {
                System.out.println("Order submitted successfully: " + order.getId());
            } else {
                System.err.println("Order failed: " + order.getId());
            }
            return success;
        } catch (IOException e) {
            System.err.println("Error submitting order: " + e.getMessage());
            return false;
        }
    }

    // Request different types of reports
    public Object requestBranchSalesReport() {
        if (networkManager == null || !networkManager.isConnected()) {
            System.err.println("Not connected to headquarters server");
            return null;
        }

        try {
            return networkManager.requestReport("BRANCH_SALES");
        } catch (IOException e) {
            System.err.println("Error requesting branch sales report: " + e.getMessage());
            return null;
        }
    }

    public Object requestCustomerOrdersReport() {
        if (networkManager == null || !networkManager.isConnected()) {
            System.err.println("Not connected to headquarters server");
            return null;
        }

        try {
            return networkManager.requestReport("CUSTOMER_ORDERS");
        } catch (IOException e) {
            System.err.println("Error requesting customer orders report: " + e.getMessage());
            return null;
        }
    }

    public Object requestTotalSalesReport() {
        if (networkManager == null || !networkManager.isConnected()) {
            System.err.println("Not connected to headquarters server");
            return null;
        }

        try {
            return networkManager.requestReport("TOTAL_SALES");
        } catch (IOException e) {
            System.err.println("Error requesting total sales report: " + e.getMessage());
            return null;
        }
    }

    // Get current inventory
    public List<Drink> getCurrentInventory() {
        return new ArrayList<>(currentInventory);
    }

    // Check if connected to server
    public boolean isConnected() {
        return networkManager != null && networkManager.isConnected();
    }

    // Add listeners
    public void addInventoryUpdateListener(InventoryUpdateListener listener) {
        inventoryListeners.add(listener);
    }

    public void addOrderStatusListener(OrderStatusListener listener) {
        orderStatusListeners.add(listener);
    }

    // Remove listeners
    public void removeInventoryUpdateListener(InventoryUpdateListener listener) {
        inventoryListeners.remove(listener);
    }

    public void removeOrderStatusListener(OrderStatusListener listener) {
        orderStatusListeners.remove(listener);
    }


    @Override
    public void onInventoryUpdate(List<Drink> drinks) {
        currentInventory.clear();
        currentInventory.addAll(drinks);

        System.out.println("Inventory updated: " + drinks.size() + " items received");

        // Notify all listeners
        for (InventoryUpdateListener listener : inventoryListeners) {
            listener.onInventoryUpdated(drinks);
        }
    }

    @Override
    public void onOrderResponse(String status) {
        System.out.println("Order status received: " + status);

        // Notify all listeners
        for (OrderStatusListener listener : orderStatusListeners) {
            listener.onOrderStatusReceived(status);
        }
    }

    @Override
    public void onReportResponse(Object reportData) {
        System.out.println("Report received: " + reportData);
        // This is handled synchronously in the request methods
    }

    // Helper method to create an order
    public Order createOrder(Customer customer, Branch branch) {
        // Generate a simple order ID (in real app, this should be more sophisticated)
        int orderId = (int) (System.currentTimeMillis() % 100000);
        return new Order(customer, branch);
    }

    // Helper method to find drink by ID
    public Drink findDrinkById(int drinkId) {
        return currentInventory.stream()
                .filter(drink -> drink.getId() == drinkId)
                .findFirst()
                .orElse(null);
    }

    // Helper method to check if drink is available
    public boolean isDrinkAvailable(int drinkId, int quantity) {
        Drink drink = findDrinkById(drinkId);
        return drink != null && drink.getQuantityAvailable() >= quantity;
    }
    public static void main(String []args){
        ClientService service = new ClientService("");
        service.connectToHeadquarters("localhost",5000);
        service.getCurrentInventory();
        if (service.isConnected()){
            System.out.println("The Branch Has Succesfully connected to the Headquarter");
        }else{
            System.out.println("The Branch Has Failed To Connnect To the HeadQuarter");
        }

    }
}
