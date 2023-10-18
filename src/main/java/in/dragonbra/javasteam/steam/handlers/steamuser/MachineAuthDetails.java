package in.dragonbra.javasteam.steam.handlers.steamuser;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.UpdateMachineAuthCallback;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.Callback;
import in.dragonbra.javasteam.types.JobID;

/**
 * @deprecated Steam no longer sends machine auth as of 2023, use SteamAuthentication.
 *
 * Represents details required to complete a machine auth request.
 */
@Deprecated
public class MachineAuthDetails {

    private JobID jobID;

    private EResult eResult;

    private int bytesWritten;

    private int offset;

    private String fileName = "";

    private int fileSize;

    private int lastError;

    private byte[] sentryFileHash;

    private OTPDetails oneTimePassword = new OTPDetails();

    /**
     * Gets the target Job ID for the request.
     * This is provided in the {@link Callback} for a {@link UpdateMachineAuthCallback}.
     *
     * @return the jobID.
     */
    public JobID getJobID() {
        return jobID;
    }

    /**
     * Sets the target Job ID for the request.
     * This is provided in the {@link Callback} for a {@link UpdateMachineAuthCallback}.
     *
     * @param jobID the jobID.
     */
    public void setJobID(JobID jobID) {
        this.jobID = jobID;
    }

    /**
     * Get the result of updating the machine auth.
     *
     * @return the result as {@link EResult}
     */
    public EResult getEResult() {
        return eResult;
    }

    /**
     * Set the result of updating the machine auth.
     *
     * @param eResult the result as {@link EResult}
     */
    public void setEResult(EResult eResult) {
        this.eResult = eResult;
    }

    /**
     * Gets the number of bytes written for the sentry file.
     *
     * @return the number of bytes written.
     */
    public int getBytesWritten() {
        return bytesWritten;
    }

    /**
     * Sets the number of bytes written for the sentry file.
     *
     * @param bytesWritten the number of bytes written.
     */
    public void setBytesWritten(int bytesWritten) {
        this.bytesWritten = bytesWritten;
    }

    /**
     * Gets the offset within the sentry file that was written.
     *
     * @return the offset.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the offset within the sentry file that was written.
     *
     * @param offset the offset.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Gets the filename of the sentry file that was written.
     *
     * @return the name of the sentry file.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the filename of the sentry file that was written.
     *
     * @param fileName the name of the sentry file.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the size of the sentry file.
     *
     * @return the size of the sentry file.
     */
    public int getFileSize() {
        return fileSize;
    }

    /**
     * Sets the size of the sentry file.
     *
     * @param fileSize the size of the sentry file.
     */
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Gets the last error that occurred while writing the sentry file, or 0 if no error occurred.
     *
     * @return the last error.
     */
    public int getLastError() {
        return lastError;
    }

    /**
     * Sets the last error that occurred while writing the sentry file, or 0 if no error occurred.
     *
     * @param lastError the last error.
     */
    public void setLastError(int lastError) {
        this.lastError = lastError;
    }

    /**
     * Gets the SHA-1 hash of the sentry file.
     *
     * @return the sentry file hash.
     */
    public byte[] getSentryFileHash() {
        return sentryFileHash;
    }

    /**
     * Sets the SHA-1 hash of the sentry file.
     *
     * @param sentryFileHash the sentry file hash.
     */
    public void setSentryFileHash(byte[] sentryFileHash) {
        this.sentryFileHash = sentryFileHash;
    }

    /**
     * Gets the one-time-password details.
     *
     * @return the one time password details.
     */
    public OTPDetails getOneTimePassword() {
        return oneTimePassword;
    }

    /**
     * Sets the one-time-password details.
     *
     * @param oneTimePassword the one time password details.
     */
    public void setOneTimePassword(OTPDetails oneTimePassword) {
        this.oneTimePassword = oneTimePassword;
    }
}
