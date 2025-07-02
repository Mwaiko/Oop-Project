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
    private Map<String, Double> drinkPrices;
    private String HqIpaddress;
    public Main_ui(Branch branch, String HqipAddress) {
        this.branch = branch;
        this.HqIpaddress = HqipAddress;
        this.clientService = new ClientService(branch.getName());
        boolean Connection = clientService.connectToHeadquarters("localhost", 5000);

        if (!Connection) {
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to headquarters server",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        viewInventory();
        initializeDrinkPrices();
        setupGUI();
    }

    public Main_ui(Branch branch) {
        this.branch = branch;
        this.HqIpaddress = "localhost";
        this.clientService = new ClientService(branch.getName());
        boolean Connection = clientService.connectToHeadquarters("localhost", 5000);

        if (!Connection) {
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to headquarters server",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        initializeDrinkPrices();
        viewInventory();
        setupGUI();
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
    private void viewInventory() {
        List<Drink> inventory = clientService.getCurrentInventory();
        if (inventory.isEmpty()) {
            System.out.println("No inventory data available.");
            return;
        }

        displayInventory(inventory);
    }
    private void initializeDrinkPrices() {
        drinkPrices = new HashMap<>();
        drinkPrices.put("Select Drink", 0.0);

        try {
            List<Drink> inventory = clientService.getCurrentInventory();
            System.out.println(inventory);
            for (Drink drink : inventory) {
                drinkPrices.put(drink.getName(), drink.getPrice().doubleValue());

            }
        } catch (Exception e) {

            System.err.println("Could not load inventory from server: " + e.getMessage());
            drinkPrices.put("Coffee", 2.50);
            drinkPrices.put("Tea", 2.00);
            drinkPrices.put("Soda", 1.50);
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
                    BorderFactory.createLineBorder(new Color(220, 220, 220)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            panel.setBackground(Color.WHITE);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Drink selection
            JLabel drinkLabel = new JLabel("Drink:");
            drinkLabel.setFont(new Font("Arial", Font.BOLD, 12));
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(drinkLabel, gbc);

            String[] drinks = drinkPrices.keySet().toArray(new String[0]);
            drinkComboBox = new JComboBox<>(drinks);
            drinkComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
            drinkComboBox.setPreferredSize(new Dimension(150, 30));
            drinkComboBox.setBackground(Color.WHITE);
            drinkComboBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

            drinkComboBox.addActionListener(e -> {
                updateUnitPriceForSelection(this);
                updateTotalPrice();
            });

            gbc.gridx = 1;
            gbc.gridy = 0;
            panel.add(drinkComboBox, gbc);

            // Unit price
            JLabel priceLabel = new JLabel("Price:");
            priceLabel.setFont(new Font("Arial", Font.BOLD, 12));
            gbc.gridx = 2;
            gbc.gridy = 0;
            panel.add(priceLabel, gbc);

            unitPriceLabel = new JLabel("$0.00");
            unitPriceLabel.setFont(new Font("Arial", Font.BOLD, 12));
            unitPriceLabel.setForeground(new Color(34, 139, 34));
            unitPriceLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            unitPriceLabel.setOpaque(true);
            unitPriceLabel.setBackground(new Color(248, 248, 248));
            gbc.gridx = 3;
            gbc.gridy = 0;
            panel.add(unitPriceLabel, gbc);

            // Quantity
            JLabel quantityLabel = new JLabel("Qty:");
            quantityLabel.setFont(new Font("Arial", Font.BOLD, 12));
            gbc.gridx = 4;
            gbc.gridy = 0;
            panel.add(quantityLabel, gbc);

            quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
            quantitySpinner.setFont(new Font("Arial", Font.PLAIN, 12));
            quantitySpinner.setPreferredSize(new Dimension(60, 30));
            ((JSpinner.DefaultEditor) quantitySpinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);

            quantitySpinner.addChangeListener(e -> updateTotalPrice());

            gbc.gridx = 5;
            gbc.gridy = 0;
            panel.add(quantitySpinner, gbc);

            // Remove button
            removeButton = new JButton("×");
            removeButton.setFont(new Font("Arial", Font.BOLD, 16));
            removeButton.setBackground(new Color(47, 250, 236));
            removeButton.setForeground(new Color(47, 250, 236));
            removeButton.setPreferredSize(new Dimension(30, 30));
            removeButton.setFocusPainted(false);
            removeButton.setBorder(BorderFactory.createEmptyBorder());
            removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            removeButton.setToolTipText("Remove this drink");

            removeButton.addActionListener(e -> removeDrinkSelection(this));

            gbc.gridx = 6;
            gbc.gridy = 0;
            panel.add(removeButton, gbc);

            // Initialize the unit price
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
            return drinkPrices.getOrDefault(selectedDrink, 0.0);
        }

        public double getTotalPrice() {
            return getUnitPrice() * getQuantity();
        }
    }

    private void setupGUI() {
        setTitle("Customer Order Form");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // Initialize drink selections list
        drinkSelections = new ArrayList<>();

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("Customer Order Form");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 51, 51));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(titleLabel, gbc);

        // Reset constraints
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 10, 5);

        // Customer Name
        JLabel nameLabel = new JLabel("Customer Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(nameLabel, gbc);

        nameField = new JTextField(20);
        nameField.setFont(new Font("Arial", Font.PLAIN, 14));
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(nameField, gbc);

        // Phone Number
        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(phoneLabel, gbc);

        phoneField = new JTextField(20);
        phoneField.setFont(new Font("Arial", Font.PLAIN, 14));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(phoneField, gbc);

        // Drinks Section Header
        JLabel drinksHeaderLabel = new JLabel("Select Drinks:");
        drinksHeaderLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(20, 5, 10, 5);
        mainPanel.add(drinksHeaderLabel, gbc);

        // Drink selection panel with scroll
        drinkSelectionPanel = new JPanel();
        drinkSelectionPanel.setLayout(new BoxLayout(drinkSelectionPanel, BoxLayout.Y_AXIS));
        drinkSelectionPanel.setBackground(Color.WHITE);

        drinkScrollPane = new JScrollPane(drinkSelectionPanel);
        drinkScrollPane.setPreferredSize(new Dimension(600, 200));
        drinkScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        drinkScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        drinkScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 10, 5);
        mainPanel.add(drinkScrollPane, gbc);

        // Add drink button
        JButton addDrinkButton = new JButton("+ Add Drink");
        addDrinkButton.setFont(new Font("Arial", Font.BOLD, 14));
        addDrinkButton.setBackground(new Color(40, 167, 69));
        addDrinkButton.setForeground(new Color(47, 250, 236));
        addDrinkButton.setPreferredSize(new Dimension(150, 35));
        addDrinkButton.setFocusPainted(false);
        addDrinkButton.setBorder(BorderFactory.createEmptyBorder());
        addDrinkButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        addDrinkButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addDrinkButton.setBackground(new Color(35, 145, 60));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                addDrinkButton.setBackground(new Color(40, 167, 69));
            }
        });

        addDrinkButton.addActionListener(e -> addDrinkSelection());

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 10, 5);
        mainPanel.add(addDrinkButton, gbc);

        // Total Price
        JLabel totalLabel = new JLabel("Total Price:");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 5, 10, 5);
        mainPanel.add(totalLabel, gbc);

        totalPriceLabel = new JLabel("$0.00");
        totalPriceLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalPriceLabel.setForeground(new Color(34, 139, 34));
        totalPriceLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        totalPriceLabel.setOpaque(true);
        totalPriceLabel.setBackground(new Color(248, 248, 248));
        gbc.gridx = 1;
        gbc.gridy = 6;
        mainPanel.add(totalPriceLabel, gbc);

        // Submit button
        submitButton = new JButton("Submit Order");
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setBackground(new Color(70, 130, 180));
        submitButton.setForeground(new Color(47, 250, 236));
        submitButton.setPreferredSize(new Dimension(200, 45));
        submitButton.setFocusPainted(false);
        submitButton.setBorder(BorderFactory.createEmptyBorder());
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        submitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                submitButton.setBackground(new Color(60, 110, 160));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                submitButton.setBackground(new Color(70, 130, 180));
            }
        });

        submitButton.addActionListener(e -> submitOrder());

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 5, 10, 5);
        mainPanel.add(submitButton, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Add the first drink selection by default
        addDrinkSelection();

        pack();
        setLocationRelativeTo(null);

        nameField.requestFocusInWindow();
    }

    public void showui() {

    }

    private void addDrinkSelection() {
        DrinkSelection selection = new DrinkSelection();
        drinkSelections.add(selection);

        drinkSelectionPanel.add(selection.panel);
        drinkSelectionPanel.add(Box.createVerticalStrut(10)); // Add spacing

        // Enable remove button only if there's more than one drink
        updateRemoveButtonStates();

        // Refresh the display
        drinkSelectionPanel.revalidate();
        drinkSelectionPanel.repaint();

        // Update total price
        updateTotalPrice();

        // Scroll to show the new drink selection
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = drinkScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void removeDrinkSelection(DrinkSelection selection) {
        if (drinkSelections.size() > 1) {
            drinkSelections.remove(selection);
            drinkSelectionPanel.remove(selection.panel);

            // Remove the spacing component if it exists
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
        double price = drinkPrices.getOrDefault(selectedDrink, 0.0);
        selection.unitPriceLabel.setText(String.format("$%.2f", price));
    }

    private void updateTotalPrice() {
        double total = 0.0;
        for (DrinkSelection selection : drinkSelections) {
            total += selection.getTotalPrice();
        }
        totalPriceLabel.setText(String.format("$%.2f", total));
    }

    // Updated submit order method to handle multiple drinks
    private void submitOrder() {
        String customerName = nameField.getText().trim();
        String phoneNumber = phoneField.getText().trim();

        if (customerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter customer name.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (phoneNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter phone number.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return;
        }

        if (drinkSelections.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one drink.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if connected to HQ
        if (!clientService.isConnected()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot submit order: Not connected to headquarters",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder orderSummary = new StringBuilder();
        orderSummary.append("Order Summary:\n\n");
        orderSummary.append("Customer: ").append(customerName).append("\n");
        orderSummary.append("Phone: ").append(phoneNumber).append("\n\n");
        orderSummary.append("Items:\n");

        double grandTotal = 0.0;
        for (int i = 0; i < drinkSelections.size(); i++) {
            DrinkSelection selection = drinkSelections.get(i);
            double itemTotal = selection.getTotalPrice();
            grandTotal += itemTotal;

            orderSummary.append(String.format("%d. %s x%d = $%.2f\n",
                    i + 1, selection.getSelectedDrink(), selection.getQuantity(), itemTotal));
        }

        orderSummary.append(String.format("\nTotal: $%.2f", grandTotal));

        int result = JOptionPane.showConfirmDialog(this, orderSummary.toString(),
                "Confirm Order", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            // Create and submit order
            Customer currentCustomer = new Customer(customerName, phoneNumber);
            Order currentOrder = new Order(currentCustomer, branch);

            // Add order items (you'll need to add items to the order based on selections)
            for (DrinkSelection selection : drinkSelections) {
                // This assumes you have a method to add items to order
                // You may need to modify your Order class to support this
            }

            boolean orderSubmitted = clientService.submitOrder(currentOrder);

            if (orderSubmitted) {
                // Clear the form for next order
                nameField.setText("");
                phoneField.setText("");

                // Reset to one drink selection
                drinkSelections.clear();
                drinkSelectionPanel.removeAll();
                addDrinkSelection();

                JOptionPane.showMessageDialog(this, "Order submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                nameField.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to submit order. Please try again.",
                        "Order Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void dispose() {
        if (clientService != null) {
            clientService.disconnect();
        }
        super.dispose();
    }
}