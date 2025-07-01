package client.network;

import common.models.*;
import common.network.NetworkMessage;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.sql.SQLException;

public class ClientNetworkManager {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String branchName;
    private boolean connected = false;
    private Thread listenerThread;
    private BlockingQueue<NetworkMessage> responseQueue;

    // Callback interface for handling server messages
    public interface MessageHandler {
        void onInventoryUpdate(List<Drink> drinks);
        void onOrderResponse(String status);
        void onReportResponse(Object reportData);
    }

    private MessageHandler messageHandler;

    public static String getLocalIPAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            return "Unable to determine IP address: " + e.getMessage();
        }
    }

    public ClientNetworkManager(String serverAddress, int port, String branchName) throws IOException {
        this.branchName = branchName;
        this.responseQueue = new LinkedBlockingQueue<>();

        socket = new Socket(serverAddress, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        // Register this branch with the server
        registerWithServer();

        // Start listener thread
        startListenerThread();

        connected = true;
        System.out.println("Connected to headquarters server as " + branchName);
    }

    private void registerWithServer() throws IOException {
        NetworkMessage registrationMessage = new NetworkMessage(
                NetworkMessage.TYPE_REGISTRATION,
                "BRANCH_REGISTRATION",
                branchName,
                Branch.HEADQUARTERS
        );
        out.writeObject(registrationMessage);
        out.flush();
    }

    private void startListenerThread() {
        listenerThread = new Thread(() -> {
            try {
                while (connected && !Thread.currentThread().isInterrupted()) {
                    NetworkMessage message = (NetworkMessage) in.readObject();
                    handleServerMessage(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    System.err.println("Error receiving message from server: " + e.getMessage());
                }
            }
        }, "ServerListenerThread");

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void handleServerMessage(NetworkMessage message) {
        switch (message.getType()) {
            case NetworkMessage.TYPE_INVENTORY_UPDATE:
                if (messageHandler != null) {
                    @SuppressWarnings("unchecked")
                    List<Drink> drinks = (List<Drink>) message.getPayload();
                    messageHandler.onInventoryUpdate(drinks);
                }
                break;

            case NetworkMessage.TYPE_ORDER:
                if (messageHandler != null) {
                    String status = (String) message.getPayload();
                    messageHandler.onOrderResponse(status);
                }
                break;

            case NetworkMessage.TYPE_REPORT_RESPONSE:
                if (messageHandler != null) {
                    messageHandler.onReportResponse(message.getPayload());
                }
                break;

            default:
                System.out.println("Unknown message type received: " + message.getType());
        }

        // Also add to response queue for synchronous operations
        responseQueue.offer(message);
    }

    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }

    // Send order to headquarters
    public boolean sendOrder(Order order) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to server");
        }

        NetworkMessage orderMessage = new NetworkMessage(
                NetworkMessage.TYPE_ORDER,
                order,
                branchName,
                Branch.HEADQUARTERS
        );

        out.writeObject(orderMessage);
        out.flush();

        // Wait for response
        try {
            NetworkMessage response = responseQueue.take(); // Blocking wait
            if (response.getType().equals(NetworkMessage.TYPE_ORDER)) {
                String status = (String) response.getPayload();
                return "SUCCESS".equals(status);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for order response");
        }

        return false;
    }

    // Request report from headquarters
    public Object requestReport(String reportType) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to server");
        }

        NetworkMessage reportRequest = new NetworkMessage(
                NetworkMessage.TYPE_REPORT_REQUEST,
                reportType,
                branchName,
                Branch.HEADQUARTERS
        );

        out.writeObject(reportRequest);
        out.flush();

        // Wait for response
        try {
            NetworkMessage response = responseQueue.take(); // Blocking wait
            if (response.getType().equals(NetworkMessage.TYPE_REPORT_RESPONSE)) {
                return response.getPayload();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for report response");
        }

        return null;
    }

    public void sendRequest(Object request) throws IOException {
        if (!connected) {
            throw new IOException("Not connected to server");
        }
        out.writeObject(request);
        out.flush();
    }

    public Object receiveResponse() throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public void close() throws IOException {
        connected = false;

        if (listenerThread != null) {
            listenerThread.interrupt();
            try {
                listenerThread.join(3000); // Wait up to 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();

        System.out.println("Disconnected from headquarters server");
    }
}