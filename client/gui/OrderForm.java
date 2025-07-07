package client.gui;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderForm extends JFrame{
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JLabel totalAmountLabel;
    private ArrayList<Order> orders;
    private DecimalFormat df = new DecimalFormat("#0.00");

    // Dark theme colors
    private final Color darkCharcoal = new Color(36, 39, 46);
    private final Color lightCharcoal = new Color(52, 58, 70);
    private final Color cardBackground = new Color(62, 68, 80);
    private final Color beautifulBlue = new Color(64, 149, 255);
    private final Color hoverBlue = new Color(85, 170, 255);
    private final Color selectedBlue = new Color(44, 129, 235);
    private final Color textPrimary = new Color(255, 255, 255);
    private final Color textSecondary = new Color(170, 178, 189);
    private final Color accentGreen = new Color(46, 204, 113);

    public OrderForm() {
        orders = new ArrayList<>();
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Order Display System");
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

        // Create table panel
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Create summary panel
        JPanel summaryPanel = createSummaryPanel();
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);

        add(mainPanel);

        addSampleOrders();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("📋 Order Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(textPrimary);

        JLabel subtitleLabel = new JLabel("Track and manage customer orders", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(textSecondary);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        // Create styled title panel
        JPanel titlePanel = createStyledTitlePanel("Orders Display", "📦");
        panel.add(titlePanel, BorderLayout.NORTH);

        String[] columnNames = {"Order ID", "Phone Number", "Customer Name", "Quantity", "Unit Price", "Total Amount"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderTable = new JTable(tableModel);
        styleTable(orderTable);

        // Set column widths
        orderTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        orderTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        orderTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        orderTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        orderTable.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        styleScrollPane(scrollPane);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add refresh button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JButton refreshButton = createStyledButton("🔄 Refresh Orders", beautifulBlue);
        refreshButton.addActionListener(e -> refreshOrderDisplay());
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Create styled summary card
        JPanel summaryCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(cardBackground);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2d.setColor(beautifulBlue);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
            }
        };
        summaryCard.setOpaque(false);
        summaryCard.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel summaryIcon = new JLabel("💰");
        summaryIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        summaryIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        totalAmountLabel = new JLabel("Total Orders Value: $0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalAmountLabel.setForeground(textPrimary);

        JLabel summarySubtext = new JLabel("Combined value of all orders");
        summarySubtext.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summarySubtext.setForeground(textSecondary);

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setOpaque(false);
        labelPanel.add(totalAmountLabel, BorderLayout.NORTH);
        labelPanel.add(summarySubtext, BorderLayout.CENTER);

        summaryCard.add(summaryIcon, BorderLayout.WEST);
        summaryCard.add(labelPanel, BorderLayout.CENTER);

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
        header.setForeground(textPrimary);
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
                    buttonColor = selectedBlue;
                } else if (getModel().isRollover()) {
                    buttonColor = hoverBlue;
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
        button.setPreferredSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);

        return button;
    }

    // Method to add order from external source
    public void addOrder(String phoneNumber, String customerName, int quantity, double unitPrice) {
        Order order = new Order(phoneNumber, customerName, quantity, unitPrice);
        orders.add(order);
        refreshOrderDisplay();
    }

    // Method to refresh the order display
    private void refreshOrderDisplay() {
        tableModel.setRowCount(0);

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
            new OrderForm();
        });
    }
}