package com.GoAero.ui;

import com.GoAero.dao.BookingDAO;
import com.GoAero.dao.FlightDAO;
import com.GoAero.model.Booking;
import com.GoAero.model.Flight;
import com.GoAero.model.SessionManager;
import com.GoAero.model.User;
import com.GoAero.util.PNRGenerator;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Dialog for booking a selected flight
 */
public class FlightBookingDialog extends JDialog {
    private Flight selectedFlight;
    private User currentUser;
    private BookingDAO bookingDAO;
    private FlightDAO flightDAO;
    
    private JLabel flightInfoLabel, priceLabel, passengerInfoLabel;
    private JButton confirmBookingButton, cancelButton;

    public FlightBookingDialog(Frame parent, Flight flight) {
        super(parent, "Book Flight", true);
        this.selectedFlight = flight;
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.bookingDAO = new BookingDAO();
        this.flightDAO = new FlightDAO();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
    }

    private void initializeComponents() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        setResizable(true);

        // Flight information
        String flightInfo = String.format(
            "<html><h3>Flight Information</h3>" +
            "<b>Flight:</b> %s (%s)<br>" +
            "<b>Airline:</b> %s<br>" +
            "<b>Route:</b> %s<br>" +
            "<b>Departure:</b> %s<br>" +
            "<b>Arrival:</b> %s<br>" +
            "<b>Available Seats:</b> %d</html>",
            selectedFlight.getFlightCode(),
            selectedFlight.getFlightName(),
            selectedFlight.getCompanyName(),
            selectedFlight.getFullRoute(),
            selectedFlight.getDepartureTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            selectedFlight.getDestinationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            selectedFlight.getAvailableSeats()
        );
        flightInfoLabel = new JLabel(flightInfo);

        // Price information
        priceLabel = new JLabel(String.format("<html><h3>Total Price: ₹%.2f</h3></html>", selectedFlight.getPrice()));
        priceLabel.setForeground(new Color(0, 128, 0));

        // Passenger information
        String passengerInfo = String.format(
            "<html><h3>Passenger Information</h3>" +
            "<b>Name:</b> %s<br>" +
            "<b>Email:</b> %s<br>" +
            "<b>Phone:</b> %s</html>",
            currentUser.getFullName(),
            currentUser.getEmail(),
            currentUser.getPhone() != null ? currentUser.getPhone() : "Not provided"
        );
        passengerInfoLabel = new JLabel(passengerInfo);

        confirmBookingButton = new JButton("Confirm Booking");
        confirmBookingButton.setFont(new Font("Arial", Font.BOLD, 14));
        confirmBookingButton.setBackground(new Color(0, 128, 0));
        confirmBookingButton.setForeground(Color.WHITE);

        cancelButton = new JButton("Cancel");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Flight info panel
        JPanel flightPanel = new JPanel(new BorderLayout());
        flightPanel.setBorder(BorderFactory.createEtchedBorder());
        flightPanel.add(flightInfoLabel, BorderLayout.CENTER);
        contentPanel.add(flightPanel);

        contentPanel.add(Box.createVerticalStrut(15));

        // Price panel
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pricePanel.add(priceLabel);
        contentPanel.add(pricePanel);

        contentPanel.add(Box.createVerticalStrut(15));

        // Passenger info panel
        JPanel passengerPanel = new JPanel(new BorderLayout());
        passengerPanel.setBorder(BorderFactory.createEtchedBorder());
        passengerPanel.add(passengerInfoLabel, BorderLayout.CENTER);
        contentPanel.add(passengerPanel);

        contentPanel.add(Box.createVerticalStrut(15));

        // Terms and conditions
        JLabel termsLabel = new JLabel(
            "<html><small><i>By confirming this booking, you agree to the terms and conditions.<br>" +
            "Your booking will be confirmed and a PNR will be generated.</i></small></html>"
        );
        termsLabel.setForeground(Color.GRAY);
        termsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(termsLabel);

        // Add scroll pane for the content
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        buttonPanel.add(confirmBookingButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        confirmBookingButton.addActionListener(e -> confirmBooking());
        cancelButton.addActionListener(e -> dispose());
    }

    private void confirmBooking() {
        // Disable button to prevent double-clicking
        confirmBookingButton.setEnabled(false);
        
        try {
            // Check if flight still has available seats
            int currentAvailableSeats = flightDAO.getAvailableSeats(selectedFlight.getFlightId());
            if (currentAvailableSeats <= 0) {
                showError("Sorry, this flight is now fully booked.");
                return;
            }

            // Generate unique PNR
            String pnr;
            do {
                pnr = PNRGenerator.generatePNRWithAirline(selectedFlight.getCompanyCode());
            } while (bookingDAO.pnrExists(pnr));

            // Create booking
            Booking booking = new Booking();
            booking.setUserId(currentUser.getUserId());
            booking.setFlightId(selectedFlight.getFlightId());
            booking.setDepartureAirportId(selectedFlight.getDepartureAirportId());
            booking.setDestinationAirportId(selectedFlight.getDestinationAirportId());
            booking.setDepartureTime(selectedFlight.getDepartureTime());
            booking.setDestinationTime(selectedFlight.getDestinationTime());
            booking.setPnr(pnr);
            booking.setDateOfDeparture(selectedFlight.getDepartureTime().toLocalDate());
            booking.setDateOfDestination(selectedFlight.getDestinationTime().toLocalDate());
            booking.setAmount(selectedFlight.getPrice());
            booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
            booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);

            // Save booking to database
            Booking savedBooking = bookingDAO.create(booking);
            
            if (savedBooking != null) {
                showBookingConfirmation(savedBooking);
                dispose();
            } else {
                showError("Booking failed. Please try again.");
            }

        } catch (Exception e) {
            showError("Booking failed: " + e.getMessage());
        } finally {
            confirmBookingButton.setEnabled(true);
        }
    }

    private void showBookingConfirmation(Booking booking) {
        String confirmationMessage = String.format(
            "Booking Confirmed!\n\n" +
            "PNR: %s\n" +
            "Flight: %s\n" +
            "Route: %s\n" +
            "Departure: %s\n" +
            "Amount: ₹%.2f\n\n" +
            "Please save your PNR for future reference.\n" +
            "You can view your booking details in 'My Bookings'.",
            booking.getPnr(),
            selectedFlight.getFlightCode(),
            selectedFlight.getFullRoute(),
            selectedFlight.getDepartureTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            booking.getAmount()
        );

        JOptionPane.showMessageDialog(
            this,
            confirmationMessage,
            "Booking Confirmed",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Booking Error", JOptionPane.ERROR_MESSAGE);
    }
}
