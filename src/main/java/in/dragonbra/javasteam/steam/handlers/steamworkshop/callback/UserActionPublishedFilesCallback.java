package in.dragonbra.javasteam.steam.handlers.steamworkshop.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUCMEnumeratePublishedFilesByUserActionResponse;
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

    private EResult result;

    private List<File> files;

    private int totalResults;

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
     * @return Gets the result.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return Gets the list of enumerated files.
     */
    public List<File> getFiles() {
        return files;
    }

    /**
     * @return Gets the count of total results.
     */
    public int getTotalResults() {
        return totalResults;
    }

    /**
     * Represents the details of a single published file.
     */
    public static class File {

        private long fileID;

        private Date timestamp;

        public File(CMsgClientUCMEnumeratePublishedFilesByUserActionResponse.PublishedFileId file) {
            fileID = file.getPublishedFileId();
            timestamp = new Date(file.getRtimeTimeStamp() * 1000L);
        }

        /**
         * @return Gets the file ID.
         */
        public long getFileID() {
            return fileID;
        }

        /**
         * @return Gets the timestamp of this file.
         */
        public Date getTimestamp() {
            return timestamp;
        }
    }
}
