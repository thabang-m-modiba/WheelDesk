import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * WheelDesk - a simple JavaFX desktop app.
 *
 * Everything lives in this one file on purpose (no Maven, no separate
 * CSS file) so it can just be dropped into an Eclipse project that
 * already has the JavaFX jars on its build path.
 *
 * Screens:
 *  - Explore   : home/landing page, shows cars + dealer prices
 *  - Login     : shown when "MyDealership" menu item is clicked
 *  - Sign Up   : shown when "Register my dealership" link is clicked
 *  - Search    : shown after typing something in the top search bar
 */
public class Main extends Application {

    // ---------------------------------------------------------------
    // Theme colors (flat colors only, no gradients)
    // ---------------------------------------------------------------
    private static final String COLOR_BACKGROUND = "#474747";/*check*/
    private static final String COLOR_MENU_BAR    = "#ffffff";
    private static final String COLOR_CARD        = "#565656";/*check*/
    private static final String COLOR_CHIP        = "#4d4d4d";
    private static final String COLOR_FIELD       = "#5c5c5c";/*check*/
    private static final String COLOR_BUTTON      = "#6e6e6e";
    private static final String COLOR_BUTTON_HOVER = "#808080";
    private static final String COLOR_BORDER      = "#2f2f2f";/*check*/
    private static final String COLOR_TEXT_LIGHT  = "#f2f2f2";/*check*/
    private static final String COLOR_TEXT_MUTED  = "#cfcfcf";/*check*/

    // Root layout: top = menu + search bar, center = whichever screen is active
    private BorderPane rootLayout;
    private TextField searchField;

    // Remembers what was on screen before a search, so "Back" can restore it
    private Node previousView;

    // list of cars, used by both the Explore page and Search
    private List<Car> cars;

    @Override
    public void start(Stage primaryStage) {
        cars = buildSampleCars();

        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");
        rootLayout.setTop(buildTopBar());

        showExplore();

        Scene scene = new Scene(rootLayout, 1080, 720);

        primaryStage.setTitle("WheelDesk");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(760);
        primaryStage.setMinHeight(520);
        primaryStage.show();
    }

    // =================================================================
    // TOP BAR: menu (Explore / MyDealership) + search bar
    // =================================================================

    private VBox buildTopBar() {
        // --- Menu bar ---
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: " + COLOR_MENU_BAR + ";");

        Menu menu = new Menu("Menu");
        menu.setStyle("-fx-font-size: 16px;"
        		+ " -fx-font-weight: bold;"
        );

        MenuItem exploreItem = new MenuItem("Explore");
        exploreItem.setOnAction(e -> showExplore());

        MenuItem myDealershipItem = new MenuItem("MyDealership");
        myDealershipItem.setOnAction(e -> showLogin());

        menu.getItems().addAll(exploreItem, myDealershipItem);
        menuBar.getMenus().add(menu);

        // --- Search bar (always visible, on top of every page) ---
        searchField = new TextField();
        searchField.setPromptText("Search for a car e.g. \"Corolla\"...");
        searchField.setStyle(
                "-fx-background-color: " + "#ffffff" + ";" +
                "-fx-text-fill: " + "#000" + ";" +
                "-fx-prompt-text-fill: #bdbdbd;" +
                "-fx-background-radius: 50;" +
                "-fx-border-radius: 50;" +
                "-fx-padding: 8 12 8 12;"
        );
        searchField.setOnAction(e -> performSearch());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchButton = styledButton("Search");
        searchButton.setOnAction(e -> performSearch());

        HBox searchBar = new HBox(10, searchField, searchButton);
        searchBar.setAlignment(Pos.CENTER);
        searchBar.setPadding(new Insets(12, 20, 12, 20));
        searchBar.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");

        VBox topBar = new VBox(menuBar, searchBar);
        return topBar;
    }
    
    /**
     * A function to search specific cars displayed on the Explore page
     */
    private void performSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        if (query.isEmpty()) {
            return;
        }

        List<Car> results = new ArrayList<>();
        for (Car car : cars) {
            String combined = (car.name + " " + car.model).toLowerCase();
            if (combined.contains(query.toLowerCase())) {
                results.add(car);
            }
        }

        // Remember what page we were on so "Back" can return to it
        previousView = rootLayout.getCenter();

