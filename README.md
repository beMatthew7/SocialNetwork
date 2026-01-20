# 🦆 Duck Social Network

A desktop social network application built in Java, enabling interaction between human users and... ducks. This project was developed as a semester assignment for the **Advanced Programming Methods** course.

The application combines classic social media features (friend requests, chat) with an algorithmic simulation of a duck race ("The Swimming Problem"), demonstrating the use of layered architecture and design patterns.

## 📸 Screenshots
*(Place a few screenshots of your app interface here - e.g., Login, Chat, Race Event)*

## ✨ Key Features

### 1. User Management
* **Human & Duck Users:** The system supports two types of entities: Persons and Ducks (with specific attributes like swimming speed or flying capacity).
* **Secure Authentication:** Login and Registration system with password encryption (hashing).
* **User Profile:** View profile details, edit personal data.

### 2. Social Graph
* **Friends:** Add/Remove friends.
* **Friend Requests:** Send requests, accept or reject (Status: Pending, Approved, Rejected).
* **Pagination:** Listing of friends and users is paginated for efficiency.

### 3. Messaging (Chat)
* **Private Chat:** Send messages between users.
* **Reply:** Ability to reply to specific messages.
* **History:** View past conversations.

### 4. Events & Simulations (Duck Race)
* **Race Event:** Organize race events for swimming ducks.
* **Algorithmic Simulation:** Calculate race results using various strategies (e.g., Backtracking, Binary Search) for "The Swimming Problem".
* **Observer Pattern:** Real-time notifications for users subscribed to events.
* **Duck Cards:** Grouping ducks into "flocks" (teams).

## 🛠️ Technologies Used

* **Language:** Java (JDK 17+)
* **GUI Framework:** JavaFX (using FXML and CSS for design)
* **Build Tool:** Gradle (Kotlin DSL)
* **Database:** PostgreSQL
* **Design Patterns:**
    * *MVC (Model-View-Controller)* for GUI architecture.
    * *Observer* for the notification and event system.
    * *Strategy* for race solving algorithms.
    * *Singleton & Factory* for service and validator instantiation.
    * *Decorator* (used in initial labs for TaskRunner).

## 🚀 Installation and Setup

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/your-username/Duck-Social-Network.git](https://github.com/your-username/Duck-Social-Network.git)
    ```

2.  **Database Configuration:**
    * You need a PostgreSQL server installed locally.
    * Create a database (e.g., `socialnetwork`).
    * Update the `src/main/resources/db.properties` file with your credentials:
        ```properties
        jdbc.url=jdbc:postgresql://localhost:5432/socialnetwork
        jdbc.user=postgres
        jdbc.password=your_password
        ```

3.  **Run:**
    * You can run the application directly from IntelliJ IDEA or using Gradle:
    ```bash
    ./gradlew run
    ```

## 📚 Project Structure (Package Overview)

* `org.example.domain`: Entities (User, Person, Duck, Friendship, Message).
* `org.example.repository`: Data persistence (Implementations for DB and File).
* `org.example.service`: Business logic (Services for each entity).
* `org.example.gui`: Graphical User Interface (JavaFX Controllers and FXML files).
* `org.example.utils`: Utilities (Observer, PasswordHasher, Events).

## 📝 Academic Requirements (Labs)

This project was developed progressively over 10 labs, covering:
* **Lab 1-3:** Domain definition, in-memory architecture, file persistence.
* **Lab 4-5:** Database (PostgreSQL), validations.
* **Lab 6-7:** JavaFX GUI, tables, filters.
* **Lab 8-9:** Social features (Login, Messages, Friendships), Pagination.
* **Lab 10:** Concurrent programming, Events, PDF Reports (simulation).

---

