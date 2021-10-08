package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientEmailAddrInfo;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * This callback is received when email information is received from the network.
 */
public class EmailAddrInfoCallback extends CallbackMsg {

    private String emailAddress;

    private boolean emailIsValidated;

    private boolean emailValidationChanged;

    private boolean credentialChangeRequiresCode;

    private boolean passwordOrSecretqaChangeRequiresCode;

    private boolean remindUserAboutEmail;

    public EmailAddrInfoCallback(CMsgClientEmailAddrInfo.Builder msg) {
        emailAddress = msg.getEmailAddress();

        emailIsValidated = msg.getEmailIsValidated();

        emailValidationChanged = msg.getEmailValidationChanged();

        credentialChangeRequiresCode = msg.getCredentialChangeRequiresCode();

        passwordOrSecretqaChangeRequiresCode = msg.getPasswordOrSecretqaChangeRequiresCode();

        remindUserAboutEmail = msg.getRemindUserAboutEmail();
    }

    /**
     * Gets the email address of this account.
     *
     * @return the email address
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Gets a value indicating validated email or not.
     *
     * @return whether the email address is validated or not.
     */
    public boolean isEmailValidated() {
        return emailIsValidated;
    }

    public boolean isEmailValidationChanged() {
        return emailValidationChanged;
    }

    public boolean isCredentialChangeRequiresCode() {
        return credentialChangeRequiresCode;
    }

    public boolean isPasswordOrSecretqaChangeRequiresCode() {
        return passwordOrSecretqaChangeRequiresCode;
    }

    public boolean isRemindUserAboutEmail() {
        return remindUserAboutEmail;
    }
}
