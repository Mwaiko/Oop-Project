package client.gui;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import common.models.Customer;
import common.models.Order;
import common.models.Branch;
import common.models.Drink;
import client.service.ClientService;

public class Main_ui extends JFrame {
    private ClientService clientService;
    private JTextField nameField;
    private JTextField phoneField;
    private JComboBox<String> drinkComboBox;
    private JLabel unitPriceLabel;
    private JSpinner quantitySpinner;
    private JButton submitButton;
    ArrayList<Map<String, String>> inventory;
    public Branch branch;
    private Map<String, Integer> drinkPrices = new HashMap<>();
    private String HqIpaddress;

    // Modern Color Palette
    private static final Color CHARCOAL_BLACK = new Color(33, 37, 41);
    private static final Color DARK_GRAY = new Color(52, 58, 64);
    private static final Color MEDIUM_GRAY = new Color(73, 80, 87);
    private static final Color LIGHT_GRAY = new Color(108, 117, 125);
    private static final Color ACCENT_BLUE = new Color(0, 123, 255);
    private static final Color ACCENT_BLUE_HOVER = new Color(0, 105, 217);
    private static final Color SUCCESS_GREEN = new Color(40, 167, 69);
    private static final Color SUCCESS_GREEN_HOVER = new Color(33, 136, 56);
    private static final Color DANGER_RED = new Color(220, 53, 69);
    private static final Color WHITE_TEXT = new Color(248, 249, 250);
    private static final Color LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color CARD_BACKGROUND = new Color(40, 44, 52);
    private static final Color INPUT_BACKGROUND = new Color(56, 61, 66);
    private static final Color BORDER_COLOR = new Color(73, 80, 87);

