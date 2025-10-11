package com.GoAero.ui;

import com.GoAero.model.SessionManager;
import com.GoAero.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main dashboard for logged-in passengers with modern UI design
 */
public class UserDashboard extends JFrame {
    // Professional color scheme (consistent with LandingPage)
    private static final Color PRIMARY_BLUE = new Color(25, 118, 210);
    private static final Color ACCENT_ORANGE = new Color(255, 152, 0);
    private static final Color DARK_BLUE = new Color(13, 71, 161);
    private static final Color LIGHT_GRAY = new Color(245, 245, 245);
    private static final Color HOVER_BLUE = new Color(30, 136, 229);
    private static final Color SUCCESS_GREEN = new Color(76, 175, 80);
    private static final Color BACKGROUND_GRAY = new Color(250, 250, 250);

    private JLabel welcomeLabel, subtitleLabel;
    private JButton searchFlightsButton, viewBookingsButton, profileButton, logoutButton;
    private User currentUser;

    public UserDashboard() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(null, "Please login first.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initializeComponents();
        setupLayout();
        setupEventListeners();
    }

    private void initializeComponents() {
        setTitle("GoAero - Passenger Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Welcome label with modern styling
        welcomeLabel = new JLabel("Welcome back, " + currentUser.getFullName() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeLabel.setForeground(DARK_BLUE);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Subtitle label
        subtitleLabel = new JLabel("What would you like to do today?");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create styled buttons with icons and modern design
        searchFlightsButton = createStyledButton("✈ Search & Book Flights", PRIMARY_BLUE, Color.WHITE, 18);
        searchFlightsButton.setPreferredSize(new Dimension(320, 70));
        searchFlightsButton.setMaximumSize(new Dimension(320, 70));

        viewBookingsButton = createStyledButton("📋 My Bookings", ACCENT_ORANGE, Color.WHITE, 18);
        viewBookingsButton.setPreferredSize(new Dimension(320, 70));
        viewBookingsButton.setMaximumSize(new Dimension(320, 70));

        profileButton = createStyledButton("👤 My Profile", SUCCESS_GREEN, Color.WHITE, 18);
        profileButton.setPreferredSize(new Dimension(320, 70));
        profileButton.setMaximumSize(new Dimension(320, 70));

        logoutButton = createStyledButton("🚪 Logout", new Color(244, 67, 54), Color.WHITE, 14);
        logoutButton.setPreferredSize(new Dimension(120, 45));
        logoutButton.setMaximumSize(new Dimension(120, 45));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_GRAY);

        // Create main content panel with modern styling
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBackground(BACKGROUND_GRAY);
        mainContentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header section with welcome message and logout
        JPanel headerSection = createHeaderSection();
        mainContentPanel.add(headerSection, BorderLayout.NORTH);

        // Main dashboard content
        JPanel dashboardContent = createDashboardContent();
        mainContentPanel.add(dashboardContent, BorderLayout.CENTER);

        // Footer section
        JPanel footerSection = createFooterSection();
        mainContentPanel.add(footerSection, BorderLayout.SOUTH);

        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderSection() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_GRAY);
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Welcome section
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(BACKGROUND_GRAY);

        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createVerticalStrut(8));
        welcomePanel.add(subtitleLabel);

        // Logout button panel
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setBackground(BACKGROUND_GRAY);
        logoutPanel.add(logoutButton);

        headerPanel.add(welcomePanel, BorderLayout.CENTER);
        headerPanel.add(logoutPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createDashboardContent() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(BACKGROUND_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.anchor = GridBagConstraints.CENTER;

        // Search Flights button (primary action)
        gbc.gridx = 0; gbc.gridy = 0;
        searchFlightsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(searchFlightsButton, gbc);

        // View Bookings button
        gbc.gridy = 1;
        viewBookingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(viewBookingsButton, gbc);

        // Profile button
        gbc.gridy = 2;
        profileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(profileButton, gbc);

        return contentPanel;
    }

    private JPanel createFooterSection() {
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(BACKGROUND_GRAY);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel footerLabel = new JLabel("GoAero Flight Booking System - Passenger Portal");
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel.setForeground(new Color(120, 120, 120));

        footerPanel.add(footerLabel);
        return footerPanel;
    }

    private void setupEventListeners() {
        searchFlightsButton.addActionListener(e -> openSearchFlights());
        viewBookingsButton.addActionListener(e -> openBookingHistory());
        profileButton.addActionListener(e -> openProfile());
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
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
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
        if (originalColor.equals(PRIMARY_BLUE)) {
            return HOVER_BLUE;
        } else if (originalColor.equals(ACCENT_ORANGE)) {
            return new Color(255, 167, 38);
        } else if (originalColor.equals(SUCCESS_GREEN)) {
            return new Color(102, 187, 106);
        } else {
            // For other colors, create a lighter version
            int r = Math.min(255, originalColor.getRed() + 20);
            int g = Math.min(255, originalColor.getGreen() + 20);
            int b = Math.min(255, originalColor.getBlue() + 20);
            return new Color(r, g, b);
        }
    }

    private void openSearchFlights() {
        SwingUtilities.invokeLater(() -> {
            new SearchFlights().setVisible(true);
            this.setVisible(false);
        });
    }

    private void openBookingHistory() {
        SwingUtilities.invokeLater(() -> {
            new BookingHistory().setVisible(true);
        });
    }

    private void openProfile() {
        SwingUtilities.invokeLater(() -> {
            new UserProfileDialog(this).setVisible(true);
        });
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
