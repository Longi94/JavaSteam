package in.dragonbra.javasteam.steam.handlers.steamfriends.cache;

import in.dragonbra.javasteam.enums.EFriendRelationship;
import in.dragonbra.javasteam.enums.EPersonaState;
import in.dragonbra.javasteam.types.GameID;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public class User extends Account {
    private EFriendRelationship relationship;

    private EPersonaState personaState;

    private int personaStateFlags;

    private int gameAppID;

    private GameID gameID = new GameID();

    private String gameName;

    public EFriendRelationship getRelationship() {
        return relationship;
    }

    public void setRelationship(EFriendRelationship relationship) {
        this.relationship = relationship;
    }

    public EPersonaState getPersonaState() {
        return personaState;
    }

    public void setPersonaState(EPersonaState personaState) {
        this.personaState = personaState;
    }

    public int getPersonaStateFlags() {
        return personaStateFlags;
    }

    public void setPersonaStateFlags(int personaStateFlasg) {
        this.personaStateFlags = personaStateFlags;
    }

    public int getGameAppID() {
        return gameAppID;
    }

    public void setGameAppID(int gameAppID) {
        this.gameAppID = gameAppID;
    }

    public GameID getGameID() {
        return gameID;
    }

    public void setGameID(GameID gameID) {
        this.gameID = gameID;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
}
