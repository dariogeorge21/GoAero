package com.GoAero.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LandingPage extends JFrame {

    public LandingPage() {
        // Frame setup
        setTitle("Welcome to Flight Booking");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame

        // Use a background panel
        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Title Label
        JLabel titleLabel = new JLabel("Your Journey Begins Here");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        mainPanel.add(titleLabel, gbc);

        // Book Ticket Button
        JButton bookTicketButton = new JButton("Book a Flight");
        bookTicketButton.setFont(new Font("Arial", Font.BOLD, 24));
        bookTicketButton.setPreferredSize(new Dimension(300, 60));
        bookTicketButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.insets = new Insets(20, 10, 10, 10);
        mainPanel.add(bookTicketButton, gbc);

        // Admin Login Label (acts as a hyperlink)
        JLabel adminLoginLabel = new JLabel("<html><u>Admin Login</u></html>");
        adminLoginLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        adminLoginLabel.setForeground(Color.WHITE);
        adminLoginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.anchor = GridBagConstraints.PAGE_END; // Pushes it to the bottom
        gbc.weighty = 1.0; // Fills vertical space
        mainPanel.add(adminLoginLabel, gbc);

        // --- Action Listeners ---

        // Action for the Book Ticket button
        bookTicketButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the Login screen
                new LoginScreen().setVisible(true);
                // Hide the landing page
                LandingPage.this.setVisible(false);
            }
        });

        // Mouse listener for the Admin Login label to make it clickable
        adminLoginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Open the Login screen with Admin tab selected
                LoginScreen loginScreen = new LoginScreen();
                loginScreen.setVisible(true);
                // Hide the landing page
                LandingPage.this.setVisible(false);
            }
        });
    }

    // A custom panel for drawing a background image
    static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel() {
            try {
                // Load an image from a URL for the background
                // Using a placeholder service here for a nice travel-themed image
                backgroundImage = new ImageIcon(new java.net.URL("https://placehold.co/800x600/003366/FFFFFF?text=Flight+Booking&font=serif")).getImage();
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to a solid color if the image fails to load
                setBackground(new Color(0, 51, 102));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // Draw the image, scaling it to cover the entire panel
                g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        }
    }
}
