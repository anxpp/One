package com.anxpp.one.tinyim.client.core;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.anxpp.one.tinyim.client.ClientCoreSDK;
import com.anxpp.one.tinyim.client.conf.ConfigEntity;
import com.anxpp.one.tinyim.client.message.MessageFactory;
import com.anxpp.one.tinyim.client.message.MessageType;
import com.anxpp.one.tinyim.client.message.server.ErrorResponse;
import com.anxpp.one.tinyim.client.message.server.LoginInfoResponse;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Observable;
import java.util.Observer;

/**
 * 消息接收管理
 */
public class MessageReceiver {
    private static final String TAG = MessageReceiver.class.getSimpleName();
    private static MessageReceiver instance = null;
    private static MessageHandler messageHandler = null;
    private Thread thread = null;
    private Context context = null;

    private MessageReceiver(Context context) {
        this.context = context;
    }

    public static MessageReceiver getInstance(Context context) {
        if (instance == null) {
            instance = new MessageReceiver(context);
            messageHandler = new MessageHandler(context);
        }
        return instance;
    }

    public void stop() {
        if (this.thread != null) {
            this.thread.interrupt();
            this.thread = null;
        }
    }

    void startup() {
        stop();
        try {
            this.thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ClientCoreSDK.DEBUG) {
                            Log.d(MessageReceiver.TAG, "【IMCORE】本地UDP端口侦听中，端口=" + ConfigEntity.localUDPPort + "...");
                        }
                        //开始侦听
                        MessageReceiver.this.p2pListeningImpl();
                    } catch (Exception eee) {
                        Log.w(MessageReceiver.TAG, "【IMCORE】本地UDP监听停止了(socket被关闭了?)," + eee.getMessage(), eee);
                    }
                }
            });
            this.thread.start();
        } catch (Exception e) {
            Log.w(TAG, "【IMCORE】本地UDPSocket监听开启时发生异常," + e.getMessage(), e);
        }
    }

    private void p2pListeningImpl() throws Exception {
        while (true) {
            // 缓冲区
            byte[] data = new byte[1024];
            // 接收数据报的包
            DatagramPacket packet = new DatagramPacket(data, data.length);
            DatagramSocket localUDPSocket = SocketProvider.getInstance().getLocalUDPSocket();
            if ((localUDPSocket == null) || (localUDPSocket.isClosed())) {
                continue;
            }
            localUDPSocket.receive(packet);

            Message m = Message.obtain();
            m.obj = packet;
            messageHandler.sendMessage(m);
        }
    }

    private static class MessageHandler extends Handler {
        private Context context = null;

        MessageHandler(Context context) {
            this.context = context;
        }

        public void handleMessage(Message msg) {
            DatagramPacket packet = (DatagramPacket) msg.obj;
            if (packet == null) {
                return;
            }

            try {
                com.anxpp.one.tinyim.client.message.Message pFromServer =
                        MessageFactory.parse(packet.getData(), packet.getLength());

                if (pFromServer.isQoS()) {
                    if (QoS4ReceiveDaemon.getInstance(this.context).hasReceived(pFromServer.getFp())) {
                        if (ClientCoreSDK.DEBUG) {
                            Log.d(MessageReceiver.TAG, "【IMCORE】【QoS机制】" + pFromServer.getFp() + "已经存在于发送列表中，这是重复包，通知应用层收到该包罗！");
                        }
                        QoS4ReceiveDaemon.getInstance(this.context).addReceived(pFromServer);
                        sendReceivedBack(pFromServer);

                        return;
                    }

                    QoS4ReceiveDaemon.getInstance(this.context).addReceived(pFromServer);

                    sendReceivedBack(pFromServer);
                }

                switch (pFromServer.getType()) {
                    case MessageType.Client.FROM_CLIENT_TYPE_OF_COMMON$DATA: {
                        if (ClientCoreSDK.getInstance().getChatTransDataEvent() == null)
                            break;
                        ClientCoreSDK.getInstance().getChatTransDataEvent().onTransBuffer(
                                pFromServer.getFp(), pFromServer.getFrom(), pFromServer.getDataContent());

                        break;
                    }
                    case MessageType.Server.FROM_SERVER_TYPE_OF_RESPONSE$KEEP$ALIVE: {
                        if (ClientCoreSDK.DEBUG) {
                            Log.d(MessageReceiver.TAG, "【IMCORE】收到服务端回过来的Keep Alive心跳响应包.");
                        }
                        KeepAliveDaemon.getInstance(this.context).updateGetKeepAliveResponseFromServerTimstamp();
                        break;
                    }
                    case MessageType.Client.FROM_CLIENT_TYPE_OF_RECEIVED: {
                        String theFingerPrint = pFromServer.getDataContent();
                        if (ClientCoreSDK.DEBUG) {
                            Log.d(MessageReceiver.TAG, "【IMCORE】【QoS】收到" + pFromServer.getFrom() + "发过来的指纹为" + theFingerPrint + "的应答包.");
                        }

                        if (ClientCoreSDK.getInstance().getMessageQoSEvent() != null) {
                            ClientCoreSDK.getInstance().getMessageQoSEvent().messagesBeReceived(theFingerPrint);
                        }

                        QoS4SendDaemon.getInstance(this.context).remove(theFingerPrint);
                        break;
                    }
                    case MessageType.Server.FROM_SERVER_TYPE_OF_RESPONSE$LOGIN: {
                        LoginInfoResponse loginInfoRes = MessageFactory.parsePLoginInfoResponse(pFromServer.getDataContent());

                        if (loginInfoRes.getCode() == 0) {
                            ClientCoreSDK.getInstance()
                                    .setLoginHasInit(true)
                                    .setCurrentUserId(loginInfoRes.getUserId());
                            AutoReLoginDaemon.getInstance(this.context).stop();
                            KeepAliveDaemon.getInstance(this.context).setNetworkConnectionLostObserver(new Observer() {
                                @Override
                                public void update(Observable o, Object arg) {
                                    QoS4SendDaemon.getInstance(MessageHandler.this.context).stop();
                                    QoS4ReceiveDaemon.getInstance(MessageHandler.this.context).stop();
                                    ClientCoreSDK.getInstance().setConnectedToServer(false);
                                    ClientCoreSDK.getInstance().setCurrentUserId(-1);
                                    ClientCoreSDK.getInstance().getChatBaseEvent().onLinkCloseMessage(-1);
                                    AutoReLoginDaemon.getInstance(MessageHandler.this.context).start(true);
                                }
                            });
                            KeepAliveDaemon.getInstance(this.context).start(false);
                            QoS4SendDaemon.getInstance(this.context).startup(true);
                            QoS4ReceiveDaemon.getInstance(this.context).startup(true);
                            ClientCoreSDK.getInstance().setConnectedToServer(true);
                        } else {
                            ClientCoreSDK.getInstance().setConnectedToServer(false);
                            ClientCoreSDK.getInstance().setCurrentUserId(-1);
                        }

                        if (ClientCoreSDK.getInstance().getChatBaseEvent() == null)
                            break;
                        ClientCoreSDK.getInstance().getChatBaseEvent().onLoginMessage(
                                loginInfoRes.getUserId(), loginInfoRes.getCode());
                        break;
                    }
                    case MessageType.Server.FROM_SERVER_TYPE_OF_RESPONSE$FOR$ERROR: {
                        ErrorResponse errorRes = MessageFactory.parsePErrorResponse(pFromServer.getDataContent());

                        if (errorRes.getErrorCode() == 301) {
                            ClientCoreSDK.getInstance().setLoginHasInit(false);
                            Log.e(MessageReceiver.TAG, "【IMCORE】收到服务端的“尚未登陆”的错误消息，心跳线程将停止，请应用层重新登陆.");
                            KeepAliveDaemon.getInstance(this.context).stop();
                            AutoReLoginDaemon.getInstance(this.context).start(false);
                        }

                        if (ClientCoreSDK.getInstance().getChatTransDataEvent() == null)
                            break;
                        ClientCoreSDK.getInstance().getChatTransDataEvent().onErrorResponse(
                                errorRes.getErrorCode(), errorRes.getErrorMsg());

                        break;
                    }
                    default:
                        Log.w(MessageReceiver.TAG, "【IMCORE】收到的服务端消息类型：" + pFromServer.getType() + "，但目前该类型客户端不支持解析和处理！");
                }
            } catch (Exception e) {
                Log.w(MessageReceiver.TAG, "【IMCORE】处理消息的过程中发生了错误.", e);
            }
        }

        private void sendReceivedBack(final com.anxpp.one.tinyim.client.message.Message pFromServer) {
            if (pFromServer.getFp() != null) {
                new MessageSender.SendCommonDataAsync(
                        context
                        , MessageFactory.createRecivedBack(
                        pFromServer.getTo()
                        , pFromServer.getFrom()
                        , pFromServer.getFp())) {
                    @Override
                    protected void onPostExecute(Integer code) {
                        if (ClientCoreSDK.DEBUG)
                            Log.d(TAG, "【IMCORE】【QoS】向" + pFromServer.getFrom() + "发送" + pFromServer.getFp() + "包的应答包成功,from=" + pFromServer.getTo() + "！");
                    }
                }.execute();
            } else {
                Log.w(TAG, "【IMCORE】【QoS】收到" + pFromServer.getFrom() + "发过来需要QoS的包，但它的指纹码却为null！无法发应答包！");
            }
        }
    }
}