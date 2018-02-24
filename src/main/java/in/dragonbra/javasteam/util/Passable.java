package in.dragonbra.javasteam.util;

public class Passable<T> {

    private T value;

    public Passable() {
        this(null);
    }

	public Passable(T value) {
		this.value = value;
	}

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
