package main;

import Controllers.Base;
import Controllers.View;
import Models.Record;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Base.setLocaleAndBundle();
        Record.bundle = Base.getBundle();
        Record.locale = Base.getLocale();
        final Scene scene = new Scene(new StackPane());

        View viewController = new View(scene, primaryStage);
        viewController.showLoginView();

        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
