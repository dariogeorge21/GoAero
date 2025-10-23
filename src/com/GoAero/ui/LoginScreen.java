package com.GoAero.ui;

import com.GoAero.dao.AdminDAO;
import com.GoAero.dao.FlightOwnerDAO;
import com.GoAero.dao.UserDAO;
import com.GoAero.model.Admin;
import com.GoAero.model.FlightOwner;
import com.GoAero.model.SessionManager;
import com.GoAero.model.User;
import com.GoAero.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Unified login screen for Users, Admins, and Flight Owners
 */
public class LoginScreen extends JFrame {
    private JTabbedPane tabbedPane;
    private JTextField userEmailField, adminUsernameField, ownerCodeField;
    private JPasswordField userPasswordField, adminPasswordField, ownerPasswordField;
    private JButton userLoginButton, adminLoginButton, ownerLoginButton;
    private JButton userRegisterButton, ownerRegisterButton;
    
    private UserDAO userDAO;
    private AdminDAO adminDAO;
    private FlightOwnerDAO flightOwnerDAO;

    public LoginScreen() {
        initializeDAOs();
        initializeComponents();
        setupLayout();
        setupEventListeners();
    }

    private void initializeDAOs() {
        userDAO = new UserDAO();
        adminDAO = new AdminDAO();
        flightOwnerDAO = new FlightOwnerDAO();
    }

    private void initializeComponents() {
        setTitle("GoAero - Login");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        tabbedPane = new JTabbedPane();

        // User login components
        userEmailField = new JTextField(20);
        userPasswordField = new JPasswordField(20);
        userLoginButton = new JButton("Login");
        userRegisterButton = new JButton("Register");

        // Admin login components
        adminUsernameField = new JTextField(20);
        adminPasswordField = new JPasswordField(20);
        adminLoginButton = new JButton("Login");

        // Flight Owner login components
        ownerCodeField = new JTextField(20);
        ownerPasswordField = new JPasswordField(20);
        ownerLoginButton = new JButton("Login");
        ownerRegisterButton = new JButton("Register Company");
    }

    private void setupLayout() {
        // User login panel
        JPanel userPanel = createUserLoginPanel();
        tabbedPane.addTab("Passenger", userPanel);

        // Admin login panel
        JPanel adminPanel = createAdminLoginPanel();
        tabbedPane.addTab("Admin", adminPanel);

        // Flight Owner login panel
        JPanel ownerPanel = createFlightOwnerLoginPanel();
        tabbedPane.addTab("Airline", ownerPanel);

        add(tabbedPane);
    }

    private JPanel createUserLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel titleLabel = new JLabel("Passenger Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Email
        gbc.gridwidth = 1; gbc.gridy = 1;
        gbc.gridx = 0; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; panel.add(userEmailField, gbc);

        // Password
        gbc.gridy = 2;
        gbc.gridx = 0; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(userPasswordField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(userLoginButton);
        buttonPanel.add(userRegisterButton);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createAdminLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel titleLabel = new JLabel("Administrator Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1; gbc.gridy = 1;
        gbc.gridx = 0; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; panel.add(adminUsernameField, gbc);

        // Password
        gbc.gridy = 2;
        gbc.gridx = 0; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(adminPasswordField, gbc);

        // Login button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(adminLoginButton, gbc);

        return panel;
    }

    private JPanel createFlightOwnerLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel titleLabel = new JLabel("Airline Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Company Code
        gbc.gridwidth = 1; gbc.gridy = 1;
        gbc.gridx = 0; panel.add(new JLabel("Company Code:"), gbc);
        gbc.gridx = 1; panel.add(ownerCodeField, gbc);

        // Password
        gbc.gridy = 2;
        gbc.gridx = 0; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(ownerPasswordField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(ownerLoginButton);
        buttonPanel.add(ownerRegisterButton);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private void setupEventListeners() {
        // User login
        userLoginButton.addActionListener(e -> handleUserLogin());
        userRegisterButton.addActionListener(e -> openUserRegistration());

        // Admin login
        adminLoginButton.addActionListener(e -> handleAdminLogin());

        // Flight Owner login
        ownerLoginButton.addActionListener(e -> handleFlightOwnerLogin());
        ownerRegisterButton.addActionListener(e -> openFlightOwnerRegistration());

        // Enter key listeners
        userPasswordField.addActionListener(e -> handleUserLogin());
        adminPasswordField.addActionListener(e -> handleAdminLogin());
        ownerPasswordField.addActionListener(e -> handleFlightOwnerLogin());
    }

    private void handleUserLogin() {
        String email = userEmailField.getText().trim();
        String password = new String(userPasswordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }

        try {
            User user = userDAO.findByEmail(email);
            if (user != null && PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                SessionManager.getInstance().loginUser(user);
                showSuccess("Login successful! Welcome, " + user.getFullName());
                openUserDashboard();
                dispose();
            } else {
                showError("Invalid email or password.");
            }
        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
        }
    }

    private void handleAdminLogin() {
        String username = adminUsernameField.getText().trim();
        String password = new String(adminPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        try {
            Admin admin = adminDAO.findByUsername(username);
            if (admin != null && PasswordUtil.verifyPassword(password, admin.getPasswordHash())) {
                SessionManager.getInstance().loginAdmin(admin);
                showSuccess("Admin login successful! Welcome, " + admin.getUsername());
                openAdminDashboard();
                dispose();
            } else {
                showError("Invalid username or password.");
            }
        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
        }
    }

    private void handleFlightOwnerLogin() {
        String companyCode = ownerCodeField.getText().trim().toUpperCase();
        String password = new String(ownerPasswordField.getPassword());

        if (companyCode.isEmpty() || password.isEmpty()) {
            showError("Please enter both company code and password.");
            return;
        }

        try {
            FlightOwner owner = flightOwnerDAO.findByCode(companyCode);
            if (owner != null && PasswordUtil.verifyPassword(password, owner.getPasswordHash())) {
                SessionManager.getInstance().loginFlightOwner(owner);
                showSuccess("Login successful! Welcome, " + owner.getCompanyName());
                openFlightOwnerDashboard();
                dispose();
            } else {
                showError("Invalid company code or password.");
            }
        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
        }
    }

    private void openUserRegistration() {
        new UserRegistrationDialog(this).setVisible(true);
    }

    private void openFlightOwnerRegistration() {
        new FlightOwnerRegistrationDialog(this).setVisible(true);
    }

    private void openUserDashboard() {
        SwingUtilities.invokeLater(() -> new UserDashboard().setVisible(true));
    }

    private void openAdminDashboard() {
        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }

    private void openFlightOwnerDashboard() {
        SwingUtilities.invokeLater(() -> new FlightOwnerDashboard().setVisible(true));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void clearFields() {
        userEmailField.setText("");
        userPasswordField.setText("");
        adminUsernameField.setText("");
        adminPasswordField.setText("");
        ownerCodeField.setText("");
        ownerPasswordField.setText("");
    }
}
