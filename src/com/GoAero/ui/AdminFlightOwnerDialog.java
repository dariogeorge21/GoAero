package com.GoAero.ui;

import com.GoAero.dao.FlightOwnerDAO;
import com.GoAero.model.FlightOwner;
import com.GoAero.util.PasswordUtil;
import com.GoAero.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for admin to add or edit flight owners (airline companies)
 */
public class AdminFlightOwnerDialog extends JDialog {
    private FlightOwner flightOwner;
    private FlightOwnerDAO flightOwnerDAO;
    private boolean isEditMode;
    private boolean dataChanged = false;
    
    private JTextField companyNameField, companyCodeField, contactInfoField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton saveButton, cancelButton, generatePasswordButton;
    private JLabel passwordLabel, confirmPasswordLabel;

    public AdminFlightOwnerDialog(Frame parent, FlightOwner flightOwner, FlightOwnerDAO flightOwnerDAO) {
        super(parent, flightOwner == null ? "Add Airline" : "Edit Airline", true);
        this.flightOwner = flightOwner;
        this.flightOwnerDAO = flightOwnerDAO;
        this.isEditMode = (flightOwner != null);
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        if (isEditMode) {
            loadFlightOwnerData();
        }
    }

    private void initializeComponents() {
        setSize(550, 500);
        setLocationRelativeTo(getParent());
        setResizable(false);

        companyNameField = new JTextField(20);
        companyCodeField = new JTextField(20);
        contactInfoField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        
        saveButton = new JButton(isEditMode ? "Update Airline" : "Create Airline");
        cancelButton = new JButton("Cancel");
        generatePasswordButton = new JButton("Generate");
        
        passwordLabel = new JLabel("Password:");
        confirmPasswordLabel = new JLabel("Confirm Password:");
        
        // In edit mode, password fields are optional
        if (isEditMode) {
            passwordLabel.setText("New Password (optional):");
            confirmPasswordLabel.setText("Confirm New Password:");
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel(isEditMode ? "Edit Airline Company" : "Add New Airline Company");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Company Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Company Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(companyNameField, gbc);

        // Company Code
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Company Code:"), gbc);
        gbc.gridx = 1;
        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        codePanel.add(companyCodeField);
        codePanel.add(new JLabel(" (2-3 letters)"));
        formPanel.add(codePanel, gbc);

        // Contact Info
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Contact Info:"), gbc);
        gbc.gridx = 1;
        formPanel.add(contactInfoField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        passwordPanel.add(passwordField);
        passwordPanel.add(generatePasswordButton);
        formPanel.add(passwordPanel, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(confirmPasswordLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(confirmPasswordField, gbc);

        // Password requirements
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        String requirementText = isEditMode ? 
            "Leave password fields empty to keep current password" : 
            PasswordUtil.getPasswordRequirements();
        JLabel requirementsLabel = new JLabel("<html><small>" + requirementText + "</small></html>");
        requirementsLabel.setForeground(Color.GRAY);
        formPanel.add(requirementsLabel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        saveButton.addActionListener(e -> saveFlightOwner());
        cancelButton.addActionListener(e -> dispose());
        generatePasswordButton.addActionListener(e -> generatePassword());
        
        // Enter key on confirm password field
        confirmPasswordField.addActionListener(e -> saveFlightOwner());

        // Auto-format company code as user types
        companyCodeField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String text = companyCodeField.getText().toUpperCase();
                if (!text.equals(companyCodeField.getText())) {
                    companyCodeField.setText(text);
                }
            }
        });
    }

    private void loadFlightOwnerData() {
        companyNameField.setText(flightOwner.getCompanyName());
        companyCodeField.setText(flightOwner.getCompanyCode());
        contactInfoField.setText(flightOwner.getContactInfo());
    }

    private void generatePassword() {
        String generatedPassword = PasswordUtil.generateRandomPassword(8);
        passwordField.setText(generatedPassword);
        confirmPasswordField.setText(generatedPassword);
        
        JOptionPane.showMessageDialog(this, 
            "Generated password: " + generatedPassword + "\n\nPlease save this password and share it with the airline company.",
            "Password Generated", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveFlightOwner() {
        if (!validateInput()) {
            return;
        }

        try {
            if (isEditMode) {
                updateFlightOwner();
            } else {
                createFlightOwner();
            }
        } catch (Exception e) {
            showError("Save failed: " + e.getMessage());
        }
    }

    private void createFlightOwner() {
        FlightOwner newOwner = new FlightOwner();
        newOwner.setCompanyName(companyNameField.getText().trim());
        newOwner.setCompanyCode(ValidationUtil.formatCompanyCode(companyCodeField.getText().trim()));
        newOwner.setContactInfo(contactInfoField.getText().trim());
        newOwner.setPasswordHash(PasswordUtil.hashPassword(new String(passwordField.getPassword())));

        FlightOwner savedOwner = flightOwnerDAO.create(newOwner);
        if (savedOwner != null) {
            showSuccess("Airline created successfully!");
            dataChanged = true;
            dispose();
        } else {
            showError("Failed to create airline. Please try again.");
        }
    }

    private void updateFlightOwner() {
        flightOwner.setCompanyName(companyNameField.getText().trim());
        flightOwner.setCompanyCode(ValidationUtil.formatCompanyCode(companyCodeField.getText().trim()));
        flightOwner.setContactInfo(contactInfoField.getText().trim());

        // Update password only if new password is provided
        String newPassword = new String(passwordField.getPassword());
        if (!newPassword.isEmpty()) {
            flightOwner.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        }

        boolean success = flightOwnerDAO.update(flightOwner);
        if (success) {
            showSuccess("Airline updated successfully!");
            dataChanged = true;
            dispose();
        } else {
            showError("Failed to update airline. Please try again.");
        }
    }

    private boolean validateInput() {
        // Company Name validation
        if (!ValidationUtil.isNotEmpty(companyNameField.getText())) {
            showError("Company name is required.");
            companyNameField.requestFocus();
            return false;
        }

        if (!ValidationUtil.hasMinLength(companyNameField.getText(), 2)) {
            showError("Company name must be at least 2 characters long.");
            companyNameField.requestFocus();
            return false;
        }

        // Company Code validation
        String code = companyCodeField.getText().trim();
        if (!ValidationUtil.isValidCompanyCode(code)) {
            showError(ValidationUtil.getCompanyCodeErrorMessage());
            companyCodeField.requestFocus();
            return false;
        }

        // Check if company code already exists
        int excludeOwnerId = isEditMode ? flightOwner.getOwnerId() : -1;
        if (flightOwnerDAO.codeExists(code.toUpperCase(), excludeOwnerId)) {
            showError("An airline with this company code already exists.");
            companyCodeField.requestFocus();
            return false;
        }

        // Contact Info validation
        if (!ValidationUtil.isNotEmpty(contactInfoField.getText())) {
            showError("Contact information is required.");
            contactInfoField.requestFocus();
            return false;
        }

        // Password validation
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!isEditMode || !password.isEmpty()) {
            if (password.isEmpty()) {
                showError("Password is required.");
                passwordField.requestFocus();
                return false;
            }

            if (!PasswordUtil.isValidPassword(password)) {
                showError(PasswordUtil.getPasswordRequirements());
                passwordField.requestFocus();
                return false;
            }

            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match.");
                confirmPasswordField.requestFocus();
                return false;
            }
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
