package com.GoAero.ui;

import com.GoAero.model.FlightOwner;
import com.GoAero.model.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main dashboard for flight owners (airline companies) with modern flight management interface
 */
public class FlightOwnerDashboard extends JFrame {
    // Professional color scheme (consistent with other pages)
    private static final Color PRIMARY_BLUE = new Color(25, 118, 210);
    private static final Color ACCENT_ORANGE = new Color(255, 152, 0);
    private static final Color DARK_BLUE = new Color(13, 71, 161);
    private static final Color LIGHT_GRAY = new Color(245, 245, 245);
    private static final Color HOVER_BLUE = new Color(30, 136, 229);
    private static final Color SUCCESS_GREEN = new Color(76, 175, 80);
    private static final Color BACKGROUND_GRAY = new Color(250, 250, 250);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color DANGER_RED = new Color(244, 67, 54);

    private FlightOwner currentFlightOwner;
    private JTabbedPane tabbedPane;
    private JLabel welcomeLabel, subtitleLabel;
    private JButton logoutButton;

    public FlightOwnerDashboard() {
        currentFlightOwner = SessionManager.getInstance().getCurrentFlightOwner();
        if (currentFlightOwner == null) {
            JOptionPane.showMessageDialog(null, "Flight owner access required.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
    }

    private void initializeComponents() {
        setTitle("GoAero - Airline Dashboard");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Modern welcome label
        welcomeLabel = new JLabel("Welcome, " + currentFlightOwner.getCompanyName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(DARK_BLUE);

        // Subtitle label with company code
        subtitleLabel = new JLabel("Airline Code: " + currentFlightOwner.getCompanyCode() + " | Flight Management Portal");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(100, 100, 100));

        // Modern styled logout button
        logoutButton = createStyledButton("🚪 Logout", DANGER_RED, Color.WHITE, 14);
        logoutButton.setPreferredSize(new Dimension(120, 40));

        // Modern styled tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(CARD_WHITE);
        tabbedPane.setForeground(DARK_BLUE);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add management panels with icons
        tabbedPane.addTab("✈ My Flights", new OwnerFlightManagementPanel());
        tabbedPane.addTab("📊 Booking Statistics", new OwnerBookingStatsPanel());
        tabbedPane.addTab("🏢 Company Profile", new OwnerProfilePanel());

        // Style individual tabs
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setBackgroundAt(i, LIGHT_GRAY);
            tabbedPane.setForegroundAt(i, DARK_BLUE);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_GRAY);

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_GRAY);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header section
        JPanel headerSection = createHeaderSection();
        mainPanel.add(headerSection, BorderLayout.NORTH);

        // Tabbed content section
        JPanel contentSection = createContentSection();
        mainPanel.add(contentSection, BorderLayout.CENTER);

        // Footer section
        JPanel footerSection = createFooterSection();
        mainPanel.add(footerSection, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderSection() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_GRAY);
        headerPanel.setBorder(new EmptyBorder(0, 0, 25, 0));

        // Title section
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(BACKGROUND_GRAY);

        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(welcomeLabel);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(subtitleLabel);

        // Logout button panel
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setBackground(BACKGROUND_GRAY);
        logoutPanel.add(logoutButton);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(logoutPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createContentSection() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(CARD_WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_GRAY, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Content title
        JLabel contentTitle = new JLabel("Airline Management");
        contentTitle.setFont(new Font("Arial", Font.BOLD, 18));
        contentTitle.setForeground(DARK_BLUE);
        contentTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        contentPanel.add(contentTitle, BorderLayout.NORTH);
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        return contentPanel;
    }

    private JPanel createFooterSection() {
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(BACKGROUND_GRAY);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel footerLabel = new JLabel("GoAero Flight Booking System - Airline Portal");
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel.setForeground(new Color(120, 120, 120));

        footerPanel.add(footerLabel);
        return footerPanel;
    }

    private void setupEventListeners() {
        logoutButton.addActionListener(e -> handleLogout());
    }

    /**
     * Creates a styled button with hover effects and modern design
     */
    private JButton createStyledButton(String text, Color bgColor, Color textColor, int fontSize) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        // Add hover effects
        Color originalBg = bgColor;
        Color hoverColor = createHoverColor(bgColor);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBg);
            }
        });

        return button;
    }

    /**
     * Creates a hover color that's slightly lighter than the original
     */
    private Color createHoverColor(Color originalColor) {
        if (originalColor.equals(DANGER_RED)) {
            return new Color(255, 87, 87);
        } else {
            // For other colors, create a lighter version
            int r = Math.min(255, originalColor.getRed() + 20);
            int g = Math.min(255, originalColor.getGreen() + 20);
            int b = Math.min(255, originalColor.getBlue() + 20);
            return new Color(r, g, b);
        }
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            JOptionPane.showMessageDialog(this, "You have been logged out successfully.", "Logout", JOptionPane.INFORMATION_MESSAGE);
            
            // Return to landing page
            SwingUtilities.invokeLater(() -> {
                new LandingPage().setVisible(true);
                dispose();
            });
        }
    }
}
