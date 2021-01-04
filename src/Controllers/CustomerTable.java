package Controllers;

import Models.Customer;
import Models.Item;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class CustomerTable extends Table {
    @FXML
    protected TableView<Customer> tableView;

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
        divisionColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDivision()));
        final TableColumn<Customer, String> countryColumn = new TableColumn(bundle.getString("customer.country"));
        countryColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCountry()));
        tableView.getColumns().addAll(nameColumn, addressColumn, postalCodeColumn, phoneColumn, divisionColumn, countryColumn);
    }

    @Override
    protected final void populateData() {
        System.out.println("querying database for customer information");
        executeQuery("SELECT Customer_ID, Customer_Name, Address, Postal_Code, Phone, Division, Country " +
                "FROM customers c " +
                "JOIN first_level_divisions d ON d.Division_ID = c.Division_ID " +
                "JOIN countries ctry ON ctry.Country_ID = d.COUNTRY_ID;", null, (ex, rs) -> {
            if (ex == null) {
                consumeResultSet(rs);
            } else {
                printSQLException(ex);
            }
            return null;
        });
    }

    @Override
    protected void addToDatabase(Item item) {
        // TODO: implement
    }

    @Override
    protected Item getNewItem() {
        // TODO: implement
        return null;
    }

    @Override
    protected void updateInDatabase(Item item) {
        // TODO: implement
    }

    @Override
    protected void deleteFromDatabase(Item item) {
        // TODO: implement
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
                        rs.getString(6),
                        rs.getString(7)
                ));
            }
        } catch (SQLException ex) {
            printSQLException(ex);
        }
    }
}
