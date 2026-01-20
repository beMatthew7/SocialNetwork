module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    // Deschidem pachetele catre JavaFX pentru a putea incarca interfata
    opens org.example.gui to javafx.fxml;
    opens org.example.domain to javafx.base; // Important pentru TableView

    exports org.example.gui;
    exports org.example;


}