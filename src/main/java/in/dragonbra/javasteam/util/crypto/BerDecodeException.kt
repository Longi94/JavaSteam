package in.dragonbra.javasteam.util.crypto;

@SuppressWarnings("unused")
public final class BerDecodeException extends Exception {

    private final int _position;

	public BerDecodeException() {
		_position = 0;
	}

	public BerDecodeException(String message) {
		super(message);
		_position = 0;
	}

	public BerDecodeException(String message, Exception ex) {
		super(message, ex);
		_position = 0;
	}

	public BerDecodeException(String message, int position) {
		super(message);
		_position = position;
	}

	public BerDecodeException(String message, int position, Exception ex) {
		super(message, ex);
		_position = position;
	}

    public int get_position() {
        return _position;
    }

    @Override
	public String getMessage() {
        return super.getMessage() + String.format(" (Position %d)%s", _position, System.lineSeparator());
	}
}
