package server;
import java.util.ArrayList;
import java.util.List;
import common.models.Branch;
import javax.swing.*;
import client.gui.Main_ui;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.plaf.basic.BasicScrollBarUI;

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

        // Dark theme colors
        Color darkCharcoal = new Color(36, 39, 46);
        Color lightCharcoal = new Color(52, 58, 70);
        Color beautifulBlue = new Color(64, 149, 255);
        Color hoverBlue = new Color(85, 170, 255);
        Color selectedBlue = new Color(44, 129, 235);
        Color textPrimary = new Color(255, 255, 255);
        Color textSecondary = new Color(170, 178, 189);

        // Set window background
        getContentPane().setBackground(darkCharcoal);

        // Main panel with gradient-like effect
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Subtle gradient background
                GradientPaint gradient = new GradientPaint(0, 0, darkCharcoal, 0, getHeight(), lightCharcoal);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Header with modern styling
        JLabel titleLabel = new JLabel("Select Your Branch", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(textPrimary);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));

        // Subtitle for better context
        JLabel subtitleLabel = new JLabel("Choose the branch you want to manage", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(textSecondary);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Branch selection panel with better spacing
        branchPanel = new JPanel(new GridLayout(0, 1, 0, 20));
        branchPanel.setOpaque(false);

        // Scroll pane for better UX with many branches
        JScrollPane scrollPane = new JScrollPane(branchPanel);
        scrollPane.setBackground(darkCharcoal);
        scrollPane.getViewport().setBackground(darkCharcoal);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Style the scrollbar
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = beautifulBlue;
                this.trackColor = lightCharcoal;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });

        branchButtonGroup = new ButtonGroup();
        createBranchCards();

        // Enhanced confirm button with modern design
        confirmButton = new JButton("Continue") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isEnabled()) {
                    if (getModel().isPressed()) {
                        g2d.setColor(selectedBlue);
                    } else if (getModel().isRollover()) {
                        g2d.setColor(hoverBlue);
                    } else {
                        g2d.setColor(beautifulBlue);
                    }
                } else {
                    g2d.setColor(new Color(70, 70, 70));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Button text
                g2d.setColor(isEnabled() ? Color.WHITE : new Color(120, 120, 120));
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                g2d.drawString(getText(),
                        (getWidth() - textWidth) / 2,
                        (getHeight() + textHeight) / 2 - 2);
            }
        };

        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        confirmButton.setPreferredSize(new Dimension(220, 50));
        confirmButton.setFocusPainted(false);
        confirmButton.setBorder(BorderFactory.createEmptyBorder());
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmButton.setEnabled(false);
        confirmButton.setContentAreaFilled(false);

        confirmButton.addActionListener(e -> proceedWithSelection());

        // Button panel with better spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        buttonPanel.add(confirmButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
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
        // Theme colors
        Color darkCharcoal = new Color(36, 39, 46);
        Color cardBackground = new Color(52, 58, 70);
        Color cardHover = new Color(62, 68, 80);
        Color cardSelected = new Color(44, 129, 235, 40);
        Color beautifulBlue = new Color(64, 149, 255);
        Color textPrimary = new Color(255, 255, 255);
        Color textSecondary = new Color(170, 178, 189);

        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card background with rounded corners
                if (selectedBranch == branch) {
                    g2d.setColor(cardSelected);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2d.setColor(beautifulBlue);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 16, 16);
                } else {
                    g2d.setColor(getBackground());
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2d.setColor(new Color(70, 70, 70));
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                }
            }
        };

        card.setPreferredSize(new Dimension(450, 85));
        card.setBackground(cardBackground);
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setOpaque(false);

        // Hidden radio button for selection tracking
        JRadioButton radioButton = new JRadioButton();
        radioButton.setVisible(false);
        branchButtonGroup.add(radioButton);

        // Branch icon (using Unicode symbol)
        JLabel iconLabel = new JLabel("🏢");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        // Branch info with better typography
        JLabel nameLabel = new JLabel(branch.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(textPrimary);

        JLabel detailsLabel = new JLabel(String.format("Port: %d • Ready to connect", branch.getPort()));
        detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detailsLabel.setForeground(textSecondary);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(detailsLabel, BorderLayout.CENTER);

        // Status indicator
        JLabel statusLabel = new JLabel("●");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        statusLabel.setForeground(new Color(46, 204, 113)); // Green for active
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(statusLabel, BorderLayout.EAST);

        // Enhanced click handling with smooth animations
        MouseAdapter clickHandler = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectBranch(branch, card, radioButton);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectedBranch != branch) {
                    card.setBackground(cardHover);
                    card.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedBranch != branch) {
                    card.setBackground(cardBackground);
                    card.repaint();
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

        // Repaint cards to show selection
        card.repaint();

        // Enable continue button with smooth transition
        confirmButton.setEnabled(true);

        // Add a subtle animation effect
        Timer timer = new Timer(50, null);
        timer.addActionListener(e -> {
            card.repaint();
            timer.stop();
        });
        timer.start();
    }

    private void clearPreviousSelection() {
        Component[] components = branchPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                comp.repaint();
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
            new Main_ui(selectedBranch,"localhost").setVisible(true);
            
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