package server;
import common.models.*;
import common.network.NetworkMessage;
import server.database.DatabaseManager;
import server.inventory.InventoryManager;
import server.reports.ReportGenerator;
import server.services.OrderService;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HeadquartersServer {
    private static final int PORT = 5000;
    private static final int MAX_THREADS = 50;
    
    private DatabaseManager dbManager;
    private InventoryManager inventoryManager;
    private OrderService orderService;
    private ReportGenerator reportGenerator;
    private volatile boolean serverRunning = false;
    private Thread serverThread;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean running;
    
    private ConcurrentHashMap<String, BranchHandler> connectedBranches;
    

    public HeadquartersServer() {
        dbManager = new DatabaseManager();
        inventoryManager = new InventoryManager();

        // FIXED: Pass the initialized dependencies to OrderService
        orderService = new OrderService(dbManager, inventoryManager);
        // OR if you want to use the default constructor:
        // orderService = new OrderService();
        initializeSystemData();
        reportGenerator = new ReportGenerator(dbManager);
        connectedBranches = new ConcurrentHashMap<>();
        executorService = Executors.newFixedThreadPool(MAX_THREADS);
    }
    public void start(){
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Headquarters server started on port " + PORT);
            
            // Initialize system data
            initializeSystemData();
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                BranchHandler handler = new BranchHandler(clientSocket);
                executorService.submit(handler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }
    public void startAsThread() {
        if (serverRunning) {
            System.out.println("Server is already running");
            return;
        }
        
        serverThread = new Thread(() -> {
            start();
        }, "HeadquartersServerThread");
        
        serverThread.setDaemon(true); // Dies when main program exits
        serverThread.start();
        serverRunning = true;
        
        System.out.println("Headquarters Server started as background thread");
    }

    // Graceful shutdown method
    public void shutdown() {
        running = false;
        serverRunning = false;
        
        if (serverThread != null && serverThread.isAlive()) {
            try {
                serverSocket.close();
                serverThread.interrupt();
                serverThread.join(5000); // Wait up to 5 seconds
            } catch (IOException | InterruptedException e) {
                System.err.println("Error during server shutdown: " + e.getMessage());
            }
        }
        
        System.out.println("Headquarters Server shutdown complete");
    }

    public boolean isServerRunning() {
        return serverRunning && serverThread != null && serverThread.isAlive();
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            executorService.shutdown();
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }
    
    private void initializeSystemData() {
        try{
             // Initialize branches
            if (dbManager.getAllBranches().isEmpty()) {
                dbManager.addBranch(new Branch(1, "Headquarters", Branch.HEADQUARTERS, "localhost", 5000));
                dbManager.addBranch(new Branch(2, "Nakuru Branch", Branch.BRANCH_NAKURU, "localhost", 5001));
                dbManager.addBranch(new Branch(3, "Mombasa Branch", Branch.BRANCH_MOMBASA, "localhost", 5002));
                dbManager.addBranch(new Branch(4, "Kisumu Branch", Branch.BRANCH_KISUMU, "localhost", 5003));


            }


        }catch(SQLException e){
            System.err.println(e);
        }
    }
    
    // Send stock updates to all branches
    public void broadcastInventoryUpdate() throws SQLException{
        List<Drink> drinks = inventoryManager.getAllDrinks();
        NetworkMessage message = new NetworkMessage(
                NetworkMessage.TYPE_INVENTORY_UPDATE,
                drinks,
                Branch.HEADQUARTERS,
                "ALL"
        );
        
        for (BranchHandler handler : connectedBranches.values()) {
            handler.sendMessage(message);
        }
    }


    private class BranchHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String branchName;

        public BranchHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.err.println("Error initializing streams: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                // First message is branch registration
                NetworkMessage registrationMessage = (NetworkMessage) in.readObject();
                branchName = registrationMessage.getSourceBranch();
                connectedBranches.put(branchName, this);

                System.out.println("Branch connected: " + branchName);

                // Send initial inventory update
                sendInventoryUpdate();


                while (socket.isConnected() && !socket.isClosed()) {
                    try {
                        NetworkMessage message = (NetworkMessage) in.readObject();
                        System.out.println("Received message type: " + message.getType() + " from " + branchName);
                        processMessage(message);
                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("Error reading message from " + branchName + ": " + e.getMessage());
                        break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error in branch connection: " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private void processMessage(NetworkMessage message) {
            try {
                switch (message.getType()) {
                    case NetworkMessage.TYPE_ORDER:
                        System.out.println("Order Received By The Server");
                        handleOrderMessage(message);
                        break;

                    case NetworkMessage.TYPE_REPORT_REQUEST:
                        handleReportRequest(message);
                        break;

                    case NetworkMessage.TYPE_INVENTORY_UPDATE:
                        // Handle explicit inventory requests
                        sendInventoryUpdate();
                        break;

                    default:
                        System.err.println("Unknown message type: " + message.getType());
                }
            } catch (Exception e) {
                System.err.println("Error processing message: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleOrderMessage(NetworkMessage message) {
            try {
                Order order = (Order) message.getPayload();
                System.out.println("Processing order: " + order.getId() + " from " + branchName);

                if (order == null) {
                    System.err.println("Received null order from " + branchName);
                    sendOrderResponse("FAILED", "Order is null");
                    return;
                }

                if (order.getItems() == null || order.getItems().isEmpty()) {
                    System.err.println("Order has no items: " + order.getId());
                    sendOrderResponse("FAILED", "Order has no items");
                    return;
                }

                // Process the order
                boolean success = false;
                String errorMessage = "";

                try {
                    success = orderService.processOrder(order);
                    if (success) {
                        System.out.println("Order processed successfully: " + order.getId());
                    } else {
                        errorMessage = "Order processing failed - insufficient inventory or other issue";
                        System.err.println("Order processing failed: " + order.getId());
                    }
                } catch (Exception e) {
                    errorMessage = "Order processing exception: " + e.getMessage();
                    System.err.println("Exception processing order " + order.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }

                // Send response
                sendOrderResponse(success ? "SUCCESS" : "FAILED", errorMessage);

                // If inventory changed, broadcast update to all branches
//                if (success) {
//                    try {
//
//                        broadcastInventoryUpdate();
//                        System.out.println("Inventory update broadcast after successful order");
//                    } catch (SQLException e) {
//                        System.err.println("Error broadcasting inventory update: " + e.getMessage());
//                    }
//                }

            } catch (ClassCastException e) {
                System.err.println("Invalid order payload from " + branchName + ": " + e.getMessage());
                sendOrderResponse("FAILED", "Invalid order format");
            }
        }

        private void handleReportRequest(NetworkMessage message) {
            try {
                String reportType = (String) message.getPayload();
                System.out.println("Generating report: " + reportType + " for " + branchName);

                Object reportData = generateReport(reportType, branchName);

                NetworkMessage reportResponse = new NetworkMessage(
                        NetworkMessage.TYPE_REPORT_RESPONSE,
                        reportData,
                        Branch.HEADQUARTERS,
                        message.getSourceBranch()
                );
                sendMessage(reportResponse);

            } catch (Exception e) {
                System.err.println("Error handling report request: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void sendOrderResponse(String status, String message) {
            try {
                String responsePayload = status;
                if (!message.isEmpty()) {
                    responsePayload += ": " + message;
                }

                NetworkMessage response = new NetworkMessage(
                        NetworkMessage.TYPE_ORDER,
                        responsePayload,
                        Branch.HEADQUARTERS,
                        branchName
                );
                sendMessage(response);

            } catch (Exception e) {
                System.err.println("Error sending order response: " + e.getMessage());
            }
        }

        private Object generateReport(String reportType, String branchName) throws SQLException {
            switch (reportType) {
                case "BRANCH_SALES":
                    return reportGenerator.generateBranchSalesReport(branchName);
                case "CUSTOMER_ORDERS":
                    return reportGenerator.generateCustomerOrdersReport();
                case "TOTAL_SALES":
                    return reportGenerator.generateTotalSalesReport();
                case "INVENTORY_REQUEST":
                    sendInventoryUpdate();
                    return "Inventory update sent";
                default:
                    return "Unknown report type: " + reportType;
            }
        }

        public void sendInventoryUpdate() {
            try {
                List<Drink> drinks = inventoryManager.getAllDrinks();
                System.out.println("Sending inventory update to " + branchName + ": " + drinks.size() + " items");

                NetworkMessage message = new NetworkMessage(
                        NetworkMessage.TYPE_INVENTORY_UPDATE,
                        drinks,
                        Branch.HEADQUARTERS,
                        branchName
                );
                sendMessage(message);

            } catch (SQLException e) {
                System.err.println("Error getting inventory for update: " + e.getMessage());
            }
        }

        public void sendMessage(NetworkMessage message) {
            try {
                if (out != null) {
                    out.writeObject(message);
                    out.flush();
                    System.out.println("Sent message type: " + message.getType() + " to " + branchName);
                }
            } catch (IOException e) {
                System.err.println("Error sending message to branch " + branchName + ": " + e.getMessage());
            }
        }

        private void cleanup() {
            if (branchName != null) {
                connectedBranches.remove(branchName);
                System.out.println("Branch disconnected: " + branchName);
            }
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }



    public static void main(String[] args) {
        HeadquartersServer server = new HeadquartersServer();
        server.start();
    }
}