package com.GoAero.ui;

import com.GoAero.model.Admin;
import com.GoAero.model.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Main dashboard for administrators with tabbed management panels
 */
public class AdminDashboard extends JFrame {
    private Admin currentAdmin;
    private JTabbedPane tabbedPane;
    private JLabel welcomeLabel;
    private JButton logoutButton;

    public AdminDashboard() {
        currentAdmin = SessionManager.getInstance().getCurrentAdmin();
        if (currentAdmin == null) {
            JOptionPane.showMessageDialog(null, "Admin access required.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
    }

    private void initializeComponents() {
        setTitle("GoAero - Admin Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        welcomeLabel = new JLabel("Welcome, Administrator " + currentAdmin.getUsername());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));

        tabbedPane = new JTabbedPane();
        
        // Add management panels
        tabbedPane.addTab("Users", new UserManagementPanel());
        tabbedPane.addTab("Airports", new AirportManagementPanel());
        tabbedPane.addTab("Flight Owners", new FlightOwnerManagementPanel());
        tabbedPane.addTab("Flights", new FlightManagementPanel());
        tabbedPane.addTab("Bookings", new BookingManagementPanel());
        tabbedPane.addTab("Reports", new ReportsPanel());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.setBackground(new Color(240, 240, 240));
        
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(new Color(240, 240, 240));
        rightPanel.add(logoutButton);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        add(tabbedPane, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel footerLabel = new JLabel("GoAero Flight Booking System - Administrative Portal");
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel.setForeground(Color.GRAY);
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        logoutButton.addActionListener(e -> handleLogout());
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
