package Controllers;

import Models.Country;
import Models.Customer;
import Models.Division;
import Models.Record;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;

public final class CustomerTable extends Table<Customer> {
    private final HashMap<Long, Division> divisionMap = new HashMap<>();
    private final HashMap<Long, Country> countryMap = new HashMap<>();

    public CustomerTable(Main.EventEmitter eventEmitter) {
        super(new CustomerFormFactory(Customer.class), eventEmitter);
        ((CustomerFormFactory) formFactory).setDivisionMap(Collections.unmodifiableMap(divisionMap));
        ((CustomerFormFactory) formFactory).setCountryMap(Collections.unmodifiableMap(countryMap));
    }

    /**
     * lambda1: properly convert a division id into a displayable division name
     * lambda2: properly convert a country id into a displayable country name
     *
     * @see Table#addColumns()
     */
    @Override
    protected final void addColumns() {
        final TableColumn<Customer, String> nameColumn = getStringColumn(Customer.class, "name");
        final TableColumn<Customer, String> addressColumn = getStringColumn(Customer.class, "address");
        final TableColumn<Customer, String> postalCodeColumn = getStringColumn(Customer.class, "postalCode");
        final TableColumn<Customer, String> phoneColumn = getStringColumn(Customer.class, "phone");
        final TableColumn<Customer, String> divisionColumn = new TableColumn<>(bundle.getString("customer.division"));
        // lambda to properly convert a division id into a displayable division name
        divisionColumn.setCellValueFactory(param -> {
            final Division division = divisionMap.get(param.getValue().getDivisionId());
            return new SimpleStringProperty(division.getDivision());
        });
        final TableColumn<Customer, String> countryColumn = new TableColumn<>(bundle.getString("customer.country"));
        // lambda to properly convert a country id into a displayable country name
        countryColumn.setCellValueFactory(param -> {
            final Division division = divisionMap.get(param.getValue().getDivisionId());
            return new SimpleStringProperty(countryMap.get(division.getCountryId()).getCountry());
        });
        tableView.getColumns().addAll(nameColumn, addressColumn, postalCodeColumn, phoneColumn, divisionColumn, countryColumn);
    }

    /**
     * lambda1-4: consume an exception and result set and allow for DRY resource cleanup
     *
     * @see Table#populateData()
     */
    @Override
    protected final void populateData() {
        // lambda to consume an exception and result set and allow for DRY resource cleanup
        executeQuery("SELECT Division_ID, Division, Country_ID FROM first_level_divisions", (ex, rs) -> {
            if (ex == null) buildDivisionMap(rs);
        });
        // lambda to consume an exception and result set and allow for DRY resource cleanup
        executeQuery("SELECT Country_ID, Country FROM countries", (ex, rs) -> {
            if (ex == null) addCountries(rs);
        });
        // lambda to consume an exception and result set and allow for DRY resource cleanup
        executeQuery("SELECT Customer_ID, Customer_Name, Address, Postal_Code, Phone, d.Division_ID, d.Country_ID " +
                "FROM customers c " +
                "JOIN first_level_divisions d ON d.Division_ID = c.Division_ID;", (ex, rs) -> {
            if (ex == null) consumeResultSet(rs);
        });
        // lambda to consume an exception and result set and allow for DRY resource cleanup
        executeQuery("SELECT COUNT(*) FROM appointments " +
                "WHERE `Start` BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 15 MINUTE)", (ex, rs) -> {
            if (ex != null) return;
            try {
                rs.next();
                String alertBody;
                alertBody = rs.getInt("COUNT(*)") != 0
                        ? bundle.getString("appointment.upcomingAppointment")
                        : bundle.getString("appointment.noUpcomingAppointment");
                displayAlert(bundle.getString("appointment.alertTitle"),
                        alertBody,
                        Alert.AlertType.INFORMATION);
            } catch (SQLException exception) {
                printSQLException(exception);
            }
        });
    }

    private void buildDivisionMap(ResultSet rs) {
        try {
            while (rs.next()) {
                final Division division = new Division(rs.getInt(1), rs.getString(2), rs.getInt(3));
                divisionMap.put(division.getId(), division);
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
    }

    private void addCountries(ResultSet rs) {
        try {
            while (rs.next()) {
                final Country country = new Country(rs.getInt(1), rs.getString(2));
                countryMap.put(country.getId(), country);
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
    }

    /**
     * @see Table#getNewRecord()
     */
    @Override
    protected Customer getNewRecord() {
        return new Customer(0, "", "", "", "", 0);
    }

    @Override
    protected boolean canUpdate(Customer record) {
        return true;
    }

    private void consumeResultSet(ResultSet rs) {
        try {
            while (rs.next()) {
                tableView.getItems().add(new Customer(
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getInt(6)
                ));
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
    }

    /**
     * @see Table#getInsertStatement()
     */
    @Override
    public String getInsertStatement() {
        return "INSERT INTO customers " +
                "(Customer_Name, Address, Postal_Code, Phone, Division_ID, Created_By, Last_Updated_By) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    /**
     * @see Table#getUpdateStatement()
     */
    @Override
    public String getUpdateStatement() {
        return "UPDATE customers " +
                "SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, Division_ID = ?, Last_Updated_By = ?, Last_Update = NOW() " +
                "WHERE Customer_ID = ?";
    }

    /**
     * @see Table#getDeleteStatement()
     */
    @Override
    public String getDeleteStatement() {
        return "DELETE FROM customers WHERE Customer_ID = ?";
    }

    /**
     * lambda1: consume an exception and result set and allow for DRY resource cleanup
     *
     * @see Table#deleteDependencies(Record)
     */
    @Override
    protected boolean deleteDependencies(Customer record) {
        // lambda to consume an exception and result set and allow for DRY resource cleanup
        return executeUpdate("DELETE FROM appointments WHERE Customer_ID = ?", toArray(record.getId()), (ex, updates) -> ex == null);
    }

    /**
     * lambda1: consume an exception and result set and allow for DRY resource cleanup
     *
     * @see Table#getDeletedMessage(Record)
     */
    @Override
    protected String getDeletedMessage(Customer customer) {
        final String appointments = executeQuery("SELECT Appointment_ID, Type FROM appointments WHERE Customer_ID = ?",
                toArray(customer.getId()),
                this::parseAppointments);

        String message = bundle.getString("record.deleted.message")
                .replace("%{record}", bundle.getString("customer.customer"));

        if (appointments.length() != 0) {
            message += "\n\n" + bundle.getString("appointment.deleted") + "\n" + appointments;
        }

        return message;
    }

    /**
     * parses the appointments that got deleted in association with a customer record
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing the appointment rows
     * @return the string to display
     */
    private String parseAppointments(SQLException ex, ResultSet rs) {
        String output = "";
        if (ex != null) return output;
        try {
            while (rs.next()) {
                output += String.format("%s: %d, %s: %s\n",
                        bundle.getString("record.deleted.id"),
                        rs.getInt(1),
                        bundle.getString("appointment.type"),
                        rs.getString(2));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return output;
    }

    /**
     * @see Table#emitEvent()
     */
    @Override
    protected void emitEvent() {
        eventEmitter.emit(Main.Event.CustomerDeleted);
    }
}
