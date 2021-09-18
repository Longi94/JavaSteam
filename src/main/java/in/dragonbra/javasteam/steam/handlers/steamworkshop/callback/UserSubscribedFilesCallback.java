package in.dragonbra.javasteam.steam.handlers.steamworkshop.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.FriendsMobile.CMsgClientUCMEnumerateUserSubscribedFilesResponse;
import in.dragonbra.javasteam.protobufs.steamclient.FriendsMobile.CMsgClientUCMEnumerateUserSubscribedFilesResponse_PublishedFileId;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.EnumerationUserDetails;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.SteamWorkshop;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This callback is received in response to calling {@link SteamWorkshop#enumerateUserSubscribedFiles(EnumerationUserDetails)}.
 */
public class UserSubscribedFilesCallback extends CallbackMsg {

    private EResult result;

    private List<File> files;

    private int totalResults;

    public UserSubscribedFilesCallback(JobID jobID, CMsgClientUCMEnumerateUserSubscribedFilesResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getEresult());

        List<File> fileList = new ArrayList<>();
        for (CMsgClientUCMEnumerateUserSubscribedFilesResponse_PublishedFileId f : msg.getSubscribedFilesList()) {
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

        private Date timeSubscribed;

        public File(CMsgClientUCMEnumerateUserSubscribedFilesResponse_PublishedFileId file) {
            fileID = file.getPublishedFileId();
            timeSubscribed = new Date(file.getRtime32Subscribed() * 1000L);
        }

        public long getFileID() {
            return fileID;
        }

        public Date getTimeSubscribed() {
            return timeSubscribed;
        }
    }
}
