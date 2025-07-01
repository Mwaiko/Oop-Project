package client;
import java.util.ArrayList;
import java.util.List;
import common.models.Branch;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Initialize extends JFrame {
    private List<Branch> branches;
    private Branch selectedBranch;
    private JPanel branchPanel;
    private JButton confirmButton;
    private ButtonGroup branchButtonGroup;
    
    // HQ IP Address components
    private JPanel hqConnectionPanel;
    private JTextField hqIpField;
    private JTextField hqPortField;
    private JLabel hqConnectionLabel;
    
    // Interface to handle branch selection callback
    public interface BranchSelectionListener {
        void onBranchSelected(Branch selectedBranch, String hqIpAddress, int hqPort);
    }
    
    private BranchSelectionListener selectionListener;
    
    public Initialize() {
        initializeBranches();
        setupGUI();
    }
    
    public Initialize(BranchSelectionListener listener) {
        this.selectionListener = listener;
        initializeBranches();
        setupGUI();
    }
    
    private void initializeBranches() {
        branches = new ArrayList<>();
        branches.add(new Branch(1, "Headquarters", Branch.HEADQUARTERS, "localhost", 5000));
        branches.add(new Branch(2, "Nakuru Branch", Branch.BRANCH_NAKURU, "localhost", 5001));
        branches.add(new Branch(3, "Mombasa Branch", Branch.BRANCH_MOMBASA, "localhost", 5002));
        branches.add(new Branch(4, "Kisumu Branch", Branch.BRANCH_KISUMU, "localhost", 5003));
    }
    
    private void setupGUI() {
        setTitle("Admin Branch Selection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        
        JLabel titleLabel = new JLabel("Select Your Branch", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(51, 51, 51));
        
        JLabel subtitleLabel = new JLabel("Choose the branch you want to manage", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(102, 102, 102));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Branch selection panel
        branchPanel = new JPanel(new GridLayout(0, 1, 0, 15));
        branchPanel.setBackground(new Color(245, 245, 245));
        branchPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        
        branchButtonGroup = new ButtonGroup();
        createBranchCards();
        
        // HQ Connection panel (initially hidden)
        createHqConnectionPanel();
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(245, 245, 245));
        
        confirmButton = new JButton("Continue to Dashboard");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 16));
        confirmButton.setBackground(new Color(70, 130, 180));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setPreferredSize(new Dimension(250, 50));
        confirmButton.setFocusPainted(false);
        confirmButton.setBorder(BorderFactory.createEmptyBorder());
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmButton.setEnabled(false); // Initially disabled
        
        // Hover effects for confirm button
        confirmButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (confirmButton.isEnabled()) {
                    confirmButton.setBackground(new Color(60, 110, 160));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (confirmButton.isEnabled()) {
                    confirmButton.setBackground(new Color(70, 130, 180));
                }
            }
        });
        
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                proceedWithSelectedBranch();
            }
        });
        
        buttonPanel.add(confirmButton);
        
        // Center panel to hold both branch panel and HQ connection panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(245, 245, 245));
        centerPanel.add(branchPanel, BorderLayout.CENTER);
        centerPanel.add(hqConnectionPanel, BorderLayout.SOUTH);
        
        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void createHqConnectionPanel() {
        hqConnectionPanel = new JPanel(new BorderLayout());
        hqConnectionPanel.setBackground(new Color(245, 245, 245));
        hqConnectionPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        hqConnectionPanel.setVisible(false); // Initially hidden
        
        // Header for HQ connection section
        hqConnectionLabel = new JLabel("Headquarters Connection Settings", SwingConstants.CENTER);
        hqConnectionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        hqConnectionLabel.setForeground(new Color(51, 51, 51));
        
        // Connection fields panel
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // IP Address field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel ipLabel = new JLabel("Headquarters IP Address:");
        ipLabel.setFont(new Font("Arial", Font.BOLD, 14));
        ipLabel.setForeground(new Color(51, 51, 51));
        fieldsPanel.add(ipLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        hqIpField = new JTextField("localhost", 15);
        hqIpField.setFont(new Font("Arial", Font.PLAIN, 14));
        hqIpField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        fieldsPanel.add(hqIpField, gbc);
        
        // Port field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel portLabel = new JLabel("Headquarters Port:");
        portLabel.setFont(new Font("Arial", Font.BOLD, 14));
        portLabel.setForeground(new Color(51, 51, 51));
        fieldsPanel.add(portLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        hqPortField = new JTextField("5000", 15);
        hqPortField.setFont(new Font("Arial", Font.PLAIN, 14));
        hqPortField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        fieldsPanel.add(hqPortField, gbc);
        
        // Test connection button
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        JButton testButton = new JButton("Test Connection");
        testButton.setFont(new Font("Arial", Font.PLAIN, 12));
        testButton.setBackground(new Color(40, 167, 69));
        testButton.setForeground(Color.WHITE);
        testButton.setPreferredSize(new Dimension(150, 35));
        testButton.setFocusPainted(false);
        testButton.setBorder(BorderFactory.createEmptyBorder());
        testButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        testButton.addActionListener(e -> testHqConnection());
        fieldsPanel.add(testButton, gbc);
        
        hqConnectionPanel.add(hqConnectionLabel, BorderLayout.NORTH);
        hqConnectionPanel.add(fieldsPanel, BorderLayout.CENTER);
    }
    
    private void testHqConnection() {
        String ip = hqIpField.getText().trim();
        String portText = hqPortField.getText().trim();
        
        if (ip.isEmpty() || portText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both IP address and port.",
                "Missing Information",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int port = Integer.parseInt(portText);
            
            // Here you would implement actual connection testing
            // For now, we'll simulate a test
            JOptionPane.showMessageDialog(this,
                String.format("Testing connection to %s:%d...\n(This is a simulation - implement actual connection test)", ip, port),
                "Connection Test",
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid port number.",
                "Invalid Port",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createBranchCards() {
        for (Branch branch : branches) {
            JPanel branchCard = createBranchCard(branch);
            branchPanel.add(branchCard);
        }
    }
    
    private JPanel createBranchCard(Branch branch) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(500, 80));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Radio button (hidden but functional)
        JRadioButton radioButton = new JRadioButton();
        radioButton.setOpaque(false);
        radioButton.setVisible(false);
        branchButtonGroup.add(radioButton);
        
        // Branch info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(branch.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(new Color(51, 51, 51));
        
        JLabel detailsLabel = new JLabel(String.format("ID: %d | %s:%d", 
            branch.getId(), branch.getHost(), branch.getPort()));
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        detailsLabel.setForeground(new Color(102, 102, 102));
        
        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(detailsLabel, BorderLayout.CENTER);
        
        // Status indicator
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setOpaque(false);
        
        JLabel statusIndicator = new JLabel("●");
        statusIndicator.setFont(new Font("Arial", Font.BOLD, 20));
        statusIndicator.setForeground(new Color(40, 167, 69)); // Green for available
        
        JLabel statusText = new JLabel("Available");
        statusText.setFont(new Font("Arial", Font.PLAIN, 12));
        statusText.setForeground(new Color(102, 102, 102));
        
        statusPanel.add(statusIndicator);
        statusPanel.add(statusText);
        
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(statusPanel, BorderLayout.EAST);
        
        // Click handlers
        MouseAdapter clickHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectBranch(branch, card, radioButton);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectedBranch != branch) {
                    card.setBackground(new Color(248, 249, 250));
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                    ));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedBranch != branch) {
                    card.setBackground(Color.WHITE);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                    ));
                }
            }
        };
        
        card.addMouseListener(clickHandler);
        
        return card;
    }
    
    private void selectBranch(Branch branch, JPanel card, JRadioButton radioButton) {
        // Clear previous selection styling
        if (selectedBranch != null) {
            Component[] components = branchPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    comp.setBackground(Color.WHITE);
                    ((JPanel) comp).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                    ));
                }
            }
        }
        
        // Set new selection
        selectedBranch = branch;
        radioButton.setSelected(true);
        
        // Style selected card
        card.setBackground(new Color(230, 244, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 3),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Show/hide HQ connection panel based on branch type
        if (branch.getBranchType() != Branch.HEADQUARTERS) {
            hqConnectionPanel.setVisible(true);
            hqConnectionLabel.setText("Headquarters Connection Settings for " + branch.getName());
        } else {
            hqConnectionPanel.setVisible(false);
        }
        
        // Repack the window to accommodate the new panel
        pack();
        setLocationRelativeTo(null);
        
        // Enable confirm button
        confirmButton.setEnabled(true);
        confirmButton.setBackground(new Color(70, 130, 180));
    }
    
    private void proceedWithSelectedBranch() {
        if (selectedBranch == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a branch to continue.", 
                "No Branch Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String hqIp = null;
        int hqPort = 0;
        
        // Validate HQ connection details for non-headquarters branches
        if (selectedBranch.getBranchType() != Branch.HEADQUARTERS) {
            hqIp = hqIpField.getText().trim();
            String portText = hqPortField.getText().trim();
            
            if (hqIp.isEmpty() || portText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please enter the Headquarters IP address and port.",
                    "Missing HQ Connection Details",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                hqPort = Integer.parseInt(portText);
                if (hqPort <= 0 || hqPort > 65535) {
                    throw new NumberFormatException("Port out of range");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Please enter a valid port number (1-65535).",
                    "Invalid Port Number",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Show confirmation dialog
        String confirmMessage;
        if (selectedBranch.getBranchType() == Branch.HEADQUARTERS) {
            confirmMessage = String.format("You selected: %s\nProceed to dashboard?", selectedBranch.getName());
        } else {
            confirmMessage = String.format("You selected: %s\nHeadquarters: %s:%d\nProceed to dashboard?", 
                selectedBranch.getName(), hqIp, hqPort);
        }
        
        int result = JOptionPane.showConfirmDialog(this,
            confirmMessage,
            "Confirm Branch Selection",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            // Close this window
            this.dispose();
            
            // Call listener if provided
            if (selectionListener != null) {
                selectionListener.onBranchSelected(selectedBranch, hqIp, hqPort);
            } else {
                // Default behavior - open branch-specific interface
                openBranchInterface(selectedBranch, hqIp, hqPort);
            }
        }
    }
    
    private void openBranchInterface(Branch branch, String hqIp, int hqPort) {
        SwingUtilities.invokeLater(() -> {
            switch (branch.getBranchType()) {
                case Branch.HEADQUARTERS:
                    openHeadquartersInterface(branch);
                    break;
                case Branch.BRANCH_NAKURU:
                case Branch.BRANCH_MOMBASA:
                case Branch.BRANCH_KISUMU:
                    openBranchInterface(branch, hqIp, hqPort);
                    break;
                default:
                    JOptionPane.showMessageDialog(null, 
                        "Branch interface not implemented for: " + branch.getName(),
                        "Not Implemented", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
    
    private void openHeadquartersInterface(Branch branch) {
        // Open headquarters-specific interface
        JOptionPane.showMessageDialog(null, 
            "Opening Headquarters Dashboard for: " + branch.getName(),
            "Headquarters Interface", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // TODO: Replace with actual headquarters interface
        // new HeadquartersMainFrame(branch).setVisible(true);
    }
    
    
    // Getter for selected branch
    public Branch getSelectedBranch() {
        return selectedBranch;
    }
    
    // Getters for HQ connection details
    public String getHqIpAddress() {
        return hqIpField != null ? hqIpField.getText().trim() : null;
    }
    
    public int getHqPort() {
        if (hqPortField != null) {
            try {
                return Integer.parseInt(hqPortField.getText().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    // Static method to show branch selection dialog
    public static Branch showBranchSelectionDialog() {
        final Branch[] selectedBranch = {null};
        final Object lock = new Object();
        
        SwingUtilities.invokeLater(() -> {
            new Initialize(new BranchSelectionListener() {
                @Override
                public void onBranchSelected(Branch branch, String hqIp, int hqPort) {
                    synchronized (lock) {
                        selectedBranch[0] = branch;
                        lock.notify();
                    }
                }
            });
        });
        
        // Wait for selection
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return selectedBranch[0];
    }
    
    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new Initialize();
        });
    }
}