
package server;
import java.util.ArrayList;
import java.util.List;
import common.models.Branch;
import javax.swing.*;
import client.gui.Main_ui;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Initialize extends JFrame {
    private List<Branch> branches;
    private Branch selectedBranch;
    private JPanel branchPanel;
    private JButton confirmButton;
    private ButtonGroup branchButtonGroup;
    private HeadquartersServer headquartersServer;
    String hqIP;
    public Initialize() {
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
        
        // Header
        JLabel titleLabel = new JLabel("Select Your Branch", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Branch selection panel
        branchPanel = new JPanel(new GridLayout(0, 1, 0, 15));
        branchPanel.setBackground(new Color(245, 245, 245));
        
        branchButtonGroup = new ButtonGroup();
        createBranchCards();
        
        // Confirm button
        confirmButton = new JButton("Continue");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 16));
        confirmButton.setBackground(new Color(47, 250, 236));
        confirmButton.setForeground(new Color(47, 250, 236));
        confirmButton.setPreferredSize(new Dimension(200, 45));
        confirmButton.setFocusPainted(false);
        confirmButton.setBorder(BorderFactory.createEmptyBorder());
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmButton.setEnabled(false);
        
        confirmButton.addActionListener(e -> proceedWithSelection());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        buttonPanel.add(confirmButton);
        
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(branchPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void createBranchCards() {
        for (Branch branch : branches) {
            JPanel card = createBranchCard(branch);
            branchPanel.add(card);
        }
    }
    
    private JPanel createBranchCard(Branch branch) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(400, 70));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hidden radio button for selection tracking
        JRadioButton radioButton = new JRadioButton();
        radioButton.setVisible(false);
        branchButtonGroup.add(radioButton);
        
        // Branch info
        JLabel nameLabel = new JLabel(branch.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(new Color(51, 51, 51));
        
        JLabel detailsLabel = new JLabel(String.format("Port: %d", branch.getPort()));
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        detailsLabel.setForeground(new Color(102, 102, 102));
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(detailsLabel, BorderLayout.CENTER);
        
        card.add(infoPanel, BorderLayout.CENTER);
        
        // Click handling
        MouseAdapter clickHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectBranch(branch, card, radioButton);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectedBranch != branch) {
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                    ));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedBranch != branch) {
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
        // Clear previous selection
        clearPreviousSelection();
        
        // Set new selection
        selectedBranch = branch;
        radioButton.setSelected(true);
        
        // Highlight selected card
        card.setBackground(new Color(230, 244, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 3),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Enable continue button
        confirmButton.setEnabled(true);
    }
    
    private void clearPreviousSelection() {
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
    
    private void proceedWithSelection() {
        if (selectedBranch == null) {
            JOptionPane.showMessageDialog(this, "Please select a branch first.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (selectedBranch.getBranchType() == Branch.HEADQUARTERS) {
            // Start headquarters server and open Main_ui
            startHeadquartersAndOpenUI();
        } else {
            // Prompt for HQ server details and open Main_ui
            promptForHQDetailsAndOpenUI();
        }
    }
    
    private void startHeadquartersAndOpenUI() {
        try {
            // Start headquarters server
            headquartersServer = new HeadquartersServer();
            headquartersServer.startAsThread();
            
            JOptionPane.showMessageDialog(this, 
                "Headquarters Server started successfully on port 5000", 
                "Server Started", JOptionPane.INFORMATION_MESSAGE);
            
            // Close this window and open Main_ui
            this.dispose();
            new Main_ui(selectedBranch).setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to start Headquarters Server: " + e.getMessage(),
                "Server Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void promptForHQDetailsAndOpenUI() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Headquarters IP:"));
        JTextField ipField = new JTextField("localhost");
        panel.add(ipField);
        
        panel.add(new JLabel("Headquarters Port:"));
        JTextField portField = new JTextField("5000");
        panel.add(portField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Enter Headquarters Server Details", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            hqIP = ipField.getText().trim();
            String portText = portField.getText().trim();
            
            if (hqIP.isEmpty() || portText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", 
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                int hqPort = Integer.parseInt(portText);
                
                // Store HQ details in selected branch (or pass to Main_ui as needed)
                JOptionPane.showMessageDialog(this, 
                    String.format("Connecting to Headquarters at %s:%d", hqIP, hqPort),
                    "Connecting", JOptionPane.INFORMATION_MESSAGE);
                
                // Close this window and open Main_ui
                this.dispose();
                new Main_ui(selectedBranch,hqIP).setVisible(true);
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid port number.", 
                    "Invalid Port", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
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