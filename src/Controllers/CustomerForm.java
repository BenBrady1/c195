package Controllers;

import Models.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public final class CustomerForm extends Form<Customer> {
    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox divisionComboBox;
    @FXML
    private ComboBox countryComboBox;

    private String windowTitle;

    public CustomerForm(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    @Override
    protected void setFields() {
        idField.setText(Integer.toString(item.getId()));
        idField.setDisable(true);
        nameField.setText(item.getName());
        nameField.setDisable(readOnly);
        postalCodeField.setText(item.getPostalCode());
        postalCodeField.setDisable(readOnly);
        phoneField.setText(item.getPhone());
        phoneField.setDisable(readOnly);
    }

    @Override
    protected String getWindowTitle() {
        return windowTitle;
    }

    @Override
    protected String getResourceURL() {
        return "/Views/CustomerForm.fxml";
    }
}
