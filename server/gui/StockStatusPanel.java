package server.gui;

import common.models.Branch;
import common.models.Drink;
import server.database.DatabaseManager;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
public class StockStatusPanel extends JFrame {
    private JTable stockTable, requestTable;
    private DefaultTableModel stockTableModel, requestTableModel;
    private JTextField itemNameField, brandField, quantityField, reasonField;
    private JComboBox<String> priorityComboBox;
    private JLabel totalItemsLabel, lowStockLabel;
    private ArrayList<StockItem> stockItems;
    private ArrayList<StockRequest> stockRequests;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private DatabaseManager dbManager;

    public StockStatusPanel() {
        stockItems = new ArrayList<>();
        stockRequests = new ArrayList<>();
        dbManager = new DatabaseManager(); // Initialize DatabaseManager
        initializeGUI();
        loadStockDataFromDatabase(); // Load data from database
    }

    private final Color darkCharcoal = new Color(36, 39, 46);
    private final Color lightCharcoal = new Color(52, 58, 70);
    private final Color cardBackground = new Color(62, 68, 80);
    private final Color beautifulBlue = new Color(64, 149, 255);
    private final Color hoverBlue = new Color(85, 170, 255);
    private final Color selectedBlue = new Color(44, 129, 235);
    private final Color textPrimary = new Color(255, 255, 255);
    private final Color textSecondary = new Color(170, 178, 189);
    private final Color accentGreen = new Color(46, 204, 113);
    private final Color accentRed = new Color(231, 76, 60);
    private final Color accentOrange = new Color(255, 165, 0);

