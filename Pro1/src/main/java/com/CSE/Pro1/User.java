package com.CSE.Pro1;

public class User {
    private String username;
    private String password;
    private String email;
    private String role;  // This can be "User" or "Admin"

    // Constructor
    public User(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // A method to check if the user is an Admin
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }

    // User-specific methods (e.g., view products, add to cart)
    public void viewProducts() {
        // Code to view available products
    }

    public void addToCart(int productId) {
        // Code to add a product to the user's cart
    }

    public void viewCart() {
        // Code to display the user's cart
    }
}
