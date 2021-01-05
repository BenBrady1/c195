package Controllers;

public abstract class FormFactory<T extends Form> {
    enum Type {
        Create,
        Read,
        Update
    }

    abstract public T getInstance(FormFactory.Type type);
}