    private void initializeGUI() {
        setTitle("Stock Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set dark theme
        getContentPane().setBackground(darkCharcoal);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, darkCharcoal, 0, getHeight(), lightCharcoal);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Create styled tabbed pane
        JTabbedPane tabbedPane = createStyledTabbedPane();

        // Stock Display Tab
        JPanel stockDisplayPanel = createStockDisplayPanel();
        tabbedPane.addTab("📦 Current Stock", stockDisplayPanel);

        // Stock Request Tab
        JPanel stockRequestPanel = createStockRequestPanel();
        tabbedPane.addTab("📋 Request Stock", stockRequestPanel);

        // Request History Tab
        JPanel requestHistoryPanel = createRequestHistoryPanel();
        tabbedPane.addTab("📊 Request History", requestHistoryPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);


        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("📦 Stock Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(textPrimary);

        JLabel subtitleLabel = new JLabel("Monitor inventory levels and manage stock requests", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(textSecondary);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        return headerPanel;
    }

    private JTabbedPane createStyledTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(cardBackground);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        tabbedPane.setBackground(cardBackground);
        tabbedPane.setForeground(textPrimary);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return tabbedPane;
    }
    private JPanel createStockDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create search panel
        JPanel searchPanel = createStyledCard();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel searchLabel = new JLabel("🔍 Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchLabel.setForeground(textPrimary);

        JTextField searchField = createStyledTextField(20);
        JButton searchButton = createStyledButton("Search", beautifulBlue);
        JButton showLowStockButton = createStyledButton("Show Low Stock", accentOrange);
        JButton refreshButton = createStyledButton("Refresh", accentGreen);

        searchPanel.add(searchLabel);
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
        styleTable(stockTable);
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
        styleScrollPane(stockScrollPane);
        stockScrollPane.setPreferredSize(new Dimension(800, 400));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        tablePanel.add(stockScrollPane, BorderLayout.CENTER);

        panel.add(tablePanel, BorderLayout.CENTER);

        // Create summary panel
        JPanel summaryPanel = createSummaryPanel();
        panel.add(summaryPanel, BorderLayout.SOUTH);

        // Add action listeners
        searchButton.addActionListener(e -> filterStock(searchField.getText()));
        showLowStockButton.addActionListener(e -> showLowStockItems());
        refreshButton.addActionListener(e -> refreshStockDisplay());

        return panel;
    }

    private JPanel createStockRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create request form
        JPanel formCard = createStyledCard();
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add title to form
        JLabel formTitle = new JLabel("📝 Request Stock from Headquarters");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(textPrimary);
        formTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Form title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        formCard.add(formTitle, gbc);

        // Item Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel itemNameLabel = createStyledLabel("Item Name:");
        formCard.add(itemNameLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        itemNameField = createStyledTextField(20);
        formCard.add(itemNameField, gbc);

        // Brand
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel brandLabel = createStyledLabel("Brand:");
        formCard.add(brandLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        brandField = createStyledTextField(20);
        formCard.add(brandField, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        JLabel quantityLabel = createStyledLabel("Quantity Needed:");
        formCard.add(quantityLabel, gbc);
        gbc.gridx = 1;
        quantityField = createStyledTextField(10);
        formCard.add(quantityField, gbc);

        // Priority
        gbc.gridx = 2;
        JLabel priorityLabel = createStyledLabel("Priority:");
        formCard.add(priorityLabel, gbc);
        gbc.gridx = 3;
        priorityComboBox = createStyledComboBox(new String[]{"Low", "Medium", "High", "Urgent"});
        formCard.add(priorityComboBox, gbc);

        // Reason
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel reasonLabel = createStyledLabel("Reason:");
        formCard.add(reasonLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        reasonField = createStyledTextField(30);
        formCard.add(reasonField, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 4;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);

        JButton submitButton = createStyledButton("✅ Submit Request", accentGreen);
        submitButton.addActionListener(new SubmitRequestListener());
        buttonPanel.add(submitButton);

        JButton clearButton = createStyledButton("🗑️ Clear Form", accentRed);
        clearButton.addActionListener(e -> clearRequestForm());
        buttonPanel.add(clearButton);

        JButton quickRequestButton = createStyledButton("⚡ Quick Request", beautifulBlue);
        quickRequestButton.addActionListener(new QuickRequestListener());
        buttonPanel.add(quickRequestButton);

        formCard.add(buttonPanel, gbc);

        panel.add(formCard, BorderLayout.NORTH);

        // Add styled instructions
        JPanel instructionsCard = createStyledCard();
        instructionsCard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        instructionsCard.setLayout(new BorderLayout());

        JLabel instructionsTitle = new JLabel("📖 Instructions");
        instructionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        instructionsTitle.setForeground(textPrimary);
        instructionsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JTextArea instructions = new JTextArea(
                "1. Fill in all required fields\n" +
                        "2. Select appropriate priority level\n" +
                        "3. Provide clear reason for stock request\n" +
                        "4. Use 'Quick Request' to auto-fill from current stock items\n" +
                        "5. Submit request for headquarters approval"
        );
        instructions.setEditable(false);
        instructions.setBackground(cardBackground);
        instructions.setForeground(textSecondary);
        instructions.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        instructions.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        instructionsCard.add(instructionsTitle, BorderLayout.NORTH);
        instructionsCard.add(instructions, BorderLayout.CENTER);

        panel.add(instructionsCard, BorderLayout.CENTER);

        return panel;
    }
    private JPanel createRequestHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create title panel
        JPanel titlePanel = createStyledTitlePanel("Request History", "📊");
        panel.add(titlePanel, BorderLayout.NORTH);

        // Create request history table
        String[] requestColumns = {"Request ID", "Item Name", "Brand", "Quantity", "Priority", "Status", "Date Requested", "Reason"};
        requestTableModel = new DefaultTableModel(requestColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestTable = new JTable(requestTableModel);
        styleTable(requestTable);
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
        styleScrollPane(requestScrollPane);
        requestScrollPane.setPreferredSize(new Dimension(800, 350));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        tablePanel.add(requestScrollPane, BorderLayout.CENTER);

        panel.add(tablePanel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlPanel.setOpaque(false);

        JButton refreshRequestsButton = createStyledButton("🔄 Refresh", accentGreen);
        JButton cancelRequestButton = createStyledButton("❌ Cancel Request", accentRed);

        controlPanel.add(refreshRequestsButton);
        controlPanel.add(cancelRequestButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        refreshRequestsButton.addActionListener(e -> refreshRequestHistory());
        cancelRequestButton.addActionListener(new CancelRequestListener());

        return panel;
    }
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

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Create styled summary card
        JPanel summaryCard = createStyledCard();
        summaryCard.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 15));

        JLabel summaryIcon = new JLabel("📊");
        summaryIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        totalItemsLabel = new JLabel("Total Items: 0");
        totalItemsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalItemsLabel.setForeground(textPrimary);

        lowStockLabel = new JLabel("Low Stock Items: 0");
        lowStockLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lowStockLabel.setForeground(accentOrange);

        summaryCard.add(summaryIcon);
        summaryCard.add(totalItemsLabel);
        summaryCard.add(lowStockLabel);

        panel.add(summaryCard, BorderLayout.CENTER);
        return panel;
    }
    private JPanel createStyledTitlePanel(String title, String icon) {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel(icon + " " + title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(textPrimary);

        titlePanel.add(titleLabel, BorderLayout.WEST);
        return titlePanel;
    }

    private JPanel createStyledCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(cardBackground);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2d.setColor(new Color(70, 70, 70));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return card;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(textPrimary);
        return label;
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(lightCharcoal);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        field.setBackground(lightCharcoal);
        field.setForeground(textPrimary);
        field.setCaretColor(textPrimary);
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setBackground(lightCharcoal);
        comboBox.setForeground(textPrimary);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return comboBox;
    }

    private void styleTable(JTable table) {
        table.setBackground(cardBackground);
        table.setForeground(textPrimary);
        table.setSelectionBackground(beautifulBlue);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(70, 70, 70));
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        // Style header
        JTableHeader header = table.getTableHeader();
        header.setBackground(lightCharcoal);
        header.setForeground(new Color(0,0,139));
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));

        // Custom cell renderer for better styling
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(beautifulBlue);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? cardBackground : new Color(58, 64, 76));
                    c.setForeground(textPrimary);

