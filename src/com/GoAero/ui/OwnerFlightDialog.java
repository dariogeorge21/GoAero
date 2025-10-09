package com.GoAero.ui;

import com.GoAero.dao.AirportDAO;
import com.GoAero.dao.FlightDAO;
import com.GoAero.model.Airport;
import com.GoAero.model.Flight;
import com.GoAero.model.FlightOwner;
import com.GoAero.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Dialog for flight owners to add or edit their own flights
 */
public class OwnerFlightDialog extends JDialog {
    private Flight flight;
    private FlightDAO flightDAO;
    private AirportDAO airportDAO;
    private FlightOwner currentOwner;
    private boolean isEditMode;
    private boolean dataChanged = false;
    
    private JTextField flightCodeField, flightNameField, capacityField, priceField;
    private JTextField departureTimeField, destinationTimeField;
    private JComboBox<Airport> departureAirportComboBox, destinationAirportComboBox;
    private JButton saveButton, cancelButton;

    public OwnerFlightDialog(Frame parent, Flight flight, FlightDAO flightDAO, FlightOwner currentOwner) {
        super(parent, flight == null ? "Add Flight" : "Edit Flight", true);
        this.flight = flight;
        this.flightDAO = flightDAO;
        this.airportDAO = new AirportDAO();
        this.currentOwner = currentOwner;
        this.isEditMode = (flight != null);
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadComboBoxData();
        
        if (isEditMode) {
            loadFlightData();
        }
    }

    private void initializeComponents() {
        setSize(500, 550);
        setLocationRelativeTo(getParent());
        setResizable(false);

        flightCodeField = new JTextField(20);
        flightNameField = new JTextField(20);
        capacityField = new JTextField(20);
        priceField = new JTextField(20);
        departureTimeField = new JTextField(20);
        destinationTimeField = new JTextField(20);
        
        departureAirportComboBox = new JComboBox<>();
        destinationAirportComboBox = new JComboBox<>();
        
        saveButton = new JButton(isEditMode ? "Update Flight" : "Create Flight");
        cancelButton = new JButton("Cancel");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel(isEditMode ? "Edit Flight" : "Add New Flight");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Company info panel
        JPanel companyPanel = new JPanel();
        JLabel companyLabel = new JLabel("Airline: " + currentOwner.getDisplayName());
        companyLabel.setFont(new Font("Arial", Font.BOLD, 14));
        companyPanel.add(companyLabel);
        add(companyPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Flight Code
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Flight Code:"), gbc);
        gbc.gridx = 1;
        formPanel.add(flightCodeField, gbc);

        // Flight Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Flight Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(flightNameField, gbc);

        // Departure Airport
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Departure Airport:"), gbc);
        gbc.gridx = 1;
        formPanel.add(departureAirportComboBox, gbc);

        // Destination Airport
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Destination Airport:"), gbc);
        gbc.gridx = 1;
        formPanel.add(destinationAirportComboBox, gbc);

        // Departure Time
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Departure Time:"), gbc);
        gbc.gridx = 1;
        JPanel depTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        depTimePanel.add(departureTimeField);
        depTimePanel.add(new JLabel(" (YYYY-MM-DD HH:MM)"));
        formPanel.add(depTimePanel, gbc);

        // Destination Time
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Arrival Time:"), gbc);
        gbc.gridx = 1;
        JPanel destTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        destTimePanel.add(destinationTimeField);
        destTimePanel.add(new JLabel(" (YYYY-MM-DD HH:MM)"));
        formPanel.add(destTimePanel, gbc);

        // Capacity
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        formPanel.add(capacityField, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Price ($):"), gbc);
        gbc.gridx = 1;
        formPanel.add(priceField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        saveButton.addActionListener(e -> saveFlight());
        cancelButton.addActionListener(e -> dispose());
        
        // Auto-format flight code as user types
        flightCodeField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String text = flightCodeField.getText().toUpperCase();
                if (!text.equals(flightCodeField.getText())) {
                    flightCodeField.setText(text);
                }
            }
        });
    }

    private void loadComboBoxData() {
        try {
            // Load airports
            List<Airport> airports = airportDAO.findAll();
            for (Airport airport : airports) {
                departureAirportComboBox.addItem(airport);
                destinationAirportComboBox.addItem(airport);
            }
        } catch (Exception e) {
            showError("Failed to load airports: " + e.getMessage());
        }
    }

