package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;




import org.example.domain.Friendship;
import org.example.domain.Person;
import org.example.domain.Duck;
import org.example.domain.validators.PersonValidator;
import org.example.domain.validators.DuckValidator;
import org.example.domain.validators.Validator;
import org.example.interactions.DuckCard;
import org.example.interactions.Event;
import org.example.repository.*;
import org.example.domain.validators.NoOpValidator;
import org.example.service.CardService;
import org.example.service.EventService;
import org.example.service.UserService;
import org.example.service.FriendshipService;
import org.example.service.EventService;
import org.example.ui.Console;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread;
import java.util.Properties;



public class Main{

    public static void main(String[] args) throws InterruptedException {

        Validator<Person> personValidator = new PersonValidator();
        Validator<Duck> duckValidator = new DuckValidator();

        String personFilePath = "src/main/resources/persons.csv";
        String duckFilePath = "src/main/resources/ducks.csv";
        String friendshipsFilePath = "src/main/resources/friendships.csv";
        String cardsFilePath = "src/main/resources/cards.csv";

        Properties dbProps = new Properties();
        String dbUrl, dbUser, dbPassword;
        
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("Nu s a gasit db.properties");
                return;
            }
            dbProps.load(input);
            dbUrl = dbProps.getProperty("db.url");
            dbUser = dbProps.getProperty("db.username");
            dbPassword = dbProps.getProperty("db.password");
        } catch (IOException ex) {
            System.err.println("Eroare la incarcat db: " + ex.getMessage());
            return;
        }

        //Repository<Long, Person> personRepo = new PersonFileRepository(personFilePath, personValidator);
        Repository<Long, Person> personRepo = new PersonDbRepository(dbUrl, dbUser, dbPassword, personValidator);


        //Repository<Long, DuckCard> cardRepo = new CardFileRepository(cardsFilePath);
        Repository<Long, DuckCard> cardRepo = new CardDbRepository(dbUrl,dbUser, dbPassword);

        //Repository<Long, Duck> duckRepo = new DuckFileRepository(duckFilePath, duckValidator);
        DuckRepo duckRepo = new DuckDbRepository(dbUrl, dbUser, dbPassword, duckValidator);


        //CardMembershipRepository membershipRepo = new CardMembershipRepository("src/main/resources/card_memberships.csv");
        CardMembershipRepository membershipRepo = new CardMembershipDbRepository(dbUrl, dbUser, dbPassword);

        CardService cardService = new CardService(cardRepo, (DuckDbRepository) duckRepo, membershipRepo);

        UserService userService = new UserService(personRepo, duckRepo);


        //FriendshipFileRepository friendshipRepo = new FriendshipFileRepository(friendshipsFilePath, new NoOpValidator<>());
        FriendshipRepo friendshipRepo = new FriendshipDbRepository(dbUrl, dbUser, dbPassword, new NoOpValidator<>());
        FriendshipService friendshipService = new FriendshipService(personRepo, duckRepo, friendshipRepo);

        //Repository<Long, Event> eventRepo = new InMemoryRepository<>(new NoOpValidator<>());
        Repository<Long, Event> eventRepo = new EventDbRepository(dbUrl,dbUser, dbPassword, personRepo, duckRepo);
        EventHistoryRepository historyRepo = new EventHistoryRepository(dbUrl,dbUser, dbPassword);
        EventService eventService = new EventService(eventRepo, historyRepo);

        Console console = new Console(userService, friendshipService, cardService, eventService);

        console.run();
    }
}