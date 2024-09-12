package com.CSE.Pro1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminGUI extends JFrame {
    private JTable productTable;
    private DefaultTableModel productTableModel;
    private JTextField productNameField, priceField, quantityField;
    private JComboBox<String> categoryComboBox;
    private JButton addProductButton, deleteProductButton, updateProductButton, addCategoryButton, viewUsersButton, logoutButton;
    private JTextField categoryField;

    public AdminGUI() {
        setTitle("Admin - Inventory Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with GridBagLayout for complex layout management
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        add(mainPanel);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        headerPanel.add(new JLabel("Admin Dashboard", JLabel.CENTER));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setBackground(new Color(0x2196F3)); // Blue header background
        headerPanel.setForeground(Color.WHITE);
        headerPanel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(headerPanel, gbc);

        // Product Table
        String[] productColumnNames = {"Product ID", "Product Name", "Price", "Quantity", "Category"};
        productTableModel = new DefaultTableModel(productColumnNames, 0);
        productTable = new JTable(productTableModel);
        JScrollPane productScrollPane = new JScrollPane(productTable);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(productScrollPane, gbc);

        // Product Details Panel
        JPanel productPanel = new JPanel();
        productPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));
        productPanel.setLayout(new GridLayout(4, 2, 10, 10));
        productPanel.setBackground(Color.WHITE);

        productPanel.add(new JLabel("Product Name:"));
        productNameField = new JTextField();
        productPanel.add(productNameField);

        productPanel.add(new JLabel("Price:"));
        priceField = new JTextField();
        productPanel.add(priceField);

        productPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        productPanel.add(quantityField);

        productPanel.add(new JLabel("Category:"));
        categoryComboBox = new JComboBox<>();
        productPanel.add(categoryComboBox);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(productPanel, gbc);

        // Category Panel
        JPanel categoryPanel = new JPanel();
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Add Category"));
        categoryPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        categoryPanel.setBackground(Color.WHITE);

        categoryPanel.add(new JLabel("Category Name:"));
        categoryField = new JTextField(15);
        categoryPanel.add(categoryField);

        addCategoryButton = new JButton("Add Category");
        categoryPanel.add(addCategoryButton);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(categoryPanel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        addProductButton = new JButton("Add Product");
        deleteProductButton = new JButton("Delete Product");
        updateProductButton = new JButton("Update Product");
        viewUsersButton = new JButton("View Users");
        logoutButton = new JButton("Logout");

        buttonPanel.add(addProductButton);
        buttonPanel.add(deleteProductButton);
        buttonPanel.add(updateProductButton);
        buttonPanel.add(viewUsersButton);
        buttonPanel.add(logoutButton);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(buttonPanel, gbc);

        // Action Listeners
        addProductButton.addActionListener(e -> addProduct());
        deleteProductButton.addActionListener(e -> deleteProduct());
        updateProductButton.addActionListener(e -> updateProduct());
        addCategoryButton.addActionListener(e -> addCategory());
        viewUsersButton.addActionListener(e -> viewUsers());
        logoutButton.addActionListener(e -> logout());

        // Load categories and products
        loadCategories();
        loadProducts();

        setVisible(true);
    }

    private void loadCategories() {
        categoryComboBox.removeAllItems();
        try (Connection conn = DBconnect.getConnection()) {
            String query = "SELECT category_name FROM categories";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categoryComboBox.addItem(rs.getString("category_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading categories.");
        }
    }

    private void loadProducts() {
        productTableModel.setRowCount(0);
        try (Connection conn = DBconnect.getConnection()) {
            String query = "SELECT p.product_id, p.product_name, p.price, p.quantity, c.category_name " +
                           "FROM products p JOIN categories c ON p.category_id = c.category_id";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    rs.getString("category_name")
                };
                productTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products.");
        }
    }

    private void addProduct() {
        String productName = productNameField.getText().trim();
        String priceText = priceField.getText().trim();
        String quantityText = quantityField.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();

        if (productName.isEmpty() || priceText.isEmpty() || quantityText.isEmpty() || category == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceText);
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price or quantity.");
            return;
        }

        try (Connection conn = DBconnect.getConnection()) {
            // Get category_id
            String categoryQuery = "SELECT category_id FROM categories WHERE category_name=?";
            PreparedStatement categoryStmt = conn.prepareStatement(categoryQuery);
            categoryStmt.setString(1, category);
            ResultSet rs = categoryStmt.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Category not found.");
                return;
            }
            int categoryId = rs.getInt("category_id");

            // Insert new product
            String insertQuery = "INSERT INTO products (product_name, price, quantity, category_id) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, productName);
            insertStmt.setDouble(2, price);
            insertStmt.setInt(3, quantity);
            insertStmt.setInt(4, categoryId);

            int rowsInserted = insertStmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                loadProducts();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding product.");
        }
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            return;
        }

        int productId = (int) productTableModel.getValueAt(selectedRow, 0);

        try (Connection conn = DBconnect.getConnection()) {
            String deleteQuery = "DELETE FROM products WHERE product_id=?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.setInt(1, productId);

            int rowsDeleted = deleteStmt.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                loadProducts();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting product.");
        }
    }

    private void updateProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.");
            return;
        }

        int productId = (int) productTableModel.getValueAt(selectedRow, 0);
        String productName = productNameField.getText().trim();
        String priceText = priceField.getText().trim();
        String quantityText = quantityField.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();

        if (productName.isEmpty() || priceText.isEmpty() || quantityText.isEmpty() || category == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceText);
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price or quantity.");
            return;
        }

        try (Connection conn = DBconnect.getConnection()) {
            // Get category_id
            String categoryQuery = "SELECT category_id FROM categories WHERE category_name=?";
            PreparedStatement categoryStmt = conn.prepareStatement(categoryQuery);
            categoryStmt.setString(1, category);
            ResultSet rs = categoryStmt.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Category not found.");
                return;
            }
            int categoryId = rs.getInt("category_id");

            // Update product
            String updateQuery = "UPDATE products SET product_name=?, price=?, quantity=?, category_id=? WHERE product_id=?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, productName);
            updateStmt.setDouble(2, price);
            updateStmt.setInt(3, quantity);
            updateStmt.setInt(4, categoryId);
            updateStmt.setInt(5, productId);

            int rowsUpdated = updateStmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Product updated successfully!");
                loadProducts();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating product.");
        }
    }

    private void addCategory() {
        String categoryName = categoryField.getText().trim();

        if (categoryName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a category name.");
            return;
        }

        try (Connection conn = DBconnect.getConnection()) {
            String query = "INSERT INTO categories (category_name) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, categoryName);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Category added successfully!");
                loadCategories();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding category.");
        }
    }

    private void viewUsers() {
        try (Connection conn = DBconnect.getConnection()) {
            String query = "SELECT * FROM users";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            JTable userTable = new JTable();
            userTable.setModel(new DefaultTableModel(new String[] {"ID", "Username", "Password", "Email", "Role"}, 0));

            DefaultTableModel userTableModel = (DefaultTableModel) userTable.getModel();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("role")
                };
                userTableModel.addRow(row);
            }

            JScrollPane userScrollPane = new JScrollPane(userTable);
            JOptionPane.showMessageDialog(this, userScrollPane, "Users", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving users.");
        }
    }

    private void logout() {
        // Close the current window and show login screen
        dispose();
        new Login();
    }

    public static void main(String[] args) {
        new AdminGUI();
    }
}
