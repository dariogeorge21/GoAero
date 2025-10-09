package com.GoAero.ui;

import com.GoAero.dao.BookingDAO;
import com.GoAero.model.Booking;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing bookings in the admin dashboard
 */
public class BookingManagementPanel extends JPanel {
    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private JButton viewDetailsButton, updateStatusButton, updatePaymentButton, refreshButton;
    private JTextField searchField;
    private JButton searchButton;
    private BookingDAO bookingDAO;
    private List<Booking> bookings;

    public BookingManagementPanel() {
        bookingDAO = new BookingDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadBookings();
    }

    private void initializeComponents() {
        // Table setup
        String[] columnNames = {"ID", "PNR", "Passenger", "Flight", "Route", "Date", "Amount", "Payment", "Status"};
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
        bookingsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        bookingsTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // PNR
        bookingsTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Passenger
        bookingsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Flight
        bookingsTable.getColumnModel().getColumn(4).setPreferredWidth(200); // Route
        bookingsTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Date
        bookingsTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Amount
        bookingsTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Payment
        bookingsTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Status

        // Buttons
        viewDetailsButton = new JButton("View Details");
        updateStatusButton = new JButton("Update Status");
        updatePaymentButton = new JButton("Update Payment");
        refreshButton = new JButton("Refresh");

        // Search components
        searchField = new JTextField(20);
        searchButton = new JButton("Search");

        updateButtonStates();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel with search and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search (PNR/Name):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        topPanel.add(searchPanel, BorderLayout.WEST);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(updateStatusButton);
        buttonPanel.add(updatePaymentButton);
        buttonPanel.add(refreshButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table panel
        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Bookings"));
        add(scrollPane, BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("Total Bookings: ");
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        viewDetailsButton.addActionListener(e -> viewBookingDetails());
        updateStatusButton.addActionListener(e -> updateBookingStatus());
        updatePaymentButton.addActionListener(e -> updatePaymentStatus());
        refreshButton.addActionListener(e -> loadBookings());
        searchButton.addActionListener(e -> searchBookings());
        
        // Enter key on search field
        searchField.addActionListener(e -> searchBookings());

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
            bookings = bookingDAO.findAll();
            displayBookings(bookings);
            updateInfoPanel();
        } catch (Exception e) {
            showError("Failed to load bookings: " + e.getMessage());
        }
    }

    private void displayBookings(List<Booking> bookingList) {
        // Clear existing data
        tableModel.setRowCount(0);

        // Add bookings to table
        for (Booking booking : bookingList) {
            Object[] row = {
                booking.getBookingId(),
                booking.getPnr(),
                booking.getUserFullName() != null ? booking.getUserFullName() : "N/A",
                booking.getFlightCode(),
                booking.getFullRoute(),
                booking.getDateOfDeparture().format(DateTimeFormatter.ofPattern("MM-dd")),
                String.format("$%.2f", booking.getAmount()),
                booking.getPaymentStatus().getDisplayName(),
                booking.getBookingStatus().getDisplayName()
            };
            tableModel.addRow(row);
        }

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = bookingsTable.getSelectedRow() != -1;
        viewDetailsButton.setEnabled(hasSelection);
        updateStatusButton.setEnabled(hasSelection);
        updatePaymentButton.setEnabled(hasSelection);
    }

    private void updateInfoPanel() {
        // Update the info label in the south panel
        Component[] components = ((JPanel) getComponent(2)).getComponents();
        if (components.length > 0 && components[0] instanceof JLabel) {
            ((JLabel) components[0]).setText("Total Bookings: " + (bookings != null ? bookings.size() : 0));
        }
    }

    private void viewBookingDetails() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a booking to view details.");
            return;
        }

        Booking selectedBooking = bookings.get(selectedRow);
        new BookingDetailsDialog((Frame) SwingUtilities.getWindowAncestor(this), selectedBooking).setVisible(true);
    }

    private void updateBookingStatus() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a booking to update status.");
            return;
        }

        Booking selectedBooking = bookings.get(selectedRow);
        
        Booking.BookingStatus[] statuses = Booking.BookingStatus.values();
        String[] statusNames = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            statusNames[i] = statuses[i].getDisplayName();
        }

        String selectedStatus = (String) JOptionPane.showInputDialog(
            this,
            "Select new booking status for PNR: " + selectedBooking.getPnr(),
            "Update Booking Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statusNames,
            selectedBooking.getBookingStatus().getDisplayName()
        );

        if (selectedStatus != null) {
            // Find the corresponding enum value
            Booking.BookingStatus newStatus = null;
            for (Booking.BookingStatus status : statuses) {
                if (status.getDisplayName().equals(selectedStatus)) {
                    newStatus = status;
                    break;
                }
            }

            if (newStatus != null) {
                try {
                    boolean success = bookingDAO.updateBookingStatus(selectedBooking.getBookingId(), newStatus);
                    if (success) {
                        showSuccess("Booking status updated successfully.");
                        loadBookings();
                    } else {
                        showError("Failed to update booking status.");
                    }
                } catch (Exception e) {
                    showError("Update failed: " + e.getMessage());
                }
            }
        }
    }

    private void updatePaymentStatus() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a booking to update payment status.");
            return;
        }

        Booking selectedBooking = bookings.get(selectedRow);
        
        Booking.PaymentStatus[] statuses = Booking.PaymentStatus.values();
        String[] statusNames = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            statusNames[i] = statuses[i].getDisplayName();
        }

        String selectedStatus = (String) JOptionPane.showInputDialog(
            this,
            "Select new payment status for PNR: " + selectedBooking.getPnr(),
            "Update Payment Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statusNames,
            selectedBooking.getPaymentStatus().getDisplayName()
        );

        if (selectedStatus != null) {
            // Find the corresponding enum value
            Booking.PaymentStatus newStatus = null;
            for (Booking.PaymentStatus status : statuses) {
                if (status.getDisplayName().equals(selectedStatus)) {
                    newStatus = status;
                    break;
                }
            }

            if (newStatus != null) {
                try {
                    boolean success = bookingDAO.updatePaymentStatus(selectedBooking.getBookingId(), newStatus);
                    if (success) {
                        showSuccess("Payment status updated successfully.");
                        loadBookings();
                    } else {
                        showError("Failed to update payment status.");
                    }
                } catch (Exception e) {
                    showError("Update failed: " + e.getMessage());
                }
            }
        }
    }

    private void searchBookings() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            displayBookings(bookings);
            return;
        }

        // Filter bookings based on search term
        List<Booking> filteredBookings = bookings.stream()
            .filter(booking ->
                booking.getPnr().toLowerCase().contains(searchTerm.toLowerCase()) ||
                (booking.getUserFullName() != null && booking.getUserFullName().toLowerCase().contains(searchTerm.toLowerCase())) ||
                (booking.getFlightCode() != null && booking.getFlightCode().toLowerCase().contains(searchTerm.toLowerCase()))
            )
            .collect(java.util.stream.Collectors.toList());

        displayBookings(filteredBookings);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
