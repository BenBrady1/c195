package Models;

public class Country extends Record {
    private final String country;

    public Country(int id, String country) {
        super(id);
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return country;
    }
}
