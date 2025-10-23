package com.GoAero.ui;

import com.GoAero.dao.UserDAO;
import com.GoAero.model.User;
import com.GoAero.util.PasswordUtil;
import com.GoAero.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * User registration dialog for new passenger accounts
 */
public class UserRegistrationDialog extends JDialog {
    private JTextField firstNameField, lastNameField, emailField, phoneField, dobField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton registerButton, cancelButton;
    private UserDAO userDAO;

    public UserRegistrationDialog(Frame parent) {
        super(parent, "Passenger Registration", true);
        userDAO = new UserDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
    }

    private void initializeComponents() {
        setSize(550, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);

        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        dobField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        
        registerButton = new JButton("Register");
        cancelButton = new JButton("Cancel");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Create New Passenger Account");
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
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(confirmPasswordField, gbc);

        // Password requirements
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
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
    }

    private void handleRegistration() {
        if (!validateInput()) {
            return;
        }

        try {
            // Create new user
            User user = new User();
            user.setFirstName(firstNameField.getText().trim());
            user.setLastName(lastNameField.getText().trim());
            user.setEmail(emailField.getText().trim().toLowerCase());
            user.setPhone(ValidationUtil.cleanPhoneNumber(phoneField.getText().trim()));
            user.setDateOfBirth(LocalDate.parse(dobField.getText().trim()));
            user.setPasswordHash(PasswordUtil.hashPassword(new String(passwordField.getPassword())));

            // Save to database
            User savedUser = userDAO.create(user);
            if (savedUser != null) {
                showSuccess("Registration successful! You can now login with your email and password.");
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
        if (userDAO.emailExists(email)) {
            showError("An account with this email already exists.");
            emailField.requestFocus();
            return false;
        }

        // Phone validation
        String phone = phoneField.getText().trim();
        if (!ValidationUtil.isNotEmpty(phone)) {
            showError("Phone number is required.");
            phoneField.requestFocus();
            return false;
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            showError(ValidationUtil.getPhoneErrorMessage());
            phoneField.requestFocus();
            return false;
        }

        // Date of Birth validation
        String dobText = dobField.getText().trim();
        if (!ValidationUtil.isNotEmpty(dobText)) {
            showError("Date of birth is required.");
            dobField.requestFocus();
            return false;
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobText);
        } catch (DateTimeParseException e) {
            showError("Please enter date of birth in YYYY-MM-DD format.");
            dobField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isValidDateOfBirth(dob)) {
            showError("Please enter a valid date of birth. You must be at least 12 years old.");
            dobField.requestFocus();
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
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        dobField.setText("");
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
