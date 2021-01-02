package Controllers;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.net.URL;
import java.util.ResourceBundle;

public class Main extends Base {
    @FXML
    private Tab customerTab;
    @FXML
    private Tab appointmentTab;

    @FXML
    private void handleTabChange(Event event) {
        if (customerTab != null && appointmentTab != null) {
            System.out.println(event);
            System.out.println(customerTab.isSelected());
            System.out.println(appointmentTab.isSelected());
        }
    }
}
