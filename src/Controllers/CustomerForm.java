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
import java.util.*;
import java.util.stream.Collectors;

public final class CustomerForm extends Form<Customer> implements Initializable {
    @FXML
    private TextField idField;
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

    private List<Country> countries;
    private final Map<Long, Division> divisionMap;

    public CustomerForm(String windowTitle, Map<Long, Division> divisionMap, List<Country> countries) {
        super(windowTitle);
        this.divisionMap = divisionMap;
        this.countries = countries;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<Long> countriesWithDivisions = new ArrayList<>();
        divisionMap.forEach((id, division) -> {
            if (!countriesWithDivisions.contains(division.getCountryId())) {
                countriesWithDivisions.add(division.getCountryId());
            }
        });
        countryComboBox.getItems()
                .addAll(countries.stream().filter(country -> countriesWithDivisions.contains(country.getId()))
                        .collect(Collectors.toList()));
        idField.setDisable(true);
        divisionComboBox.setDisable(true);
        super.initialize(url, resourceBundle);
    }

    @Override
    protected void setFields() {
        idField.setText(Long.toString(record.getId()));
        nameField.setText(record.getName());
        nameField.setDisable(readOnly);
        addressField.setText(record.getAddress());
        addressField.setDisable(readOnly);
        postalCodeField.setText(record.getPostalCode());
        postalCodeField.setDisable(readOnly);
        phoneField.setText(record.getPhone());
        phoneField.setDisable(readOnly);
        final Division division = divisionMap.get(record.getDivisionId());
        countryComboBox.getSelectionModel().select(findCountry(division.getCountryId()));
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
        final Country country = countryComboBox.getSelectionModel().getSelectedItem();
        final ObservableList<Division> divisions = divisionComboBox.getItems();
        divisions.removeAll(divisions);
        divisionMap.forEach((key, division) -> {
            if (division.getCountryId() == country.getId()) divisions.add(division);
        });
        divisionComboBox.setDisable(divisionComboBox.getItems().isEmpty());
        divisions.sort(Comparator.comparing(Division::getDivision));
    }

    @Override
    protected void validateRecord() {
        record.setName(nameField.getText());
        record.setAddress(addressField.getText());
        record.setPostalCode(postalCodeField.getText());
        record.setPhone(phoneField.getText());
        record.setDivisionId(divisionComboBox.getSelectionModel().getSelectedItem().getId());
    }

    private Country findCountry(long countryId) {
        for (Country country : countries) {
            if (country.getId() == countryId) return country;
        }

        return null;
    }
}