    public Main_ui(Branch branch, String HqipAddress) {
        this.branch = branch;
        this.clientService = new ClientService(branch.getName());
        drinkPrices.put("Select Drink", 0);

        setupGUI();
        setupListeners();

        boolean connected = clientService.connectToHeadquarters("localhost", 5000);
        if (!connected) {
            showStyledMessage("Failed to connect to headquarters server", "Connection Error", JOptionPane.ERROR_MESSAGE);
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(10);
                    initializeDrinkPrices();
                    if (clientService.getCurrentInventory().isEmpty()) {
                        System.out.println("Inventory still empty, requesting from server...");
                        clientService.requestInventory();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public Main_ui(Branch branch) {
        this.branch = branch;
        this.clientService = new ClientService(branch.getName());
        drinkPrices.put("Select Drink", 0);

        setupGUI();
        setupListeners();

        boolean connected = clientService.connectToHeadquarters("localhost", 5000);
        if (!connected) {
            showStyledMessage("Failed to connect to headquarters server", "Connection Error", JOptionPane.ERROR_MESSAGE);
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(10);
                    initializeDrinkPrices();
                    if (clientService.getCurrentInventory().isEmpty()) {
                        System.out.println("Inventory still empty, requesting from server...");
                        clientService.requestInventory();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    private void setupListeners() {
        clientService.addInventoryUpdateListener(drinks -> {
            SwingUtilities.invokeLater(() -> {
                System.out.println("Received inventory update with " + drinks.size() + " items");

                drinkPrices.clear();
                drinkPrices.put("Select Drink", 0);

                for (Drink drink : drinks) {
                    drinkPrices.put(drink.getName(), drink.getPrice().intValue());
                }

                updateDrinkComboBoxes();
                submitButton.setEnabled(!drinks.isEmpty());
                System.out.println("Inventory UI updated successfully");
            });
        });

        clientService.addOrderStatusListener(status -> {
            SwingUtilities.invokeLater(() -> {
                showStyledMessage("Order Status: " + status, "Order Update", JOptionPane.INFORMATION_MESSAGE);
            });
        });
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

    private void initializeDrinkPrices() {
        drinkPrices.put("Select Drink", 0);

        try {
            List<Drink> inventory = clientService.getCurrentInventory();
            if (inventory.isEmpty()) {
                System.out.println("Inventory is empty, waiting for server update...");
                return;
            }

            for (Drink drink : inventory) {
                drinkPrices.put(drink.getName(), drink.getPrice().intValue());
            }

            updateDrinkComboBoxes();
            System.out.println("Inventory loaded successfully: " + inventory.size() + " items");
        } catch (Exception e) {
            System.err.println("Could not load inventory from server: " + e.getMessage());
            showStyledMessage("Failed to load inventory. Please check connection.", "Inventory Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel drinkSelectionPanel;
    private JScrollPane drinkScrollPane;
    private JLabel totalPriceLabel;
    private java.util.List<DrinkSelection> drinkSelections;

    private class DrinkSelection {
        JComboBox<String> drinkComboBox;
        JLabel unitPriceLabel;
        JSpinner quantitySpinner;
        JButton removeButton;
        JPanel panel;

        public DrinkSelection() {
            panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            panel.setBackground(CARD_BACKGROUND);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.anchor = GridBagConstraints.WEST;

            // Drink selection
            JLabel drinkLabel = new JLabel("Drink:");
            drinkLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            drinkLabel.setForeground(WHITE_TEXT);
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(drinkLabel, gbc);

            String[] drinks = drinkPrices.keySet().toArray(new String[0]);
            drinkComboBox = new JComboBox<>(drinks);
            drinkComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            drinkComboBox.setPreferredSize(new Dimension(160, 35));
            drinkComboBox.setBackground(INPUT_BACKGROUND);
            drinkComboBox.setForeground(new Color(50, 177, 209));
            drinkComboBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

            // Style the dropdown arrow
            drinkComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setBackground(isSelected ? ACCENT_BLUE : INPUT_BACKGROUND);
                    setForeground(WHITE_TEXT);
                    setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                    return this;
                }
            });

            drinkComboBox.addActionListener(e -> {
                updateUnitPriceForSelection(this);
                updateTotalPrice();
            });

            gbc.gridx = 1;
            gbc.gridy = 0;
            panel.add(drinkComboBox, gbc);

            // Unit price
            JLabel priceLabel = new JLabel("Price:");
            priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            priceLabel.setForeground(WHITE_TEXT);
            gbc.gridx = 2;
            gbc.gridy = 0;
            panel.add(priceLabel, gbc);

            unitPriceLabel = new JLabel("$0.00");
            unitPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            unitPriceLabel.setForeground(LIGHT_BLUE);
            unitPriceLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            unitPriceLabel.setOpaque(true);
            unitPriceLabel.setBackground(INPUT_BACKGROUND);
            unitPriceLabel.setPreferredSize(new Dimension(80, 35));
            unitPriceLabel.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridx = 3;
            gbc.gridy = 0;
            panel.add(unitPriceLabel, gbc);

            // Quantity
            JLabel quantityLabel = new JLabel("Qty:");
            quantityLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            quantityLabel.setForeground(WHITE_TEXT);
            gbc.gridx = 4;
            gbc.gridy = 0;
            panel.add(quantityLabel, gbc);

            quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
            quantitySpinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            quantitySpinner.setPreferredSize(new Dimension(65, 35));

            // Style the spinner
            JComponent editor = quantitySpinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
                textField.setHorizontalAlignment(JTextField.CENTER);
                textField.setBackground(INPUT_BACKGROUND);
                textField.setForeground(WHITE_TEXT);
                textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            }

            quantitySpinner.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            quantitySpinner.addChangeListener(e -> updateTotalPrice());

            gbc.gridx = 5;
            gbc.gridy = 0;
            panel.add(quantitySpinner, gbc);

            // Remove button
            removeButton = new JButton("×");
            removeButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
            removeButton.setBackground(DANGER_RED);
            removeButton.setForeground(CHARCOAL_BLACK);
            removeButton.setPreferredSize(new Dimension(35, 35));
            removeButton.setFocusPainted(false);
            removeButton.setBorder(BorderFactory.createEmptyBorder());
            removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            removeButton.setToolTipText("Remove this drink");

            removeButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    removeButton.setBackground(DANGER_RED.darker());
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    removeButton.setBackground(DANGER_RED);
                }
            });

            removeButton.addActionListener(e -> removeDrinkSelection(this));

            gbc.gridx = 6;
            gbc.gridy = 0;
            panel.add(removeButton, gbc);

            updateUnitPriceForSelection(this);
        }

        public String getSelectedDrink() {
            return (String) drinkComboBox.getSelectedItem();
        }

        public int getQuantity() {
            return (Integer) quantitySpinner.getValue();
        }

        public double getUnitPrice() {
            String selectedDrink = getSelectedDrink();
            return drinkPrices.getOrDefault(selectedDrink, 0);
        }

        public double getTotalPrice() {
            return getUnitPrice() * getQuantity();
        }
    }

    private void setupGUI() {
        setTitle("Customer Order System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(true);

        // Set the frame background
        getContentPane().setBackground(CHARCOAL_BLACK);

        drinkSelections = new ArrayList<>();

        // Create main panel with gradient background
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, CHARCOAL_BLACK, 0, getHeight(), DARK_GRAY);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 8, 12, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Title with modern styling
        JLabel titleLabel = new JLabel("Customer Order System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(WHITE_TEXT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 40, 0);
        mainPanel.add(titleLabel, gbc);

        // Branch info
        JLabel branchLabel = new JLabel("Branch: " + branch.getName());
        branchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        branchLabel.setForeground(LIGHT_BLUE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(branchLabel, gbc);

        // Reset constraints
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(12, 8, 12, 8);

        // Customer Information Card
        JPanel customerPanel = createStyledCard("Customer Information");

        // Customer Name
        JLabel nameLabel = new JLabel("Customer Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(WHITE_TEXT);

        nameField = createStyledTextField();
        nameField.setPreferredSize(new Dimension(250, 40));

        customerPanel.add(nameLabel);
        customerPanel.add(Box.createHorizontalStrut(10));
        customerPanel.add(nameField);
        customerPanel.add(Box.createHorizontalStrut(30));

        // Phone Number
        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        phoneLabel.setForeground(WHITE_TEXT);

        phoneField = createStyledTextField();
        phoneField.setPreferredSize(new Dimension(250, 40));

        customerPanel.add(phoneLabel);
        customerPanel.add(Box.createHorizontalStrut(10));
        customerPanel.add(phoneField);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 20, 0);
        mainPanel.add(customerPanel, gbc);

        // Drinks Section
        JPanel drinksHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        drinksHeaderPanel.setOpaque(false);

        JLabel drinksHeaderLabel = new JLabel("Order Items");
        drinksHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        drinksHeaderLabel.setForeground(WHITE_TEXT);
        drinksHeaderPanel.add(drinksHeaderLabel);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        mainPanel.add(drinksHeaderPanel, gbc);

        // Drink selection panel
        drinkSelectionPanel = new JPanel();
        drinkSelectionPanel.setLayout(new BoxLayout(drinkSelectionPanel, BoxLayout.Y_AXIS));
        drinkSelectionPanel.setBackground(CHARCOAL_BLACK);

        drinkScrollPane = new JScrollPane(drinkSelectionPanel);
        drinkScrollPane.setPreferredSize(new Dimension(750, 250));
        drinkScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        drinkScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        drinkScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        drinkScrollPane.getViewport().setBackground(CHARCOAL_BLACK);

        // Style scrollbar
        drinkScrollPane.getVerticalScrollBar().setBackground(DARK_GRAY);
        drinkScrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = MEDIUM_GRAY;
                this.trackColor = DARK_GRAY;
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 0, 15, 0);
        mainPanel.add(drinkScrollPane, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);

        // Add drink button
        JButton addDrinkButton = createStyledButton("+ Add Drink", SUCCESS_GREEN, SUCCESS_GREEN_HOVER);
        addDrinkButton.setPreferredSize(new Dimension(150, 40));
        addDrinkButton.addActionListener(e -> addDrinkSelection());
        buttonPanel.add(addDrinkButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.insets = new Insets(5, 0, 15, 0);
        mainPanel.add(buttonPanel, gbc);

        // Total and Submit section
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        totalPanel.setOpaque(false);

        JLabel totalLabel = new JLabel("Total: ");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(WHITE_TEXT);

        totalPriceLabel = new JLabel("$0.00");
        totalPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalPriceLabel.setForeground(LIGHT_BLUE);
        totalPriceLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        totalPriceLabel.setOpaque(true);
        totalPriceLabel.setBackground(CARD_BACKGROUND);

        totalPanel.add(totalLabel);
        totalPanel.add(totalPriceLabel);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 20, 0);
        mainPanel.add(totalPanel, gbc);

        // Submit button
        submitButton = createStyledButton("Submit Order", ACCENT_BLUE, ACCENT_BLUE_HOVER);
        submitButton.setPreferredSize(new Dimension(200, 50));
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitButton.addActionListener(e -> submitOrder());

        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        submitPanel.setOpaque(false);
        submitPanel.add(submitButton);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 10, 0);
        mainPanel.add(submitPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        addDrinkSelection();
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        nameField.requestFocusInWindow();
    }

    private JPanel createStyledCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.X_AXIS));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return card;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(INPUT_BACKGROUND);
        field.setForeground(WHITE_TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setCaretColor(WHITE_TEXT);
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(LIGHT_BLUE); button.setForeground(CHARCOAL_BLACK);
        button.setForeground(CHARCOAL_BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_BLUE); button.setForeground(CHARCOAL_BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(LIGHT_BLUE); button.setForeground(CHARCOAL_BLACK);
            }
        });

        return button;
    }

