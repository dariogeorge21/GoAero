package com.GoAero.ui;

import com.GoAero.dao.UserDAO;
import com.GoAero.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for managing user accounts in the admin dashboard
 */
public class UserManagementPanel extends JPanel {
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JButton addUserButton, editUserButton, deleteUserButton, refreshButton;
    private JTextField searchField;
    private JButton searchButton;
    private UserDAO userDAO;
    private List<User> users;

    public UserManagementPanel() {
        userDAO = new UserDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadUsers();
    }

    private void initializeComponents() {
        // Table setup
        String[] columnNames = {"ID", "Name", "Email", "Phone", "Date of Birth", "Created"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        usersTable = new JTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        // Set column widths
        usersTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        usersTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        usersTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Email
        usersTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Phone
        usersTable.getColumnModel().getColumn(4).setPreferredWidth(100); // DOB
        usersTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Created

        // Buttons
        addUserButton = new JButton("Add User");
        editUserButton = new JButton("Edit User");
        deleteUserButton = new JButton("Delete User");
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
        buttonPanel.add(addUserButton);
        buttonPanel.add(editUserButton);
        buttonPanel.add(deleteUserButton);
        buttonPanel.add(refreshButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table panel
        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Registered Users"));
        add(scrollPane, BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("Total Users: ");
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        addUserButton.addActionListener(e -> addUser());
        editUserButton.addActionListener(e -> editUser());
        deleteUserButton.addActionListener(e -> deleteUser());
        refreshButton.addActionListener(e -> loadUsers());
        searchButton.addActionListener(e -> searchUsers());
        
        // Enter key on search field
        searchField.addActionListener(e -> searchUsers());

        // Double-click to edit
        usersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editUser();
                }
            }
        });
    }

    private void loadUsers() {
        try {
            users = userDAO.findAll();
            displayUsers(users);
            updateInfoPanel();
        } catch (Exception e) {
            showError("Failed to load users: " + e.getMessage());
        }
    }

    private void displayUsers(List<User> userList) {
        // Clear existing data
        tableModel.setRowCount(0);

        // Add users to table
        for (User user : userList) {
            Object[] row = {
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone() != null ? user.getPhone() : "",
                user.getDateOfBirth() != null ? 
                    user.getDateOfBirth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "",
                user.getCreatedAt() != null ? 
                    user.getCreatedAt().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : ""
            };
            tableModel.addRow(row);
        }

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = usersTable.getSelectedRow() != -1;
        editUserButton.setEnabled(hasSelection);
        deleteUserButton.setEnabled(hasSelection);
    }

    private void updateInfoPanel() {
        // Update the info label in the south panel
        Component[] components = ((JPanel) getComponent(2)).getComponents();
        if (components.length > 0 && components[0] instanceof JLabel) {
            ((JLabel) components[0]).setText("Total Users: " + (users != null ? users.size() : 0));
        }
    }

    private void addUser() {
        AdminUserDialog dialog = new AdminUserDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            null, 
            userDAO
        );
        dialog.setVisible(true);
        
        if (dialog.isDataChanged()) {
            loadUsers();
        }
    }

    private void editUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a user to edit.");
            return;
        }

        User selectedUser = users.get(selectedRow);
        AdminUserDialog dialog = new AdminUserDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            selectedUser, 
            userDAO
        );
        dialog.setVisible(true);
        
        if (dialog.isDataChanged()) {
            loadUsers();
        }
    }

    private void deleteUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a user to delete.");
            return;
        }

        User selectedUser = users.get(selectedRow);
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            String.format("Are you sure you want to delete user '%s'?\n\nThis action cannot be undone.", 
                selectedUser.getFullName()),
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                boolean success = userDAO.delete(selectedUser.getUserId());
                if (success) {
                    showSuccess("User deleted successfully.");
                    loadUsers();
                } else {
                    showError("Failed to delete user. Please try again.");
                }
            } catch (Exception e) {
                showError("Deletion failed: " + e.getMessage());
            }
        }
    }

    private void searchUsers() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            displayUsers(users);
            return;
        }

        // Filter users based on search term
        List<User> filteredUsers = users.stream()
            .filter(user ->
                user.getFullName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                user.getEmail().toLowerCase().contains(searchTerm.toLowerCase()) ||
                (user.getPhone() != null && user.getPhone().contains(searchTerm))
            )
            .collect(java.util.stream.Collectors.toList());

        displayUsers(filteredUsers);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
