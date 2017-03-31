package com.anxpp.one.tinyim.client.message;

import com.anxpp.one.tinyim.client.message.client.LoginInfo;
import com.anxpp.one.tinyim.client.message.client.PKeepAlive;
import com.anxpp.one.tinyim.client.message.server.ErrorResponse;
import com.anxpp.one.tinyim.client.message.server.KeepAliveResponse;
import com.anxpp.one.tinyim.client.message.server.LoginInfoResponse;
import com.google.gson.Gson;

public class MessageFactory {

    private static String create(Object data) {
        return new Gson().toJson(data);
    }

    public static <T> T parse(byte[] fullProtocalJASOnBytes, int len, Class<T> clazz) {
        return parse(CharsetHelper.getString(fullProtocalJASOnBytes, len), clazz);
    }

    public static <T> T parse(String dataContentOfProtocal, Class<T> clazz) {
        return new Gson().fromJson(dataContentOfProtocal, clazz);
    }

    public static Message parse(byte[] fullProtocalJASOnBytes, int len) {
        return parse(fullProtocalJASOnBytes, len, Message.class);
    }

    public static Message createPKeepAliveResponse(int to_user_id) {
        return new Message(MessageType.Server.FROM_SERVER_TYPE_OF_RESPONSE$KEEP$ALIVE, create(new KeepAliveResponse()), 0, to_user_id);
    }

    public static KeepAliveResponse parsePKeepAliveResponse(String dataContentOfProtocal) {
        return parse(dataContentOfProtocal, KeepAliveResponse.class);
    }

    public static Message createPKeepAlive(int fromUserId) {
        return new Message(MessageType.Client.FROM_CLIENT_TYPE_OF_KEEP$ALIVE, create(new PKeepAlive()), fromUserId, 0);
    }

    public static PKeepAlive parsePKeepAlive(String dataContentOfProtocal) {
        return parse(dataContentOfProtocal, PKeepAlive.class);
    }

    public static Message createPErrorResponse(int errorCode, String errorMsg, int userId) {
        return new Message(MessageType.Server.FROM_SERVER_TYPE_OF_RESPONSE$FOR$ERROR, create(new ErrorResponse(errorCode, errorMsg)), 0, userId);
    }

    public static ErrorResponse parsePErrorResponse(String dataContentOfProtocal) {
        return parse(dataContentOfProtocal, ErrorResponse.class);
    }

    public static Message createPLoginoutInfo(int userId, String loginName) {
        return new Message(MessageType.Client.FROM_CLIENT_TYPE_OF_LOGOUT
//				, create(new PLogoutInfo(userId, loginName))
                , null
                , userId, 0);
    }

    public static Message createPLoginInfo(String username, String password, String extra) {
        return new Message(MessageType.Client.FROM_CLIENT_TYPE_OF_LOGIN, create(new LoginInfo(username, password, extra)), -1, 0);
    }

    public static LoginInfo parsePLoginInfo(String dataContentOfProtocal) {
        return parse(dataContentOfProtocal, LoginInfo.class);
    }

    public static Message createPLoginInfoResponse(int code, int userId) {
        return new Message(MessageType.Server.FROM_SERVER_TYPE_OF_RESPONSE$LOGIN, create(new LoginInfoResponse(code, userId)), 0, userId, true, Message.genFingerPrint());
    }

    public static LoginInfoResponse parsePLoginInfoResponse(String dataContentOfProtocal) {
        return (LoginInfoResponse) parse(dataContentOfProtocal, LoginInfoResponse.class);
    }

    public static Message createCommonData(String dataContent, int fromUserId, int toUserId, boolean QoS, String fingerPrint) {
        return new Message(MessageType.Client.FROM_CLIENT_TYPE_OF_COMMON$DATA, dataContent, fromUserId, toUserId, QoS, fingerPrint);
    }

    public static Message createCommonData(String dataContent, int fromUserId, int toUserId) {
        return new Message(MessageType.Client.FROM_CLIENT_TYPE_OF_COMMON$DATA, dataContent, fromUserId, toUserId);
    }

    public static Message createRecivedBack(int fromUserId, int toUserId, String recievedMessageFingerPrint) {
        return new Message(MessageType.Client.FROM_CLIENT_TYPE_OF_RECEIVED, recievedMessageFingerPrint, fromUserId, toUserId);// 该包当然不需要QoS支持！
    }
}