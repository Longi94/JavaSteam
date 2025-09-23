package in.dragonbra.javasteam.types;

/**
 * Represents a single unique handle to a piece of User Generated Content.
 */
public class UGCHandle extends GlobalID {

    /**
     * Initializes a new instance of the {@link UGCHandle} class.
     */
    public UGCHandle() {
        super();
    }

    /**
     * Initializes a new instance of the {@link UGCHandle} class.
     *
     * @param ugcId The UGC ID.
     */
    public UGCHandle(long ugcId) {
        super(ugcId);
    }
}
