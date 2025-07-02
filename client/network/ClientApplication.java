package client.network;

import client.services.ClientService;
import common.models.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class ClientApplication {
    private ClientService clientService;
    private Scanner scanner;
    private String branchName;

    public ClientApplication(String branchName) {
        this.branchName = branchName;
        this.clientService = new ClientService(branchName);
        this.scanner = new Scanner(System.in);

        // Add listeners for real-time updates
        setupListeners();
    }

    private void setupListeners() {
        // Listen for inventory updates
        clientService.addInventoryUpdateListener(drinks -> {
            System.out.println("\n*** INVENTORY UPDATED ***");
            System.out.println("New inventory received with " + drinks.size() + " items");
            displayInventory(drinks);
        });

        // Listen for order status updates
        clientService.addOrderStatusListener(status -> {
            System.out.println("\n*** ORDER STATUS UPDATE ***");
            System.out.println("Order status: " + status);
        });
    }

    public void start() {
        System.out.println("=== " + branchName + " Client Application ===");

        // Connect to headquarters
        if (!connectToHeadquarters()) {
            System.out.println("Failed to connect to headquarters. Exiting.");
            return;
        }

        // Main menu loop
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    viewInventory();
                    break;
                case 2:
                    createNewOrder();
                    break;
                case 3:
                    viewBranchSalesReport();
                    break;
                case 4:
                    viewCustomerOrdersReport();
                    break;
                case 5:
                    viewTotalSalesReport();
                    break;
                case 6:
                    checkConnectionStatus();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        shutdown();
    }

    private boolean connectToHeadquarters() {
        System.out.print("Enter headquarters server address (default: localhost): ");
        String serverAddress = scanner.nextLine().trim();
        if (serverAddress.isEmpty()) {
            serverAddress = "localhost";
        }

        System.out.print("Enter headquarters server port (default: 5000): ");
        String portStr = scanner.nextLine().trim();
        int port = portStr.isEmpty() ? 5000 : Integer.parseInt(portStr);

        System.out.println("Connecting to headquarters at " + serverAddress + ":" + port + "...");

        boolean connected = clientService.connectToHeadquarters(serverAddress, port);
        if (connected) {
            System.out.println("Successfully connected to headquarters!");
            // Wait a moment for initial inventory update
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return connected;
    }

    private void displayMainMenu() {
        System.out.println("\n=== MAIN MENU ===");
        System.out.println("1. View Inventory");
        System.out.println("2. Create New Order");
        System.out.println("3. View Branch Sales Report");
        System.out.println("4. View Customer Orders Report");
        System.out.println("5. View Total Sales Report");
        System.out.println("6. Check Connection Status");
        System.out.println("0. Exit");
    }

    private void viewInventory() {
        List<Drink> inventory = clientService.getCurrentInventory();
        if (inventory.isEmpty()) {
            System.out.println("No inventory data available.");
            return;
        }

        displayInventory(inventory);
    }

    private void displayInventory(List<Drink> drinks) {
        System.out.println("\n=== CURRENT INVENTORY ===");
        System.out.printf("%-5s %-20s %-15s %-10s %-8s %-12s%n",
                "ID", "Name", "Brand", "Price", "Stock", "Min Threshold");
        System.out.println("-".repeat(80));

        for (Drink drink : drinks) {
            System.out.printf("%-5d %-20s %-15s %-10s %-8d %-12d%n",
                    drink.getId(),
                    drink.getName(),
                    drink.getBrand(),
                    "Ksh " + drink.getPrice(),
                    drink.getQuantityAvailable(),
                    drink.getMinThreshold());
        }
    }

    private void createNewOrder() {
        List<Drink> inventory = clientService.getCurrentInventory();
        if (inventory.isEmpty()) {
            System.out.println("No inventory available to create orders.");
            return;
        }

        // Get customer information
        System.out.println("\n=== CREATE NEW ORDER ===");
        System.out.print("Customer Name: ");
        String customerName = scanner.nextLine();
        System.out.print("Customer Phone: ");
        String customerPhone = scanner.nextLine();
        System.out.print("Customer Email: ");
        String customerEmail = scanner.nextLine();

        Customer customer = new Customer(customerName, customerPhone);

        // Create branch object
        Branch branch = new Branch(0, branchName, branchName, "localhost", 0);

        // Create order
        Order order = clientService.createOrder(customer, branch);

        // Add items to order
        boolean addingItems = true;
        while (addingItems) {
            displayInventory(inventory);

            int drinkId = getIntInput("\nEnter drink ID (0 to finish): ");
            if (drinkId == 0) {
                addingItems = false;
                continue;
            }

            Drink selectedDrink = clientService.findDrinkById(drinkId);
            if (selectedDrink == null) {
                System.out.println("Invalid drink ID.");
                continue;
            }

            int quantity = getIntInput("Enter quantity: ");
            if (quantity <= 0) {
                System.out.println("Invalid quantity.");
                continue;
            }

            if (!clientService.isDrinkAvailable(drinkId, quantity)) {
                System.out.println("Insufficient stock for this drink.");
                continue;
            }

            order.addItem(selectedDrink, quantity);
            System.out.println("Added " + quantity + " x " + selectedDrink.getName() + " to order.");
        }

        if (order.getItems().isEmpty()) {
            System.out.println("No items added to order. Order cancelled.");
            return;
        }

        // Display order summary
        System.out.println("\n=== ORDER SUMMARY ===");
        System.out.printf("Customer: %s (%s)%n", customer.getName(), customer.getPhone());
        System.out.printf("Branch: %s%n", branch.getName());
        System.out.println("Items:");
        for (Order.OrderItem item : order.getItems()) {
            System.out.printf("  %s x%d = Ksh %s%n",
                    item.getDrink().getName(),
                    item.getQuantity(),
                    item.getSubtotal());
        }
        System.out.printf("Total: Ksh %s%n", order.getTotalAmount());

        System.out.print("\nConfirm order? (y/n): ");
        String confirm = scanner.nextLine();
        if (confirm.equalsIgnoreCase("y")) {
            boolean success = clientService.submitOrder(order);
            if (success) {
                System.out.println("Order submitted successfully!");
            } else {
                System.out.println("Failed to submit order.");
            }
        } else {
            System.out.println("Order cancelled.");
        }
    }

    private void viewBranchSalesReport() {
        System.out.println("\nRequesting branch sales report...");
        Object report = clientService.requestBranchSalesReport();
        if (report != null) {
            System.out.println("Branch Sales Report:");
            System.out.println(report);
        } else {
            System.out.println("Failed to retrieve branch sales report.");
        }
    }

    private void viewCustomerOrdersReport() {
        System.out.println("\nRequesting customer orders report...");
        Object report = clientService.requestCustomerOrdersReport();
        if (report != null) {
            System.out.println("Customer Orders Report:");
            System.out.println(report);
        } else {
            System.out.println("Failed to retrieve customer orders report.");
        }
    }

    private void viewTotalSalesReport() {
        System.out.println("\nRequesting total sales report...");
        Object report = clientService.requestTotalSalesReport();
        if (report != null) {
            System.out.println("Total Sales Report:");
            System.out.println(report);
        } else {
            System.out.println("Failed to retrieve total sales report.");
        }
    }

    private void checkConnectionStatus() {
        if (clientService.isConnected()) {
            System.out.println("✓ Connected to headquarters server");
        } else {
            System.out.println("✗ Not connected to headquarters server");
        }
    }

    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void shutdown() {
        System.out.println("Shutting down...");
        clientService.disconnect();
        scanner.close();
        System.out.println("Goodbye!");
    }

    public static void main(String[] args) {
        String branchName = args.length > 0 ? args[0] : "Nakuru";
        ClientApplication app = new ClientApplication(branchName);
        app.start();
    }
}