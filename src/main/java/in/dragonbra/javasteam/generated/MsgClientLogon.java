package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;

import java.io.*;

public class MsgClientLogon implements ISteamSerializableMessage {

    public static final long ObfuscationMask = 0xBAADF00D;

    public static final long CurrentProtocol = 65579;

    public static final long ProtocolVerMajorMask = 0xFFFF0000;

    public static final long ProtocolVerMinorMask = 0xFFFF;

    public static final int ProtocolVerMinorMinGameServers = 4;

    public static final int ProtocolVerMinorMinForSupportingEMsgMulti = 12;

    public static final int ProtocolVerMinorMinForSupportingEMsgClientEncryptPct = 14;

    public static final int ProtocolVerMinorMinForExtendedMsgHdr = 17;

    public static final int ProtocolVerMinorMinForCellId = 18;

    public static final int ProtocolVerMinorMinForSessionIDLast = 19;

    public static final int ProtocolVerMinorMinForServerAvailablityMsgs = 24;

    public static final int ProtocolVerMinorMinClients = 25;

    public static final int ProtocolVerMinorMinForOSType = 26;

    public static final int ProtocolVerMinorMinForCegApplyPESig = 27;

    public static final int ProtocolVerMinorMinForMarketingMessages2 = 27;

    public static final int ProtocolVerMinorMinForAnyProtoBufMessages = 28;

    public static final int ProtocolVerMinorMinForProtoBufLoggedOffMessage = 28;

    public static final int ProtocolVerMinorMinForProtoBufMultiMessages = 28;

    public static final int ProtocolVerMinorMinForSendingProtocolToUFS = 30;

    public static final int ProtocolVerMinorMinForMachineAuth = 33;

    public static final int ProtocolVerMinorMinForSessionIDLastAnon = 36;

    public static final int ProtocolVerMinorMinForEnhancedAppList = 40;

    public static final int ProtocolVerMinorMinForSteamGuardNotificationUI = 41;

    public static final int ProtocolVerMinorMinForProtoBufServiceModuleCalls = 42;

    public static final int ProtocolVerMinorMinForGzipMultiMessages = 43;

    public static final int ProtocolVerMinorMinForNewVoiceCallAuthorize = 44;

    public static final int ProtocolVerMinorMinForClientInstanceIDs = 44;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientLogon;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

    }
}
