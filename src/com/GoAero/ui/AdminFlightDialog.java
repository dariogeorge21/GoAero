package com.GoAero.ui;

import com.GoAero.dao.AirportDAO;
import com.GoAero.dao.FlightDAO;
import com.GoAero.dao.FlightOwnerDAO;
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
 * Dialog for admin to add or edit flights
 */
public class AdminFlightDialog extends JDialog {
    private Flight flight;
    private FlightDAO flightDAO;
    private AirportDAO airportDAO;
    private FlightOwnerDAO flightOwnerDAO;
    private boolean isEditMode;
    private boolean dataChanged = false;
    
    private JTextField flightCodeField, flightNameField, capacityField, priceField;
    private JTextField departureTimeField, destinationTimeField;
    private JComboBox<FlightOwner> companyComboBox;
    private JComboBox<Airport> departureAirportComboBox, destinationAirportComboBox;
    private JButton saveButton, cancelButton;

    public AdminFlightDialog(Frame parent, Flight flight, FlightDAO flightDAO) {
        super(parent, flight == null ? "Add Flight" : "Edit Flight", true);
        this.flight = flight;
        this.flightDAO = flightDAO;
        this.airportDAO = new AirportDAO();
        this.flightOwnerDAO = new FlightOwnerDAO();
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
        setSize(950, 850);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // Initialize text fields with proper sizing and padding
        flightCodeField = new JTextField(25);
        flightCodeField.setPreferredSize(new Dimension(250, 30));
        flightNameField = new JTextField(25);
        flightNameField.setPreferredSize(new Dimension(250, 30));
        capacityField = new JTextField(25);
        capacityField.setPreferredSize(new Dimension(250, 30));
        priceField = new JTextField(25);
        priceField.setPreferredSize(new Dimension(250, 30));
        departureTimeField = new JTextField(25);
        departureTimeField.setPreferredSize(new Dimension(200, 30));
        destinationTimeField = new JTextField(25);
        destinationTimeField.setPreferredSize(new Dimension(200, 30));
        
        // Initialize combo boxes with proper sizing and custom renderers
        companyComboBox = new JComboBox<>();
        companyComboBox.setPreferredSize(new Dimension(250, 30));
        companyComboBox.setRenderer(new FlightOwnerRenderer());
        
        departureAirportComboBox = new JComboBox<>();
        departureAirportComboBox.setPreferredSize(new Dimension(250, 30));
        departureAirportComboBox.setRenderer(new AirportRenderer());
        
        destinationAirportComboBox = new JComboBox<>();
        destinationAirportComboBox.setPreferredSize(new Dimension(250, 30));
        destinationAirportComboBox.setRenderer(new AirportRenderer());
        
        // Initialize buttons with proper sizing and styling
        saveButton = new JButton(isEditMode ? "Update Flight" : "Create Flight");
        saveButton.setPreferredSize(new Dimension(140, 35));
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title panel with padding
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        JLabel titleLabel = new JLabel(isEditMode ? "Edit Flight" : "Add New Flight");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Form panel with proper spacing
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Flight Code
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel flightCodeLabel = new JLabel("Flight Code:");
        flightCodeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(flightCodeLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(flightCodeField, gbc);

        // Flight Name
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel flightNameLabel = new JLabel("Flight Name:");
        flightNameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(flightNameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(flightNameField, gbc);

        // Company
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel companyLabel = new JLabel("Airline Company:");
        companyLabel.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(companyLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(companyComboBox, gbc);

        // Departure Airport
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel depAirportLabel = new JLabel("Departure Airport:");
        depAirportLabel.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(depAirportLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(departureAirportComboBox, gbc);

        // Destination Airport
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel destAirportLabel = new JLabel("Destination Airport:");
        destAirportLabel.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(destAirportLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(destinationAirportComboBox, gbc);

        // Departure Time
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel depTimeLabel = new JLabel("Departure Time:");
        depTimeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(depTimeLabel, gbc);
        gbc.gridx = 1;
        JPanel depTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        depTimePanel.add(departureTimeField);
        JLabel depTimeFormat = new JLabel("  (YYYY-MM-DD HH:MM)");
        depTimeFormat.setFont(new Font("Arial", Font.ITALIC, 10));
        depTimeFormat.setForeground(Color.GRAY);
        depTimePanel.add(depTimeFormat);
        formPanel.add(depTimePanel, gbc);

        // Destination Time
        gbc.gridx = 0; gbc.gridy = 6;
        JLabel arrTimeLabel = new JLabel("Arrival Time:");
        arrTimeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(arrTimeLabel, gbc);
        gbc.gridx = 1;
        JPanel destTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        destTimePanel.add(destinationTimeField);
        JLabel destTimeFormat = new JLabel("  (YYYY-MM-DD HH:MM)");
        destTimeFormat.setFont(new Font("Arial", Font.ITALIC, 10));
        destTimeFormat.setForeground(Color.GRAY);
        destTimePanel.add(destTimeFormat);
        formPanel.add(destTimePanel, gbc);

        // Capacity
        gbc.gridx = 0; gbc.gridy = 7;
        JLabel capacityLabel = new JLabel("Capacity:");
        capacityLabel.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(capacityLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(capacityField, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 8;
        JLabel priceLabel = new JLabel("Price ($):");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(priceLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(priceField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button panel with proper spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
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
            // Load flight owners
            List<FlightOwner> flightOwners = flightOwnerDAO.findAll();
            for (FlightOwner owner : flightOwners) {
                companyComboBox.addItem(owner);
            }

            // Load airports
            List<Airport> airports = airportDAO.findAll();
            for (Airport airport : airports) {
                departureAirportComboBox.addItem(airport);
                destinationAirportComboBox.addItem(airport);
            }
        } catch (Exception e) {
            showError("Failed to load data: " + e.getMessage());
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
        for (int i = 0; i < companyComboBox.getItemCount(); i++) {
            FlightOwner owner = companyComboBox.getItemAt(i);
            if (owner.getOwnerId() == flight.getCompanyId()) {
                companyComboBox.setSelectedIndex(i);
                break;
            }
        }

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
        flight.setCompanyId(((FlightOwner) companyComboBox.getSelectedItem()).getOwnerId());
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

        // Check if flight code already exists (simplified check for now)
        // Note: This would need a proper flightCodeExists method in FlightDAO
        try {
            List<Flight> existingFlights = flightDAO.findAll();
            for (Flight existingFlight : existingFlights) {
                if (existingFlight.getFlightCode().equalsIgnoreCase(code) &&
                    (!isEditMode || existingFlight.getFlightId() != flight.getFlightId())) {
                    showError("A flight with this code already exists.");
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

        // Company validation
        if (companyComboBox.getSelectedItem() == null) {
            showError("Please select an airline company.");
            companyComboBox.requestFocus();
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

    // Custom renderer for FlightOwner combo box
    private static class FlightOwnerRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof FlightOwner) {
                FlightOwner owner = (FlightOwner) value;
                setText(owner.getCompanyName());
            }
            
            return this;
        }
    }

    // Custom renderer for Airport combo box
    private static class AirportRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Airport) {
                Airport airport = (Airport) value;
                setText(airport.getAirportCode() + " - " + airport.getAirportName() + " (" + airport.getCity() + ")");
            }
            
            return this;
        }
    }
}