                    // Special coloring for status column
                    if (column == 7 && value != null) { // Status column
                        String status = value.toString();
                        if (status.equals("Out of Stock")) {
                            c.setForeground(accentRed);
                        } else if (status.equals("Low Stock")) {
                            c.setForeground(accentOrange);
                        } else if (status.equals("Normal")) {
                            c.setForeground(accentGreen);
                        }
                    }
                }

                setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBackground(cardBackground);
        scrollPane.getViewport().setBackground(cardBackground);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Style scrollbars
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = beautifulBlue;
                this.trackColor = lightCharcoal;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });

        scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = beautifulBlue;
                this.trackColor = lightCharcoal;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color buttonColor = baseColor;
                if (getModel().isPressed()) {
                    buttonColor = baseColor.darker();
                } else if (getModel().isRollover()) {
                    buttonColor = baseColor.brighter();
                }

                g2d.setColor(buttonColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                g2d.drawString(getText(),
                        (getWidth() - textWidth) / 2,
                        (getHeight() + textHeight) / 2 - 2);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(160, 40));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);

        return button;
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
    private void loadStockDataFromDatabase() {
        try {
            // Get all branches
            List<Branch> branches = dbManager.getAllBranches();

            for (Branch branch : branches) {
                int branchId = branch.getId();

                List<Map<String, Object>> stockItems = dbManager.getStockByBranch(branchId);

                for (Map<String, Object> stockItem : stockItems) {
                    int drinkId = (int) stockItem.get("drink_id");
                    int quantity = (int) stockItem.get("quantity");
                    int minThreshold = (int) stockItem.get("min_threshold");

                    // Get drink details
                    Drink drink = dbManager.getDrink(drinkId);
                    if (drink != null) {
                        
                        addStockItem(
                                "BR" + branchId + "-DR" + drinkId,
                                drink.getName(),
                                drink.getBrand(), // Brand
                                quantity, // Current quantity
                                minThreshold, // Min level
                                minThreshold * 2,
                                drink.getPrice().doubleValue() // Unit price
                        );
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading stock data from database: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


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