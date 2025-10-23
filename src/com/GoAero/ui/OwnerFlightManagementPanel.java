package com.GoAero.ui;

import com.GoAero.dao.FlightDAO;
import com.GoAero.model.Flight;
import com.GoAero.model.FlightOwner;
import com.GoAero.model.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for flight owners to manage their own flights
 */
public class OwnerFlightManagementPanel extends JPanel {
    private JTable flightsTable;
    private DefaultTableModel tableModel;
    private JButton addFlightButton, editFlightButton, deleteFlightButton, refreshButton;
    private JTextField searchField;
    private JButton searchButton;
    private FlightDAO flightDAO;
    private FlightOwner currentOwner;
    private List<Flight> flights;

    public OwnerFlightManagementPanel() {
        currentOwner = SessionManager.getInstance().getCurrentFlightOwner();
        if (currentOwner == null) {
            JLabel errorLabel = new JLabel("Access denied. Please login as a flight owner.");
            add(errorLabel);
            return;
        }
        
        flightDAO = new FlightDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadFlights();
    }

    private void initializeComponents() {
        // Table setup
        String[] columnNames = {"ID", "Flight Code", "Flight Name", "Route", "Departure", "Arrival", "Price", "Capacity", "Available"};
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
                updateButtonStates();
            }
        });

        // Set column widths
        flightsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        flightsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Flight Code
        flightsTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Flight Name
        flightsTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Route
        flightsTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Departure
        flightsTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Arrival
        flightsTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Price
        flightsTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Capacity
        flightsTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // Available

        // Buttons
        addFlightButton = new JButton("Add Flight");
        editFlightButton = new JButton("Edit Flight");
        deleteFlightButton = new JButton("Delete Flight");
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
        
        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel companyLabel = new JLabel("Managing flights for: " + currentOwner.getDisplayName());
        companyLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(companyLabel);
        topPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Search and button panel
        JPanel searchButtonPanel = new JPanel(new BorderLayout());
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchButtonPanel.add(searchPanel, BorderLayout.WEST);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addFlightButton);
        buttonPanel.add(editFlightButton);
        buttonPanel.add(deleteFlightButton);
        buttonPanel.add(refreshButton);
        searchButtonPanel.add(buttonPanel, BorderLayout.EAST);
        
        topPanel.add(searchButtonPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Table panel
        JScrollPane scrollPane = new JScrollPane(flightsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("My Flights"));
        add(scrollPane, BorderLayout.CENTER);

        // Info panel
        JPanel bottomInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("Total Flights: ");
        bottomInfoPanel.add(infoLabel);
        add(bottomInfoPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        addFlightButton.addActionListener(e -> addFlight());
        editFlightButton.addActionListener(e -> editFlight());
        deleteFlightButton.addActionListener(e -> deleteFlight());
        refreshButton.addActionListener(e -> loadFlights());
        searchButton.addActionListener(e -> searchFlights());
        
        // Enter key on search field
        searchField.addActionListener(e -> searchFlights());

        // Double-click to edit
        flightsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editFlight();
                }
            }
        });
    }

    private void loadFlights() {
        try {
            flights = flightDAO.findByCompanyId(currentOwner.getOwnerId());
            displayFlights(flights);
            updateInfoPanel();
        } catch (Exception e) {
            showError("Failed to load flights: " + e.getMessage());
        }
    }

    private void displayFlights(List<Flight> flightList) {
        // Clear existing data
        tableModel.setRowCount(0);

        // Add flights to table
        for (Flight flight : flightList) {
            Object[] row = {
                flight.getFlightId(),
                flight.getFlightCode(),
                flight.getFlightName() != null ? flight.getFlightName() : "",
                flight.getRoute(),
                flight.getDepartureTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                flight.getDestinationTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                String.format("₹%.2f", flight.getPrice()),
                flight.getCapacity(),
                flight.getAvailableSeats()
            };
            tableModel.addRow(row);
        }

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = flightsTable.getSelectedRow() != -1;
        editFlightButton.setEnabled(hasSelection);
        deleteFlightButton.setEnabled(hasSelection);
    }

    private void updateInfoPanel() {
        // Update the info label in the south panel
        Component[] components = ((JPanel) getComponent(2)).getComponents();
        if (components.length > 0 && components[0] instanceof JLabel) {
            ((JLabel) components[0]).setText("Total Flights: " + (flights != null ? flights.size() : 0));
        }
    }

    private void addFlight() {
        OwnerFlightDialog dialog = new OwnerFlightDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            null, 
            flightDAO,
            currentOwner
        );
        dialog.setVisible(true);
        
        if (dialog.isDataChanged()) {
            loadFlights();
        }
    }

    private void editFlight() {
        int selectedRow = flightsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a flight to edit.");
            return;
        }

        Flight selectedFlight = flights.get(selectedRow);
        OwnerFlightDialog dialog = new OwnerFlightDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            selectedFlight, 
            flightDAO,
            currentOwner
        );
        dialog.setVisible(true);
        
        if (dialog.isDataChanged()) {
            loadFlights();
        }
    }

    private void deleteFlight() {
        int selectedRow = flightsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a flight to delete.");
            return;
        }

        Flight selectedFlight = flights.get(selectedRow);
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            String.format("Are you sure you want to delete flight '%s'?\n\nRoute: %s\nDeparture: %s\n\nThis will also cancel all associated bookings.\nThis action cannot be undone.", 
                selectedFlight.getFlightCode(),
                selectedFlight.getRoute(),
                selectedFlight.getDepartureTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            ),
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                boolean success = flightDAO.delete(selectedFlight.getFlightId());
                if (success) {
                    showSuccess("Flight deleted successfully.");
                    loadFlights();
                } else {
                    showError("Failed to delete flight. It may have associated bookings.");
                }
            } catch (Exception e) {
                showError("Deletion failed: " + e.getMessage());
            }
        }
    }

    private void searchFlights() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            displayFlights(flights);
            return;
        }

        // Filter flights based on search term
        List<Flight> filteredFlights = flights.stream()
            .filter(flight ->
                flight.getFlightCode().toLowerCase().contains(searchTerm.toLowerCase()) ||
                (flight.getFlightName() != null && flight.getFlightName().toLowerCase().contains(searchTerm.toLowerCase())) ||
                flight.getRoute().toLowerCase().contains(searchTerm.toLowerCase())
            )
            .collect(java.util.stream.Collectors.toList());

        displayFlights(filteredFlights);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
