import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import java.security.*;

import Objects.Car;
import Objects.DealerPrice;
import Objects.Dealership;
import Classes.DealershipController;
import Classes.LoginController;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class Main extends Application {

    //
    // Theme colors
    // 
    private static final String COLOR_BACKGROUND     = "#1c1d22";
    private static final String COLOR_SURFACE         = "#26272e";
    private static final String COLOR_SURFACE_ALT     = "#2f3038";
    private static final String COLOR_MENU_BAR         = "#141519";
    private static final String COLOR_CHIP             = "#33343d";
    private static final String COLOR_FIELD            = "#2f3038";
    private static final String COLOR_BORDER           = "#3a3b44";

    private static final String COLOR_ACCENT           = "#5b8cff";
    private static final String COLOR_ACCENT_HOVER     = "#4a76e0";
    private static final String COLOR_ACCENT_MUTED     = "#3a4a7a";

    private static final String COLOR_TEXT_LIGHT       = "#f2f3f7";
    private static final String COLOR_TEXT_MUTED       = "#9a9ba8";
    private static final String COLOR_TEXT_FAINT       = "#6d6e79";

    private static final String FONT_FAMILY = "'Segoe UI', 'Helvetica Neue', Arial, sans-serif";

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
        rootLayout.setStyle(
                "-fx-background-color: " + COLOR_BACKGROUND + ";" +
                "-fx-font-family: " + FONT_FAMILY + ";"
        );
        rootLayout.setTop(buildTopBar());

        showExplore();

        Scene scene = new Scene(rootLayout, 1080, 720);

        primaryStage.setTitle("WheelDesk");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(760);
        primaryStage.setMinHeight(520);
        primaryStage.show();
    }

    //
    // TOP BAR: menu (Explore / MyDealership) + search bar
    //

    private VBox buildTopBar() {
        //Menu bar
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle(
                /*"-fx-background-color: " + COLOR_MENU_BAR + ";" +*/
                "-fx-border-color: transparent transparent " + COLOR_BORDER + " transparent;" +
                "-fx-border-width: 0 0 1 0;"
        );

        Menu menu = new Menu("WheelDesk");
        menu.setStyle(
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" /*+
                "-fx-text-fill: " + COLOR_TEXT_LIGHT + ";"*/
        );

        MenuItem exploreItem = new MenuItem("Explore");
        exploreItem.setOnAction(e -> showExplore());

        MenuItem myDealershipItem = new MenuItem("MyDealership");
        myDealershipItem.setOnAction(e -> showLogin());

        menu.getItems().addAll(exploreItem, myDealershipItem);
        menuBar.getMenus().add(menu);
        menuBar.setPadding(new Insets(4, 8, 4, 8));

        // Search bar (always visible, on top of every page)
        searchField = new TextField();
        searchField.setPromptText("Search for a car e.g. \"Corolla\"...");
        searchField.setStyle(
                "-fx-background-color: " + COLOR_FIELD + ";" +
                "-fx-text-fill: " + COLOR_TEXT_LIGHT + ";" +
                "-fx-prompt-text-fill: " + COLOR_TEXT_FAINT + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-border-width: 1;" +
                "-fx-padding: 9 14 9 14;" +
                "-fx-font-size: 13px;"
        );
        searchField.setOnAction(e -> performSearch());
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchButton = styledButton("Search", true);
        searchButton.setOnAction(e -> performSearch());

        HBox searchBar = new HBox(10, searchField, searchButton);
        searchBar.setAlignment(Pos.CENTER);
        searchBar.setPadding(new Insets(14, 24, 14, 24));
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
            String combined = (car.getName() + " " + car.getModel()).toLowerCase();
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
    
    /**
     * Helper function to display the cars
     * @return page with list of the cars (Page has privilege access. Dealership admin should be able to delete cars they have posted ***To Do***)
     */
    private Node buildExploreView() {
        VBox headerBox = buildPageHeader(
                "Explore Cars",
                "Compare prices for the same car across different dealerships."
        );

        VBox carListBox = buildCarListBox(cars);

        ScrollPane scrollPane = new ScrollPane(carListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;"
        		+ " -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox page = new VBox(headerBox, scrollPane);
        page.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");
        return page;
    }

    /**
     * Builds the list of car cards, each showing name, model and every dealer's price for that car. Reused by both Explore and Search
     * @param carsToShow
     * @return
     */
    private VBox buildCarListBox(List<Car> carsToShow) {
        VBox container = new VBox(14);
        container.setPadding(new Insets(20, 24, 24, 24));

        if (carsToShow.isEmpty()) {
            Label empty = new Label("No cars found.");
            empty.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + "; -fx-font-size: 14px;");
            container.getChildren().add(empty);
            return container;
        }

        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "ZA")); // Come back to this to check what it does!!!!! I think it's for the currency and stuff
        currency.setMaximumFractionDigits(0);

        for (Car car : carsToShow) {
            VBox card = new VBox(12);
            card.setPadding(new Insets(18, 20, 18, 20));
            card.setStyle(
                    "-fx-background-color: " + COLOR_SURFACE + ";" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: " + COLOR_BORDER + ";" +
                    "-fx-border-radius: 10;" +
                    "-fx-border-width: 1;"
            );
            card.setEffect(subtleShadow());

            Label title = new Label(car.getName() + " " + car.getModel());
            title.setStyle(
                    "-fx-text-fill: " + COLOR_TEXT_LIGHT + ";" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: bold;"
            );

            // Lowest price is called out so the comparison is easy to read at a glance
            double lowest = Double.MAX_VALUE;
            for (DealerPrice dp : car.getDealerPrices()) {
                lowest = Math.min(lowest, dp.getPrice());
            }

            FlowPane pricesPane = new FlowPane();
            pricesPane.setHgap(10);
            pricesPane.setVgap(10);

            for (DealerPrice dp : car.getDealerPrices()) {
                boolean isLowest = dp.getPrice() == lowest;

                HBox chip = new HBox(10);
                chip.setAlignment(Pos.CENTER_LEFT);
                chip.setPadding(new Insets(8, 14, 8, 14));
                chip.setStyle(
                        "-fx-background-color: " + (isLowest ? COLOR_ACCENT_MUTED : COLOR_CHIP) + ";" +
                        "-fx-background-radius: 6;" +
                        (isLowest ? "-fx-border-color: " + COLOR_ACCENT + "; -fx-border-width: 1; -fx-border-radius: 6;" : "")
                );

                Label dealerName = new Label(dp.getDealerName());
                dealerName.setStyle(
                        "-fx-text-fill: " + COLOR_TEXT_MUTED + ";" +
                        "-fx-font-size: 12px;"
                );

                Label price = new Label(currency.format(dp.getPrice()));
                price.setStyle(
                        "-fx-text-fill: " + (isLowest ? COLOR_ACCENT : COLOR_TEXT_LIGHT) + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
                );

                chip.getChildren().addAll(dealerName, price);
                pricesPane.getChildren().add(chip);
            }

            card.getChildren().addAll(title, pricesPane);
            container.getChildren().add(card);
        }

        return container;
    }

    //
    // LOGIN PAGE
    //

    private void showLogin() {
        rootLayout.setCenter(buildLoginView());
    }
    /**
     * Helper function to build the login page
     * @return
     */
    private Node buildLoginView() {
        VBox card = new VBox(16);
        card.setMaxWidth(380);
        card.setPadding(new Insets(36));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: " + COLOR_SURFACE + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;"
        );
        card.setEffect(subtleShadow());

        Label title = new Label("Dealership Login");
        title.setStyle(
                "-fx-text-fill: " + COLOR_TEXT_LIGHT + ";" +
                "-fx-font-size: 21px;" +
                "-fx-font-weight: bold;"
        );

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

        Button loginButton = styledButton("Log In", true);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> {
        	String email = emailField.getText();
        	String password = passwordField.getText();
        	// Validate the email
            if(!isValidEmail(email)) {
            	showInfoAlert("Error: Invalid Email!");
            	return;
            }
            
            // Create the login controller
            LoginController loginUser = new LoginController(email, password);
            loginUser.loginUser();
        });

        Hyperlink forgotPasswordLink = new Hyperlink("Forgot password?");
        forgotPasswordLink.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + "; -fx-font-size: 12px;");
        // does nothing for now.
        forgotPasswordLink.setOnAction(e -> { /* no-op for now */ });

        Hyperlink registerLink = new Hyperlink("Register my dealership");
        registerLink.setStyle("-fx-text-fill: " + COLOR_ACCENT + "; -fx-font-size: 12px;");
        registerLink.setOnAction(e -> showSignup());

        VBox linksBox = new VBox(4, forgotPasswordLink, registerLink);
        linksBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(title, subtitle, emailField, passwordField, loginButton, linksBox);

        VBox page = new VBox(card);
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(40));
        page.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");
        VBox.setVgrow(page, Priority.ALWAYS);
        return page;
    }

    //
    // SIGN UP PAGE
    //

    private void showSignup() {
        rootLayout.setCenter(buildSignupView());
    }

    private Node buildSignupView() {
        VBox card = new VBox(16);
        card.setMaxWidth(420);
        card.setPadding(new Insets(36));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: " + COLOR_SURFACE + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;"
        );
        card.setEffect(subtleShadow());

        Label title = new Label("Register My Dealership");
        title.setStyle(
                "-fx-text-fill: " + COLOR_TEXT_LIGHT + ";" +
                "-fx-font-size: 21px;" +
                "-fx-font-weight: bold;"
        );

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

        Button registerButton = styledButton("Register Dealership", true);
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(e -> {
        	// Extract values from the UI fields
            String dealershipName = dealershipNameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Add validation
            if (dealershipName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showInfoAlert("Error: All fields are required.");
                return;
            }
            
            // Validate the email
            if(!isValidEmail(email)) {
            	showInfoAlert("Error: Invalid Email!");
            	return;
            }
            // Confirm equal passwords
            if (!password.equals(confirmPassword)) {
                showInfoAlert("Error: Passwords do not match.");
                return;
            }
            
            // Hash the password
            String hashedPwd = hashPassword(confirmPassword);
            // Create the Dealership object
            Dealership dealer = new Dealership(dealershipName, email, hashedPwd);
            
            // Create Dealership Controller
            DealershipController dealershipCtrl = new DealershipController(dealer);
            dealershipCtrl.signupDealership();

            // 3. Store or process your values here
            // Example: saveToDatabase(dealershipName, email, password);
            
            showInfoAlert("Registration data captured successfully!");
        });

        Hyperlink backToLoginLink = new Hyperlink("Already have an account? Log in");
        backToLoginLink.setStyle("-fx-text-fill: " + COLOR_ACCENT + "; -fx-font-size: 12px;");
        backToLoginLink.setOnAction(e -> showLogin());

        card.getChildren().addAll(title, subtitle, dealershipNameField, emailField,
                passwordField, confirmPasswordField, registerButton, backToLoginLink);

        VBox page = new VBox(card);
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(40));
        page.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");
        VBox.setVgrow(page, Priority.ALWAYS);
        return page;
    }

    //
    // SEARCH RESULTS PAGE
    //
    
    /**
     * Helper function to build the results page
     * @param query
     * @param results
     * @return
     */
    private Node buildSearchResultsView(String query, List<Car> results) {
        Button backButton = styledButton("\u2190 Back", false);
        backButton.setOnAction(e -> rootLayout.setCenter(previousView != null ? previousView : buildExploreView()));

        HBox topBar = new HBox(backButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20, 24, 0, 24));

        VBox headerBox = buildPageHeader(
                "Search results for \"" + query + "\"",
                results.size() + " car" + (results.size() == 1 ? "" : "s") + " found"
        );
        headerBox.setPadding(new Insets(12, 24, 0, 24));

        VBox carListBox = buildCarListBox(results);

        ScrollPane scrollPane = new ScrollPane(carListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox page = new VBox(topBar, headerBox, scrollPane);
        page.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");
        return page;
    }

    //
    // Small reusable helpers
    //

    private VBox buildPageHeader(String headingText, String subheadingText) {
        Label heading = new Label(headingText);
        heading.setStyle(
                "-fx-text-fill: " + COLOR_TEXT_LIGHT + ";" +
                "-fx-font-size: 23px;" +
                "-fx-font-weight: bold;"
        );

        Label subheading = new Label(subheadingText);
        subheading.setStyle("-fx-text-fill: " + COLOR_TEXT_MUTED + "; -fx-font-size: 13px;");

        VBox headerBox = new VBox(4, heading, subheading);
        headerBox.setPadding(new Insets(20, 24, 0, 24));
        return headerBox;
    }

    private void styleAuthField(TextField field) {
        field.setPrefWidth(300);
        field.setStyle(
                "-fx-background-color: " + COLOR_FIELD + ";" +
                "-fx-text-fill: " + COLOR_TEXT_LIGHT + ";" +
                "-fx-prompt-text-fill: " + COLOR_TEXT_FAINT + ";" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: " + COLOR_BORDER + ";" +
                "-fx-border-radius: 6;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 9 12 9 12;" +
                "-fx-font-size: 13px;"
        );
    }

    /**
     * @param primary true for the main call-to-action style (accent fill), false for a lower-emphasis secondary style (outlined).
     */
    private Button styledButton(String text, boolean primary) {
        Button button = new Button(text);

        String baseStyle = primary
                ? "-fx-background-color: " + COLOR_ACCENT + ";" +
                  "-fx-text-fill: white;"
                : "-fx-background-color: " + COLOR_SURFACE_ALT + ";" +
                  "-fx-text-fill: " + COLOR_TEXT_LIGHT + ";" +
                  "-fx-border-color: " + COLOR_BORDER + ";" +
                  "-fx-border-width: 1;" +
                  "-fx-border-radius: 6;";

        String common =
                "-fx-background-radius: 6;" +
                "-fx-padding: 9 18 9 18;" +
                "-fx-cursor: hand;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;";

        String finalBase = baseStyle + common;
        String hoverStyle = primary
                ? finalBase.replace(COLOR_ACCENT, COLOR_ACCENT_HOVER)
                : finalBase.replace(COLOR_SURFACE_ALT, COLOR_CHIP);

        button.setStyle(finalBase);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(finalBase));
        return button;
    }

    private DropShadow subtleShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.35));
        shadow.setRadius(14);
        shadow.setOffsetY(4);
        return shadow;
    }

    private void showInfoAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
    
    // Sign form methods
    private static boolean isValidEmail(String email) {
    	String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    	return Pattern.matches(regex, email);
    }
    
    // Password hashing - Generated by co-pilot
    private static String hashPassword(String password) {
    	try {
    		MessageDigest md = MessageDigest.getInstance("SHA-256");
    		byte[] hash = md.digest(password.getBytes());
    		
    		StringBuilder hexString = new StringBuilder();
    		
    		for(byte b : hash) {
    			String hex = Integer.toHexString(0xff & b);
    			
    			if(hex.length() == 1) {
    				hexString.append('0');
    			}
    			
    			hexString.append(hex);
    		}
    		return hexString.toString();
    	}catch(NoSuchAlgorithmException e) {
    		throw new RuntimeException(e);
    	}
    	
    }

    //
    // Sample data (name, model, and prices from different dealerships)
    //

    private List<Car> buildSampleCars() {
        List<Car> list = new ArrayList<>();

        // I have this data as demo. I will soon grab the actual data from my database

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

    public static void main(String[] args) {
        launch(args);
    }
}