package Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * manages which view to display at any given point
 */
public final class View extends Base {
    private final Scene scene;
    private final Stage primaryStage;

    public View(final Scene scene, final Stage primaryStage) {
        this.scene = scene;
        this.primaryStage = primaryStage;
    }

    /**
     * displays the log in view so the user can log in
     *
     * @throws Exception any exception within the scene building
     */
    public void showLoginView() throws Exception {
        userId = Optional.ofNullable(1L);
        showMainView();
//        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Login.fxml"), bundle);
//        scene.setRoot(loader.load());
//        loader.<Login>getController().setViewController(this);
        primaryStage.setTitle(bundle.getString("app.title"));
//        primaryStage.setWidth(600);
//        primaryStage.setHeight(400);
    }

    public void showMainView() {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Main.fxml"), bundle);
            scene.setRoot(loader.load());
            final Main mainController = loader.getController();
            mainController.setViewController(this);
            primaryStage.setWidth(800);
            primaryStage.setHeight(600);
        } catch (Exception e) {
            System.out.println("error opening main view:");
            System.out.println(e);
        }
    }
}
