package in.dragonbra.javasteam.steam.handlers.steamuser;

/**
 * The One-Time-Password details for this response.
 */
public class OTPDetails {

    private int type;

    private String identifier;

    private int value;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
