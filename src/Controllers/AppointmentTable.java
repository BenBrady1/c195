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
import java.util.*;

public final class AppointmentTable extends Table<Appointment> implements Initializable {
    private Filter filterController = new Filter();

    private HashMap<Long, Contact> contactMap = new HashMap<>();

    private ObservableList<Customer> customers;
    private final String selectQuery = "SELECT Appointment_ID, Title, Description, `Location`, `Type`, `Start`, `End`, Customer_ID, User_ID, Contact_ID " +
            "FROM appointments";

    public AppointmentTable(ObservableList<Customer> customers, Main.EventEmitter eventEmitter) {
        super(new AppointmentFormFactory(Appointment.class), eventEmitter);
        ((AppointmentFormFactory) formFactory).setContactMap(Collections.unmodifiableMap(contactMap));
        ((AppointmentFormFactory) formFactory).setCustomers(Collections.unmodifiableList(customers));
        this.customers = customers;
        eventEmitter.addListener(Main.Event.CustomerDeleted, this::populateTable);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        filterButton.setDisable(false);
        filterButton.setVisible(true);
    }

    @Override
    protected void addColumns() {
        final TableColumn<Appointment, String> contactCol = new TableColumn<>(bundle.getString("appointment.contact"));
        contactCol
                .setCellValueFactory(param -> {
                    final Optional<Contact> contact = Optional.ofNullable(contactMap.get(param.getValue().getContactId()));
                    return new SimpleStringProperty(contact.map(Contact::getName).orElse(""));
                });
        final TableColumn<Appointment, String> startCol = new TableColumn<>(bundle.getString("appointment.start"));
        startCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedStart()));
        final TableColumn<Appointment, String> endCol = new TableColumn<>(bundle.getString("appointment.end"));
        endCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedEnd()));
        final TableColumn<Appointment, String> customerIdCol = new TableColumn<>(bundle.getString("appointment.customerId"));
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
    protected final void populateData() {
        executeQuery(selectQuery, this::parseAppointments);
        executeQuery("SELECT * FROM contacts", this::buildContactMap);
    }

    private void populateTable() {
        executeQuery(selectQuery, this::parseAppointments);
    }

    private void parseAppointments(SQLException ex, ResultSet rs) {
        if (ex != null) return;
        final ObservableList<Appointment> appointments = tableView.getItems();
        appointments.clear();
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
        return bundle.getString("record.deleted.message")
                .replace("%{record}", bundle.getString("appointment.appointment"));
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
                query += String.format(" WHERE YEAR(`Start`) = ? AND %s(`Start`) = ?", fields.field);
                arguments = toArray(fields.year, fields.fieldValue);
            }
            executeQuery(query, arguments, this::parseAppointments);
        });
    }

    @Override
    protected boolean canUpdate(Appointment record) {
        String query = "SELECT COUNT(*) FROM appointments " +
                "WHERE (UNIX_TIMESTAMP(`START`) BETWEEN UNIX_TIMESTAMP(?) AND UNIX_TIMESTAMP(?)" +
                "OR UNIX_TIMESTAMP(`END`) BETWEEN UNIX_TIMESTAMP(?) AND UNIX_TIMESTAMP(?))";
        final List<Object> arguments = toArray(record.getSQLStart(), record.getSQLEnd(), record.getSQLStart(), record.getSQLEnd());
        if (record.getId() != 0L) {
            query += " AND Appointment_Id != ?";
            arguments.add(record.getId());
        }
        return executeQuery(query, arguments, (ex, rs) -> {
            if (ex != null) return false;
            try {
                rs.next();
                return rs.getInt("COUNT(*)") == 0;
            } catch (SQLException exception) {
                printSQLException(exception);
                return false;
            }
        });
    }
}
