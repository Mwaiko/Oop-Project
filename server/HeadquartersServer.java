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
        orderService = new OrderService();
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
            
            // Initialize sample drinks
            inventoryManager.initializeSampleDrinks();
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
    
    // Inner class to handle branch connections
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
                

                sendInventoryUpdate();
                
                // Process messages
                while (socket.isConnected()) {
                    NetworkMessage message = (NetworkMessage) in.readObject();
                    processMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error in branch connection: " + e.getMessage());
            } finally {
                if (branchName != null) {
                    connectedBranches.remove(branchName);
                    System.out.println("Branch disconnected: " + branchName);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }
        
        private void processMessage(NetworkMessage message) {
            try{
                switch (message.getType()) {
                    case NetworkMessage.TYPE_ORDER:
                        Order order = (Order) message.getPayload();
                        boolean success = orderService.processOrder(order);
                        
                        // Respond with status
                        NetworkMessage response = new NetworkMessage(
                                NetworkMessage.TYPE_ORDER,
                                success ? "SUCCESS" : "FAILED",
                                Branch.HEADQUARTERS,
                                message.getSourceBranch()
                        );
                        sendMessage(response);
                        
                        // If inventory changed, broadcast update
                        if (success) {
                            broadcastInventoryUpdate();
                        }
                        break;
                        
                    case NetworkMessage.TYPE_REPORT_REQUEST:
                        String reportType = (String) message.getPayload();
                        Object reportData = generateReport(reportType, message.getSourceBranch());
                        
                        NetworkMessage reportResponse = new NetworkMessage(
                                NetworkMessage.TYPE_REPORT_RESPONSE,
                                reportData,
                                Branch.HEADQUARTERS,
                                message.getSourceBranch()
                        );
                        sendMessage(reportResponse);
                        break;
                }
            }catch(SQLException e){
                System.err.println(e);
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
                default:
                    return "Unknown report type";
            }
        }
        
        public void sendInventoryUpdate(){
            try{
                List<Drink> drinks = inventoryManager.getAllDrinks();
                NetworkMessage message = new NetworkMessage(
                        NetworkMessage.TYPE_INVENTORY_UPDATE,
                        drinks,
                        Branch.HEADQUARTERS,
                        branchName
                );
                sendMessage(message);
            }catch(SQLException e){
                System.err.println(e);
            }
            
        }
        
        public void sendMessage(NetworkMessage message) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error sending message to branch " + branchName + ": " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        HeadquartersServer server = new HeadquartersServer();
        server.start();
    }
}