package in.dragonbra.javasteam.steam.handlers.steamfriends.cache;

import in.dragonbra.javasteam.enums.EClanRelationship;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public class Clan extends Account {
    private EClanRelationship relationship;

    public EClanRelationship getRelationship() {
        return relationship;
    }

    public void setRelationship(EClanRelationship relationship) {
        this.relationship = relationship;
    }
}
