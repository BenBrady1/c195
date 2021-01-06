package Controllers;

import Models.Country;
import Models.Customer;
import Models.Division;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class CustomerTable extends Table<Customer> {
    private final HashMap<Long, Division> divisionMap = new HashMap();
    private final List<Country> countries = new ArrayList();

    public CustomerTable(Optional<Integer> userId) {
        super(userId, new CustomerFormFactory());
        ((CustomerFormFactory) formFactory).setDivisionMap(Collections.unmodifiableMap(divisionMap));
        ((CustomerFormFactory) formFactory).setCountries(Collections.unmodifiableList(countries));
    }

    private String getCountry(long id) {
        for (Country country : countries) {
            if (country.getId() == id) return country.getCountry();
        }

        return null;
    }

    @Override
    protected final void addColumns() {
        final TableColumn<Customer, String> nameColumn = new TableColumn(bundle.getString("customer.name"));
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        final TableColumn<Customer, String> addressColumn = new TableColumn(bundle.getString("customer.address"));
        addressColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getAddress()));
        final TableColumn<Customer, String> postalCodeColumn = new TableColumn(bundle.getString("customer.postalCode"));
        postalCodeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPostalCode()));
        final TableColumn<Customer, String> phoneColumn = new TableColumn(bundle.getString("customer.phone"));
        phoneColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPhone()));
        final TableColumn<Customer, String> divisionColumn = new TableColumn(bundle.getString("customer.division"));
        divisionColumn.setCellValueFactory(param -> {
            final Division division = divisionMap.get(param.getValue().getDivisionId());
            return new SimpleStringProperty(division.getDivision());
        });
        final TableColumn<Customer, String> countryColumn = new TableColumn(bundle.getString("customer.country"));
        countryColumn.setCellValueFactory(param -> {
            final Division division = divisionMap.get(param.getValue().getDivisionId());
            return new SimpleStringProperty(getCountry(division.getCountryId()));
        });
        tableView.getColumns().addAll(nameColumn, addressColumn, postalCodeColumn, phoneColumn, divisionColumn, countryColumn);
    }

    @Override
    protected final void populateData() {
        executeQuery("SELECT Division_ID, Division, Country_ID " +
                "FROM first_level_divisions", (ex, rs) -> {
            if (ex == null) {
                buildDivisionMap(rs);
            } else {
                printSQLException(ex);
            }
        });
        executeQuery("SELECT Country_ID, Country FROM countries", (ex, rs) -> {
            if (ex == null) {
                addCountries(rs);
            } else {
                printSQLException(ex);
            }
        });
        executeQuery("SELECT Customer_ID, Customer_Name, Address, Postal_Code, Phone, d.Division_ID, d.Country_ID " +
                "FROM customers c " +
                "JOIN first_level_divisions d ON d.Division_ID = c.Division_ID;", (ex, rs) -> {
            if (ex == null) {
                consumeResultSet(rs);
            } else {
                printSQLException(ex);
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
                countries.add(country);
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
    }

    @Override
    protected void addToDatabase(Customer record) {
        executeInsert("INSERT INTO customers " +
                "(Customer_Name, Address, Postal_Code, Phone, Division_ID) " +
                "VALUES (?, ?, ?, ?, ?)", record.toValues(), (ex, newId) -> {
            if (ex != null) printSQLException(ex);
            if (newId != null) record.setId(newId);
        });
    }

    @Override
    protected Customer getNewRecord() {
        return new Customer(0, "", "", "", "", 0);
    }

    @Override
    protected void updateInDatabase(Customer record) {
//        executeQuery()
    }

    @Override
    protected void deleteFromDatabase(Customer record) {
        executeUpdate("DELETE FROM customers WHERE Customer_ID = ?",
                new Object[]{record.getId()},
                (ex, updates) -> {
                    if (ex != null) printSQLException(ex);
                    if (updates == 1) record.setId(0);
                });
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
}
