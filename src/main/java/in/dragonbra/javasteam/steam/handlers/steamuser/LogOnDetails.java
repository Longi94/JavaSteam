package in.dragonbra.javasteam.steam.handlers.steamuser;

import in.dragonbra.javasteam.enums.EOSType;
import in.dragonbra.javasteam.steam.authentication.AuthPollResult;
import in.dragonbra.javasteam.steam.authentication.SteamAuthentication;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.Utils;

/**
 * Represents the details required to log into Steam3 as a user.
 */
@SuppressWarnings("unused")
public class LogOnDetails {

    private String username = "";

    private String password = "";

    private Integer cellID;

    private Integer loginID;

    private String authCode = "";

    private String twoFactorCode = "";

    private boolean shouldRememberPassword;

    private String accessToken;

    private long accountInstance;

    private long accountID;

    private boolean requestSteam2Ticket;

    private EOSType clientOSType;

    private String clientLanguage;

    private String machineName;

    public LogOnDetails() {
        accountInstance = SteamID.DESKTOP_INSTANCE;
        accountID = 0L;

        clientOSType = Utils.getOSType();
        clientLanguage = "english";

        String envName = System.getenv("COMPUTERNAME");
        if (envName == null) {
            envName = System.getenv("HOSTNAME");
        }
        machineName = envName + " (JavaSteam)";
    }

    /**
     * Gets the username.
     *
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     *
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the CellID.
     *
     * @return the CellID.
     */
    public Integer getCellID() {
        return cellID;
    }

    /**
     * Sets the CellID.
     *
     * @param cellID the CellID.
     */
    public void setCellID(int cellID) {
        this.cellID = cellID;
    }

    /**
     * Gets the LoginID. This number is used for identifying logon session.
     *
     * @return the LoginID.
     */
    public Integer getLoginID() {
        return loginID;
    }

    /**
     * Sets the LoginID. This number is used for identifying logon session.
     * The purpose of this field is to allow multiple sessions to the same steam account from the same machine.
     * This is because Steam Network doesn't allow more than one session with the same LoginID to access given account at the same time from the same public IP.
     * If you want to establish more than one active session to given account, you must make sure that every session (to that account) from the same public IP has a unique LoginID.
     * By default, LoginID is automatically generated based on machine's primary bind address, which is the same for all sessions.
     * Null value will cause this property to be automatically generated based on default behaviour.
     * If in doubt, set this property to null.
     *
     * @param loginID the LoginID.
     */
    public void setLoginID(Integer loginID) {
        this.loginID = loginID;
    }

    /**
     * Gets the Steam Guard auth code used to login. This is the code sent to the user's email.
     *
     * @return the auth code.
     */
    public String getAuthCode() {
        return authCode;
    }

    /**
     * Sets the Steam Guard auth code used to login. This is the code sent to the user's email.
     *
     * @param authCode the auth code.
     */
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    /**
     * Gets the 2-factor auth code used to login. This is the code that can be received from the authenticator apps.
     *
     * @return the two-factor auth code.
     */
    public String getTwoFactorCode() {
        return twoFactorCode;
    }

    /**
     * Sets the 2-factor auth code used to login. This is the code that can be received from the authenticator apps.
     *
     * @param twoFactorCode the two-factor auth code.
     */
    public void setTwoFactorCode(String twoFactorCode) {
        this.twoFactorCode = twoFactorCode;
    }

    /**
     * Gets the 'Should Remember Password' flag.
     * This is used in combination with the {@link AuthPollResult#getAccessToken()} for password-less login.
     *
     * @return the 'Should Remember Password' flag.
     */
    public boolean isShouldRememberPassword() {
        return shouldRememberPassword;
    }

    /**
     * Sets the 'Should Remember Password' flag.
     * This is used in combination with the {@link LogOnDetails#accessToken } for password-less login.
     * Set this to true when {@link in.dragonbra.javasteam.steam.authentication.AuthSessionDetails#persistentSession } is set to true.
     *
     * @param shouldRememberPassword the 'Should Remember Password' flag.
     */
    public void setShouldRememberPassword(boolean shouldRememberPassword) {
        this.shouldRememberPassword = shouldRememberPassword;
    }

    /**
     * Gets the access token used to log in. This a token that has been provided after a successful login using
     * {@link SteamAuthentication}.
     *
     * @return the access token used to log in.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the access token used to log in. This a token that has been provided after a successful login using
     * {@link SteamAuthentication}.
     *
     * @param accessToken the access token used to log in.
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Gets the account instance. 1 for the PC instance or 2 for the Console (PS3) instance.
     * <p>
     * See: {@link SteamID#DESKTOP_INSTANCE}
     * See: {@link SteamID#CONSOLE_INSTANCE}
     *
     * @return the account instance.
     */
    public long getAccountInstance() {
        return accountInstance;
    }

    /**
     * Sets the account instance. 1 for the PC instance or 2 for the Console (PS3) instance.
     * <p>
     * See: {@link SteamID#DESKTOP_INSTANCE}
     * See: {@link SteamID#CONSOLE_INSTANCE}
     *
     * @param accountInstance the account instance.
     */
    public void setAccountInstance(long accountInstance) {
        this.accountInstance = accountInstance;
    }

    /**
     * Gets the account ID used for connecting clients when using the Console instance.
     *
     * @return the account ID.
     */
    public long getAccountID() {
        return accountID;
    }

    /**
     * Sets the account ID used for connecting clients when using the Console instance.
     *
     * @param accountID the account ID.
     */
    public void setAccountID(long accountID) {
        this.accountID = accountID;
    }

    /**
     * Gets a value indicating whether to request the Steam2 ticket.
     * This is an optional request only needed for Steam2 content downloads.
     *
     * @return true if the Steam2 ticket should be requested; otherwise, false.
     */
    public boolean isRequestSteam2Ticket() {
        return requestSteam2Ticket;
    }

    /**
     * Sets a value indicating whether to request the Steam2 ticket.
     * This is an optional request only needed for Steam2 content downloads.
     *
     * @param requestSteam2Ticket true if the Steam2 ticket should be requested; otherwise, false.
     */
    public void setRequestSteam2Ticket(boolean requestSteam2Ticket) {
        this.requestSteam2Ticket = requestSteam2Ticket;
    }

    /**
     * Gets the client operating system type.
     *
     * @return the client operating system type.
     */
    public EOSType getClientOSType() {
        return clientOSType;
    }

    /**
     * Sets the client operating system type.
     *
     * @param clientOSType the client operating system type.
     */
    public void setClientOSType(EOSType clientOSType) {
        this.clientOSType = clientOSType;
    }

    /**
     * Gets the client language.
     *
     * @return the client language.
     */
    public String getClientLanguage() {
        return clientLanguage;
    }

    /**
     * Sets the client language.
     *
     * @param clientLanguage the client language.
     */
    public void setClientLanguage(String clientLanguage) {
        this.clientLanguage = clientLanguage;
    }

    /**
     * Gets the machine name
     *
     * @return the machine name
     */
    public String getMachineName() {
        return machineName;
    }

    /**
     * Sets the machine name
     *
     * @param machineName the machine name
     */
    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }
}
