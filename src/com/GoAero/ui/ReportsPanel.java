package com.GoAero.ui;

import com.GoAero.dao.BookingDAO;
import com.GoAero.dao.FlightDAO;
import com.GoAero.dao.UserDAO;
import com.GoAero.dao.FlightOwnerDAO;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Panel for displaying reports and analytics in the admin dashboard
 */
public class ReportsPanel extends JPanel {
    private UserDAO userDAO;
    private FlightDAO flightDAO;
    private FlightOwnerDAO flightOwnerDAO;
    private BookingDAO bookingDAO;
    
    private JLabel totalUsersLabel, totalFlightsLabel, totalAirlinesLabel, totalBookingsLabel;
    private JLabel totalRevenueLabel, pendingBookingsLabel, confirmedBookingsLabel, cancelledBookingsLabel;
    private JButton refreshButton, exportButton;

    public ReportsPanel() {
        userDAO = new UserDAO();
        flightDAO = new FlightDAO();
        flightOwnerDAO = new FlightOwnerDAO();
        bookingDAO = new BookingDAO();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadReports();
    }

    private void initializeComponents() {
        totalUsersLabel = new JLabel("0");
        totalFlightsLabel = new JLabel("0");
        totalAirlinesLabel = new JLabel("0");
        totalBookingsLabel = new JLabel("0");
        totalRevenueLabel = new JLabel("$0.00");
        pendingBookingsLabel = new JLabel("0");
        confirmedBookingsLabel = new JLabel("0");
        cancelledBookingsLabel = new JLabel("0");
        
        refreshButton = new JButton("Refresh Reports");
        exportButton = new JButton("Export to Text");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("System Reports & Analytics");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // System Overview Panel
        JPanel overviewPanel = createOverviewPanel();
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        contentPanel.add(overviewPanel, gbc);

        // Booking Statistics Panel
        JPanel bookingStatsPanel = createBookingStatsPanel();
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        contentPanel.add(bookingStatsPanel, gbc);

        // Revenue Panel
        JPanel revenuePanel = createRevenuePanel();
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        contentPanel.add(revenuePanel, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("System Overview"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Total Users
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Total Registered Users:"), gbc);
        gbc.gridx = 1;
        totalUsersLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(totalUsersLabel, gbc);

        // Total Airlines
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Total Airlines:"), gbc);
        gbc.gridx = 1;
        totalAirlinesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(totalAirlinesLabel, gbc);

        // Total Flights
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Total Flights:"), gbc);
        gbc.gridx = 1;
        totalFlightsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(totalFlightsLabel, gbc);

        // Total Bookings
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Total Bookings:"), gbc);
        gbc.gridx = 1;
        totalBookingsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(totalBookingsLabel, gbc);

        return panel;
    }

    private JPanel createBookingStatsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Booking Statistics"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Confirmed Bookings
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Confirmed Bookings:"), gbc);
        gbc.gridx = 1;
        confirmedBookingsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        confirmedBookingsLabel.setForeground(new Color(0, 128, 0));
        panel.add(confirmedBookingsLabel, gbc);

        // Pending Bookings
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Pending Bookings:"), gbc);
        gbc.gridx = 1;
        pendingBookingsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        pendingBookingsLabel.setForeground(new Color(255, 140, 0));
        panel.add(pendingBookingsLabel, gbc);

        // Cancelled Bookings
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Cancelled Bookings:"), gbc);
        gbc.gridx = 1;
        cancelledBookingsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cancelledBookingsLabel.setForeground(Color.RED);
        panel.add(cancelledBookingsLabel, gbc);

        return panel;
    }

    private JPanel createRevenuePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Revenue Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Total Revenue
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Total Revenue (Confirmed Bookings):"), gbc);
        gbc.gridx = 1;
        totalRevenueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalRevenueLabel.setForeground(new Color(0, 128, 0));
        panel.add(totalRevenueLabel, gbc);

        return panel;
    }

    private void setupEventListeners() {
        refreshButton.addActionListener(e -> loadReports());
        exportButton.addActionListener(e -> exportReports());
    }

    private void loadReports() {
        try {
            // Load basic counts
            long totalUsers = userDAO.count();
            long totalFlights = flightDAO.count();
            long totalAirlines = flightOwnerDAO.count();
            long totalBookings = bookingDAO.count();

            // Update labels
            totalUsersLabel.setText(String.valueOf(totalUsers));
            totalFlightsLabel.setText(String.valueOf(totalFlights));
            totalAirlinesLabel.setText(String.valueOf(totalAirlines));
            totalBookingsLabel.setText(String.valueOf(totalBookings));

            // Load booking statistics (this would require additional DAO methods)
            loadBookingStatistics();
            
        } catch (Exception e) {
            showError("Failed to load reports: " + e.getMessage());
        }
    }

    private void loadBookingStatistics() {
        try {
            // For now, we'll use simple counts
            // In a real implementation, you'd add methods to BookingDAO for these statistics
            int confirmed = 0, pending = 0, cancelled = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;

            // This is a simplified approach - in practice you'd add specific DAO methods
            var allBookings = bookingDAO.findAll();
            for (var booking : allBookings) {
                switch (booking.getBookingStatus()) {
                    case CONFIRMED:
                        confirmed++;
                        if (booking.getPaymentStatus() == com.GoAero.model.Booking.PaymentStatus.COMPLETED) {
                            totalRevenue = totalRevenue.add(booking.getAmount());
                        }
                        break;
                    case PENDING:
                        pending++;
                        break;
                    case CANCELLED:
                        cancelled++;
                        break;
                }
            }

            confirmedBookingsLabel.setText(String.valueOf(confirmed));
            pendingBookingsLabel.setText(String.valueOf(pending));
            cancelledBookingsLabel.setText(String.valueOf(cancelled));
            totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));

        } catch (Exception e) {
            showError("Failed to load booking statistics: " + e.getMessage());
        }
    }

    private void exportReports() {
        try {
            StringBuilder report = new StringBuilder();
            report.append("GoAero Flight Booking System - Reports\n");
            report.append("=====================================\n\n");
            
            report.append("System Overview:\n");
            report.append("- Total Registered Users: ").append(totalUsersLabel.getText()).append("\n");
            report.append("- Total Airlines: ").append(totalAirlinesLabel.getText()).append("\n");
            report.append("- Total Flights: ").append(totalFlightsLabel.getText()).append("\n");
            report.append("- Total Bookings: ").append(totalBookingsLabel.getText()).append("\n\n");
            
            report.append("Booking Statistics:\n");
            report.append("- Confirmed Bookings: ").append(confirmedBookingsLabel.getText()).append("\n");
            report.append("- Pending Bookings: ").append(pendingBookingsLabel.getText()).append("\n");
            report.append("- Cancelled Bookings: ").append(cancelledBookingsLabel.getText()).append("\n\n");
            
            report.append("Revenue Information:\n");
            report.append("- Total Revenue: ").append(totalRevenueLabel.getText()).append("\n");
            
            report.append("\nGenerated on: ").append(java.time.LocalDateTime.now().toString()).append("\n");

            // Show in a dialog
            JTextArea textArea = new JTextArea(report.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Exported Report", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            showError("Failed to export reports: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
