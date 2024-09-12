package com.CSE.Pro1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnect {
    private static final String URL = "jdbc:mysql://localhost:3306/management";
    private static final String USERNAME = "root";  
    private static final String PASSWORD = "Nw9890871046!";      

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
