package Controllers;

import Models.Appointment;
import Models.Contact;
import Models.Customer;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AppointmentFormFactory extends FormFactory<Appointment, AppointmentForm> {
    private Map<Long, Contact> contactMap;
    private List<Customer> customers;

    public AppointmentFormFactory(Class<Appointment> modelClass) {
        super(modelClass);
    }

    @Override
    public AppointmentForm getInstance(Mode mode, Appointment record, Consumer<Appointment> callback) {
        return new AppointmentForm(getTitle(mode), contactMap, customers, mode, record, callback);
    }

    public void setContactMap(Map<Long, Contact> contactMap) {
        this.contactMap = contactMap;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
