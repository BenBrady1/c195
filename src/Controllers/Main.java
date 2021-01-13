package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Main extends Base implements Initializable {
    public enum Event {
        CustomerDeleted
    }

    final public class EventEmitter implements java.util.EventListener {
        final private HashMap<Event, List<Runnable>> eventMap = new HashMap<>();

        public void addListener(Event e, Runnable r) {
            List<Runnable> listeners = eventMap.get(e);
            if (listeners == null) {
                listeners = new ArrayList<>();
                eventMap.put(e, listeners);
            }
            listeners.add(r);
        }

        public void emit(Event e) {
            for (Runnable runnable : eventMap.get(e)) {
                runnable.run();
            }
        }
    }

    @FXML
    private TabPane tabPane;
    @FXML
    Tab customerTab;
    @FXML
    Tab appointmentTab;

    private boolean customerTabInitialized = false;
    private boolean appointmentTabInitialized = false;

    private EventEmitter eventEmitter = new EventEmitter();
    private CustomerTable customerTableController;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Table.fxml"), bundle);
        customerTableController = new CustomerTable(eventEmitter);
        loader.setController(customerTableController);
        try {
            customerTab.setContent(loader.load());
        } catch (IOException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }

    private void populateAppointmentData() {
        if (appointmentTabInitialized) return;
        appointmentTabInitialized = true;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Table.fxml"), bundle);
        loader.setController(new AppointmentTable(customerTableController.getData(), eventEmitter));
        try {
            appointmentTab.setContent(loader.load());
        } catch (IOException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }
}
