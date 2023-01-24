package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUpdateMachineAuth;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is received when the backend wants the client to update it's local machine authentication data.
 */
public class UpdateMachineAuthCallback extends CallbackMsg {

    private final byte[] data;

    private final int bytesToWrite;

    private final int offset;

    private final String fileName;

    private final OTPDetails oneTimePassword;

    public UpdateMachineAuthCallback(JobID jobID, CMsgClientUpdateMachineAuth.Builder msg) {
        setJobID(jobID);

        data = msg.getBytes().toByteArray();

        bytesToWrite = msg.getCubtowrite();
        offset = msg.getOffset();

        fileName = msg.getFilename();

        oneTimePassword = new OTPDetails();
        oneTimePassword.type = msg.getOtpType();
        oneTimePassword.identifier = msg.getOtpIdentifier();
        oneTimePassword.sharedSecret = msg.getOtpSharedsecret().toByteArray();
        oneTimePassword.timeDrift = msg.getOtpTimedrift();
    }

    /**
     * @return the sentry file data that should be written.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @return the number of bytes to write.
     */
    public int getBytesToWrite() {
        return bytesToWrite;
    }

    /**
     * @return the offset to write to.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @return the name of the sentry file to write.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the one-time-password details as {@link OTPDetails}.
     */
    public OTPDetails getOneTimePassword() {
        return oneTimePassword;
    }

    /**
     * Represents various one-time-password details.
     */
    public static class OTPDetails {

        private int type;

        private String identifier;

        private byte[] sharedSecret;

        private int timeDrift;

        /**
         * @return the OTP type.
         */
        public int getType() {
            return type;
        }

        /**
         * @return the OTP identifier.
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * @return the OTP shared secret.
         */
        public byte[] getSharedSecret() {
            return sharedSecret;
        }

        /**
         * @return the OTP time drift.
         */
        public int getTimeDrift() {
            return timeDrift;
        }
    }

}
