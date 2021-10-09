package in.dragonbra.javasteam.steam.handlers.steamuser;

/**
 * The One-Time-Password details for this response.
 */
public class OTPDetails {

    private int type;

    private String identifier;

    private int value;

    /**
     * @return the one-time-password type.
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the one-time-password type.
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the one-time-password identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the one-time-password identifier.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the one-time-password value.
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value the one-time-password value.
     */
    public void setValue(int value) {
        this.value = value;
    }
}
