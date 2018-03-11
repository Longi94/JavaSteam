package in.dragonbra.javasteam.steam.handlers.steamworkshop.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgCREEnumeratePublishedFilesResponse;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.EnumerationUserDetails;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.SteamWorkshop;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This callback is received in response to calling {@link SteamWorkshop#enumerateUserPublishedFiles(EnumerationUserDetails)}.
 */
public class PublishedFilesCallback extends CallbackMsg {

    private EResult result;

    private List<File> files;

    private int totalResults;

    public PublishedFilesCallback(JobID jobID, CMsgCREEnumeratePublishedFilesResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getEresult());

        List<File> fileList = new ArrayList<>();
        for (CMsgCREEnumeratePublishedFilesResponse.PublishedFileId f : msg.getPublishedFilesList()) {
            fileList.add(new File(f));
        }
        files = Collections.unmodifiableList(fileList);

        totalResults = msg.getTotalResults();
    }

    public EResult getResult() {
        return result;
    }

    public List<File> getFiles() {
        return files;
    }

    public int getTotalResults() {
        return totalResults;
    }

    /**
     * Represents the details of a single published file.
     */
    public static class File {
        private long fileID;

        private int reports;

        private float score;

        private int upVotes;

        private int downVotes;

        public File(CMsgCREEnumeratePublishedFilesResponse.PublishedFileId file) {
            fileID = file.getPublishedFileId();
            reports = file.getReports();
            score = file.getScore();
            upVotes = file.getVotesFor();
            downVotes = file.getVotesAgainst();
        }

        public long getFileID() {
            return fileID;
        }

        public int getReports() {
            return reports;
        }

        public float getScore() {
            return score;
        }

        public int getUpVotes() {
            return upVotes;
        }

        public int getDownVotes() {
            return downVotes;
        }
    }
}
