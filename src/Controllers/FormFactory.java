package Controllers;

import Models.Record;

public abstract class FormFactory<T extends Form> extends Base {
    private final Class<? extends Record> modelClass;

    enum Type {
        Create,
        Read,
        Update
    }

    public <M extends Record> FormFactory(Class<M> modelClass) {
        this.modelClass = modelClass;
    }

    protected String getTitle(Type type) {
        return bundle.getString(String.format("form.%s.%s", type.toString().toLowerCase(), modelClass.getSimpleName().toLowerCase()));
    }

    abstract public T getInstance(FormFactory.Type type);
}
