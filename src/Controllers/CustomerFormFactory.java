package Controllers;

import Models.Country;
import Models.Customer;
import Models.Division;

import java.util.Map;
import java.util.function.Consumer;

public final class CustomerFormFactory extends FormFactory<Customer, CustomerForm> {
    private Map<Long, Division> divisionMap;
    private Map<Long, Country> countryMap;

    public CustomerFormFactory(Class<Customer> modelClass) {
        super(modelClass);
    }

    @Override
    public CustomerForm getInstance(Mode mode, Customer record, Consumer<Customer> callback) {
        return new CustomerForm(getTitle(mode), divisionMap, countryMap, mode, record, callback);
    }

    public void setDivisionMap(Map<Long, Division> divisionMap) {
        this.divisionMap = divisionMap;
    }

    public void setCountryMap(Map<Long, Country> countryMap) {
        this.countryMap = countryMap;
    }
}
