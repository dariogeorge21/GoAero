package com.GoAero.ui;

import com.GoAero.dao.BookingDAO;
import com.GoAero.model.Booking;
import com.GoAero.model.SessionManager;
import com.GoAero.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Screen to display user's booking history
 */
public class BookingHistory extends JFrame {
    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private JButton viewDetailsButton, cancelBookingButton, refreshButton, closeButton;
    private BookingDAO bookingDAO;
    private User currentUser;
    private List<Booking> userBookings;

    public BookingHistory() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(null, "Please login first.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        bookingDAO = new BookingDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadBookings();
    }

    private void initializeComponents() {
        setTitle("GoAero - My Bookings");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Table setup
        String[] columnNames = {"PNR", "Flight Code", "Route", "Departure Date", "Status", "Payment", "Amount"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        bookingsTable = new JTable(tableModel);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        // Set column widths
        bookingsTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // PNR
        bookingsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Flight Code
        bookingsTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Route
        bookingsTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Date
        bookingsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Status
        bookingsTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Payment
        bookingsTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Amount

        viewDetailsButton = new JButton("View Details");
        cancelBookingButton = new JButton("Cancel Booking");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");

        updateButtonStates();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("My Booking History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel userLabel = new JLabel("Passenger: " + currentUser.getFullName());
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        headerPanel.add(userLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Bookings"));
        
        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(cancelBookingButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        viewDetailsButton.addActionListener(e -> viewBookingDetails());
        cancelBookingButton.addActionListener(e -> cancelSelectedBooking());
        refreshButton.addActionListener(e -> loadBookings());
        closeButton.addActionListener(e -> dispose());

        // Double-click to view details
        bookingsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewBookingDetails();
                }
            }
        });
    }

    private void loadBookings() {
        try {
            userBookings = bookingDAO.findByUserId(currentUser.getUserId());
            displayBookings();
        } catch (Exception e) {
            showError("Failed to load bookings: " + e.getMessage());
        }
    }

    private void displayBookings() {
        // Clear existing data
        tableModel.setRowCount(0);

        if (userBookings.isEmpty()) {
            showInfo("No bookings found. Book your first flight to see it here!");
            return;
        }

        // Add bookings to table
        for (Booking booking : userBookings) {
            Object[] row = {
                booking.getPnr(),
                booking.getFlightCode(),
                booking.getFullRoute(),
                booking.getDateOfDeparture().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                booking.getBookingStatus().getDisplayName(),
                booking.getPaymentStatus().getDisplayName(),
                String.format("₹%.2f", booking.getAmount())
            };
            tableModel.addRow(row);
        }

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = bookingsTable.getSelectedRow() != -1;
        viewDetailsButton.setEnabled(hasSelection);
        
        // Enable cancel button only for cancellable bookings
        boolean canCancel = false;
        if (hasSelection && userBookings != null) {
            int selectedRow = bookingsTable.getSelectedRow();
            if (selectedRow < userBookings.size()) {
                Booking selectedBooking = userBookings.get(selectedRow);
                canCancel = selectedBooking.isCancellable();
            }
        }
        cancelBookingButton.setEnabled(canCancel);
    }

    private void viewBookingDetails() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a booking to view details.");
            return;
        }

        Booking selectedBooking = userBookings.get(selectedRow);
        new BookingDetailsDialog(this, selectedBooking).setVisible(true);
    }

    private void cancelSelectedBooking() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a booking to cancel.");
            return;
        }

        Booking selectedBooking = userBookings.get(selectedRow);
        
        if (!selectedBooking.isCancellable()) {
            showError("This booking cannot be cancelled.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            String.format("Are you sure you want to cancel booking %s?\n\nFlight: %s\nRoute: %s\nDeparture: %s",
                selectedBooking.getPnr(),
                selectedBooking.getFlightCode(),
                selectedBooking.getFullRoute(),
                selectedBooking.getDateOfDeparture().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ),
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                boolean success = bookingDAO.updateBookingStatus(
                    selectedBooking.getBookingId(), 
                    Booking.BookingStatus.CANCELLED
                );
                
                if (success) {
                    showSuccess("Booking cancelled successfully.");
                    loadBookings(); // Refresh the list
                } else {
                    showError("Failed to cancel booking. Please try again.");
                }
            } catch (Exception e) {
                showError("Cancellation failed: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
