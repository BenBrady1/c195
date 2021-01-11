package Controllers;

import Models.Appointment;
import Models.Contact;
import Models.Customer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.*;

public class AppointmentTable extends Table<Appointment> implements Initializable {
    private Filter filterController = new Filter();

    private HashMap<Long, Contact> contactMap = new HashMap<>();

    private ObservableList<Customer> customers;
    final private String selectQuery = "SELECT Appointment_ID, Title, Description, `Location`, `Type`, `Start`, `End`, Customer_ID, User_ID, Contact_ID " +
            "FROM appointments";

    public AppointmentTable(ObservableList<Customer> customers) {
        super(new AppointmentFormFactory(Appointment.class));
        ((AppointmentFormFactory) formFactory).setContactMap(Collections.unmodifiableMap(contactMap));
        ((AppointmentFormFactory) formFactory).setCustomers(Collections.unmodifiableList(customers));
        this.customers = customers;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        filterButton.setDisable(false);
        filterButton.setVisible(true);
    }

    @Override
    protected void addColumns() {
        final TableColumn<Appointment, String> contactCol = new TableColumn<>(getBundleString("appointment.contact"));
        contactCol
                .setCellValueFactory(param -> {
                    final Optional<Contact> contact = Optional.ofNullable(contactMap.get(param.getValue().getContactId()));
                    return new SimpleStringProperty(contact.map(Contact::getName).orElse(""));
                });
        final TableColumn<Appointment, String> startCol = new TableColumn<>(getBundleString("appointment.start"));
        startCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedStart()));
        final TableColumn<Appointment, String> endCol = new TableColumn<>(getBundleString("appointment.end"));
        endCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedEnd()));
        final TableColumn<Appointment, String> customerIdCol = new TableColumn<>(getBundleString("appointment.customerId"));
        customerIdCol.setCellValueFactory(param -> new SimpleStringProperty(nonZero(param.getValue().getCustomerId())));
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
        executeQuery(selectQuery, this::parseAppointments);
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
                        rs.getTimestamp(6).toLocalDateTime().atOffset(ZoneOffset.UTC),
                        rs.getTimestamp(7).toLocalDateTime().atOffset(ZoneOffset.UTC),
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
        return "INSERT INTO appointments (Title, Description, `Location`, `Type`, `Start`, `End`, Customer_ID, User_ID, Contact_ID, Created_By, Last_Updated_By) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected Appointment getNewRecord() {
        return new Appointment(0, null, null, null, null, null, null, 0, 0, 0);
    }

    @Override
    protected String getUpdateStatement() {
        return "UPDATE appointments " +
                "SET Title = ?, Description = ?, `Location` = ?, `Type` = ?, `Start` = ?, `End` = ?, Customer_ID = ?, User_ID = ?, Contact_ID = ?, Last_Updated_By = ?, Last_Update = NOW() " +
                "WHERE Appointment_ID = ?";
    }

    @Override
    protected boolean deleteDependencies(Appointment record) {
        return true;
    }

    @Override
    protected String getDeleteStatement() {
        return "DELETE FROM appointments WHERE Appointment_ID = ?";
    }

    @Override
    protected String getDeletedMessage() {
        return getBundleString("record.deleted.message")
                .replace("%{record}", getBundleString("appointment.appointment"));
    }

    protected String nonZero(long val) {
        return val == 0 ? "" : Long.toString(val);
    }

    @Override
    protected void addFilter() {
        filterController.openFilterWindow((fields) -> {
            List<Object> arguments = null;
            String query = selectQuery;
            tableView.getItems().clear();
            if (fields != null) {
                System.out.println(fields.year);
                System.out.println(fields.field);
                System.out.println(fields.fieldValue);
                query += String.format(" WHERE YEAR(`Start`) = ? AND %s(`Start`) = ?", fields.field);
                arguments = toArray(fields.year, fields.fieldValue);
            }
            executeQuery(query, arguments, this::parseAppointments);
        });
    }
}
