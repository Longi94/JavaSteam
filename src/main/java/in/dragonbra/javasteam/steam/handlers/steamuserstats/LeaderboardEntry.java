package in.dragonbra.javasteam.steam.handlers.steamuserstats;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLbs.CMsgClientLBSGetLBEntriesResponse;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.types.UGCHandle;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.MemoryStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single package in this response.
 */
public class LeaderboardEntry {

    private SteamID steamID;

    private int globalRank;

    private int score;

    private UGCHandle ugcId;

    private List<Integer> details;

    public LeaderboardEntry(CMsgClientLBSGetLBEntriesResponse.Entry entry) {
        globalRank = entry.getGlobalRank();
        score = entry.getScore();
        steamID = new SteamID(entry.getSteamIdUser());
        ugcId = new UGCHandle(entry.getUgcId());

        details = new ArrayList<>();

        MemoryStream ms = new MemoryStream(entry.getDetails().toByteArray());
        BinaryReader br = new BinaryReader(ms);
        try {
            while (ms.getLength() - ms.getPosition() > 4) {
                details.add(br.readInt());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("failed to read details", e);
        }

        details = Collections.unmodifiableList(details);
    }

    public SteamID getSteamID() {
        return steamID;
    }

    public int getGlobalRank() {
        return globalRank;
    }

    public int getScore() {
        return score;
    }

    public UGCHandle getUgcId() {
        return ugcId;
    }

    public List<Integer> getDetails() {
        return details;
    }
}
