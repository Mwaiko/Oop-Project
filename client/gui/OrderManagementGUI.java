import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderManagementGUI extends JFrame {

    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JLabel totalAmountLabel;
    private ArrayList<Order> orders;
    private DecimalFormat df = new DecimalFormat("#0.00");

    public OrderManagementGUI() {
        orders = new ArrayList<>();
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Order Display System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create table panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Create summary panel
        JPanel summaryPanel = createSummaryPanel();
        add(summaryPanel, BorderLayout.SOUTH);

        // Add some sample orders for demonstration
        addSampleOrders();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }



    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Orders Display"));

        // Create table model
        String[] columnNames = {"Order ID", "Phone Number", "Customer Name", "Quantity", "Unit Price", "Total Amount"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        orderTable = new JTable(tableModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        orderTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        orderTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        orderTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        orderTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        orderTable.getColumnModel().getColumn(6).setPreferredWidth(40);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add refresh button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh Orders");
        refreshButton.addActionListener(e -> refreshOrderDisplay());
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createTitledBorder("Summary"));
        
        totalAmountLabel = new JLabel("Total Orders Value: $0.00");
        totalAmountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(totalAmountLabel);

        return panel;
    }

    // Method to add order from external source
    public void addOrder(String phoneNumber, String customerName, int quantity, double unitPrice) {
        Order order = new Order(phoneNumber, customerName, quantity, unitPrice);
        orders.add(order);
        refreshOrderDisplay();
    }

    // Method to refresh the order display
    private void refreshOrderDisplay() {
        tableModel.setRowCount(0); // Clear existing rows
        
        for (Order order : orders) {
            Object[] rowData = {
                order.getOrderId(),
                order.getPhoneNumber(),
                order.getCustomerName(),
                order.getQuantity(),
                "$" + df.format(order.getUnitPrice()),
                "$" + df.format(order.getTotalAmount())
            };
            tableModel.addRow(rowData);
        }
        updateTotalAmount();
    }

    // Method to add sample orders for demonstration
    private void addSampleOrders() {
        addOrder("555-0123", "John Smith", 2, 25.99);
        addOrder("555-0456", "Jane Doe", 1, 15.50);
        addOrder("555-0789", "Bob Johnson", 3, 12.75);
        addOrder("555-0321", "Alice Brown", 5, 8.99);
    }

    private void updateTotalAmount() {
        double total = orders.stream().mapToDouble(Order::getTotalAmount).sum();
        totalAmountLabel.setText("Total Orders Value: $" + df.format(total));
    }



    // Order class to represent individual orders
    private static class Order {
        private static int nextId = 1;
        private int orderId;
        private String phoneNumber;
        private String customerName;
        private int quantity;
        private double unitPrice;
        private double totalAmount;

        public Order(String phoneNumber, String customerName, int quantity, double unitPrice) {
            this.orderId = nextId++;
            this.phoneNumber = phoneNumber;
            this.customerName = customerName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalAmount = quantity * unitPrice;
        }

        // Getters
        public int getOrderId() { return orderId; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getCustomerName() { return customerName; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotalAmount() { return totalAmount; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new OrderManagementGUI();
        });
    }
}