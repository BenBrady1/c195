package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Locale locale = Locale.getDefault();
        switch (locale.getLanguage()) {
            case "en":
            case "fr":
                break;
            default:
                locale = new Locale("en", "US");
        }
//        locale = new Locale("fr", "CA");
        ResourceBundle bundle = ResourceBundle.getBundle("App", locale);

        Parent root = FXMLLoader.load(getClass().getResource("/Views/Login.fxml"), bundle);
        primaryStage.setTitle(bundle.getString("Login.welcome"));
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
