package org.example.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.domain.User;
import org.example.service.*;

import java.io.IOException;
import java.util.List;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private UserService userService;
    private FriendshipService friendshipService;
    private CardService cardService;
    private EventService eventService;
    private org.example.service.MessageService messageService;
    private Stage stage;
    private FriendRequestService requestService;
    private RaceEventService raceService; // [NEW]

    /**
     * Setează serviciile necesare pentru LoginController.
     * Acestea vin din MainApp.
     */
    public void setServices(UserService u, FriendshipService f, CardService c, EventService e, org.example.service.MessageService m, Stage stage, FriendRequestService requestService, RaceEventService raceService) {
        this.userService = u;
        this.friendshipService = f;
        this.cardService = c;
        this.eventService = e;
        this.messageService = m;
        this.stage = stage;
        this.requestService = requestService;
        this.raceService = raceService;
    }

    @FXML
    public void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        try {
            // Încercăm să găsim utilizatorul (Persoană sau Rață)
            User loggedUser = userService.login(user, pass);

            if (loggedUser != null) {
                // Încărcăm prietenii utilizatorului înainte de a deschide fereastra principală
                if (friendshipService != null) {
                    List<User> friends = friendshipService.getFriendsForUser(loggedUser);
                    loggedUser.getFriends().clear();
                    loggedUser.getFriends().addAll(friends);
                }

                // Dacă datele sunt corecte, deschidem fereastra principală
                openMainView(loggedUser);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = ex.getMessage();
            if (msg == null) {
                msg = "Internal error: " + ex.toString();
            }
            errorLabel.setText(msg);
        }
    }

    @FXML
    public void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/gui/register-view.fxml"));
            Scene scene = new Scene(loader.load());

            Stage registerStage = new Stage();
            registerStage.setTitle("Registration");
            registerStage.setScene(scene);

            RegisterController controller = loader.getController();
            controller.setService(userService, registerStage);

            registerStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Error opening window: " + e.getMessage());
        }
    }

    @FXML
    public void handleNewWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/gui/login-view.fxml"));
            Stage newStage = new Stage();
            newStage.setScene(new Scene(loader.load()));
            newStage.setTitle("New Login Window");
            
            LoginController controller = loader.getController();
            // Pass the SAME service instances to share state/DB connection
            controller.setServices(userService, friendshipService, cardService, eventService, messageService, newStage, requestService, raceService);
            
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Error opening new window: " + e.getMessage());
        }
    }

    /**
     * Deschide fereastra principală (Duck View) după logare reușită.
     */
    private void openMainView(User user) throws IOException {
        // 1. Încărcăm fișierul FXML pentru fereastra principală
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/gui/duck-view.fxml"));
        Scene scene = new Scene(loader.load());

        // 2. Obținem controller-ul ferestrei principale
        DuckController controller = loader.getController();

        // 3. Îi pasăm TOATE datele necesare (servicii + utilizatorul logat)
        controller.setServices(userService, friendshipService, cardService, eventService, messageService, user, stage, requestService, raceService);

        // 4. Schimbăm scena pe fereastra curentă
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setTitle("Duck Social Network - Logged in as: " + user.getUsername());
    }
}