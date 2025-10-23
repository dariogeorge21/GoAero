package com.GoAero.ui;

import com.GoAero.dao.UserDAO;
import com.GoAero.model.SessionManager;
import com.GoAero.model.User;
import com.GoAero.util.PasswordUtil;
import com.GoAero.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialog for users to view and edit their profile information
 */
public class UserProfileDialog extends JDialog {
    private User currentUser;
    private UserDAO userDAO;
    
    private JTextField firstNameField, lastNameField, emailField, phoneField, dobField;
    private JPasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    private JButton saveButton, cancelButton, changePasswordButton;
    private JPanel passwordPanel;
    private boolean isPasswordChangeMode = false;

    public UserProfileDialog(Frame parent) {
        super(parent, "My Profile", true);
        currentUser = SessionManager.getInstance().getCurrentUser();
        userDAO = new UserDAO();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadUserData();
    }

    private void initializeComponents() {
        setSize(650, 700);
        setLocationRelativeTo(getParent());
        setResizable(false);

        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        dobField = new JTextField(20);
        
        currentPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        
        saveButton = new JButton("Save Changes");
        cancelButton = new JButton("Cancel");
        changePasswordButton = new JButton("Change Password");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Profile information panel
        JPanel profilePanel = createProfilePanel();
        contentPanel.add(profilePanel);

        // Password change panel (initially hidden)
        passwordPanel = createPasswordPanel();
        passwordPanel.setVisible(false);
        contentPanel.add(passwordPanel);

        add(contentPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(changePasswordButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Profile Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // First Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        panel.add(firstNameField, gbc);

        // Last Name
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        panel.add(lastNameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        // Date of Birth
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1;
        JPanel dobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dobPanel.add(dobField);
        dobPanel.add(new JLabel(" (YYYY-MM-DD)"));
        panel.add(dobPanel, gbc);

        return panel;
    }

    private JPanel createPasswordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Change Password"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
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
        cancelButton.addActionListener(e -> dispose());
        changePasswordButton.addActionListener(e -> togglePasswordChangeMode());
    }

    private void loadUserData() {
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        dobField.setText(currentUser.getDateOfBirth() != null ? 
            currentUser.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
    }

    private void togglePasswordChangeMode() {
        isPasswordChangeMode = !isPasswordChangeMode;
        passwordPanel.setVisible(isPasswordChangeMode);
        
        if (isPasswordChangeMode) {
            changePasswordButton.setText("Cancel Password Change");
            setSize(650, 700);
        } else {
            changePasswordButton.setText("Change Password");
            setSize(650, 700);
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
            // Update user object
            currentUser.setFirstName(firstNameField.getText().trim());
            currentUser.setLastName(lastNameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim().toLowerCase());
            currentUser.setPhone(ValidationUtil.cleanPhoneNumber(phoneField.getText().trim()));
            
            String dobText = dobField.getText().trim();
            if (!dobText.isEmpty()) {
                currentUser.setDateOfBirth(LocalDate.parse(dobText));
            }

            // Handle password change if in password change mode
            if (isPasswordChangeMode) {
                String newPassword = new String(newPasswordField.getPassword());
                if (!newPassword.isEmpty()) {
                    currentUser.setPasswordHash(PasswordUtil.hashPassword(newPassword));
                }
            }

            // Save to database
            boolean success = userDAO.update(currentUser);
            
            if (success) {
                // Update session
                SessionManager.getInstance().loginUser(currentUser);
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
        // First Name validation
        if (!ValidationUtil.isNotEmpty(firstNameField.getText())) {
            showError("First name is required.");
            firstNameField.requestFocus();
            return false;
        }

        // Last Name validation
        if (!ValidationUtil.isNotEmpty(lastNameField.getText())) {
            showError("Last name is required.");
            lastNameField.requestFocus();
            return false;
        }

        // Email validation
        String email = emailField.getText().trim();
        if (!ValidationUtil.isValidEmail(email)) {
            showError(ValidationUtil.getEmailErrorMessage());
            emailField.requestFocus();
            return false;
        }

        // Check if email already exists for another user
        if (userDAO.emailExists(email, currentUser.getUserId())) {
            showError("An account with this email already exists.");
            emailField.requestFocus();
            return false;
        }

        // Phone validation (optional)
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            showError(ValidationUtil.getPhoneErrorMessage());
            phoneField.requestFocus();
            return false;
        }

        // Date of Birth validation (optional)
        String dobText = dobField.getText().trim();
        if (!dobText.isEmpty()) {
            try {
                LocalDate dob = LocalDate.parse(dobText);
                if (!ValidationUtil.isValidDateOfBirth(dob)) {
                    showError("Please enter a valid date of birth. You must be at least 12 years old.");
                    dobField.requestFocus();
                    return false;
                }
            } catch (DateTimeParseException e) {
                showError("Please enter date of birth in YYYY-MM-DD format.");
                dobField.requestFocus();
                return false;
            }
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

            if (!PasswordUtil.verifyPassword(currentPassword, currentUser.getPasswordHash())) {
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
