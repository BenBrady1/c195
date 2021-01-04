package Controllers;

import Models.Item;

public abstract class Form extends Base {
    public enum Mode {
        Create,
        Read,
        Update
    }

    private Item item;

    abstract public void open(Item item, Mode mode);
}
