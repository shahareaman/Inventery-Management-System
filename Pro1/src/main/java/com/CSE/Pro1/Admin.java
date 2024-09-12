package com.CSE.Pro1;

public class Admin extends User {
    // Additional attributes or methods specific to the Admin can be added here

    // Constructor to create an Admin
    public Admin(String username, String password, String email) {
        super(username, password, email, "Admin");  // Role is set as Admin
    }

    // Admin-specific methods
    public void insertProduct(String name, double price, String category) {
        // Code to insert product
    }

    public void deleteProduct(int productId) {
        // Code to delete product by ID
    }

    public void updateProductPrice(int productId, double newPrice) {
        // Code to update product price
    }
}
