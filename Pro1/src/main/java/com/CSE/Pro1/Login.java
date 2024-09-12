package com.CSE.Pro1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JComboBox<String> roleComboBox;

    public Login() {
        setTitle("Login System");
        setSize(500, 400);  // Slightly larger window for better spacing
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Center the window on the screen

        // Main panel with BoxLayout for vertical stacking of components
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),  // Outer line border
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));  // Inner padding
        mainPanel.setBackground(Color.WHITE);  // Set background color to white

        // Create a title label
        JLabel titleLabel = new JLabel("Inventory Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        // Add spacing between title and form
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Center the form fields (Username, Password, Email, Role)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(4, 2, 10, 10));  // Grid layout for the form fields (2 columns)
        formPanel.setBackground(Color.WHITE);  // Set background color to white

        // Username field
        formPanel.add(new JLabel("Username:", SwingConstants.CENTER));
        usernameField = new JTextField(20);
        formPanel.add(usernameField);

        // Password field
        formPanel.add(new JLabel("Password:", SwingConstants.CENTER));
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField);

        // Email field
        formPanel.add(new JLabel("Email:", SwingConstants.CENTER));
        emailField = new JTextField(20);
        formPanel.add(emailField);

        // Role selection
        formPanel.add(new JLabel("Role:", SwingConstants.CENTER));
        roleComboBox = new JComboBox<>(new String[]{"User", "Admin"});
        formPanel.add(roleComboBox);

        // Add formPanel to mainPanel and center it
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(formPanel);

        // Add spacing between form and buttons
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);  // Set background color to white
        JButton loginButton = new JButton("Login");
        JButton createButton = new JButton("Create New User");

        loginButton.setPreferredSize(new Dimension(150, 30));
        createButton.setPreferredSize(new Dimension(150, 30));

        buttonPanel.add(loginButton);
        buttonPanel.add(createButton);
        mainPanel.add(buttonPanel);

        // Add action listeners for buttons
        loginButton.addActionListener(e -> login());
        createButton.addActionListener(e -> createUser());

        // Add main panel to the frame
        add(mainPanel);

        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleComboBox.getSelectedItem();

        // Check for blank fields
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid details! Please fill all fields.");
            return;
        }

        try (Connection conn = DBconnect.getConnection()) {
            String query = "SELECT * FROM users WHERE username=? AND password=? AND role=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login successful as " + role);
                
                // Open AdminGUI or UserGUI based on role
                if (role.equals("Admin")) {
                    new AdminGUI();  // Open Admin GUI
                } else {
                    new UserGUI();  // Open User GUI
                }

                dispose();  // Close login window
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void createUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String email = emailField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        // Check for blank fields
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid details! Please fill all fields.");
            return;
        }

        try (Connection conn = DBconnect.getConnection()) {
            // Check if username already exists
            String checkQuery = "SELECT * FROM users WHERE username=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username already taken!");
            } else {
                // Insert new user into the database
                String insertQuery = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, email);
                insertStmt.setString(4, role);

                int rowsInserted = insertStmt.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "User created successfully!");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Login();
    }
}
