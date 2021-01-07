package Models;

import java.util.List;

public interface Model<T> {
    T copy();

    List<Object> toValues();

    T applyChanges(T other);
}
