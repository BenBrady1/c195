package Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ResourceBundle;

/**
 * manages which view to display at any given point
 */
public final class Main extends Base {
    private Scene scene;
    private Stage primaryStage;

    public Main(final Scene scene, final Stage primaryStage) {
        this.scene = scene;
        this.primaryStage = primaryStage;
    }

    public void showLoginView() throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("App", getLocale());

        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Login.fxml"), bundle);
        scene.setRoot(loader.load());
        loader.<Login>getController().setMainController(this);
        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.setWidth(600);
        primaryStage.setHeight(400);
    }
}