    private void loadFlightData() {
        flightCodeField.setText(flight.getFlightCode());
        flightNameField.setText(flight.getFlightName());
        capacityField.setText(String.valueOf(flight.getCapacity()));
        priceField.setText(flight.getPrice().toString());
        
        departureTimeField.setText(flight.getDepartureTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        destinationTimeField.setText(flight.getDestinationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        // Set selected items in combo boxes
        for (int i = 0; i < departureAirportComboBox.getItemCount(); i++) {
            Airport airport = departureAirportComboBox.getItemAt(i);
            if (airport.getAirportId() == flight.getDepartureAirportId()) {
                departureAirportComboBox.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < destinationAirportComboBox.getItemCount(); i++) {
            Airport airport = destinationAirportComboBox.getItemAt(i);
            if (airport.getAirportId() == flight.getDestinationAirportId()) {
                destinationAirportComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void saveFlight() {
        if (!validateInput()) {
            return;
        }

        try {
            if (isEditMode) {
                updateFlight();
            } else {
                createFlight();
            }
        } catch (Exception e) {
            showError("Save failed: " + e.getMessage());
        }
    }

    private void createFlight() {
        Flight newFlight = new Flight();
        populateFlightFromForm(newFlight);

        Flight savedFlight = flightDAO.create(newFlight);
        if (savedFlight != null) {
            showSuccess("Flight created successfully!");
            dataChanged = true;
            dispose();
        } else {
            showError("Failed to create flight. Please try again.");
        }
    }

    private void updateFlight() {
        populateFlightFromForm(flight);

        boolean success = flightDAO.update(flight);
        if (success) {
            showSuccess("Flight updated successfully!");
            dataChanged = true;
            dispose();
        } else {
            showError("Failed to update flight. Please try again.");
        }
    }

    private void populateFlightFromForm(Flight flight) {
        flight.setFlightCode(ValidationUtil.formatFlightCode(flightCodeField.getText().trim()));
        flight.setFlightName(flightNameField.getText().trim());
        flight.setCompanyId(currentOwner.getOwnerId()); // Use current owner's ID
        flight.setDepartureAirportId(((Airport) departureAirportComboBox.getSelectedItem()).getAirportId());
        flight.setDestinationAirportId(((Airport) destinationAirportComboBox.getSelectedItem()).getAirportId());
        flight.setDepartureTime(LocalDateTime.parse(departureTimeField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        flight.setDestinationTime(LocalDateTime.parse(destinationTimeField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        flight.setCapacity(Integer.parseInt(capacityField.getText().trim()));
        flight.setPrice(new BigDecimal(priceField.getText().trim()));
    }

    private boolean validateInput() {
        // Flight Code validation
        String code = flightCodeField.getText().trim();
        if (!ValidationUtil.isValidFlightCode(code)) {
            showError(ValidationUtil.getFlightCodeErrorMessage());
            flightCodeField.requestFocus();
            return false;
        }

        // Check if flight code already exists for this owner
        try {
            List<Flight> ownerFlights = flightDAO.findByCompanyId(currentOwner.getOwnerId());
            for (Flight existingFlight : ownerFlights) {
                if (existingFlight.getFlightCode().equalsIgnoreCase(code) && 
                    (!isEditMode || existingFlight.getFlightId() != flight.getFlightId())) {
                    showError("You already have a flight with this code.");
                    flightCodeField.requestFocus();
                    return false;
                }
            }
        } catch (Exception e) {
            // Continue with validation
        }

        // Flight Name validation
        if (!ValidationUtil.isNotEmpty(flightNameField.getText())) {
            showError("Flight name is required.");
            flightNameField.requestFocus();
            return false;
        }

        // Airport validation
        if (departureAirportComboBox.getSelectedItem() == null) {
            showError("Please select a departure airport.");
            departureAirportComboBox.requestFocus();
            return false;
        }

        if (destinationAirportComboBox.getSelectedItem() == null) {
            showError("Please select a destination airport.");
            destinationAirportComboBox.requestFocus();
            return false;
        }

        if (departureAirportComboBox.getSelectedItem() == destinationAirportComboBox.getSelectedItem()) {
            showError("Departure and destination airports cannot be the same.");
            destinationAirportComboBox.requestFocus();
            return false;
        }

        // Time validation
        try {
            LocalDateTime depTime = LocalDateTime.parse(departureTimeField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime destTime = LocalDateTime.parse(destinationTimeField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            if (depTime.isAfter(destTime)) {
                showError("Departure time cannot be after arrival time.");
                destinationTimeField.requestFocus();
                return false;
            }
            
            if (depTime.isBefore(LocalDateTime.now())) {
                showError("Departure time cannot be in the past.");
                departureTimeField.requestFocus();
                return false;
            }
        } catch (DateTimeParseException e) {
            showError("Please enter valid date and time in YYYY-MM-DD HH:MM format.");
            return false;
        }

        // Capacity validation
        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            if (capacity <= 0) {
                showError("Capacity must be a positive number.");
                capacityField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid capacity number.");
            capacityField.requestFocus();
            return false;
        }

        // Price validation
        try {
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Price must be greater than 0.");
                priceField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid price.");
            priceField.requestFocus();
            return false;
        }

        return true;
    }

    public boolean isDataChanged() {
        return dataChanged;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
