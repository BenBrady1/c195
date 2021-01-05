package Controllers;

public final class CustomerFormFactory extends FormFactory<CustomerForm> {
    @Override
    public CustomerForm getInstance(Type type) {
        if (type == Type.Create) return new CustomerForm("Create new customer");
        if (type == Type.Read) return new CustomerForm("View existing customer");
        return new CustomerForm("Update existing customer");
    }
}
