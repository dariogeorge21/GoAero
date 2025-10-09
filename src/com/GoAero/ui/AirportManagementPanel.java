package com.GoAero.ui;

import com.GoAero.dao.AirportDAO;
import com.GoAero.model.Airport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for managing airports in the admin dashboard
 */
public class AirportManagementPanel extends JPanel {
    private JTable airportsTable;
    private DefaultTableModel tableModel;
    private JButton addAirportButton, editAirportButton, deleteAirportButton, refreshButton;
    private JTextField searchField;
    private JButton searchButton;
    private AirportDAO airportDAO;
    private List<Airport> airports;

    public AirportManagementPanel() {
        airportDAO = new AirportDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadAirports();
    }

    private void initializeComponents() {
        // Table setup
        String[] columnNames = {"ID", "Code", "Airport Name", "City", "Country"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        airportsTable = new JTable(tableModel);
        airportsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        airportsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        // Set column widths
        airportsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        airportsTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Code
        airportsTable.getColumnModel().getColumn(2).setPreferredWidth(250); // Name
        airportsTable.getColumnModel().getColumn(3).setPreferredWidth(150); // City
        airportsTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Country

        // Buttons
        addAirportButton = new JButton("Add Airport");
        editAirportButton = new JButton("Edit Airport");
        deleteAirportButton = new JButton("Delete Airport");
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
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        topPanel.add(searchPanel, BorderLayout.WEST);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addAirportButton);
        buttonPanel.add(editAirportButton);
        buttonPanel.add(deleteAirportButton);
        buttonPanel.add(refreshButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table panel
        JScrollPane scrollPane = new JScrollPane(airportsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Airports"));
        add(scrollPane, BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("Total Airports: ");
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        addAirportButton.addActionListener(e -> addAirport());
        editAirportButton.addActionListener(e -> editAirport());
        deleteAirportButton.addActionListener(e -> deleteAirport());
        refreshButton.addActionListener(e -> loadAirports());
        searchButton.addActionListener(e -> searchAirports());
        
        // Enter key on search field
        searchField.addActionListener(e -> searchAirports());

        // Double-click to edit
        airportsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editAirport();
                }
            }
        });
    }

    private void loadAirports() {
        try {
            airports = airportDAO.findAll();
            displayAirports(airports);
            updateInfoPanel();
        } catch (Exception e) {
            showError("Failed to load airports: " + e.getMessage());
        }
    }

    private void displayAirports(List<Airport> airportList) {
        // Clear existing data
        tableModel.setRowCount(0);

        // Add airports to table
        for (Airport airport : airportList) {
            Object[] row = {
                airport.getAirportId(),
                airport.getAirportCode(),
                airport.getAirportName(),
                airport.getCity(),
                airport.getCountry()
            };
            tableModel.addRow(row);
        }

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = airportsTable.getSelectedRow() != -1;
        editAirportButton.setEnabled(hasSelection);
        deleteAirportButton.setEnabled(hasSelection);
    }

    private void updateInfoPanel() {
        // Update the info label in the south panel
        Component[] components = ((JPanel) getComponent(2)).getComponents();
        if (components.length > 0 && components[0] instanceof JLabel) {
            ((JLabel) components[0]).setText("Total Airports: " + (airports != null ? airports.size() : 0));
        }
    }

    private void addAirport() {
        AdminAirportDialog dialog = new AdminAirportDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            null, 
            airportDAO
        );
        dialog.setVisible(true);
        
        if (dialog.isDataChanged()) {
            loadAirports();
        }
    }

    private void editAirport() {
        int selectedRow = airportsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an airport to edit.");
            return;
        }

        Airport selectedAirport = airports.get(selectedRow);
        AdminAirportDialog dialog = new AdminAirportDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            selectedAirport, 
            airportDAO
        );
        dialog.setVisible(true);
        
        if (dialog.isDataChanged()) {
            loadAirports();
        }
    }

    private void deleteAirport() {
        int selectedRow = airportsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an airport to delete.");
            return;
        }

        Airport selectedAirport = airports.get(selectedRow);
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            String.format("Are you sure you want to delete airport '%s (%s)'?\n\nThis action cannot be undone.", 
                selectedAirport.getAirportName(), selectedAirport.getAirportCode()),
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                boolean success = airportDAO.delete(selectedAirport.getAirportId());
                if (success) {
                    showSuccess("Airport deleted successfully.");
                    loadAirports();
                } else {
                    showError("Failed to delete airport. It may be referenced by existing flights.");
                }
            } catch (Exception e) {
                showError("Deletion failed: " + e.getMessage());
            }
        }
    }

    private void searchAirports() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            displayAirports(airports);
            return;
        }

        try {
            List<Airport> searchResults = airportDAO.searchAirports(searchTerm);
            displayAirports(searchResults);
        } catch (Exception e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
