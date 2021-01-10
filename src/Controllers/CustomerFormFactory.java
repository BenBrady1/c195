package Controllers;

import Models.Country;
import Models.Division;
import Models.Record;

import java.util.Map;

public final class CustomerFormFactory extends FormFactory<CustomerForm> {
    private Map<Long, Division> divisionMap;
    private Map<Long, Country> countryMap;

    public <M extends Record> CustomerFormFactory(Class<M> modelClass) {
        super(modelClass);
    }

    @Override
    public CustomerForm getInstance(Type type) {
        return new CustomerForm(getTitle(type), divisionMap, countryMap);
    }

    public void setDivisionMap(Map<Long, Division> divisionMap) {
        this.divisionMap = divisionMap;
    }

    public void setCountryMap(Map<Long, Country> countryMap) {
        this.countryMap = countryMap;
    }
}
