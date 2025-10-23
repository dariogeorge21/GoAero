package com.GoAero.ui;

import com.GoAero.dao.AirportDAO;
import com.GoAero.model.Airport;
import com.GoAero.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for admin to add or edit airports
 */
public class AdminAirportDialog extends JDialog {
    private Airport airport;
    private AirportDAO airportDAO;
    private boolean isEditMode;
    private boolean dataChanged = false;
    
    private JTextField codeField, nameField, cityField, countryField;
    private JButton saveButton, cancelButton;

    public AdminAirportDialog(Frame parent, Airport airport, AirportDAO airportDAO) {
        super(parent, airport == null ? "Add Airport" : "Edit Airport", true);
        this.airport = airport;
        this.airportDAO = airportDAO;
        this.isEditMode = (airport != null);
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        if (isEditMode) {
            loadAirportData();
        }
    }

    private void initializeComponents() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);

        codeField = new JTextField(20);
        nameField = new JTextField(20);
        cityField = new JTextField(20);
        countryField = new JTextField(20);
        
        saveButton = new JButton(isEditMode ? "Update Airport" : "Create Airport");
        cancelButton = new JButton("Cancel");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel(isEditMode ? "Edit Airport" : "Add New Airport");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Airport Code
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Airport Code:"), gbc);
        gbc.gridx = 1;
        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        codePanel.add(codeField);
        codePanel.add(new JLabel(" (3-4 letters)"));
        formPanel.add(codePanel, gbc);

        // Airport Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Airport Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        // City
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("City:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cityField, gbc);

        // Country
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Country:"), gbc);
        gbc.gridx = 1;
        formPanel.add(countryField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        saveButton.addActionListener(e -> saveAirport());
        cancelButton.addActionListener(e -> dispose());
        
        // Enter key on country field
        countryField.addActionListener(e -> saveAirport());

        // Auto-format airport code as user types
        codeField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String text = codeField.getText().toUpperCase();
                if (!text.equals(codeField.getText())) {
                    codeField.setText(text);
                }
            }
        });
    }

    private void loadAirportData() {
        codeField.setText(airport.getAirportCode());
        nameField.setText(airport.getAirportName());
        cityField.setText(airport.getCity());
        countryField.setText(airport.getCountry());
    }

    private void saveAirport() {
        if (!validateInput()) {
            return;
        }

        try {
            if (isEditMode) {
                updateAirport();
            } else {
                createAirport();
            }
        } catch (Exception e) {
            showError("Save failed: " + e.getMessage());
        }
    }

    private void createAirport() {
        Airport newAirport = new Airport();
        newAirport.setAirportCode(ValidationUtil.formatAirportCode(codeField.getText().trim()));
        newAirport.setAirportName(nameField.getText().trim());
        newAirport.setCity(cityField.getText().trim());
        newAirport.setCountry(countryField.getText().trim());

        Airport savedAirport = airportDAO.create(newAirport);
        if (savedAirport != null) {
            showSuccess("Airport created successfully!");
            dataChanged = true;
            dispose();
        } else {
            showError("Failed to create airport. Please try again.");
        }
    }

    private void updateAirport() {
        airport.setAirportCode(ValidationUtil.formatAirportCode(codeField.getText().trim()));
        airport.setAirportName(nameField.getText().trim());
        airport.setCity(cityField.getText().trim());
        airport.setCountry(countryField.getText().trim());

        boolean success = airportDAO.update(airport);
        if (success) {
            showSuccess("Airport updated successfully!");
            dataChanged = true;
            dispose();
        } else {
            showError("Failed to update airport. Please try again.");
        }
    }

    private boolean validateInput() {
        // Airport Code validation
        String code = codeField.getText().trim();
        if (!ValidationUtil.isValidAirportCode(code)) {
            showError(ValidationUtil.getAirportCodeErrorMessage());
            codeField.requestFocus();
            return false;
        }

        // Check if airport code already exists
        int excludeAirportId = isEditMode ? airport.getAirportId() : -1;
        if (airportDAO.codeExists(code.toUpperCase(), excludeAirportId)) {
            showError("An airport with this code already exists.");
            codeField.requestFocus();
            return false;
        }

        // Airport Name validation
        if (!ValidationUtil.isNotEmpty(nameField.getText())) {
            showError("Airport name is required.");
            nameField.requestFocus();
            return false;
        }

        if (!ValidationUtil.hasMinLength(nameField.getText(), 3)) {
            showError("Airport name must be at least 3 characters long.");
            nameField.requestFocus();
            return false;
        }

        // City validation
        if (!ValidationUtil.isNotEmpty(cityField.getText())) {
            showError("City is required.");
            cityField.requestFocus();
            return false;
        }

        if (!ValidationUtil.hasMinLength(cityField.getText(), 2)) {
            showError("City name must be at least 2 characters long.");
            cityField.requestFocus();
            return false;
        }

        // Country validation
        if (!ValidationUtil.isNotEmpty(countryField.getText())) {
            showError("Country is required.");
            countryField.requestFocus();
            return false;
        }

        if (!ValidationUtil.hasMinLength(countryField.getText(), 2)) {
            showError("Country name must be at least 2 characters long.");
            countryField.requestFocus();
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
