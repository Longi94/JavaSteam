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
     * @return the email address of this account.
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * @return a value indicating validated email or not.
     */
    public boolean isEmailValidated() {
        return emailIsValidated;
    }

    /**
     * @return ???
     */
    public boolean isEmailValidationChanged() {
        return emailValidationChanged;
    }

    /**
     * @return ???
     */
    public boolean isCredentialChangeRequiresCode() {
        return credentialChangeRequiresCode;
    }

    /**
     * @return ???
     */
    public boolean isPasswordOrSecretqaChangeRequiresCode() {
        return passwordOrSecretqaChangeRequiresCode;
    }

    /**
     * @return ???
     */
    public boolean isRemindUserAboutEmail() {
        return remindUserAboutEmail;
    }
}
