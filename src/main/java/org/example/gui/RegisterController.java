package org.example.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.domain.*;
import org.example.service.UserService;

import java.time.ZoneId;
import java.util.Date;

public class RegisterController {

    // --- Campuri Comune ---
    @FXML private ComboBox<String> userTypeCombo;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // --- Containeri (pentru a ascunde/arata sectiuni) ---
    @FXML private VBox personFieldsBox;
    @FXML private VBox duckFieldsBox;

    // --- Campuri Person ---
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker dobPicker;
    @FXML private TextField occupationField;
    @FXML private TextField empathyField;

    // --- Campuri Duck ---
    @FXML private ComboBox<DuckType> duckTypeCombo;
    @FXML private TextField speedField;
    @FXML private TextField enduranceField;

    private UserService userService;
    private Stage dialogStage;

    public void setService(UserService userService, Stage stage) {
        this.userService = userService;
        this.dialogStage = stage;
    }

    @FXML
    public void initialize() {
        // Populam combo-ul principal
        userTypeCombo.getItems().addAll("Person", "Duck");

        // Populam combo-ul pentru rate
        duckTypeCombo.getItems().setAll(DuckType.values());

        // Ascultam schimbarile la tipul de user pentru a schimba interfata
        userTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Person".equals(newVal)) {
                showPersonFields(true);
            } else {
                showPersonFields(false); // Arata Duck
            }
        });

        // Selectam implicit Person la inceput
        userTypeCombo.getSelectionModel().select("Person");
    }

    private void showPersonFields(boolean showPerson) {
        // Daca showPerson e true -> Person e visible si managed, Duck e invizibil
        personFieldsBox.setVisible(showPerson);
        personFieldsBox.setManaged(showPerson); // managed=false inseamna ca nu ocupa spatiu

        duckFieldsBox.setVisible(!showPerson);
        duckFieldsBox.setManaged(!showPerson);
    }

    @FXML
    private void handleSave() {
        String type = userTypeCombo.getValue();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required!");
            return;
        }

        try {
            if ("Person".equals(type)) {
                savePerson(username, email, password);
            } else {
                saveDuck(username, email, password);
            }

            // Daca nu a crapat pana aici, inchidem fereastra
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null); // Poți lăsa null ca să nu ai un header urât
            alert.setContentText("User saved!");
            alert.showAndWait(); // Așteaptă ca userul să dea OK înainte să meargă mai departe

            dialogStage.close();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void savePerson(String username, String email, String pass) {
        String first = firstNameField.getText();
        String last = lastNameField.getText();
        String occupation = occupationField.getText();

        if (dobPicker.getValue() == null) throw new RuntimeException("Choose date of birth!");
        // Conversie LocalDate -> Date
        Date dob = Date.from(dobPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

        int empathy = Integer.parseInt(empathyField.getText());

        Person p = new Person(username, email, pass, first, last, dob, occupation, empathy);
        Person result = userService.createPerson(p);
        if (result != null) {
            throw new RuntimeException("Username or email already exists!");
        }
    }

    private void saveDuck(String username, String email, String pass) {
        DuckType dType = duckTypeCombo.getValue();
        if (dType == null) throw new RuntimeException("Choose duck type!");

        double speed = Double.parseDouble(speedField.getText());
        double endurance = Double.parseDouble(enduranceField.getText());

        Duck d;
        // Creare obiect specific in functie de tip
        if (dType == DuckType.FLYING)
            d = new FlyingDuck(username, email, pass, dType, speed, endurance);
        else if (dType == DuckType.SWIMMING)
            d = new SwimmingDuck(username, email, pass, dType, speed, endurance);
        else
            d = new FlyingSwimmingDuck(username, email, pass, dType, speed, endurance);

        Duck result = userService.createDuck(d);
        if (result != null) {
            throw new RuntimeException("Username or email already exists!");
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}