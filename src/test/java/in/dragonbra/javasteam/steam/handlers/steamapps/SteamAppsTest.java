package in.dragonbra.javasteam.steam.handlers.steamapps;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientGetAppOwnershipTicket;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSAccessTokenRequest;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSChangesSinceRequest;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSProductInfoRequest;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientCheckAppBetaPassword;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientGetCDNAuthToken;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientGetDepotDecryptionKey;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRequestFreeLicense;
import in.dragonbra.javasteam.steam.handlers.HandlerTestBase;
import in.dragonbra.javasteam.steam.handlers.steamapps.callback.*;
import in.dragonbra.javasteam.types.JobID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.LinkedList;

import static org.junit.Assert.*;

/**
 * @author lngtr
 * @since 2018-04-09
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SteamAppsTest extends HandlerTestBase<SteamApps> {

    @Override
    protected SteamApps createHandler() {
        return new SteamApps();
    }

    @Test
    public void getAppOwnershipTicket() {
        JobID jobID = handler.getAppOwnershipTicket(440);

        ClientMsgProtobuf<CMsgClientGetAppOwnershipTicket.Builder> msg = verifySend(EMsg.ClientGetAppOwnershipTicket);

        assertEquals(SOURCE_JOB_ID, msg.getSourceJobID());
        assertEquals(SOURCE_JOB_ID, jobID);
        assertEquals(440, msg.getBody().getAppId());
    }

    @Test
    public void getDepotDecryptionKey() {
        JobID jobID = handler.getDepotDecryptionKey(731, 730);

        ClientMsgProtobuf<CMsgClientGetDepotDecryptionKey.Builder> msg = verifySend(EMsg.ClientGetDepotDecryptionKey);

        assertEquals(SOURCE_JOB_ID, msg.getSourceJobID());
        assertEquals(SOURCE_JOB_ID, jobID);
        assertEquals(731, msg.getBody().getDepotId());
        assertEquals(730, msg.getBody().getAppId());
    }

    @Test
    public void picsGetAccessTokens() {
        JobID jobID = handler.picsGetAccessTokens(440, 420);

        ClientMsgProtobuf<CMsgClientPICSAccessTokenRequest.Builder> msg = verifySend(EMsg.ClientPICSAccessTokenRequest);

        assertEquals(SOURCE_JOB_ID, msg.getSourceJobID());
        assertEquals(SOURCE_JOB_ID, jobID);
        assertEquals(1, msg.getBody().getAppidsCount());
        assertEquals(1, msg.getBody().getPackageidsCount());
        assertEquals(440, msg.getBody().getAppids(0));
        assertEquals(420, msg.getBody().getPackageids(0));
    }

    @Test
    public void picsGetChangesSince() {
        JobID jobID = handler.picsGetChangesSince();

        ClientMsgProtobuf<CMsgClientPICSChangesSinceRequest.Builder> msg = verifySend(EMsg.ClientPICSChangesSinceRequest);

        assertEquals(SOURCE_JOB_ID, msg.getSourceJobID());
        assertEquals(SOURCE_JOB_ID, jobID);
        assertTrue(msg.getBody().getSendAppInfoChanges());
        assertFalse(msg.getBody().getSendPackageInfoChanges());
        assertEquals(0, msg.getBody().getSinceChangeNumber());
    }

    @Test
    public void picsGetProductInfo() {
        PICSRequest app = new PICSRequest(440);
        PICSRequest _package = new PICSRequest(420);
        JobID jobID = handler.picsGetProductInfo(app, _package);

        ClientMsgProtobuf<CMsgClientPICSProductInfoRequest.Builder> msg = verifySend(EMsg.ClientPICSProductInfoRequest);

        assertEquals(SOURCE_JOB_ID, msg.getSourceJobID());
        assertEquals(SOURCE_JOB_ID, jobID);
        assertEquals(1, msg.getBody().getAppsCount());
        assertEquals(1, msg.getBody().getPackagesCount());
        assertEquals(440, msg.getBody().getApps(0).getAppid());
        assertEquals(420, msg.getBody().getPackages(0).getPackageid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void picsGetProductInfoNull() {
        handler.picsGetProductInfo(null, new LinkedList<PICSRequest>(), false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void picsGetProductInfoNull2() {
        handler.picsGetProductInfo(new LinkedList<PICSRequest>(), null, false);
    }

    @Test
    public void getCDNAuthToken() {
        JobID jobID = handler.getCDNAuthToken(1, 2, "testhostname");

        ClientMsgProtobuf<CMsgClientGetCDNAuthToken.Builder> msg = verifySend(EMsg.ClientGetCDNAuthToken);

        assertEquals(SOURCE_JOB_ID, msg.getSourceJobID());
        assertEquals(SOURCE_JOB_ID, jobID);
        assertEquals(1, msg.getBody().getAppId());
        assertEquals(2, msg.getBody().getDepotId());
        assertEquals("testhostname", msg.getBody().getHostName());
    }

    @Test
    public void requestFreeLicense() {
        JobID jobID = handler.requestFreeLicense(440);

        ClientMsgProtobuf<CMsgClientRequestFreeLicense.Builder> msg = verifySend(EMsg.ClientRequestFreeLicense);

        assertEquals(SOURCE_JOB_ID, msg.getSourceJobID());
        assertEquals(SOURCE_JOB_ID, jobID);
        assertEquals(1, msg.getBody().getAppidsCount());
        assertEquals(440, msg.getBody().getAppids(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void requestFreeLicenseNull() {
        handler.requestFreeLicense(null);
    }

    @Test
    public void checkAppBetaPassword() {
        JobID jobID = handler.checkAppBetaPassword(440, "testpassword");

        ClientMsgProtobuf<CMsgClientCheckAppBetaPassword.Builder> msg = verifySend(EMsg.ClientCheckAppBetaPassword);

        assertEquals(SOURCE_JOB_ID, msg.getSourceJobID());
        assertEquals(SOURCE_JOB_ID, jobID);
        assertEquals(440, msg.getBody().getAppId());
        assertEquals("testpassword", msg.getBody().getBetapassword());
    }

    @Test
    public void handleAppOwnershipTicketResponse() {
        IPacketMsg msg = getPacket(EMsg.ClientGetAppOwnershipTicketResponse, true);

        handler.handleMsg(msg);

        AppOwnershipTicketCallback callback = verifyCallback();

        assertEquals(EResult.OK, callback.getResult());
        assertEquals(440, callback.getAppID());
        assertArrayEquals(new byte[]{}, callback.getTicket());
    }

    @Test
    public void handleDepotKeyResponse() {
        IPacketMsg msg = getPacket(EMsg.ClientGetDepotDecryptionKeyResponse, true);

        handler.handleMsg(msg);

        DepotKeyCallback callback = verifyCallback();

        assertEquals(EResult.OK, callback.getResult());
        assertEquals(1, callback.getDepotID());
        assertArrayEquals(new byte[]{}, callback.getDepotKey());
    }

    @Test
    public void handleGameConnectTokens() {
        IPacketMsg msg = getPacket(EMsg.ClientGameConnectTokens, true);

        handler.handleMsg(msg);

        GameConnectTokensCallback callback = verifyCallback();

        assertEquals(10, callback.getTokensToKeep());
        assertEquals(10, callback.getTokens().size());
    }

    @Test
    public void handleLicenseList() {
        IPacketMsg msg = getPacket(EMsg.ClientLicenseList, true);

        handler.handleMsg(msg);

        LicenseListCallback callback = verifyCallback();

        assertEquals(EResult.OK, callback.getResult());
        assertEquals(136, callback.getLicenseList().size());
    }

    @Test
    public void handleFreeLicense() {
        IPacketMsg msg = getPacket(EMsg.ClientRequestFreeLicenseResponse, true);

        handler.handleMsg(msg);

        FreeLicenseCallback callback = verifyCallback();

        assertEquals(EResult.OK, callback.getResult());
        assertEquals(1, callback.getGrantedApps().size());
        assertEquals(440, callback.getGrantedApps().get(0).intValue());
    }

    @Test
    public void handleVACBanStatus() {
        IPacketMsg msg = getPacket(EMsg.ClientVACBanStatus, true);

        handler.handleMsg(msg);

        VACStatusCallback callback = verifyCallback();

        assertEquals(0, callback.getBannedApps().size());
    }

    @Test
    public void handlePICSAccessTokenResponse() {
        IPacketMsg msg = getPacket(EMsg.ClientPICSAccessTokenResponse, true);

        handler.handleMsg(msg);

        PICSTokensCallback callback = verifyCallback();

        assertEquals(30, callback.getAppTokensDenied().size());
        assertEquals(0, callback.getPackageTokensDenied().size());
        assertEquals(0, callback.getPackageTokens().size());
        assertEquals(0, callback.getAppTokens().size());
    }

    @Test
    public void handlePICSChangesSinceResponse() {
        IPacketMsg msg = getPacket(EMsg.ClientPICSChangesSinceResponse, true);

        handler.handleMsg(msg);

        PICSChangesCallback callback = verifyCallback();

        assertEquals(4216262, callback.getCurrentChangeNumber());
        assertEquals(4216227, callback.getLastChangeNumber());
        assertEquals(1, callback.getPackageChanges().size());
        assertEquals(31, callback.getAppChanges().size());
    }

    @Test
    public void handlePICSProductInfoResponse() {
        IPacketMsg msg = getPacket(EMsg.ClientPICSProductInfoResponse, true);

        handler.handleMsg(msg);

        PICSProductInfoCallback callback = verifyCallback();

        assertEquals(1, callback.getApps().size());
        assertEquals(1, callback.getPackages().size());
        assertNotNull(callback.getApps().get(440));
        assertNotNull(callback.getApps().get(440).getKeyValues());
        assertNotNull(callback.getPackages().get(0));
        assertNotNull(callback.getPackages().get(0).getKeyValues());
    }

    @Test
    public void handleGuestPassList() {
        IPacketMsg msg = getPacket(EMsg.ClientUpdateGuestPassesList, true);

        handler.handleMsg(msg);

        GuestPassListCallback callback = verifyCallback();

        assertEquals(EResult.OK, callback.getResult());
        assertEquals(0, callback.getCountGuestPassesToGive());
        assertEquals(0, callback.getCountGuestPassesToRedeem());
        assertEquals(0, callback.getGuestPasses().size());
    }

    @Test
    public void handleCDNAuthTokenResponse() {
        IPacketMsg msg = getPacket(EMsg.ClientGetCDNAuthTokenResponse, true);

        handler.handleMsg(msg);

        CDNAuthTokenCallback callback = verifyCallback();

        assertEquals(EResult.OK, callback.getResult());
        assertEquals("testtoken", callback.getToken());
        assertEquals(new Date(946684800000L), callback.getExpiration());
    }

    @Test
    public void handleCheckAppBetaPasswordResponse() {
        IPacketMsg msg = getPacket(EMsg.ClientCheckAppBetaPasswordResponse, true);

        handler.handleMsg(msg);

        CheckAppBetaPasswordCallback callback = verifyCallback();

        assertEquals(EResult.OK, callback.getResult());
        assertEquals(1, callback.getBetaPasswords().size());
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xAA}, callback.getBetaPasswords().get("testname"));
    }
}