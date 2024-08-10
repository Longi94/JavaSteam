package in.dragonbra.javasteam.steam.handlers.steamworkshop.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUcm.CMsgClientUCMEnumeratePublishedFilesByUserActionResponse;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.EnumerationUserDetails;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.SteamWorkshop;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This callback is received in response to calling {@link SteamWorkshop#enumeratePublishedFilesByUserAction(EnumerationUserDetails)}.
 */
public class UserActionPublishedFilesCallback extends CallbackMsg {

    private final EResult result;

    private final List<File> files;

    private final int totalResults;

    public UserActionPublishedFilesCallback(JobID jobID, CMsgClientUCMEnumeratePublishedFilesByUserActionResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getEresult());

        List<File> fileList = new ArrayList<>();
        for (CMsgClientUCMEnumeratePublishedFilesByUserActionResponse.PublishedFileId f : msg.getPublishedFilesList()) {
            fileList.add(new File(f));
        }
        files = Collections.unmodifiableList(fileList);

        totalResults = msg.getTotalResults();
    }

    /**
     * @return the result by {@link EResult}.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the list of enumerated files.
     */
    public List<File> getFiles() {
        return files;
    }

    /**
     * @return the count of total results.
     */
    public int getTotalResults() {
        return totalResults;
    }

    /**
     * Represents the details of a single published file.
     */
    public static class File {

        private final long fileID;

        private final Date timestamp;

        public File(CMsgClientUCMEnumeratePublishedFilesByUserActionResponse.PublishedFileId file) {
            fileID = file.getPublishedFileId();
            timestamp = new Date(file.getRtimeTimeStamp() * 1000L);
        }

        /**
         * @return the file ID.
         */
        public long getFileID() {
            return fileID;
        }

        /**
         * @return the timestamp of this file.
         */
        public Date getTimestamp() {
            return timestamp;
        }
    }
}
