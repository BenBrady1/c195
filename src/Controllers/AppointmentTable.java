package Controllers;

import Models.Appointment;
import Models.Contact;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

public class AppointmentTable extends Table<Appointment> implements Initializable {
    private final String insertStatement = "INSERT INTO appointments " +
            "SET Title = ?, Description = ?, `Location` = ?, `Type` = ?, `Start` = ?, `End` = ?, Customer_ID = ?, User_ID = ?, Contact_ID = ?, Created_By = ?, Last_Updated_By ?";

    public AppointmentTable() {
        super(null);
    }

    private HashMap<Long, Contact> contactMap = new HashMap();

    @Override
    protected void addColumns() {
        final TableColumn<Appointment, String> contactCol = new TableColumn(bundle.getString("appointment.contact"));
        contactCol
                .setCellValueFactory(param -> {
                    final Optional<Contact> contact = Optional.ofNullable(contactMap.get(param.getValue().getContactId()));
                    return new SimpleStringProperty(contact.map(Contact::getName).orElse(""));
                });
        final TableColumn<Appointment, String> startCol = new TableColumn(bundle.getString("appointment.start"));
        startCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedStart()));
        final TableColumn<Appointment, String> endCol = new TableColumn(bundle.getString("appointment.end"));
        endCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedEnd()));
        final TableColumn<Appointment, Long> customerIdCol = new TableColumn(bundle.getString("appointment.customerId"));
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
        executeQuery("SELECT Appointment_ID, Title, Description, `Location`, `Type`, `Start`, `End`, Customer_ID, User_ID, Contact_ID " +
                "FROM appointments", this::parseAppointments);
        executeQuery("SELECT * FROM contacts", this::buildContactMap);
    }

    private void parseAppointments(SQLException ex, ResultSet rs) {
        if (ex != null) return;
        final ObservableList<Appointment> appointments = tableView.getItems();
        try {
            while (rs.next()) {
                appointments.add(new Appointment(rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getTimestamp(6).toLocalDateTime(),
                        rs.getTimestamp(7).toLocalDateTime(),
                        rs.getLong(8),
                        rs.getLong(9),
                        rs.getLong(10)));
            }
        } catch (SQLException exception) {
            printSQLException(exception);
        }
    }

    private void buildContactMap(SQLException ex, ResultSet rs) {
        if (ex != null) return;
        try {
            while (rs.next()) {
                final Contact contact = new Contact(rs.getLong(1), rs.getString(2), rs.getString((3)));
                contactMap.put(contact.getId(), contact);
            }
        } catch (SQLException exception) {
            printSQLException(exception);
        }
    }

    @Override
    protected String getInsertStatement() {
        return insertStatement;
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
