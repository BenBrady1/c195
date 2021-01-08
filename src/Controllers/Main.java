package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Main extends Base implements Initializable {
    @FXML
    private TabPane tabPane;
    @FXML
    Tab customerTab;
    @FXML
    Tab appointmentTab;

    private boolean customerTabInitialized = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // lambda to easily determine which tab has been selected and display the correct data
        tabPane.getSelectionModel().selectedItemProperty().addListener(((observableValue, oldTab, newTab) -> {
            populateData(newTab);
        }));
        populateData(tabPane.getSelectionModel().getSelectedItem());
    }

    private void populateData(Tab newTab) {
        if (newTab == customerTab) {
            populateCustomerData();
        } else if (newTab == appointmentTab) {
            populateAppointmentData();
        }
    }

    private void populateCustomerData() {
        if (customerTabInitialized) return;
        customerTabInitialized = true;
        System.out.println("Fetching customer data");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Table.fxml"), bundle);
        loader.setController(new CustomerTable());
        try {
            customerTab.setContent(loader.load());
        } catch (IOException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }

    private void populateAppointmentData() {
        System.out.println("Fetching appointment data");
    }
}
