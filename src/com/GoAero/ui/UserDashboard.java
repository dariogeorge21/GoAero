package com.GoAero.ui;

import com.GoAero.model.SessionManager;
import com.GoAero.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main dashboard for logged-in passengers
 */
public class UserDashboard extends JFrame {
    private JLabel welcomeLabel;
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
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        searchFlightsButton = new JButton("Search & Book Flights");
        searchFlightsButton.setFont(new Font("Arial", Font.PLAIN, 16));
        searchFlightsButton.setPreferredSize(new Dimension(250, 60));

        viewBookingsButton = new JButton("My Bookings");
        viewBookingsButton.setFont(new Font("Arial", Font.PLAIN, 16));
        viewBookingsButton.setPreferredSize(new Dimension(250, 60));

        profileButton = new JButton("My Profile");
        profileButton.setFont(new Font("Arial", Font.PLAIN, 16));
        profileButton.setPreferredSize(new Dimension(250, 60));

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutButton.setPreferredSize(new Dimension(100, 40));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);
        
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.add(logoutButton);
        headerPanel.add(logoutPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;

        // Search Flights button
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(searchFlightsButton, gbc);

        // View Bookings button
        gbc.gridy = 1;
        mainPanel.add(viewBookingsButton, gbc);

        // Profile button
        gbc.gridy = 2;
        mainPanel.add(profileButton, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Footer panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel footerLabel = new JLabel("GoAero Flight Booking System - Passenger Portal");
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerLabel.setForeground(Color.GRAY);
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        searchFlightsButton.addActionListener(e -> openSearchFlights());
        viewBookingsButton.addActionListener(e -> openBookingHistory());
        profileButton.addActionListener(e -> openProfile());
        logoutButton.addActionListener(e -> handleLogout());
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
