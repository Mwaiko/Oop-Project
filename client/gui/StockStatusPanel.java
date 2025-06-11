import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class StockStatusPanel extends JFrame {
    private JTable stockTable, requestTable;
    private DefaultTableModel stockTableModel, requestTableModel;
    private JTextField itemNameField, brandField, quantityField, reasonField;
    private JComboBox<String> priorityComboBox;
    private JLabel totalItemsLabel, lowStockLabel;
    private ArrayList<StockItem> stockItems;
    private ArrayList<StockRequest> stockRequests;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public StockStatusPanel() {
        stockItems = new ArrayList<>();
        stockRequests = new ArrayList<>();
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Stock Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Stock Display Tab
        JPanel stockDisplayPanel = createStockDisplayPanel();
        tabbedPane.addTab("Current Stock", stockDisplayPanel);
        
        // Stock Request Tab
        JPanel stockRequestPanel = createStockRequestPanel();
        tabbedPane.addTab("Request Stock", stockRequestPanel);
        
        // Request History Tab
        JPanel requestHistoryPanel = createRequestHistoryPanel();
        tabbedPane.addTab("Request History", requestHistoryPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Add sample data
        addSampleStockData();
        addSampleRequestData();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createStockDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create search panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));
        
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JButton showLowStockButton = new JButton("Show Low Stock");
        JButton refreshButton = new JButton("Refresh");
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showLowStockButton);
        searchPanel.add(refreshButton);
        
        panel.add(searchPanel, BorderLayout.NORTH);

        // Create stock table
        String[] stockColumns = {"Item Code", "Item Name", "Brand", "Current Qty", "Min Level", "Max Level", "Unit Price", "Status"};
        stockTableModel = new DefaultTableModel(stockColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        stockTable = new JTable(stockTableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set column widths
        stockTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        stockTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        stockTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        stockTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        stockTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        stockTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        stockTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        stockTable.getColumnModel().getColumn(7).setPreferredWidth(100);

        // Add table sorter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(stockTableModel);
        stockTable.setRowSorter(sorter);

        JScrollPane stockScrollPane = new JScrollPane(stockTable);
        stockScrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(stockScrollPane, BorderLayout.CENTER);

        // Create summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Stock Summary"));
        
        totalItemsLabel = new JLabel("Total Items: 0");
        lowStockLabel = new JLabel("Low Stock Items: 0");
        
        summaryPanel.add(totalItemsLabel);
        summaryPanel.add(Box.createHorizontalStrut(20));
        summaryPanel.add(lowStockLabel);
        
        panel.add(summaryPanel, BorderLayout.SOUTH);

        // Add action listeners
        searchButton.addActionListener(e -> filterStock(searchField.getText()));
        showLowStockButton.addActionListener(e -> showLowStockItems());
        refreshButton.addActionListener(e -> refreshStockDisplay());
        
        return panel;
    }

    private JPanel createStockRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create request form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Request Stock from Headquarters"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Item Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        itemNameField = new JTextField(20);
        formPanel.add(itemNameField, gbc);

        // Brand
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Brand:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        brandField = new JTextField(20);
        formPanel.add(brandField, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Quantity Needed:"), gbc);
        gbc.gridx = 1;
        quantityField = new JTextField(10);
        formPanel.add(quantityField, gbc);

        // Priority
        gbc.gridx = 2;
        formPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 3;
        priorityComboBox = new JComboBox<>(new String[]{"Low", "Medium", "High", "Urgent"});
        formPanel.add(priorityComboBox, gbc);

        // Reason
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        reasonField = new JTextField(30);
        formPanel.add(reasonField, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton submitButton = new JButton("Submit Request");
        submitButton.addActionListener(new SubmitRequestListener());
        buttonPanel.add(submitButton);
        
        JButton clearButton = new JButton("Clear Form");
        clearButton.addActionListener(e -> clearRequestForm());
        buttonPanel.add(clearButton);
        
        JButton quickRequestButton = new JButton("Quick Request from Stock");
        quickRequestButton.addActionListener(new QuickRequestListener());
        buttonPanel.add(quickRequestButton);
        
        formPanel.add(buttonPanel, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Add instructions
        JTextArea instructions = new JTextArea(
            "Instructions:\n" +
            "1. Fill in all required fields\n" +
            "2. Select appropriate priority level\n" +
            "3. Provide clear reason for stock request\n" +
            "4. Use 'Quick Request' to auto-fill from current stock items\n" +
            "5. Submit request for headquarters approval"
        );
        instructions.setEditable(false);
        instructions.setBorder(BorderFactory.createTitledBorder("How to Request Stock"));
        instructions.setBackground(panel.getBackground());
        panel.add(instructions, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRequestHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create request history table
        String[] requestColumns = {"Request ID", "Item Name", "Brand", "Quantity", "Priority", "Status", "Date Requested", "Reason"};
        requestTableModel = new DefaultTableModel(requestColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestTable = new JTable(requestTableModel);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set column widths
        requestTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        requestTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        requestTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        requestTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        requestTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        requestTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        requestTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        requestTable.getColumnModel().getColumn(7).setPreferredWidth(200);

        JScrollPane requestScrollPane = new JScrollPane(requestTable);
        requestScrollPane.setPreferredSize(new Dimension(800, 350));
        panel.add(requestScrollPane, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton refreshRequestsButton = new JButton("Refresh");
        JButton cancelRequestButton = new JButton("Cancel Selected Request");
        
        controlPanel.add(refreshRequestsButton);
        controlPanel.add(cancelRequestButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        refreshRequestsButton.addActionListener(e -> refreshRequestHistory());
        cancelRequestButton.addActionListener(new CancelRequestListener());

        return panel;
    }

    // Public methods for external integration
    public void addStockItem(String itemCode, String itemName, String brand, int currentQty, int minLevel, int maxLevel, double unitPrice) {
        StockItem item = new StockItem(itemCode, itemName, brand, currentQty, minLevel, maxLevel, unitPrice);
        stockItems.add(item);
        refreshStockDisplay();
    }

    public void updateStockQuantity(String itemCode, int newQuantity) {
        for (StockItem item : stockItems) {
            if (item.getItemCode().equals(itemCode)) {
                item.setCurrentQty(newQuantity);
                refreshStockDisplay();
                break;
            }
        }
    }

    private void refreshStockDisplay() {
        stockTableModel.setRowCount(0);
        int lowStockCount = 0;
        
        for (StockItem item : stockItems) {
            String status = item.getStatus();
            if (status.equals("Low Stock") || status.equals("Out of Stock")) {
                lowStockCount++;
            }
            
            Object[] rowData = {
                item.getItemCode(),
                item.getItemName(),
                item.getBrand(),
                item.getCurrentQty(),
                item.getMinLevel(),
                item.getMaxLevel(),
                String.format("$%.2f", item.getUnitPrice()),
                status
            };
            stockTableModel.addRow(rowData);
        }
        
        totalItemsLabel.setText("Total Items: " + stockItems.size());
        lowStockLabel.setText("Low Stock Items: " + lowStockCount);
    }

    private void refreshRequestHistory() {
        requestTableModel.setRowCount(0);
        
        for (StockRequest request : stockRequests) {
            Object[] rowData = {
                request.getRequestId(),
                request.getItemName(),
                request.getBrand(),
                request.getQuantity(),
                request.getPriority(),
                request.getStatus(),
                dateFormat.format(request.getDateRequested()),
                request.getReason()
            };
            requestTableModel.addRow(rowData);
        }
    }

    private void filterStock(String searchText) {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) stockTable.getRowSorter();
        if (searchText.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

    private void showLowStockItems() {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) stockTable.getRowSorter();
        sorter.setRowFilter(RowFilter.regexFilter("Low Stock|Out of Stock"));
    }

    private void clearRequestForm() {
        itemNameField.setText("");
        brandField.setText("");
        quantityField.setText("");
        reasonField.setText("");
        priorityComboBox.setSelectedIndex(0);
        itemNameField.requestFocus();
    }

    private class SubmitRequestListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String itemName = itemNameField.getText().trim();
                String brand = brandField.getText().trim();
                String quantityText = quantityField.getText().trim();
                String reason = reasonField.getText().trim();
                String priority = (String) priorityComboBox.getSelectedItem();

                if (itemName.isEmpty() || quantityText.isEmpty() || reason.isEmpty()) {
                    JOptionPane.showMessageDialog(StockStatusPanel.this,
                        "Please fill in all required fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int quantity = Integer.parseInt(quantityText);
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(StockStatusPanel.this,
                        "Quantity must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                StockRequest request = new StockRequest(itemName, brand, quantity, priority, reason);
                stockRequests.add(request);
                refreshRequestHistory();
                clearRequestForm();

                JOptionPane.showMessageDialog(StockStatusPanel.this,
                    "Stock request submitted successfully!\nRequest ID: " + request.getRequestId(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(StockStatusPanel.this,
                    "Please enter a valid number for quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class QuickRequestListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = stockTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(StockStatusPanel.this,
                    "Please select an item from the stock table first.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int modelRow = stockTable.convertRowIndexToModel(selectedRow);
            String itemName = (String) stockTableModel.getValueAt(modelRow, 1);
            String brand = (String) stockTableModel.getValueAt(modelRow, 2);

            itemNameField.setText(itemName);
            brandField.setText(brand);
            reasonField.setText("Restocking low inventory");
            
            // Switch to request tab
            JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
            tabbedPane.setSelectedIndex(1);
        }
    }

    private class CancelRequestListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = requestTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(StockStatusPanel.this,
                    "Please select a request to cancel.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(StockStatusPanel.this,
                "Are you sure you want to cancel this request?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                StockRequest request = stockRequests.get(selectedRow);
                if (request.getStatus().equals("Pending")) {
                    request.setStatus("Cancelled");
                    refreshRequestHistory();
                    JOptionPane.showMessageDialog(StockStatusPanel.this,
                        "Request cancelled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(StockStatusPanel.this,
                        "Only pending requests can be cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void addSampleStockData() {
        addStockItem("ITM001", "Wireless Mouse", "Logitech", 25, 10, 100, 29.99);
        addStockItem("ITM002", "USB Cable", "Generic", 5, 20, 200, 9.99);
        addStockItem("ITM003", "Laptop Charger", "Dell", 15, 8, 50, 45.50);
        addStockItem("ITM004", "Keyboard", "Microsoft", 30, 15, 80, 35.99);
        addStockItem("ITM005", "Monitor Stand", "Adjustable", 0, 5, 25, 22.75);
    }

    private void addSampleRequestData() {
        stockRequests.add(new StockRequest("USB Cable", "Generic", 50, "High", "Low stock alert"));
        stockRequests.add(new StockRequest("Monitor Stand", "Adjustable", 20, "Urgent", "Out of stock"));
        stockRequests.add(new StockRequest("Wireless Headset", "Sony", 10, "Medium", "New product request"));
    }

    // Stock Item class
    private static class StockItem {
        private String itemCode, itemName, brand;
        private int currentQty, minLevel, maxLevel;
        private double unitPrice;

        public StockItem(String itemCode, String itemName, String brand, int currentQty, int minLevel, int maxLevel, double unitPrice) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.brand = brand;
            this.currentQty = currentQty;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.unitPrice = unitPrice;
        }

        public String getStatus() {
            if (currentQty == 0) return "Out of Stock";
            if (currentQty <= minLevel) return "Low Stock";
            if (currentQty >= maxLevel) return "Overstock";
            return "Normal";
        }

        // Getters and setters
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public String getBrand() { return brand; }
        public int getCurrentQty() { return currentQty; }
        public void setCurrentQty(int currentQty) { this.currentQty = currentQty; }
        public int getMinLevel() { return minLevel; }
        public int getMaxLevel() { return maxLevel; }
        public double getUnitPrice() { return unitPrice; }
    }

    // Stock Request class
    private static class StockRequest {
        private static int nextId = 1;
        private String requestId, itemName, brand, priority, reason, status;
        private int quantity;
        private Date dateRequested;

        public StockRequest(String itemName, String brand, int quantity, String priority, String reason) {
            this.requestId = "REQ" + String.format("%04d", nextId++);
            this.itemName = itemName;
            this.brand = brand;
            this.quantity = quantity;
            this.priority = priority;
            this.reason = reason;
            this.status = "Pending";
            this.dateRequested = new Date();
        }

        // Getters and setters
        public String getRequestId() { return requestId; }
        public String getItemName() { return itemName; }
        public String getBrand() { return brand; }
        public int getQuantity() { return quantity; }
        public String getPriority() { return priority; }
        public String getReason() { return reason; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Date getDateRequested() { return dateRequested; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new StockStatusPanel();
        });
    }
}