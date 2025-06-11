import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
public class Main_ui extends JFrame {
   
    private JTextField nameField;
    private JTextField phoneField;
    private JComboBox<String> drinkComboBox;
    private JLabel unitPriceLabel;
    private JSpinner quantitySpinner;
    private JButton submitButton;
    ArrayList<Map<String, String>> inventory;
    private Map<String, Double> drinkPrices;
    
    public Main_ui() {
        initializeDrinkPrices();
        setupGUI();
        
    }
    
    private void initializeDrinkPrices() {
        inventory = new FetchInventory().get();
        drinkPrices = new HashMap<>();
        drinkPrices.put("Select Drink", 0.0);
        for (Map<String, String> item : inventory) {
            drinkPrices.put((item.get("item_name")), Double.parseDouble(item.get("price")));
        }
        
    }
    
    private void setupGUI() {
        setTitle("Customer Order Form");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel titleLabel = new JLabel("Customer Order Form");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 51, 51));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
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
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(nameLabel, gbc);
        
        nameField = new JTextField(20);
        nameField.setFont(new Font("Arial", Font.PLAIN, 14));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1; gbc.gridy = 1;
        mainPanel.add(nameField, gbc);
        
        // Phone Number
        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(phoneLabel, gbc);
        
        phoneField = new JTextField(20);
        phoneField.setFont(new Font("Arial", Font.PLAIN, 14));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(phoneField, gbc);
        
        // Drink Selection
        JLabel drinkLabel = new JLabel("Select Drink:");
        drinkLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(drinkLabel, gbc);
        
        String[] drinks = drinkPrices.keySet().toArray(new String[0]);
        drinkComboBox = new JComboBox<>(drinks);
        drinkComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        drinkComboBox.setPreferredSize(new Dimension(200, 35));
        drinkComboBox.setBackground(Color.WHITE);
        drinkComboBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        // Add action listener for drink selection
        drinkComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateUnitPrice();
            }
        });
        
        gbc.gridx = 1; gbc.gridy = 3;
        mainPanel.add(drinkComboBox, gbc);
        
        // Unit Price Display
        JLabel priceLabel = new JLabel("Unit Price:");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(priceLabel, gbc);
        
        unitPriceLabel = new JLabel("$0.00");
        unitPriceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        unitPriceLabel.setForeground(new Color(34, 139, 34));
        unitPriceLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        unitPriceLabel.setOpaque(true);
        unitPriceLabel.setBackground(new Color(248, 248, 248));
        gbc.gridx = 1; gbc.gridy = 4;
        mainPanel.add(unitPriceLabel, gbc);
        
        // Quantity
        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(quantityLabel, gbc);
        
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        quantitySpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        quantitySpinner.setPreferredSize(new Dimension(80, 35));
        ((JSpinner.DefaultEditor) quantitySpinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        gbc.gridx = 1; gbc.gridy = 5;
        mainPanel.add(quantitySpinner, gbc);
        
        // Submit Button
        submitButton = new JButton("Submit Order");
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setBackground(new Color(70, 130, 180));
        submitButton.setForeground(Color.blue);
        submitButton.setPreferredSize(new Dimension(200, 45));
        submitButton.setFocusPainted(false);
        submitButton.setBorder(BorderFactory.createEmptyBorder());
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        submitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                submitButton.setBackground(new Color(60, 110, 160));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                submitButton.setBackground(new Color(70, 130, 180));
            }
        });
        
        // Add action listener for submit button
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitOrder();
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 5, 10, 5);
        mainPanel.add(submitButton, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Set window properties
        pack();
        setLocationRelativeTo(null);
        
        // Set initial focus
        nameField.requestFocusInWindow();
    }
    
    private void updateUnitPrice() {
        String selectedDrink = (String) drinkComboBox.getSelectedItem();
        double price = drinkPrices.get(selectedDrink);
        unitPriceLabel.setText(String.format("$%.2f", price));
    }
    
    private void submitOrder() {
        // Validate input
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String drink = (String) drinkComboBox.getSelectedItem();
        int quantity = (Integer) quantitySpinner.getValue();
        
        if (name.isEmpty()) {
            showError("Please enter customer name.");
            nameField.requestFocus();
            return;
        }
        
        if (phone.isEmpty()) {
            showError("Please enter phone number.");
            phoneField.requestFocus();
            return;
        }
        
        if (drink.equals("Select a drink...") || drink == null) {
            showError("Please select a drink.");
            drinkComboBox.requestFocus();
            return;
        }
        
        // Calculate total
        double unitPrice = drinkPrices.get(drink);
        double totalPrice = unitPrice * quantity;
        
        // Show order confirmation
        String orderSummary = String.format(
            "Order Confirmation\n\n" +
            "Customer: %s\n" +
            "Phone: %s\n" +
            "Drink: %s\n" +
            "Unit Price: $%.2f\n" +
            "Quantity: %d\n" +
            "Total Price: $%.2f\n\n" +
            "Order submitted successfully!",
            name, phone, drink, unitPrice, quantity, totalPrice
        );
        
        JOptionPane.showMessageDialog(this, orderSummary, "Order Confirmation", 
                                    JOptionPane.INFORMATION_MESSAGE);
        
        // Reset form
        resetForm();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", 
                                    JOptionPane.ERROR_MESSAGE);
    }
    
    private void resetForm() {
        nameField.setText("");
        phoneField.setText("");
        drinkComboBox.setSelectedIndex(0);
        quantitySpinner.setValue(1);
        unitPriceLabel.setText("$0.00");
        nameField.requestFocusInWindow();
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Main_ui().setVisible(true);
            }
        });
    }
}