    private void showStyledMessage(String message, String title, int messageType) {
        UIManager.put("OptionPane.background", CARD_BACKGROUND);
        UIManager.put("OptionPane.messageForeground", WHITE_TEXT);
        UIManager.put("Panel.background", CARD_BACKGROUND);
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    public void showui() {
        setVisible(true);
    }

    private void addDrinkSelection() {
        DrinkSelection selection = new DrinkSelection();
        drinkSelections.add(selection);

        drinkSelectionPanel.add(selection.panel);
        drinkSelectionPanel.add(Box.createVerticalStrut(10));

        updateRemoveButtonStates();
        drinkSelectionPanel.revalidate();
        drinkSelectionPanel.repaint();
        updateTotalPrice();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = drinkScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void removeDrinkSelection(DrinkSelection selection) {
        if (drinkSelections.size() > 1) {
            drinkSelections.remove(selection);
            drinkSelectionPanel.remove(selection.panel);

            Component[] components = drinkSelectionPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] instanceof Box.Filler) {
                    drinkSelectionPanel.remove(components[i]);
                    break;
                }
            }

            updateRemoveButtonStates();
            drinkSelectionPanel.revalidate();
            drinkSelectionPanel.repaint();
            updateTotalPrice();
        }
    }

    private void updateRemoveButtonStates() {
        boolean enableRemove = drinkSelections.size() > 1;
        for (DrinkSelection selection : drinkSelections) {
            selection.removeButton.setEnabled(enableRemove);
            selection.removeButton.setVisible(enableRemove);
        }
    }

    private void updateUnitPriceForSelection(DrinkSelection selection) {
        String selectedDrink = selection.getSelectedDrink();
        double price = drinkPrices.getOrDefault(selectedDrink, 0);
        selection.unitPriceLabel.setText(String.format("$%.2f", price));
    }

    private void updateTotalPrice() {
        double total = 0.0;
        for (DrinkSelection selection : drinkSelections) {
            total += selection.getTotalPrice();
        }
        totalPriceLabel.setText(String.format("$%.2f", total));
    }

    private void submitOrder() {
        String customerName = nameField.getText().trim();
        String phoneNumber = phoneField.getText().trim();

        if (customerName.isEmpty() || phoneNumber.isEmpty()) {
            showStyledMessage("Please enter customer name and phone number", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!clientService.isConnected()) {
            showStyledMessage("Cannot submit order: Not connected to headquarters", "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Customer customer = new Customer(customerName, phoneNumber);
        Order order = clientService.createOrder(customer, branch);

        for (DrinkSelection selection : drinkSelections) {
            String drinkName = selection.getSelectedDrink();
            if (!drinkName.equals("Select Drink")) {
                Drink drink = findDrinkByName(drinkName);
                if (drink != null) {
                    int quantity = selection.getQuantity();
                    order.addItem(drink, quantity);
                }
            }
        }

        if (order.getItems().isEmpty()) {
            showStyledMessage("Please add at least one valid item to the order", "Empty Order", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = clientService.submitOrder(order);
        if (success) {
            nameField.setText("");
            phoneField.setText("");
            drinkSelections.clear();
            drinkSelectionPanel.removeAll();
            addDrinkSelection();

            showStyledMessage("Order submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showStyledMessage("Failed to submit order. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Drink findDrinkByName(String name) {
        List<Drink> inventory = clientService.getCurrentInventory();
        return inventory.stream()
                .filter(d -> d.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private void updateDrinkComboBoxes() {
        String[] drinks = drinkPrices.keySet().toArray(new String[0]);

        for (DrinkSelection selection : drinkSelections) {
            String currentSelection = selection.getSelectedDrink();
            selection.drinkComboBox.setModel(new DefaultComboBoxModel<>(drinks));

            if (!currentSelection.equals("Select Drink")) {
                selection.drinkComboBox.setSelectedItem(currentSelection);
            }

            updateUnitPriceForSelection(selection);
        }

        updateTotalPrice();
    }

    @Override
    public void dispose() {
        if (clientService != null) {
            clientService.disconnect();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        // Set system look and feel properties for dark theme
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Set dark theme for dialogs
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            Branch branch = new Branch(1, "Nakuru", "Nakuru Branch", "localhost", 5000);
            Main_ui ui = new Main_ui(branch);
            ui.setVisible(true);
        });
    }
}
