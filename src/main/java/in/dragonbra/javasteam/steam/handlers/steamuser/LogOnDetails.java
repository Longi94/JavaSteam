package in.dragonbra.javasteam.steam.handlers.steamuser;

import in.dragonbra.javasteam.enums.EOSType;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.Utils;

/**
 * Represents the details required to log into Steam3 as a user.
 */
public class LogOnDetails {
    private String username;

    private String password;

    private int cellID;

    private Integer loginID;

    private String authCode;

    private String twoFactorCode;

    private String loginKey;

    private boolean shouldRememberPassword;

    private byte[] sentryFileHash;

    private long accountInstance;

    private long accountID;

    private boolean requestSteam2Ticket;

    private EOSType clientOSType;

    private String clientLanguage;

    public LogOnDetails() {
        accountInstance = SteamID.DESKTOP_INSTANCE;
        accountID = 0L;

        clientOSType = Utils.getOSType();
        clientLanguage = "english";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCellID() {
        return cellID;
    }

    public void setCellID(int cellID) {
        this.cellID = cellID;
    }

    public Integer getLoginID() {
        return loginID;
    }

    public void setLoginID(Integer loginID) {
        this.loginID = loginID;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getTwoFactorCode() {
        return twoFactorCode;
    }

    public void setTwoFactorCode(String twoFactorCode) {
        this.twoFactorCode = twoFactorCode;
    }

    public String getLoginKey() {
        return loginKey;
    }

    public void setLoginKey(String loginKey) {
        this.loginKey = loginKey;
    }

    public boolean isShouldRememberPassword() {
        return shouldRememberPassword;
    }

    public void setShouldRememberPassword(boolean shouldRememberPassword) {
        this.shouldRememberPassword = shouldRememberPassword;
    }

    public byte[] getSentryFileHash() {
        return sentryFileHash;
    }

    public void setSentryFileHash(byte[] sentryFileHash) {
        this.sentryFileHash = sentryFileHash;
    }

    public long getAccountInstance() {
        return accountInstance;
    }

    public void setAccountInstance(long accountInstance) {
        this.accountInstance = accountInstance;
    }

    public long getAccountID() {
        return accountID;
    }

    public void setAccountID(long accountID) {
        this.accountID = accountID;
    }

    public boolean isRequestSteam2Ticket() {
        return requestSteam2Ticket;
    }

    public void setRequestSteam2Ticket(boolean requestSteam2Ticket) {
        this.requestSteam2Ticket = requestSteam2Ticket;
    }

    public EOSType getClientOSType() {
        return clientOSType;
    }

    public void setClientOSType(EOSType clientOSType) {
        this.clientOSType = clientOSType;
    }

    public String getClientLanguage() {
        return clientLanguage;
    }

    public void setClientLanguage(String clientLanguage) {
        this.clientLanguage = clientLanguage;
    }
}
