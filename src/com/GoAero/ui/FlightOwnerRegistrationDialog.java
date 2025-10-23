package com.GoAero.ui;

import com.GoAero.dao.FlightOwnerDAO;
import com.GoAero.model.FlightOwner;
import com.GoAero.util.PasswordUtil;
import com.GoAero.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Flight Owner registration dialog for new airline companies
 */
public class FlightOwnerRegistrationDialog extends JDialog {
    private JTextField companyNameField, companyCodeField, contactInfoField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton registerButton, cancelButton;
    private FlightOwnerDAO flightOwnerDAO;

    public FlightOwnerRegistrationDialog(Frame parent) {
        super(parent, "Airline Registration", true);
        flightOwnerDAO = new FlightOwnerDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
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
        
        registerButton = new JButton("Register Company");
        cancelButton = new JButton("Cancel");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Register New Airline Company");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
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
        codePanel.add(new JLabel(" (2-5 letters/numbers)"));
        formPanel.add(codePanel, gbc);

        // Contact Info
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Contact Info:"), gbc);
        gbc.gridx = 1;
        formPanel.add(contactInfoField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(confirmPasswordField, gbc);

        // Password requirements
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JLabel requirementsLabel = new JLabel("<html><small>" + PasswordUtil.getPasswordRequirements() + "</small></html>");
        requirementsLabel.setForeground(Color.GRAY);
        formPanel.add(requirementsLabel, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        registerButton.addActionListener(e -> handleRegistration());
        cancelButton.addActionListener(e -> dispose());

        // Enter key on confirm password field
        confirmPasswordField.addActionListener(e -> handleRegistration());

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

    private void handleRegistration() {
        if (!validateInput()) {
            return;
        }

        try {
            // Create new flight owner
            FlightOwner flightOwner = new FlightOwner();
            flightOwner.setCompanyName(companyNameField.getText().trim());
            flightOwner.setCompanyCode(ValidationUtil.formatCompanyCode(companyCodeField.getText().trim()));
            flightOwner.setContactInfo(contactInfoField.getText().trim());
            flightOwner.setPasswordHash(PasswordUtil.hashPassword(new String(passwordField.getPassword())));
            flightOwner.setFlightCount(0);

            // Save to database
            FlightOwner savedOwner = flightOwnerDAO.create(flightOwner);
            if (savedOwner != null) {
                showSuccess("Company registration successful! You can now login with your company code and password.");
                clearFields();
                dispose();
            } else {
                showError("Registration failed. Please try again.");
            }
        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
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
        String companyCode = companyCodeField.getText().trim();
        if (!ValidationUtil.isValidCompanyCode(companyCode)) {
            showError(ValidationUtil.getCompanyCodeErrorMessage());
            companyCodeField.requestFocus();
            return false;
        }

        // Check if company code already exists
        if (flightOwnerDAO.codeExists(companyCode.toUpperCase())) {
            showError("A company with this code already exists. Please choose a different code.");
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
        if (!PasswordUtil.isValidPassword(password)) {
            showError(PasswordUtil.getPasswordRequirements());
            passwordField.requestFocus();
            return false;
        }

        // Confirm password validation
        String confirmPassword = new String(confirmPasswordField.getPassword());
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            confirmPasswordField.requestFocus();
            return false;
        }

        return true;
    }

    private void clearFields() {
        companyNameField.setText("");
        companyCodeField.setText("");
        contactInfoField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
