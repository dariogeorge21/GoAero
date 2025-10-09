package com.GoAero.ui;

import com.GoAero.dao.FlightOwnerDAO;
import com.GoAero.model.FlightOwner;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for managing flight owners (airline companies) in the admin dashboard
 */
public class FlightOwnerManagementPanel extends JPanel {
    private JTable flightOwnersTable;
    private DefaultTableModel tableModel;
    private JButton addOwnerButton, editOwnerButton, deleteOwnerButton, refreshButton;
    private JTextField searchField;
    private JButton searchButton;
    private FlightOwnerDAO flightOwnerDAO;
    private List<FlightOwner> flightOwners;

    public FlightOwnerManagementPanel() {
        flightOwnerDAO = new FlightOwnerDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadFlightOwners();
    }

    private void initializeComponents() {
        // Table setup
        String[] columnNames = {"ID", "Company Code", "Company Name", "Contact Info", "Flight Count"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        flightOwnersTable = new JTable(tableModel);
        flightOwnersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightOwnersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        // Set column widths
        flightOwnersTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        flightOwnersTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Code
        flightOwnersTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Name
        flightOwnersTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Contact
        flightOwnersTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Flight Count

        // Buttons
        addOwnerButton = new JButton("Add Airline");
        editOwnerButton = new JButton("Edit Airline");
        deleteOwnerButton = new JButton("Delete Airline");
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
        buttonPanel.add(addOwnerButton);
        buttonPanel.add(editOwnerButton);
        buttonPanel.add(deleteOwnerButton);
        buttonPanel.add(refreshButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table panel
        JScrollPane scrollPane = new JScrollPane(flightOwnersTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Airline Companies"));
        add(scrollPane, BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("Total Airlines: ");
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        addOwnerButton.addActionListener(e -> addFlightOwner());
        editOwnerButton.addActionListener(e -> editFlightOwner());
        deleteOwnerButton.addActionListener(e -> deleteFlightOwner());
        refreshButton.addActionListener(e -> loadFlightOwners());
        searchButton.addActionListener(e -> searchFlightOwners());
        
        // Enter key on search field
        searchField.addActionListener(e -> searchFlightOwners());

        // Double-click to edit
        flightOwnersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editFlightOwner();
                }
            }
        });
    }

    private void loadFlightOwners() {
        try {
            flightOwners = flightOwnerDAO.findAllWithFlightCounts();
            displayFlightOwners(flightOwners);
            updateInfoPanel();
        } catch (Exception e) {
            showError("Failed to load flight owners: " + e.getMessage());
        }
    }

    private void displayFlightOwners(List<FlightOwner> ownerList) {
        // Clear existing data
        tableModel.setRowCount(0);

        // Add flight owners to table
        for (FlightOwner owner : ownerList) {
            Object[] row = {
                owner.getOwnerId(),
                owner.getCompanyCode(),
                owner.getCompanyName(),
                owner.getContactInfo(),
                owner.getFlightCount()
            };
            tableModel.addRow(row);
        }

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = flightOwnersTable.getSelectedRow() != -1;
        editOwnerButton.setEnabled(hasSelection);
        deleteOwnerButton.setEnabled(hasSelection);
    }

    private void updateInfoPanel() {
        // Update the info label in the south panel
        Component[] components = ((JPanel) getComponent(2)).getComponents();
        if (components.length > 0 && components[0] instanceof JLabel) {
            ((JLabel) components[0]).setText("Total Airlines: " + (flightOwners != null ? flightOwners.size() : 0));
        }
    }

    private void addFlightOwner() {
        AdminFlightOwnerDialog dialog = new AdminFlightOwnerDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            null, 
            flightOwnerDAO
        );
        dialog.setVisible(true);
        
        if (dialog.isDataChanged()) {
            loadFlightOwners();
        }
    }

    private void editFlightOwner() {
        int selectedRow = flightOwnersTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an airline to edit.");
            return;
        }

        FlightOwner selectedOwner = flightOwners.get(selectedRow);
        AdminFlightOwnerDialog dialog = new AdminFlightOwnerDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            selectedOwner, 
            flightOwnerDAO
        );
        dialog.setVisible(true);
        
        if (dialog.isDataChanged()) {
            loadFlightOwners();
        }
    }

    private void deleteFlightOwner() {
        int selectedRow = flightOwnersTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an airline to delete.");
            return;
        }

        FlightOwner selectedOwner = flightOwners.get(selectedRow);
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            String.format("Are you sure you want to delete airline '%s (%s)'?\n\nThis will also delete all associated flights.\nThis action cannot be undone.", 
                selectedOwner.getCompanyName(), selectedOwner.getCompanyCode()),
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                boolean success = flightOwnerDAO.delete(selectedOwner.getOwnerId());
                if (success) {
                    showSuccess("Airline deleted successfully.");
                    loadFlightOwners();
                } else {
                    showError("Failed to delete airline. It may have associated flights or bookings.");
                }
            } catch (Exception e) {
                showError("Deletion failed: " + e.getMessage());
            }
        }
    }

    private void searchFlightOwners() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            displayFlightOwners(flightOwners);
            return;
        }

        // Filter flight owners based on search term
        List<FlightOwner> filteredOwners = flightOwners.stream()
            .filter(owner ->
                owner.getCompanyName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                owner.getCompanyCode().toLowerCase().contains(searchTerm.toLowerCase()) ||
                (owner.getContactInfo() != null && owner.getContactInfo().toLowerCase().contains(searchTerm.toLowerCase()))
            )
            .collect(java.util.stream.Collectors.toList());

        displayFlightOwners(filteredOwners);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
