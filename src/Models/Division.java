package Models;

public class Division extends Record {
    private final String division;
    private final long countryId;

    public Division(int id, String division, long countryId) {
        super(id);
        this.division = division;
        this.countryId = countryId;
    }

    public String getDivision() {
        return division;
    }

    public long getCountryId() {
        return countryId;
    }

    @Override
    public String toString() {
        return division;
    }
}
