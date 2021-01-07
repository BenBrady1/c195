package Models;

public interface Model<T> {
    T copy();

    Object[] toValues();

    Object[] toValuesWithID();

    T applyChanges(T other);
}