        rootLayout.setCenter(buildSearchResultsView(query, results));
    }

    //
    // EXPLORE PAGE (home / landing page)
    //

    private void showExplore() {
        rootLayout.setCenter(buildExploreView());
    }

    private Node buildExploreView() {
        VBox headerBox = buildPageHeader(
                "Explore Cars",
                "Compare prices for the same car across different dealerships."
        );

        VBox carListBox = buildCarListBox(cars);

        ScrollPane scrollPane = new ScrollPane(carListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox page = new VBox(headerBox, scrollPane);
        page.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");
        return page;
    }

    // Builds the list of car cards, each showing name, model and every
    // dealer's price for that car. Reused by both Explore and Search.
    private VBox buildCarListBox(List<Car> carsToShow) {
        VBox container = new VBox(16);
        container.setPadding(new Insets(20));

        if (carsToShow.isEmpty()) {
            Label empty = new Label("No cars found.");
            empty.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + "; -fx-font-size: 14px;");
            container.getChildren().add(empty);
            return container;
        }

        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "ZA"));

        for (Car car : carsToShow) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(16));
            card.setStyle(
                    "-fx-background-color: #fff;"
            );

            Label title = new Label(car.name + " " + car.model);
            title.setStyle("-fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;");

            FlowPane pricesPane = new FlowPane();
            pricesPane.setHgap(16);
            pricesPane.setVgap(8);

            for (DealerPrice dp : car.dealerPrices) {
                HBox chip = new HBox(8);
                chip.setPadding(new Insets(8, 12, 8, 12));
                chip.setStyle(
                        "-fx-background-color: #0b9ebf;" +
                        "-fx-background-radius: 4;" /*+
                        "-fx-border-color: " + COLOR_BORDER + ";" +
                        "-fx-border-radius: 4;"*/
                );

                Label dealerName = new Label(dp.dealerName);
                dealerName.setStyle("-fx-text-fill: black; -fx-font-size: 12px;");

                Label price = new Label(currency.format(dp.price));
                price.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

                chip.getChildren().addAll(dealerName, price);
                pricesPane.getChildren().add(chip);
            }

            card.getChildren().addAll(title, pricesPane);
            container.getChildren().add(card);
        }

        return container;
    }

    // =================================================================
    // LOGIN PAGE (shown when "MyDealership" menu item is clicked)
    // =================================================================

    private void showLogin() {
        rootLayout.setCenter(buildLoginView());
    }

    private Node buildLoginView() {
        VBox card = new VBox(14);
        card.setMaxWidth(380);
        card.setPadding(new Insets(30));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: " + COLOR_CARD + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-border-radius: 8;"
        );

        Label title = new Label("Dealership Login");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("Sign in to manage your dealership listings.");
        subtitle.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        styleAuthField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleAuthField(passwordField);

        Button loginButton = styledButton("Log In");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> showInfoAlert("Login functionality not yet connected."));

        Hyperlink forgotPasswordLink = new Hyperlink("Forgot password?");
        forgotPasswordLink.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + ";");
        // does nothing for now.
        forgotPasswordLink.setOnAction(e -> { /* no-op for now */ });

        Hyperlink registerLink = new Hyperlink("Register my dealership");
        registerLink.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + ";");
        registerLink.setOnAction(e -> showSignup());

        VBox linksBox = new VBox(6, forgotPasswordLink, registerLink);
        linksBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(title, subtitle, emailField, passwordField, loginButton, linksBox);

        VBox page = new VBox(card);
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(40));
        page.setStyle("-fx-background-color: white;");
        VBox.setVgrow(page, Priority.ALWAYS);
        return page;
    }

    // =================================================================
    // SIGN UP PAGE (shown when "Register my dealership" is clicked)
    // =================================================================

    private void showSignup() {
        rootLayout.setCenter(buildSignupView());
    }

    private Node buildSignupView() {
        VBox card = new VBox(14);
        card.setMaxWidth(420);
        card.setPadding(new Insets(30));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: " + COLOR_CARD + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-border-radius: 8;"
        );

        Label title = new Label("Register My Dealership");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("Create an account to list your dealership on WheelDesk.");
        subtitle.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        TextField dealershipNameField = new TextField();
        dealershipNameField.setPromptText("Dealership name");
        styleAuthField(dealershipNameField);

        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        styleAuthField(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        styleAuthField(passwordField);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm password");
        styleAuthField(confirmPasswordField);

        Button registerButton = styledButton("Register Dealership");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(e -> showInfoAlert("Registration functionality not yet connected."));

        Hyperlink backToLoginLink = new Hyperlink("Already have an account? Log in");
        backToLoginLink.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + ";");
        backToLoginLink.setOnAction(e -> showLogin());

        card.getChildren().addAll(title, subtitle, dealershipNameField, emailField,
                passwordField, confirmPasswordField, registerButton, backToLoginLink);

        VBox page = new VBox(card);
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(40));
        page.setStyle("-fx-background-color: white;");
        VBox.setVgrow(page, Priority.ALWAYS);
        return page;
    }

    // =================================================================
    // SEARCH RESULTS PAGE
    // =================================================================

    private Node buildSearchResultsView(String query, List<Car> results) {
        Button backButton = new Button("\u2190 Back");
        backButton.setStyle(
                "-fx-background-color: " + COLOR_FIELD + ";" +
                "-fx-text-fill: " + COLOR_TEXT_LIGHT + ";" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 6 14 6 14;" +
                "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> rootLayout.setCenter(previousView != null ? previousView : buildExploreView()));

        HBox topBar = new HBox(backButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20, 20, 0, 20));

        VBox headerBox = buildPageHeader(
                "Search results for \"" + query + "\"",
                results.size() + " car" + (results.size() == 1 ? "" : "s") + " found"
        );
        headerBox.setPadding(new Insets(12, 20, 0, 20));

        VBox carListBox = buildCarListBox(results);

        ScrollPane scrollPane = new ScrollPane(carListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox page = new VBox(topBar, headerBox, scrollPane);
        page.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");
        return page;
    }

    // =================================================================
    // Small reusable helpers
    // =================================================================

    private VBox buildPageHeader(String headingText, String subheadingText) {
        Label heading = new Label(headingText);
        heading.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label subheading = new Label(subheadingText);
        subheading.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + "; -fx-font-size: 13px;");

        VBox headerBox = new VBox(4, heading, subheading);
        headerBox.setPadding(new Insets(20, 20, 0, 20));
        return headerBox;
    }

    private void styleAuthField(TextField field) {
        field.setPrefWidth(300);
        field.setStyle(
                "-fx-background-color: " + COLOR_CHIP + ";" +
                "-fx-text-fill: " + COLOR_TEXT_LIGHT + ";" +
                "-fx-prompt-text-fill: #b0b0b0;" +
                "-fx-background-radius: 4;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-border-radius: 4;" +
                "-fx-padding: 8 10 8 10;"
        );
    }

    private Button styledButton(String text) {
        Button button = new Button(text);
        String baseStyle =
                "-fx-background-color: " + COLOR_BUTTON + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 8 18 8 18;" +
                "-fx-cursor: hand;" +
                "-fx-font-weight: bold;";
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(baseStyle.replace(COLOR_BUTTON, COLOR_BUTTON_HOVER)));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        return button;
    }

    private void showInfoAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // =================================================================
    // Sample data (name, model, and prices from different dealerships)
    // =================================================================

    private List<Car> buildSampleCars() {
        List<Car> list = new ArrayList<>();

        list.add(new Car("Toyota", "Corolla", new DealerPrice[] {
                new DealerPrice("CBD Motors", 349999),
                new DealerPrice("Northside Auto", 342500),
                new DealerPrice("Prestige Cars", 359900)
        }));

        list.add(new Car("Volkswagen", "Polo GTI", new DealerPrice[] {
                new DealerPrice("CBD Motors", 469000),
                new DealerPrice("Speedline Dealership", 455500),
                new DealerPrice("Northside Auto", 472000)
        }));

        list.add(new Car("BMW", "3 Series 320i", new DealerPrice[] {
                new DealerPrice("Prestige Cars", 789000),
                new DealerPrice("Speedline Dealership", 799500),
                new DealerPrice("Elite Motors", 774900)
        }));

        list.add(new Car("Ford", "Ranger Wildtrak", new DealerPrice[] {
                new DealerPrice("Northside Auto", 899000),
                new DealerPrice("Elite Motors", 875000),
                new DealerPrice("CBD Motors", 912000)
        }));

        list.add(new Car("Hyundai", "i20", new DealerPrice[] {
                new DealerPrice("Speedline Dealership", 299900),
                new DealerPrice("Prestige Cars", 305000),
                new DealerPrice("Northside Auto", 294500)
        }));

        list.add(new Car("Mercedes-Benz", "C200", new DealerPrice[] {
                new DealerPrice("Elite Motors", 899500),
                new DealerPrice("CBD Motors", 915000),
                new DealerPrice("Prestige Cars", 889900)
        }));

        return list;
    }

    // =================================================================
    // Simple data holder classes (kept in this file to avoid extra files)
    // =================================================================

    private static class Car {
        String name;
        String model;
        DealerPrice[] dealerPrices;

        Car(String name, String model, DealerPrice[] dealerPrices) {
            this.name = name;
            this.model = model;
            this.dealerPrices = dealerPrices;
        }
    }

    private static class DealerPrice {
        String dealerName;
        double price;

        DealerPrice(String dealerName, double price) {
            this.dealerName = dealerName;
            this.price = price;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}