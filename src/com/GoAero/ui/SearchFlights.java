package com.GoAero.ui;

import com.GoAero.dao.AirportDAO;
import com.GoAero.dao.FlightDAO;
import com.GoAero.model.Airport;
import com.GoAero.model.Flight;
import com.GoAero.model.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Flight search interface for passengers
 */
public class SearchFlights extends JFrame {
    private JComboBox<Airport> departureComboBox, destinationComboBox;
    private JTextField departureDateField;
    private JButton searchButton, backButton, bookButton;
    private JTable flightsTable;
    private DefaultTableModel tableModel;
    
    private AirportDAO airportDAO;
    private FlightDAO flightDAO;
    private List<Flight> searchResults;

    public SearchFlights() {
        airportDAO = new AirportDAO();
        flightDAO = new FlightDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadAirports();
    }

    private void initializeComponents() {
        setTitle("GoAero - Search Flights");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        departureComboBox = new JComboBox<>();
        departureComboBox.setPreferredSize(new Dimension(200, 30));
        
        destinationComboBox = new JComboBox<>();
        destinationComboBox.setPreferredSize(new Dimension(200, 30));
        
        departureDateField = new JTextField(10);
        departureDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        searchButton = new JButton("Search Flights");
        backButton = new JButton("Back to Dashboard");
        bookButton = new JButton("Book Selected Flight");
        bookButton.setEnabled(false);

        // Table setup
        String[] columnNames = {"Flight Code", "Airline", "Route", "Departure", "Arrival", "Price", "Available Seats"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        flightsTable = new JTable(tableModel);
        flightsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                bookButton.setEnabled(flightsTable.getSelectedRow() != -1);
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Search panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Flights"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // From
        gbc.gridx = 0; gbc.gridy = 0;
        searchPanel.add(new JLabel("From:"), gbc);
        gbc.gridx = 1;
        searchPanel.add(departureComboBox, gbc);

        // To
        gbc.gridx = 2; gbc.gridy = 0;
        searchPanel.add(new JLabel("To:"), gbc);
        gbc.gridx = 3;
        searchPanel.add(destinationComboBox, gbc);

        // Date
        gbc.gridx = 4; gbc.gridy = 0;
        searchPanel.add(new JLabel("Departure Date:"), gbc);
        gbc.gridx = 5;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        datePanel.add(departureDateField);
        datePanel.add(new JLabel(" (YYYY-MM-DD)"));
        searchPanel.add(datePanel, gbc);

        // Search button
        gbc.gridx = 6; gbc.gridy = 0;
        searchPanel.add(searchButton, gbc);

        add(searchPanel, BorderLayout.NORTH);

        // Results panel
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Search Results"));
        
        JScrollPane scrollPane = new JScrollPane(flightsTable);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(resultsPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(bookButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        searchButton.addActionListener(e -> performSearch());
        bookButton.addActionListener(e -> bookSelectedFlight());
        backButton.addActionListener(e -> goBackToDashboard());
        
        // Enter key on date field
        departureDateField.addActionListener(e -> performSearch());
    }

    private void loadAirports() {
        try {
            List<Airport> airports = airportDAO.findAll();
            
            // Add default option
            departureComboBox.addItem(null);
            destinationComboBox.addItem(null);
            
            for (Airport airport : airports) {
                departureComboBox.addItem(airport);
                destinationComboBox.addItem(airport);
            }
            
            // Custom renderer to show airport display name
            departureComboBox.setRenderer(new AirportComboBoxRenderer());
            destinationComboBox.setRenderer(new AirportComboBoxRenderer());
            
        } catch (Exception e) {
            showError("Failed to load airports: " + e.getMessage());
        }
    }

    private void performSearch() {
        if (!validateSearchInput()) {
            return;
        }

        try {
            Airport departure = (Airport) departureComboBox.getSelectedItem();
            Airport destination = (Airport) destinationComboBox.getSelectedItem();
            LocalDate departureDate = LocalDate.parse(departureDateField.getText().trim());

            searchResults = flightDAO.searchFlights(departure.getAirportId(), destination.getAirportId(), departureDate);
            
            // Update available seats for each flight
            for (Flight flight : searchResults) {
                int availableSeats = flightDAO.getAvailableSeats(flight.getFlightId());
                flight.setAvailableSeats(availableSeats);
            }
            
            displaySearchResults();
            
        } catch (Exception e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    private boolean validateSearchInput() {
        if (departureComboBox.getSelectedItem() == null) {
            showError("Please select a departure airport.");
            return false;
        }

        if (destinationComboBox.getSelectedItem() == null) {
            showError("Please select a destination airport.");
            return false;
        }

        if (departureComboBox.getSelectedItem().equals(destinationComboBox.getSelectedItem())) {
            showError("Departure and destination airports cannot be the same.");
            return false;
        }

        try {
            LocalDate departureDate = LocalDate.parse(departureDateField.getText().trim());
            if (departureDate.isBefore(LocalDate.now())) {
                showError("Departure date cannot be in the past.");
                return false;
            }
        } catch (DateTimeParseException e) {
            showError("Please enter departure date in YYYY-MM-DD format.");
            return false;
        }

        return true;
    }

    private void displaySearchResults() {
        // Clear existing data
        tableModel.setRowCount(0);

        if (searchResults.isEmpty()) {
            showInfo("No flights found for the selected criteria.");
            return;
        }

        // Add flights to table
        for (Flight flight : searchResults) {
            Object[] row = {
                flight.getFlightCode(),
                flight.getCompanyName(),
                flight.getRoute(),
                flight.getDepartureTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                flight.getDestinationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "₹" + flight.getPrice(),
                flight.getAvailableSeats()
            };
            tableModel.addRow(row);
        }
    }

    private void bookSelectedFlight() {
        int selectedRow = flightsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a flight to book.");
            return;
        }

        if (!SessionManager.getInstance().isUserLoggedIn()) {
            showError("Please login to book a flight.");
            return;
        }

        Flight selectedFlight = searchResults.get(selectedRow);
        
        if (selectedFlight.getAvailableSeats() <= 0) {
            showError("This flight is fully booked.");
            return;
        }

        // Open booking dialog
        SwingUtilities.invokeLater(() -> {
            new FlightBookingDialog(this, selectedFlight).setVisible(true);
        });
    }

    private void goBackToDashboard() {
        if (SessionManager.getInstance().isUserLoggedIn()) {
            SwingUtilities.invokeLater(() -> {
                new UserDashboard().setVisible(true);
                dispose();
            });
        } else {
            SwingUtilities.invokeLater(() -> {
                new LandingPage().setVisible(true);
                dispose();
            });
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    // Custom renderer for airport combo box
    private static class AirportComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value == null) {
                setText("Select Airport");
            } else if (value instanceof Airport) {
                Airport airport = (Airport) value;
                setText(airport.getDisplayName());
            }
            
            return this;
        }
    }
}
