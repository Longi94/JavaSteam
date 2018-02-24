package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUpdateMachineAuth;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is received when the backend wants the client to update it's local machine authentication data.
 */
public class UpdateMachineAuthCallback extends CallbackMsg {

    private byte[] data;

    private int bytesToWrite;

    private int offset;

    private String fileName;

    private OTPDetails oneTimePassword;

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

    public byte[] getData() {
        return data;
    }

    public int getBytesToWrite() {
        return bytesToWrite;
    }

    public int getOffset() {
        return offset;
    }

    public String getFileName() {
        return fileName;
    }

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

        public int getType() {
            return type;
        }

        public String getIdentifier() {
            return identifier;
        }

        public byte[] getSharedSecret() {
            return sharedSecret;
        }

        public int getTimeDrift() {
            return timeDrift;
        }
    }

}
