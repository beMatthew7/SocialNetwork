package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcUtils {
    // Configurare baza de date - ADAPTEAZĂ LA EXAMEN!
    private String url = "jdbc:postgresql://localhost:5432/nume_baza_de_date";
    private String user = "postgres";
    private String password = "parola_ta";

    private Connection instance = null;

    public Connection getConnection() {
        try {
            if (instance == null || instance.isClosed()) {
                instance = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            System.err.println("Eroare la conectarea DB: " + e);
        }
        return instance;
    }
}