package com.GoAero.ui;

import com.GoAero.model.Booking;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Dialog to display detailed booking information
 */
public class BookingDetailsDialog extends JDialog {
    private Booking booking;
    private JButton closeButton;

    public BookingDetailsDialog(Frame parent, Booking booking) {
        super(parent, "Booking Details", true);
        this.booking = booking;
        initializeComponents();
        setupLayout();
        setupEventListeners();
    }

    private void initializeComponents() {
        setSize(600, 700);
        setLocationRelativeTo(getParent());
        setResizable(true);

        closeButton = new JButton("Close");
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Booking Details");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Booking Information Panel
        JPanel bookingInfoPanel = createBookingInfoPanel();
        contentPanel.add(bookingInfoPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Flight Information Panel
        JPanel flightInfoPanel = createFlightInfoPanel();
        contentPanel.add(flightInfoPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Passenger Information Panel
        JPanel passengerInfoPanel = createPassengerInfoPanel();
        contentPanel.add(passengerInfoPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Status Information Panel
        JPanel statusInfoPanel = createStatusInfoPanel();
        contentPanel.add(statusInfoPanel);

        // Add scroll pane for the content
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createBookingInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Booking Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // PNR
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("PNR:"), gbc);
        gbc.gridx = 1;
        JLabel pnrLabel = new JLabel(booking.getPnr());
        pnrLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(pnrLabel, gbc);

        // Booking Date
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Booking Date:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(booking.getDateOfBooking().toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))), gbc);

        // Amount
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        JLabel amountLabel = new JLabel(String.format("₹%.2f", booking.getAmount()));
        amountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        amountLabel.setForeground(new Color(0, 128, 0));
        panel.add(amountLabel, gbc);

        return panel;
    }

    private JPanel createFlightInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Flight Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Flight Code
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Flight Code:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(booking.getFlightCode()), gbc);

        // Flight Name
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Flight Name:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(booking.getFlightName() != null ? booking.getFlightName() : "N/A"), gbc);

        // Airline
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Airline:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(booking.getCompanyName() != null ? booking.getCompanyName() : "N/A"), gbc);

        // Route
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Route:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(booking.getFullRoute()), gbc);

        // Departure
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Departure:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(booking.getDepartureTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))), gbc);

        // Arrival
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Arrival:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(booking.getDestinationTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))), gbc);

        return panel;
    }

    private JPanel createPassengerInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Passenger Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Passenger Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Passenger Name:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(booking.getUserFullName() != null ? booking.getUserFullName() : "N/A"), gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(booking.getUserEmail() != null ? booking.getUserEmail() : "N/A"), gbc);

        return panel;
    }

    private JPanel createStatusInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Status Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Booking Status
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Booking Status:"), gbc);
        gbc.gridx = 1;
        JLabel bookingStatusLabel = new JLabel(booking.getBookingStatus().getDisplayName());
        bookingStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Set color based on status
        switch (booking.getBookingStatus()) {
            case CONFIRMED:
                bookingStatusLabel.setForeground(new Color(0, 128, 0));
                break;
            case CANCELLED:
                bookingStatusLabel.setForeground(Color.RED);
                break;
            case PENDING:
                bookingStatusLabel.setForeground(new Color(255, 140, 0));
                break;
        }
        panel.add(bookingStatusLabel, gbc);

        // Payment Status
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Payment Status:"), gbc);
        gbc.gridx = 1;
        JLabel paymentStatusLabel = new JLabel(booking.getPaymentStatus().getDisplayName());
        paymentStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Set color based on payment status
        switch (booking.getPaymentStatus()) {
            case COMPLETED:
                paymentStatusLabel.setForeground(new Color(0, 128, 0));
                break;
            case FAILED:
                paymentStatusLabel.setForeground(Color.RED);
                break;
            case PENDING:
                paymentStatusLabel.setForeground(new Color(255, 140, 0));
                break;
        }
        panel.add(paymentStatusLabel, gbc);

        return panel;
    }

    private void setupEventListeners() {
        closeButton.addActionListener(e -> dispose());
    }
}
