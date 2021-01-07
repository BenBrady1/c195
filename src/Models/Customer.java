package Models;

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
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(long divisionId) {
        this.divisionId = divisionId;
    }

    @Override
    public Object[] toValues() {
        return new Object[]{name, address, postalCode, phone, divisionId};
    }

    @Override
    public Customer copy() {
        return new Customer(id, name, address, postalCode, phone, divisionId);
    }

    @Override
    public Customer applyChanges(Customer other) {
        this.name = other.name;
        this.address = other.address;
        this.postalCode = other.postalCode;
        this.phone = other.phone;
        this.divisionId = other.divisionId;
        return this;
    }

    @Override
    public Object[] toValuesWithID() {
        return new Object[]{name, address, postalCode, phone, divisionId, id};
    }
}
