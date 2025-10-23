package com.GoAero.ui;

import com.GoAero.dao.BookingDAO;
import com.GoAero.dao.FlightDAO;
import com.GoAero.model.Booking;
import com.GoAero.model.Flight;
import com.GoAero.model.FlightOwner;
import com.GoAero.model.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Panel for flight owners to view booking statistics for their flights
 */
public class OwnerBookingStatsPanel extends JPanel {
    private FlightOwner currentOwner;
    private FlightDAO flightDAO;
    private BookingDAO bookingDAO;
    
    private JTable flightStatsTable;
    private DefaultTableModel tableModel;
    private JLabel totalFlightsLabel, totalBookingsLabel, totalRevenueLabel;
    private JLabel confirmedBookingsLabel, pendingBookingsLabel, cancelledBookingsLabel;
    private JButton refreshButton;

    public OwnerBookingStatsPanel() {
        currentOwner = SessionManager.getInstance().getCurrentFlightOwner();
        if (currentOwner == null) {
            JLabel errorLabel = new JLabel("Access denied. Please login as a flight owner.");
            add(errorLabel);
            return;
        }
        
        flightDAO = new FlightDAO();
        bookingDAO = new BookingDAO();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadStatistics();
    }

    private void initializeComponents() {
        // Summary labels
        totalFlightsLabel = new JLabel("0");
        totalBookingsLabel = new JLabel("0");
        totalRevenueLabel = new JLabel("₹0.00");
        confirmedBookingsLabel = new JLabel("0");
        pendingBookingsLabel = new JLabel("0");
        cancelledBookingsLabel = new JLabel("0");
        
        refreshButton = new JButton("Refresh Statistics");

        // Table setup for flight-wise statistics
        String[] columnNames = {"Flight Code", "Route", "Departure", "Capacity", "Bookings", "Available", "Occupancy %", "Revenue"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        flightStatsTable = new JTable(tableModel);
        flightStatsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        flightStatsTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Flight Code
        flightStatsTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Route
        flightStatsTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Departure
        flightStatsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Capacity
        flightStatsTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Bookings
        flightStatsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Available
        flightStatsTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Occupancy
        flightStatsTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Revenue
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Booking Statistics for " + currentOwner.getDisplayName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Summary panel
        JPanel summaryPanel = createSummaryPanel();
        contentPanel.add(summaryPanel, BorderLayout.NORTH);

        // Table panel
        JScrollPane scrollPane = new JScrollPane(flightStatsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Flight-wise Statistics"));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Summary Statistics"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // First row
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Total Flights:"), gbc);
        gbc.gridx = 1;
        totalFlightsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(totalFlightsLabel, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Total Bookings:"), gbc);
        gbc.gridx = 3;
        totalBookingsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(totalBookingsLabel, gbc);

        // Second row
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Confirmed Bookings:"), gbc);
        gbc.gridx = 1;
        confirmedBookingsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        confirmedBookingsLabel.setForeground(new Color(0, 128, 0));
        panel.add(confirmedBookingsLabel, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Pending Bookings:"), gbc);
        gbc.gridx = 3;
        pendingBookingsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        pendingBookingsLabel.setForeground(new Color(255, 140, 0));
        panel.add(pendingBookingsLabel, gbc);

        // Third row
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Cancelled Bookings:"), gbc);
        gbc.gridx = 1;
        cancelledBookingsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cancelledBookingsLabel.setForeground(Color.RED);
        panel.add(cancelledBookingsLabel, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Total Revenue:"), gbc);
        gbc.gridx = 3;
        totalRevenueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalRevenueLabel.setForeground(new Color(0, 128, 0));
        panel.add(totalRevenueLabel, gbc);

        return panel;
    }

    private void setupEventListeners() {
        refreshButton.addActionListener(e -> loadStatistics());
    }

    private void loadStatistics() {
        try {
            // Load flights for this owner
            List<Flight> flights = flightDAO.findByCompanyId(currentOwner.getOwnerId());
            
            // Load all bookings
            List<Booking> allBookings = bookingDAO.findAll();
            
            // Filter bookings for this owner's flights
            List<Integer> flightIds = flights.stream()
                .map(Flight::getFlightId)
                .collect(Collectors.toList());
            
            List<Booking> ownerBookings = allBookings.stream()
                .filter(booking -> flightIds.contains(booking.getFlightId()))
                .collect(Collectors.toList());

            // Update summary statistics
            updateSummaryStatistics(flights, ownerBookings);
            
            // Update flight-wise table
            updateFlightStatsTable(flights, ownerBookings);
            
        } catch (Exception e) {
            showError("Failed to load statistics: " + e.getMessage());
        }
    }

    private void updateSummaryStatistics(List<Flight> flights, List<Booking> bookings) {
        totalFlightsLabel.setText(String.valueOf(flights.size()));
        totalBookingsLabel.setText(String.valueOf(bookings.size()));

        int confirmed = 0, pending = 0, cancelled = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Booking booking : bookings) {
            switch (booking.getBookingStatus()) {
                case CONFIRMED:
                    confirmed++;
                    if (booking.getPaymentStatus() == Booking.PaymentStatus.COMPLETED) {
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
        totalRevenueLabel.setText(String.format("₹%.2f", totalRevenue));
    }

    private void updateFlightStatsTable(List<Flight> flights, List<Booking> allBookings) {
        // Clear existing data
        tableModel.setRowCount(0);

        // Group bookings by flight ID
        Map<Integer, List<Booking>> bookingsByFlight = allBookings.stream()
            .collect(Collectors.groupingBy(Booking::getFlightId));

        // Add flight statistics to table
        for (Flight flight : flights) {
            List<Booking> flightBookings = bookingsByFlight.getOrDefault(flight.getFlightId(), List.of());

            int confirmedBookings = (int) flightBookings.stream()
                .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CONFIRMED)
                .count();
            
            BigDecimal flightRevenue = flightBookings.stream()
                .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CONFIRMED && 
                           b.getPaymentStatus() == Booking.PaymentStatus.COMPLETED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            double occupancyRate = flight.getCapacity() > 0 ? 
                (double) confirmedBookings / flight.getCapacity() * 100 : 0;

            Object[] row = {
                flight.getFlightCode(),
                flight.getRoute(),
                flight.getDepartureTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                flight.getCapacity(),
                confirmedBookings,
                flight.getAvailableSeats(),
                String.format("%.1f%%", occupancyRate),
                String.format("₹%.2f", flightRevenue)
            };
            tableModel.addRow(row);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
