package com.GoAero.ui;

import com.GoAero.dao.FlightOwnerDAO;
import com.GoAero.model.FlightOwner;
import com.GoAero.model.SessionManager;
import com.GoAero.util.PasswordUtil;
import com.GoAero.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for flight owners to view and edit their company profile
 */
public class OwnerProfilePanel extends JPanel {
    private FlightOwner currentOwner;
    private FlightOwnerDAO flightOwnerDAO;
    
    private JTextField companyNameField, companyCodeField, contactInfoField;
    private JPasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    private JButton saveButton, changePasswordButton;
    private JPanel passwordPanel;
    private boolean isPasswordChangeMode = false;

    public OwnerProfilePanel() {
        currentOwner = SessionManager.getInstance().getCurrentFlightOwner();
        if (currentOwner == null) {
            JLabel errorLabel = new JLabel("Access denied. Please login as a flight owner.");
            add(errorLabel);
            return;
        }
        
        flightOwnerDAO = new FlightOwnerDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadOwnerData();
    }

    private void initializeComponents() {
        companyNameField = new JTextField(25);
        companyCodeField = new JTextField(25);
        contactInfoField = new JTextField(25);
        
        currentPasswordField = new JPasswordField(25);
        newPasswordField = new JPasswordField(25);
        confirmPasswordField = new JPasswordField(25);
        
        saveButton = new JButton("Save Changes");
        changePasswordButton = new JButton("Change Password");
        
        // Company code field should be read-only for existing owners
        companyCodeField.setEditable(false);
        companyCodeField.setBackground(Color.LIGHT_GRAY);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Company Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Company information panel
        JPanel companyPanel = createCompanyPanel();
        contentPanel.add(companyPanel);

        // Password change panel (initially hidden)
        passwordPanel = createPasswordPanel();
        passwordPanel.setVisible(false);
        contentPanel.add(passwordPanel);

        add(contentPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(changePasswordButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createCompanyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Company Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Company Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Company Name:"), gbc);
        gbc.gridx = 1;
        panel.add(companyNameField, gbc);

        // Company Code
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Company Code:"), gbc);
        gbc.gridx = 1;
        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        codePanel.add(companyCodeField);
        codePanel.add(new JLabel(" (Cannot be changed)"));
        panel.add(codePanel, gbc);

        // Contact Info
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Contact Information:"), gbc);
        gbc.gridx = 1;
        panel.add(contactInfoField, gbc);

        return panel;
    }

    private JPanel createPasswordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Change Password"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Current Password
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        panel.add(currentPasswordField, gbc);

        // New Password
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        panel.add(newPasswordField, gbc);

        // Confirm New Password
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Confirm New Password:"), gbc);
        gbc.gridx = 1;
        panel.add(confirmPasswordField, gbc);

        // Password requirements
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JLabel requirementsLabel = new JLabel("<html><small>" + PasswordUtil.getPasswordRequirements() + "</small></html>");
        requirementsLabel.setForeground(Color.GRAY);
        panel.add(requirementsLabel, gbc);

        return panel;
    }

    private void setupEventListeners() {
        saveButton.addActionListener(e -> saveProfile());
        changePasswordButton.addActionListener(e -> togglePasswordChangeMode());
    }

    private void loadOwnerData() {
        companyNameField.setText(currentOwner.getCompanyName());
        companyCodeField.setText(currentOwner.getCompanyCode());
        contactInfoField.setText(currentOwner.getContactInfo());
    }

    private void togglePasswordChangeMode() {
        isPasswordChangeMode = !isPasswordChangeMode;
        passwordPanel.setVisible(isPasswordChangeMode);
        
        if (isPasswordChangeMode) {
            changePasswordButton.setText("Cancel Password Change");
        } else {
            changePasswordButton.setText("Change Password");
            clearPasswordFields();
        }
        
        revalidate();
        repaint();
    }

    private void clearPasswordFields() {
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void saveProfile() {
        if (!validateInput()) {
            return;
        }

        try {
            // Update owner object
            currentOwner.setCompanyName(companyNameField.getText().trim());
            currentOwner.setContactInfo(contactInfoField.getText().trim());

            // Handle password change if in password change mode
            if (isPasswordChangeMode) {
                String newPassword = new String(newPasswordField.getPassword());
                if (!newPassword.isEmpty()) {
                    currentOwner.setPasswordHash(PasswordUtil.hashPassword(newPassword));
                }
            }

            // Save to database
            boolean success = flightOwnerDAO.update(currentOwner);
            
            if (success) {
                // Update session
                SessionManager.getInstance().loginFlightOwner(currentOwner);
                showSuccess("Profile updated successfully!");
                
                if (isPasswordChangeMode) {
                    togglePasswordChangeMode();
                }
            } else {
                showError("Failed to update profile. Please try again.");
            }
            
        } catch (Exception e) {
            showError("Update failed: " + e.getMessage());
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

        // Contact Info validation
        if (!ValidationUtil.isNotEmpty(contactInfoField.getText())) {
            showError("Contact information is required.");
            contactInfoField.requestFocus();
            return false;
        }

        // Password validation if in password change mode
        if (isPasswordChangeMode) {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (currentPassword.isEmpty()) {
                showError("Please enter your current password.");
                currentPasswordField.requestFocus();
                return false;
            }

            if (!PasswordUtil.verifyPassword(currentPassword, currentOwner.getPasswordHash())) {
                showError("Current password is incorrect.");
                currentPasswordField.requestFocus();
                return false;
            }

            if (newPassword.isEmpty()) {
                showError("Please enter a new password.");
                newPasswordField.requestFocus();
                return false;
            }

            if (!PasswordUtil.isValidPassword(newPassword)) {
                showError(PasswordUtil.getPasswordRequirements());
                newPasswordField.requestFocus();
                return false;
            }

            if (!newPassword.equals(confirmPassword)) {
                showError("New passwords do not match.");
                confirmPasswordField.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
