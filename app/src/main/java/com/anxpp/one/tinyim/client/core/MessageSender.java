package com.anxpp.one.tinyim.client.core;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.anxpp.one.tinyim.client.ClientCoreSDK;
import com.anxpp.one.tinyim.client.conf.ConfigEntity;
import com.anxpp.one.tinyim.client.message.CharsetHelper;
import com.anxpp.one.tinyim.client.message.Message;
import com.anxpp.one.tinyim.client.message.MessageFactory;
import com.anxpp.one.tinyim.client.message.StatusCode;
import com.anxpp.one.tinyim.client.utils.UDPUtils;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class MessageSender {
    private static final String TAG = MessageSender.class.getSimpleName();
    private static MessageSender instance = null;

    private Context context = null;

    private MessageSender(Context context) {
        this.context = context;
    }

    public static MessageSender getInstance(Context context) {
        if (instance == null)
            instance = new MessageSender(context);
        return instance;
    }

    int sendLogin(String loginName, String loginPsw, String extra) {
        byte[] b = MessageFactory.createPLoginInfo(loginName, loginPsw, extra).toBytes();
        int code = send(b, b.length);
        // 登陆信息成功发出时就把登陆名存下来
        if (code == 0) {
            ClientCoreSDK.getInstance().setCurrentLoginName(loginName);
            ClientCoreSDK.getInstance().setCurrentLoginPsw(loginPsw);
            ClientCoreSDK.getInstance().setCurrentLoginExtra(extra);
        }

        return code;
    }

    public int sendLogout() {
        int code = StatusCode.COMMON_CODE_OK;
        if (ClientCoreSDK.getInstance().isLoginHasInit()) {
            byte[] b = MessageFactory.createPLoginoutInfo(ClientCoreSDK.getInstance().getCurrentUserId(), ClientCoreSDK.getInstance().getCurrentLoginName()).toBytes();
            code = send(b, b.length);
            // 登出信息成功发出时
            if (code == 0) {
//				// 发出退出登陆的消息同时也关闭心跳线程
//				KeepAliveDaemon.getInstance(context).stop();
//				// 重置登陆标识
//				ClientCoreSDK.getInstance().setLoginHasInit(false);
            }
        }
        // 释放SDK资源
        ClientCoreSDK.getInstance().release();
        return code;
    }

    int sendKeepAlive() {
        byte[] b = MessageFactory.createPKeepAlive(ClientCoreSDK.getInstance().getCurrentUserId()).toBytes();
        return send(b, b.length);
    }

    public int sendCommonData(byte[] dataContent, int dataLen, int toUserId) {
        return sendCommonData(
                CharsetHelper.getString(dataContent, dataLen), toUserId, false, null);
    }

    public int sendCommonData(byte[] dataContent, int dataLen, int toUserId, boolean QoS, String fingerPrint) {
        return sendCommonData(CharsetHelper.getString(dataContent, dataLen), toUserId, QoS, fingerPrint);
    }

    public int sendCommonData(String dataContentWidthStr, int toUserId) {
        return sendCommonData(MessageFactory.createCommonData(dataContentWidthStr, ClientCoreSDK.getInstance().getCurrentUserId(), toUserId));
    }

    private int sendCommonData(String dataContentWidthStr, int to_user_id, boolean QoS, String fingerPrint) {
        return sendCommonData(MessageFactory.createCommonData(dataContentWidthStr, ClientCoreSDK.getInstance().getCurrentUserId(), to_user_id, QoS, fingerPrint));
    }

    int sendCommonData(Message p) {
        if (p != null) {
            byte[] b = p.toBytes();
            int code = send(b, b.length);
            if (code == 0) {
                // 【【C2C或C2S模式下的QoS机制1/4步：将包加入到发送QoS队列中】】
                // 如果需要进行QoS质量保证，则把它放入质量保证队列中供处理(已在存在于列
                // 表中就不用再加了，已经存在则意味当前发送的这个是重传包哦)
                if (p.isQoS() && !QoS4SendDaemon.getInstance(context).exist(p.getFp()))
                    QoS4SendDaemon.getInstance(context).put(p);
            }
            return code;
        } else
            return StatusCode.COMMON_INVALID_PROTOCAL;
    }

    private int send(byte[] fullProtocalBytes, int dataLen) {
        if (!ClientCoreSDK.getInstance().isInitialed())
            return StatusCode.CLIENT_SDK_NO_INITIALED;

        if (!ClientCoreSDK.getInstance().isLocalDeviceNetworkOk()) {
            Log.e(TAG, "【IMCORE】本地网络不能工作，send数据没有继续!");
            return StatusCode.LOCAL_NETWORK_NOT_WORKING;
        }

        DatagramSocket ds = SocketProvider.getInstance().getLocalUDPSocket();
        // 如果Socket没有连接上服务端
        if (ds != null && !ds.isConnected()) {
            try {
                if (ConfigEntity.serverIP == null) {
                    Log.w(TAG, "【IMCORE】send数据没有继续，原因是ConfigEntity.server_ip==null!");
                    return StatusCode.TO_SERVER_NET_INFO_NOT_SETUP;
                }
                // 即刻连接上服务端（如果不connect，即使在DataProgram中设置了远程id和地址则服务端MINA也收不到，跟普通的服
                // 务端UDP貌似不太一样，普通UDP时客户端无需先connect可以直接send设置好远程ip和端口的DataPragramPackage）
                ds.connect(InetAddress.getByName(ConfigEntity.serverIP), ConfigEntity.serverUDPPort);
            } catch (Exception e) {
                Log.w(TAG, "【IMCORE】send时出错，原因是：" + e.getMessage(), e);
                return StatusCode.BAD_CONNECT_TO_SERVER;
            }
        }
        return UDPUtils.send(ds, fullProtocalBytes, dataLen) ? StatusCode.COMMON_CODE_OK : StatusCode.COMMON_DATA_SEND_FAILD;
    }

    /**
     * 常规消息异步发送
     */
    public static abstract class SendCommonDataAsync extends AsyncTask<Object, Integer, Integer> {
        protected Context context = null;
        Message message = null;

        public SendCommonDataAsync(Context context, byte[] dataContent, int dataLen, int toUserId) {
            this(context, CharsetHelper.getString(dataContent, dataLen), toUserId);
        }

        protected SendCommonDataAsync(Context context, String dataContentWidthStr, int toUserId, boolean QoS) {
            this(context, dataContentWidthStr, toUserId, QoS, null);
        }

        SendCommonDataAsync(Context context, String dataContentWidthStr, int toUserId, boolean QoS, String fingerPrint) {
            this(context, MessageFactory.createCommonData(dataContentWidthStr, ClientCoreSDK.getInstance().getCurrentUserId(), toUserId, QoS, fingerPrint));
        }

        SendCommonDataAsync(Context context, String dataContentWidthStr, int toUserId) {
            this(context, MessageFactory.createCommonData(dataContentWidthStr, ClientCoreSDK.getInstance().getCurrentUserId(), toUserId));
        }

        SendCommonDataAsync(Context context, Message message) {
            if (message == null) {
                Log.w(MessageSender.TAG, "【IMCORE】无效的参数p==null!");
                return;
            }
            this.context = context;
            this.message = message;
        }

        protected Integer doInBackground(Object[] params) {
            if (this.message != null)
                return MessageSender.getInstance(this.context).sendCommonData(this.message);
            return -1;
        }

        protected abstract void onPostExecute(Integer paramInteger);
    }

    /**
     * 异步发送登录消息
     */
    public static abstract class SendLoginDataAsync extends AsyncTask<Object, Integer, Integer> {
        protected Context context = null;
        String username = null;
        String password = null;
        String extra = null;

        protected SendLoginDataAsync(Context context, String username, String password) {
            this(context, username, password, null);
        }

        SendLoginDataAsync(Context context, String username, String password, String extra) {
            this.context = context;
            this.username = username;
            this.password = password;
            this.extra = extra;
        }

        protected Integer doInBackground(Object[] params) {
            return MessageSender.getInstance(this.context).sendLogin(this.username, this.password, this.extra);
        }

        protected void onPostExecute(Integer code) {
            if (code == 0) {
                MessageReceiver.getInstance(this.context).startup();
            } else {
                Log.d(MessageSender.TAG, "【IMCORE】数据发送失败, 错误码是：" + code + "！");
            }

            fireAfterSendLogin(code);
        }

        protected void fireAfterSendLogin(int code) {
            // default do nothing
        }
    }
}