package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.domain.Duck;
import org.example.domain.Friendship;
import org.example.domain.Person;
import org.example.domain.User;
import org.example.domain.validators.DuckValidator;
import org.example.domain.validators.NoOpValidator;
import org.example.domain.validators.PersonValidator;
import org.example.domain.validators.Validator;
import org.example.gui.DuckController;
import org.example.interactions.DuckCard;
import org.example.interactions.Event;
import org.example.repository.*;
import org.example.service.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main1 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Configurare Baza de Date
        Properties dbProps = new Properties();
        String dbUrl, dbUser, dbPassword;

        try (InputStream input = Main1.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("Nu s-a gasit db.properties");
                return;
            }
            dbProps.load(input);
            dbUrl = dbProps.getProperty("db.url");
            dbUser = dbProps.getProperty("db.username");
            dbPassword = dbProps.getProperty("db.password");
        }

        // 2. Initializare Repository-uri (Toate pe DB)
        Validator<Person> personValidator = new PersonValidator();
        Validator<Duck> duckValidator = new DuckValidator();

        Repository<Long, Person> personRepo = new PersonDbRepository(dbUrl, dbUser, dbPassword, personValidator);


        DuckRepo duckRepo = new DuckDbRepository(dbUrl, dbUser, dbPassword, duckValidator);

        Repository<Long, DuckCard> cardRepo = new CardDbRepository(dbUrl, dbUser, dbPassword);
        FriendshipRepo friendshipRepo = new FriendshipDbRepository(dbUrl, dbUser, dbPassword, new NoOpValidator<>());
        CardMembershipDbRepository membershipRepo = new CardMembershipDbRepository(dbUrl, dbUser, dbPassword);
        EventHistoryRepository historyRepo = new EventHistoryRepository(dbUrl, dbUser, dbPassword);

        Repository<Long, Event> eventRepo = new EventDbRepository(dbUrl, dbUser, dbPassword, personRepo, duckRepo);

        // 3. Initializare Servicii
        UserService userService = new UserService(personRepo, duckRepo);
        CardService cardService = new CardService(cardRepo, (DuckDbRepository) duckRepo, membershipRepo);
        FriendshipService friendshipService = new FriendshipService(personRepo, duckRepo, friendshipRepo);
        EventService eventService = new EventService(eventRepo, historyRepo);

        MessageDbRepository messageRepo = new org.example.repository.MessageDbRepository(dbUrl, dbUser, dbPassword, personRepo, (DuckDbRepository) duckRepo);
        MessageService messageService = new org.example.service.MessageService(messageRepo);

        FriendRequestRepository repoRequest = new FriendRequestDbRepository(dbUrl, dbUser, dbPassword, personRepo, duckRepo);

        FriendRequestService requestService = new FriendRequestService(repoRequest, friendshipService);
        
        RaceEventService raceService = new RaceEventService(eventRepo, historyRepo, userService); // [NEW]

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/gui/login-view.fxml"));
        Scene scene = new Scene(loader.load());

        org.example.gui.LoginController controller = loader.getController();
        controller.setServices(userService, friendshipService, cardService, eventService, messageService, primaryStage, requestService, raceService);

        primaryStage.setTitle("Duck Social Network - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}