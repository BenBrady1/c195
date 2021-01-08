package Controllers;

import Models.Appointment;
import Models.Contact;
import Models.Customer;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;

import java.util.Map;

public class AppointmentTable extends Table<Appointment> {
    public AppointmentTable() {
        super(null);
    }

    private Map<Long, Contact> contactMap;

    @Override
    protected void addColumns() {
        TableColumn<Appointment, String> contactCol = new TableColumn(bundle.getString("appointment.contact"));
        contactCol
                .setCellValueFactory(param -> new SimpleStringProperty(contactMap.get(param.getValue().getContactId()).getName()));
        TableColumn<Appointment, String> startCol = new TableColumn(bundle.getString("appointment.start"));
        startCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedStart()));
        TableColumn<Appointment, String> endCol = new TableColumn(bundle.getString("appointment.end"));
        endCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedEnd()));
        TableColumn<Appointment, Long> customerIdCol = new TableColumn(bundle.getString("appointment.customerId"));
        customerIdCol.setCellValueFactory(param -> new SimpleLongProperty(param.getValue().getCustomerId()).asObject());
        tableView.getColumns().addAll(getStringColumn(Appointment.class, "title"),
                getStringColumn(Appointment.class, "description"),
                getStringColumn(Appointment.class, "location"),
                contactCol,
                getStringColumn(Appointment.class, "type"),
                startCol,
                endCol,
                customerIdCol);
    }

    @Override
    protected void populateData() {

    }

    @Override
    protected String getInsertStatement() {
        return null;
    }

    @Override
    protected Appointment getNewRecord() {
        return null;
    }

    @Override
    protected String getUpdateStatement() {
        return null;
    }

    @Override
    protected boolean deleteDependencies(Appointment record) {
        return false;
    }

    @Override
    protected String getDeleteStatement() {
        return null;
    }

    @Override
    protected String getDeletedMessage() {
        return null;
    }
}
