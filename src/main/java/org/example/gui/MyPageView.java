package org.example.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.domain.Duck;
import org.example.domain.Person;
import org.example.domain.User;

import java.io.InputStream;

public class MyPageView {
    private final User user;
    private final Stage stage;

    public MyPageView(User user) {
        this.user = user;
        this.stage = new Stage();
        initUI();
    }

    private void initUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(400, 500);
        root.setStyle("-fx-background-color: #f4f4f4; -fx-font-family: 'Segoe UI', sans-serif;");


        Label lblTitle = new Label("My Profile");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        String emoji = (user instanceof Duck) ? "🦆" : "👱";
        Label photoPlaceholder = new Label(emoji);
        photoPlaceholder.setMinSize(100, 100);
        photoPlaceholder.setMaxSize(100, 100);
        photoPlaceholder.setStyle(
                "-fx-background-color: #e8f4f8; " +
                        "-fx-alignment: center; " +
                        "-fx-background-radius: 50; " +
                        "-fx-font-size: 60px;"
        );
        
        Label lblName = new Label(user.getUsername());
        lblName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label lblEmail = new Label(user.getEmail());
        lblEmail.setStyle("-fx-text-fill: #666;");

        VBox detailsBox = new VBox(10);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        detailsBox.setPadding(new Insets(10));
        detailsBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        if (user instanceof Duck) {
            Duck duck = (Duck) user;
            detailsBox.getChildren().addAll(
                styledLabel("Type: " + duck.getType()),
                styledLabel("Speed: " + duck.getSpeed()),
                styledLabel("Endurance: " + duck.getEndurance())
            );
        } else if (user instanceof Person) {
            Person person = (Person) user;
            detailsBox.getChildren().addAll(
                styledLabel("First Name: " + person.getFirstName()),
                styledLabel("Last Name: " + person.getSecondName()),
                styledLabel("Occupation: " + person.getOccupation()),
                styledLabel("Empathy Level: " + person.getEmpathyNivel())
            );
        }

        Button btnClose = new Button("Close");
        btnClose.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-cursor: hand;");
        btnClose.setOnAction(e -> stage.close());

        root.getChildren().addAll(lblTitle, photoPlaceholder, lblName, lblEmail, detailsBox, btnClose);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("My Page - " + user.getUsername());
    }

    private Label styledLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14px;");
        return l;
    }

    public void show() {
        stage.show();
    }
}
