package com.GoAero.ui;

import com.GoAero.dao.UserDAO;
import com.GoAero.model.User;
import com.GoAero.util.PasswordUtil;
import com.GoAero.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialog for admin to add or edit user accounts
 */
public class AdminUserDialog extends JDialog {
    private User user;
    private UserDAO userDAO;
    private boolean isEditMode;
    private boolean dataChanged = false;
    
    private JTextField firstNameField, lastNameField, emailField, phoneField, dobField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton saveButton, cancelButton, generatePasswordButton;
    private JLabel passwordLabel, confirmPasswordLabel;

    public AdminUserDialog(Frame parent, User user, UserDAO userDAO) {
        super(parent, user == null ? "Add User" : "Edit User", true);
        this.user = user;
        this.userDAO = userDAO;
        this.isEditMode = (user != null);
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        if (isEditMode) {
            loadUserData();
        }
    }

    private void initializeComponents() {
        setSize(450, 500);
        setLocationRelativeTo(getParent());
        setResizable(false);

        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        dobField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        
        saveButton = new JButton(isEditMode ? "Update User" : "Create User");
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
        JLabel titleLabel = new JLabel(isEditMode ? "Edit User Account" : "Create New User Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // First Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(firstNameField, gbc);

        // Last Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(lastNameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);

        // Date of Birth
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1;
        JPanel dobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dobPanel.add(dobField);
        dobPanel.add(new JLabel(" (YYYY-MM-DD)"));
        formPanel.add(dobPanel, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        passwordPanel.add(passwordField);
        passwordPanel.add(generatePasswordButton);
        formPanel.add(passwordPanel, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(confirmPasswordLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(confirmPasswordField, gbc);

        // Password requirements
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
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
        saveButton.addActionListener(e -> saveUser());
        cancelButton.addActionListener(e -> dispose());
        generatePasswordButton.addActionListener(e -> generatePassword());
        
        // Enter key on confirm password field
        confirmPasswordField.addActionListener(e -> saveUser());
    }

    private void loadUserData() {
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
        dobField.setText(user.getDateOfBirth() != null ? 
            user.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
    }

    private void generatePassword() {
        String generatedPassword = PasswordUtil.generateRandomPassword(8);
        passwordField.setText(generatedPassword);
        confirmPasswordField.setText(generatedPassword);
        
        JOptionPane.showMessageDialog(this, 
            "Generated password: " + generatedPassword + "\n\nPlease save this password and share it with the user.",
            "Password Generated", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveUser() {
        if (!validateInput()) {
            return;
        }

        try {
            if (isEditMode) {
                updateUser();
            } else {
                createUser();
            }
        } catch (Exception e) {
            showError("Save failed: " + e.getMessage());
        }
    }

    private void createUser() {
        User newUser = new User();
        newUser.setFirstName(firstNameField.getText().trim());
        newUser.setLastName(lastNameField.getText().trim());
        newUser.setEmail(emailField.getText().trim().toLowerCase());
        newUser.setPhone(ValidationUtil.cleanPhoneNumber(phoneField.getText().trim()));
        
        String dobText = dobField.getText().trim();
        if (!dobText.isEmpty()) {
            newUser.setDateOfBirth(LocalDate.parse(dobText));
        }
        
        newUser.setPasswordHash(PasswordUtil.hashPassword(new String(passwordField.getPassword())));

        User savedUser = userDAO.create(newUser);
        if (savedUser != null) {
            showSuccess("User created successfully!");
            dataChanged = true;
            dispose();
        } else {
            showError("Failed to create user. Please try again.");
        }
    }

    private void updateUser() {
        user.setFirstName(firstNameField.getText().trim());
        user.setLastName(lastNameField.getText().trim());
        user.setEmail(emailField.getText().trim().toLowerCase());
        user.setPhone(ValidationUtil.cleanPhoneNumber(phoneField.getText().trim()));
        
        String dobText = dobField.getText().trim();
        if (!dobText.isEmpty()) {
            user.setDateOfBirth(LocalDate.parse(dobText));
        }

        // Update password only if new password is provided
        String newPassword = new String(passwordField.getPassword());
        if (!newPassword.isEmpty()) {
            user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        }

        boolean success = userDAO.update(user);
        if (success) {
            showSuccess("User updated successfully!");
            dataChanged = true;
            dispose();
        } else {
            showError("Failed to update user. Please try again.");
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

        // Check if email already exists
        int excludeUserId = isEditMode ? user.getUserId() : -1;
        if (userDAO.emailExists(email, excludeUserId)) {
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
                    showError("Please enter a valid date of birth. User must be at least 12 years old.");
                    dobField.requestFocus();
                    return false;
                }
            } catch (DateTimeParseException e) {
                showError("Please enter date of birth in YYYY-MM-DD format.");
                dobField.requestFocus();
                return false;
            }
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
