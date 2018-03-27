package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.base.ClientMsg;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.*;
import in.dragonbra.javasteam.generated.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientAMGetPersonaNameHistory;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientChatInvite;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientChatGetFriendMessageHistory;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientChatGetFriendMessageHistoryForOfflineMessages;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.*;
import in.dragonbra.javasteam.steam.handlers.HandlerTestBase;
import in.dragonbra.javasteam.steam.handlers.steamfriends.callback.*;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.SeekOrigin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 * @author lngtr
 * @since 2018-03-27
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SteamFriendsTest extends HandlerTestBase<SteamFriends> {

    @Override
    protected SteamFriends createHandler() {
        return new SteamFriends();
    }

    @Test
    public void sendChatMessage() throws UnsupportedEncodingException {
        SteamID testId = new SteamID(123456789L);
        handler.sendChatMessage(testId, EChatEntryType.ChatMsg, "testmessage");

        ClientMsgProtobuf<CMsgClientFriendMsg.Builder> msg = verifySend(EMsg.ClientFriendMsg);

        assertEquals(123456789L, msg.getBody().getSteamid());
        assertEquals("testmessage", msg.getBody().getMessage().toString("UTF-8"));
        assertEquals(EChatEntryType.ChatMsg.code(), msg.getBody().getChatEntryType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendChatMessageNullId() {
        handler.sendChatMessage(null, EChatEntryType.ChatMsg, "testmessage");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendChatMessageNullType() {
        SteamID testId = new SteamID(123456789L);
        handler.sendChatMessage(testId, null, "testmessage");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendChatMessageNullMessage() {
        SteamID testId = new SteamID(123456789L);
        handler.sendChatMessage(testId, EChatEntryType.ChatMsg, null);
    }

    @Test
    public void addFriend() {
        SteamID testId = new SteamID(123456789L);
        handler.addFriend(testId);

        ClientMsgProtobuf<CMsgClientAddFriend.Builder> msg = verifySend(EMsg.ClientAddFriend);

        assertEquals(123456789L, msg.getBody().getSteamidToAdd());

        handler.addFriend("testaccount");

        msg = verifySend(EMsg.ClientAddFriend);

        assertEquals("testaccount", msg.getBody().getAccountnameOrEmailToAdd());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFriendNull() {
        handler.addFriend((SteamID) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFriendNull2() {
        handler.addFriend((String) null);
    }

    @Test
    public void removeFriend() {
        SteamID testId = new SteamID(123456789L);
        handler.removeFriend(testId);

        ClientMsgProtobuf<CMsgClientRemoveFriend.Builder> msg = verifySend(EMsg.ClientRemoveFriend);

        assertEquals(123456789L, msg.getBody().getFriendid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeFriendNull() {
        handler.removeFriend(null);
    }

    @Test
    public void joinChat() {
        SteamID testId = new SteamID(123456789L);
        testId.setAccountType(EAccountType.Chat);
        testId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.joinChat(testId);

        ClientMsg<MsgClientJoinChat> msg = verifySend(EMsg.ClientJoinChat);

        assertEquals(testId, msg.getBody().getSteamIdChat());
    }

    @Test(expected = IllegalArgumentException.class)
    public void joinChatNull() {
        handler.joinChat(null);
    }

    @Test
    public void leaveChat() {
        SteamID testId = new SteamID(123456789L);
        testId.setAccountType(EAccountType.Chat);
        testId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.leaveChat(testId);

        ClientMsg<MsgClientChatMemberInfo> msg = verifySend(EMsg.ClientChatMemberInfo);

        assertEquals(testId, msg.getBody().getSteamIdChat());
        assertEquals(EChatInfoType.StateChange, msg.getBody().getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void leaveChatNull() {
        handler.leaveChat(null);
    }

    @Test
    public void sendChatRoomMessage() throws IOException {
        SteamID testId = new SteamID(123456789L);
        testId.setAccountType(EAccountType.Chat);
        testId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.sendChatRoomMessage(testId, EChatEntryType.ChatMsg, "testmessage");

        ClientMsg<MsgClientChatMsg> msg = verifySend(EMsg.ClientChatMsg);

        assertEquals(testId, msg.getBody().getSteamIdChatRoom());
        assertEquals(steamClient.getSteamID(), msg.getBody().getSteamIdChatter());
        assertEquals(EChatEntryType.ChatMsg, msg.getBody().getChatMsgType());

        msg.getPayload().seek(0, SeekOrigin.BEGIN);
        assertEquals("testmessage", msg.readNullTermString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendChatRoomMessageNullId() {
        handler.sendChatRoomMessage(null, EChatEntryType.ChatMsg, "testmessage");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendChatRoomMessageNullType() {
        SteamID testId = new SteamID(123456789L);
        testId.setAccountType(EAccountType.Chat);
        testId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.sendChatRoomMessage(testId, null, "testmessage");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendChatRoomMessageNullMessage() {
        SteamID testId = new SteamID(123456789L);
        testId.setAccountType(EAccountType.Chat);
        testId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.sendChatRoomMessage(testId, EChatEntryType.ChatMsg, null);
    }

    @Test
    public void inviteUserToChat() {
        SteamID userId = new SteamID(987654321L);
        SteamID chatId = new SteamID(123456789L);
        chatId.setAccountType(EAccountType.Chat);
        chatId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.inviteUserToChat(userId, chatId);

        ClientMsgProtobuf<CMsgClientChatInvite.Builder> msg = verifySend(EMsg.ClientChatInvite);

        assertEquals(chatId.convertToUInt64(), msg.getBody().getSteamIdChat());
        assertEquals(userId.convertToUInt64(), msg.getBody().getSteamIdInvited());
    }

    @Test(expected = IllegalArgumentException.class)
    public void inviteUserToChatNullUser() {
        SteamID chatId = new SteamID(123456789L);
        chatId.setAccountType(EAccountType.Chat);
        chatId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.inviteUserToChat(null, chatId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void inviteUserToChatNullChat() {
        SteamID userId = new SteamID(987654321L);
        handler.inviteUserToChat(userId, null);
    }

    @Test
    public void kickChatMember() {
        SteamID userId = new SteamID(987654321L);
        SteamID chatId = new SteamID(123456789L);
        chatId.setAccountType(EAccountType.Chat);
        chatId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.kickChatMember(chatId, userId);

        ClientMsg<MsgClientChatAction> msg = verifySend(EMsg.ClientChatAction);

        assertEquals(chatId, msg.getBody().getSteamIdChat());
        assertEquals(userId, msg.getBody().getSteamIdUserToActOn());
        assertEquals(EChatAction.Kick, msg.getBody().getChatAction());
    }

    @Test(expected = IllegalArgumentException.class)
    public void kickChatMemberNullUser() {
        SteamID chatId = new SteamID(123456789L);
        chatId.setAccountType(EAccountType.Chat);
        chatId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.kickChatMember(chatId, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void kickChatMemberNullChat() {
        SteamID userId = new SteamID(987654321L);
        handler.kickChatMember(null, userId);
    }

    @Test
    public void banChatMember() {
        SteamID userId = new SteamID(987654321L);
        SteamID chatId = new SteamID(123456789L);
        chatId.setAccountType(EAccountType.Chat);
        chatId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.banChatMember(chatId, userId);

        ClientMsg<MsgClientChatAction> msg = verifySend(EMsg.ClientChatAction);

        assertEquals(chatId, msg.getBody().getSteamIdChat());
        assertEquals(userId, msg.getBody().getSteamIdUserToActOn());
        assertEquals(EChatAction.Ban, msg.getBody().getChatAction());
    }

    @Test(expected = IllegalArgumentException.class)
    public void banChatMemberNullUser() {
        SteamID chatId = new SteamID(123456789L);
        chatId.setAccountType(EAccountType.Chat);
        chatId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.banChatMember(chatId, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void banChatMemberNullChat() {
        SteamID userId = new SteamID(987654321L);
        handler.banChatMember(null, userId);
    }

    @Test
    public void unbanChatMember() {
        SteamID userId = new SteamID(987654321L);
        SteamID chatId = new SteamID(123456789L);
        chatId.setAccountType(EAccountType.Chat);
        chatId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.unbanChatMember(chatId, userId);

        ClientMsg<MsgClientChatAction> msg = verifySend(EMsg.ClientChatAction);

        assertEquals(chatId, msg.getBody().getSteamIdChat());
        assertEquals(userId, msg.getBody().getSteamIdUserToActOn());
        assertEquals(EChatAction.UnBan, msg.getBody().getChatAction());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unbanChatMemberNullUser() {
        SteamID chatId = new SteamID(123456789L);
        chatId.setAccountType(EAccountType.Chat);
        chatId.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
        handler.unbanChatMember(chatId, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unbanChatMemberNullChat() {
        SteamID userId = new SteamID(987654321L);
        handler.unbanChatMember(null, userId);
    }

    @Test
    public void requestFriendInfo() {
        SteamID userId = new SteamID(987654321L);
        handler.requestFriendInfo(userId,
                EClientPersonaStateFlag.code(EnumSet.of(EClientPersonaStateFlag.Status, EClientPersonaStateFlag.PlayerName))
        );

        ClientMsgProtobuf<CMsgClientRequestFriendData.Builder> msg = verifySend(EMsg.ClientRequestFriendData);

        assertEquals(EClientPersonaStateFlag.code(EnumSet.of(EClientPersonaStateFlag.Status, EClientPersonaStateFlag.PlayerName)), msg.getBody().getPersonaStateRequested());
        assertEquals(987654321L, msg.getBody().getFriends(0));
        assertEquals(1, msg.getBody().getFriendsCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void requestFriendInfoNull() {
        handler.requestFriendInfo((SteamID) null, 1);
    }

    @Test
    public void ignoreFriend() {
        SteamID userId = new SteamID(987654321L);
        handler.ignoreFriend(userId);

        ClientMsg<MsgClientSetIgnoreFriend> msg = verifySend(EMsg.ClientSetIgnoreFriend);

        assertEquals(0x01, msg.getBody().getIgnore());
        assertEquals(userId, msg.getBody().getSteamIdFriend());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ignoreFriendNull() {
        handler.ignoreFriend(null);
    }

    @Test
    public void requestProfileInfo() {
        SteamID userId = new SteamID(987654321L);
        handler.requestProfileInfo(userId);

        ClientMsgProtobuf<CMsgClientFriendProfileInfo.Builder> msg = verifySend(EMsg.ClientFriendProfileInfo);

        assertEquals(987654321L, msg.getBody().getSteamidFriend());
    }

    @Test(expected = IllegalArgumentException.class)
    public void requestProfileInfoNull() {
        handler.requestProfileInfo(null);
    }

    @Test
    public void requestMessageHistory() {
        SteamID userId = new SteamID(987654321L);
        handler.requestMessageHistory(userId);

        ClientMsgProtobuf<CMsgClientChatGetFriendMessageHistory.Builder> msg = verifySend(EMsg.ClientFSGetFriendMessageHistory);

        assertEquals(987654321L, msg.getBody().getSteamid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void requestMessageHistoryNull() {
        handler.requestMessageHistory(null);
    }

    @Test
    public void requestOfflineMessages() {
        handler.requestOfflineMessages();

        ClientMsgProtobuf<CMsgClientChatGetFriendMessageHistoryForOfflineMessages.Builder> msg = verifySend(EMsg.ClientFSGetFriendMessageHistoryForOfflineMessages);

        assertNotNull(msg);
    }

    @Test
    public void setFriendNickname() {
        SteamID userId = new SteamID(987654321L);
        handler.setFriendNickname(userId, "testnickname");

        ClientMsgProtobuf<CMsgClientSetPlayerNickname.Builder> msg = verifySend(EMsg.AMClientSetPlayerNickname);

        assertEquals(987654321L, msg.getBody().getSteamid());
        assertEquals("testnickname", msg.getBody().getNickname());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setFriendNicknameNullId() {
        SteamID userId = new SteamID(987654321L);
        handler.setFriendNickname(userId, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setFriendNicknameNullNickname() {
        handler.setFriendNickname(null, "testnickname");
    }

    @Test
    public void requestAliasHistory() {
        SteamID userId = new SteamID(987654321L);
        handler.requestAliasHistory(userId);

        ClientMsgProtobuf<CMsgClientAMGetPersonaNameHistory.Builder> msg = verifySend(EMsg.ClientAMGetPersonaNameHistory);

        assertEquals(1, msg.getBody().getIdCount());
        assertEquals(1, msg.getBody().getIdsCount());
        assertEquals(987654321L, msg.getBody().getIds(0).getSteamid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void requestAliasHistoryNull() {
        handler.requestAliasHistory((SteamID) null);
    }

    @Test
    public void handleFriendMsg() {
        IPacketMsg msg = getPacket(EMsg.ClientFriendMsgIncoming, true);

        handler.handleMsg(msg);

        FriendMsgCallback callback = verifyCallback();

        assertEquals("testmessage", callback.getMessage());
        assertEquals(new SteamID(123), callback.getSender());
    }

    @Test
    public void handleFriendEchoMsg() {
        IPacketMsg msg = getPacket(EMsg.ClientFriendMsgEchoToSender, true);

        handler.handleMsg(msg);

        FriendMsgEchoCallback callback = verifyCallback();

        assertEquals("testmessage", callback.getMessage());
        assertEquals(new SteamID(123), callback.getSender());
    }

    @Test
    public void handleFriendMessageHistoryResponse() {
        IPacketMsg msg = getPacket(EMsg.ClientFSGetFriendMessageHistoryResponse, true);

        handler.handleMsg(msg);

        FriendMsgHistoryCallback callback = verifyCallback();

        assertEquals(3, callback.getMessages().size());
        assertEquals(new SteamID(76561198817909313L), callback.getSteamID());
    }

    @Test
    public void handleFriendsList() {
        IPacketMsg msg = getPacket(EMsg.ClientFriendsList, true);

        handler.handleMsg(msg);

        FriendsListCallback callback = verifyCallback();

        assertEquals(2, callback.getFriendList().size());
        assertFalse(callback.isIncremental());
    }

    @Test
    public void handlePersonaState() {
        IPacketMsg msg = getPacket(EMsg.ClientPersonaState, true);

        handler.handleMsg(msg);

        PersonaStatesCallback callback = verifyCallback();

        assertEquals(1, callback.getPersonaStates().size());
        assertEquals(EPersonaState.Offline, callback.getPersonaStates().get(0).getState());
        assertEquals("klay", callback.getPersonaStates().get(0).getName());
    }

    @Test
    public void handleClanState() {
        IPacketMsg msg = getPacket(EMsg.ClientClanState, true);

        handler.handleMsg(msg);

        ClanStateCallback callback = verifyCallback();

        assertEquals(1, callback.getMemberTotalCount());
    }

    @Test
    public void handleChatEnter() {
        IPacketMsg msg = getPacket(EMsg.ClientChatEnter, false);

        handler.handleMsg(msg);

        ChatEnterCallback callback = verifyCallback();

        assertEquals(new SteamID(123), callback.getChatID());
    }

    @Test
    public void handleChatMsg() {
        IPacketMsg msg = getPacket(EMsg.ClientChatMsg, false);

        handler.handleMsg(msg);

        ChatMsgCallback callback = verifyCallback();

        assertEquals(new SteamID(123), callback.getChatterID());
    }

    @Test
    public void handleChatMemberInfo() {
        IPacketMsg msg = getPacket(EMsg.ClientChatMemberInfo, false);

        handler.handleMsg(msg);

        ChatMemberInfoCallback callback = verifyCallback();

        assertEquals(new SteamID(123), callback.getChatRoomID());
    }

    @Test
    public void handleChatRoomInfo() {
        IPacketMsg msg = getPacket(EMsg.ClientChatRoomInfo, false);

        handler.handleMsg(msg);

        ChatRoomInfoCallback callback = verifyCallback();

        assertEquals(new SteamID(123), callback.getChatRoomID());
    }

    @Test
    public void handleChatActionResult() {
        IPacketMsg msg = getPacket(EMsg.ClientChatActionResult, false);

        handler.handleMsg(msg);

        ChatActionResultCallback callback = verifyCallback();

        assertEquals(new SteamID(123), callback.getChatRoomID());
    }

    @Test
    public void handleChatInvite() {
        IPacketMsg msg = getPacket(EMsg.ClientChatInvite, true);

        handler.handleMsg(msg);

        ChatInviteCallback callback = verifyCallback();

        assertEquals(new SteamID(123), callback.getChatRoomID());
    }

    @Test
    public void handleIgnoreFriendResponse() {
        IPacketMsg msg = getPacket(EMsg.ClientSetIgnoreFriendResponse, false);

        handler.handleMsg(msg);

        IgnoreFriendCallback callback = verifyCallback();

        assertEquals(EResult.OK, callback.getResult());
    }

    @Test
    public void handleProfileInfoResponse() {
        IPacketMsg msg = getPacket(EMsg.ClientFriendProfileInfoResponse, true);

        handler.handleMsg(msg);

        ProfileInfoCallback callback = verifyCallback();

        assertEquals(EResult.OK, callback.getResult());
    }

    @Test
    public void handlePersonaChangeResponse() {
        IPacketMsg msg = getPacket(EMsg.ClientPersonaChangeResponse, true);

        handler.handleMsg(msg);

        PersonaChangeCallback callback = verifyCallback();

        assertEquals(EResult.OK, callback.getResult());
    }

    @Test
    public void handleNicknameList() {
        IPacketMsg msg = getPacket(EMsg.ClientPlayerNicknameList, true);

        handler.handleMsg(msg);

        NicknameListCallback callback = verifyCallback();

        assertEquals(1, callback.getNicknames().size());
        assertEquals("testnickname", callback.getNicknames().get(0).getNickname());
    }

    @Test
    public void handlePlayerNicknameResponse() {
        IPacketMsg msg = getPacket(EMsg.AMClientSetPlayerNicknameResponse, true);

        handler.handleMsg(msg);

        NicknameCallback callback = verifyCallback();

        assertEquals(EResult.OK, callback.getResult());
    }

    @Test
    public void handleAliasHistoryResponse() {
        IPacketMsg msg = getPacket(EMsg.ClientAMGetPersonaNameHistoryResponse, true);

        handler.handleMsg(msg);

        AliasHistoryCallback callback = verifyCallback();

        assertEquals(1, callback.getResponses().size());
        assertEquals(EResult.OK, callback.getResponses().get(0).getResult());
        assertEquals(10, callback.getResponses().get(0).getNames().size());
    }
}