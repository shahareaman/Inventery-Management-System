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

public class UserGUI extends JFrame {
    private JTable productTable;
    private DefaultTableModel productTableModel;
    private JButton addToCartButton;
    private JButton viewCartButton;
    private JButton orderButton;
    private JButton logoutButton;
    private JTextField quantityField;
    private JPanel mainPanel;
    private JScrollPane productScrollPane;

    public UserGUI() {
        setTitle("User Interface - Product Catalog");
        setSize(900, 700);  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);  

        JLabel titleLabel = new JLabel("Welcome to Our Store");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(new Color(0, 102, 204)); 
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        productTableModel = new DefaultTableModel(new String[] {"Product ID", "Product Name", "Price", "Quantity"}, 0);
        productTable = new JTable(productTableModel);
        productTable.setFillsViewportHeight(true);
        productTable.setFont(new Font("Arial", Font.PLAIN, 14));
        productTable.setRowHeight(25); 
        productScrollPane = new JScrollPane(productTable);
        mainPanel.add(productScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottomPanel.setBackground(new Color(240, 240, 240));  

        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        bottomPanel.add(quantityLabel);

        quantityField = new JTextField(5);
        quantityField.setFont(new Font("Arial", Font.PLAIN, 18));
        bottomPanel.add(quantityField);

        addToCartButton = new JButton("Add to Cart");
        addToCartButton.setFont(new Font("Arial", Font.BOLD, 18));
        addToCartButton.setBackground(new Color(0, 153, 76)); 
        addToCartButton.setForeground(Color.WHITE);
        bottomPanel.add(addToCartButton);

        viewCartButton = new JButton("View Cart");
        viewCartButton.setFont(new Font("Arial", Font.BOLD, 18));
        viewCartButton.setBackground(new Color(0, 102, 204)); 
        viewCartButton.setForeground(Color.WHITE);
        bottomPanel.add(viewCartButton);

        orderButton = new JButton("Order");
        orderButton.setFont(new Font("Arial", Font.BOLD, 18));
        orderButton.setBackground(new Color(255, 153, 51)); 
        orderButton.setForeground(Color.WHITE);
        bottomPanel.add(orderButton);

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 18));
        logoutButton.setBackground(new Color(204, 0, 0)); 
        logoutButton.setForeground(Color.WHITE);
        bottomPanel.add(logoutButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        addToCartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addToCart();
            }
        });

        viewCartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewCart();
            }
        });

        orderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeOrder();
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        loadProducts();
        add(mainPanel);
        setVisible(true);
    }

    private void loadProducts() {
        try (Connection conn = DBconnect.getConnection()) {
            String query = "SELECT * FROM products";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            productTableModel.setRowCount(0);  
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                };
                productTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products.");
        }
    }

    private void addToCart() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product.");
            return;
        }

        String quantityText = quantityField.getText().trim();
        if (quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a quantity.");
            return;
        }

        int productId = (int) productTable.getValueAt(selectedRow, 0);
        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.");
            return;
        }

        try (Connection conn = DBconnect.getConnection()) {
            String checkQuery = "SELECT quantity FROM products WHERE product_id=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, productId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                int availableQuantity = rs.getInt("quantity");
                if (quantity > availableQuantity) {
                    JOptionPane.showMessageDialog(this, "Not enough stock available.");
                    return;
                }

                String insertQuery = "INSERT INTO orders (user_id, product_id, quantity) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, /* user_id */ 1);  
                insertStmt.setInt(2, productId);
                insertStmt.setInt(3, quantity);
                int rowsInserted = insertStmt.executeUpdate();

                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Product added to cart successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Error adding product to cart.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding product to cart.");
        }
    }

    private void viewCart() {
        try (Connection conn = DBconnect.getConnection()) {
            String query = "SELECT p.product_name, o.quantity, p.price FROM orders o JOIN products p ON o.product_id = p.product_id WHERE o.user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, /* user_id */ 1);  
            ResultSet rs = stmt.executeQuery();

            JTable cartTable = new JTable();
            cartTable.setModel(new DefaultTableModel(new String[] {"Product Name", "Quantity", "Price"}, 0));
            cartTable.setFont(new Font("Arial", Font.PLAIN, 14));
            cartTable.setRowHeight(25); 

            DefaultTableModel cartTableModel = (DefaultTableModel) cartTable.getModel();
            while (rs.next()) {
                Object[] row = {
                    rs.getString("product_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")
                };
                cartTableModel.addRow(row);
            }

            JScrollPane cartScrollPane = new JScrollPane(cartTable);
            JOptionPane.showMessageDialog(this, cartScrollPane, "Your Cart", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving cart.");
        }
    }

    private void placeOrder() {
        try (Connection conn = DBconnect.getConnection()) {
            String query = "DELETE FROM orders WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, /* user_id */ 1);  // Replace with actual user ID
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Order placed successfully!");
                quantityField.setText("");  // Reset the quantity field
            } else {
                JOptionPane.showMessageDialog(this, "Your cart is empty.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error placing order.");
        }
    }

    private void logout() {
        dispose();
        new Login();
    }

    public static void main(String[] args) {
        new UserGUI();
    }
}
