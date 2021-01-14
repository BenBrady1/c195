package Controllers;

import Models.Country;
import Models.Customer;
import Models.Division;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Comparator;
import java.util.Map;
import java.util.ResourceBundle;

public final class CustomerForm extends Form<Customer> implements Initializable {
    @FXML
    private TextField nameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<Division> divisionComboBox;
    @FXML
    private ComboBox<Country> countryComboBox;

    private final Map<Long, Country> countryMap;
    private final Map<Long, Division> divisionMap;

    public CustomerForm(String windowTitle, Map<Long, Division> divisionMap, Map<Long, Country> countryMap) {
        super(windowTitle);
        this.divisionMap = divisionMap;
        this.countryMap = countryMap;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<Country> countries = countryComboBox.getItems();
        divisionMap.forEach((id, division) -> {
            Country country = countryMap.get(division.getCountryId());
            if (!countries.contains(country)) {
                countries.add(country);
            }
        });
        countries.sort(Comparator.comparing(Country::getCountry));
        divisionComboBox.setDisable(true);
        super.initialize(url, resourceBundle);
    }

    @Override
    protected void setFields() {
        final Division division = divisionMap.get(record.getDivisionId());
        countryComboBox.getSelectionModel().select(countryMap.get(division.getCountryId()));
        countryComboBox.setDisable(readOnly);
        populateDivisions(null);
        divisionComboBox.getSelectionModel().select(division);
    }

    @Override
    protected String getResourceURL() {
        return "/Views/CustomerForm.fxml";
    }

    @FXML
    private void populateDivisions(ActionEvent event) {
        final Country country = countryComboBox.getValue();
        final ObservableList<Division> divisions = divisionComboBox.getItems();
        divisions.clear();
        divisionMap.forEach((key, division) -> {
            if (division.getCountryId() == country.getId()) divisions.add(division);
        });
        divisionComboBox.setDisable(readOnly || divisionComboBox.getItems().isEmpty());
        divisions.sort(Comparator.comparing(Division::getDivision));
    }

    @Override
    protected void applyOtherFieldsToRecord() {
        record.setDivisionId(getRecordId(divisionComboBox.getValue()));
    }

    @Override
    protected double getHeight() {
        return 400;
    }

    @Override
    protected double getWidth() {
        return 400;
    }
}
