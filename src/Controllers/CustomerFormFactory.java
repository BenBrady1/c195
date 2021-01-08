package Controllers;

import Models.Country;
import Models.Division;

import java.util.Map;

public final class CustomerFormFactory extends FormFactory<CustomerForm> {
    private Map<Long, Division> divisionMap;
    private Map<Long, Country> countryMap;

    @Override
    public CustomerForm getInstance(Type type) {
        String windowTitle = "Update existing customer";
        if (type == Type.Create) windowTitle = "Create new customer";
        if (type == Type.Read) windowTitle = "View existing customer";
        return new CustomerForm(windowTitle, divisionMap, countryMap);
    }

    public void setDivisionMap(Map<Long, Division> divisionMap) {
        this.divisionMap = divisionMap;
    }

    public void setCountryMap(Map<Long, Country> countryMap) {
        this.countryMap = countryMap;
    }
}
