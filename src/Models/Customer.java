package Models;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class Customer extends Record implements Model<Customer> {
    private String name;
    private String address;
    private String postalCode;
    private String phone;
    private long divisionId;

    public Customer(long id, String name, String address, String postalCode, String phone, long divisionId) {
        super(id);
        this.name = name;
        this.address = address;
        this.postalCode = postalCode;
        this.phone = phone;
        this.divisionId = divisionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address.trim();
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone.trim();
    }

    public long getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(long divisionId) {
        this.divisionId = divisionId;
    }

    @Override
    public List<Object> toValues() {
        return new ArrayList<Object>(List.of(name, address, postalCode, phone, divisionId));
    }

    @Override
    public Customer copy() {
        return new Customer(id, name, address, postalCode, phone, divisionId);
    }

    @Override
    public Customer applyChanges(Customer other) {
        this.setName(other.name);
        this.setAddress(other.address);
        this.setPostalCode(other.postalCode);
        this.setPhone(other.phone);
        this.setDivisionId(other.divisionId);
        return this;
    }
}
