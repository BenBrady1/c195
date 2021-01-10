package Controllers;

import Models.Contact;
import Models.Customer;
import Models.Record;

import java.util.List;
import java.util.Map;

public class AppointmentFormFactory extends FormFactory<AppointmentForm> {
    private Map<Long, Contact> contactMap;
    private List<Customer> customers;

    public <M extends Record> AppointmentFormFactory(Class<M> modelClass) {
        super(modelClass);
    }

    @Override
    public AppointmentForm getInstance(Type type) {
        return new AppointmentForm(getTitle(type), contactMap, customers);
    }

    public void setContactMap(Map<Long, Contact> contactMap) {
        this.contactMap = contactMap;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
