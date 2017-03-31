package com.anxpp.one.tinyim.client.core;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.anxpp.one.tinyim.client.ClientCoreSDK;
import com.anxpp.one.tinyim.client.message.Message;

import java.util.concurrent.ConcurrentHashMap;

public class QoS4ReceiveDaemon {
    public static final int CHECH_INTERVAL = 300000;
    public static final int MESSAGES_VALID_TIME = 600000;
    private static final String TAG = QoS4ReceiveDaemon.class.getSimpleName();
    private static QoS4ReceiveDaemon instance = null;
    private ConcurrentHashMap<String, Long> recievedMessages = new ConcurrentHashMap();
    private Handler handler = null;
    private Runnable runnable = null;
    private boolean running = false;
    private boolean isRunning = false;
    private Context context = null;

    public QoS4ReceiveDaemon(Context context) {
        this.context = context;

        init();
    }

    public static QoS4ReceiveDaemon getInstance(Context context) {
        if (instance == null) {
            instance = new QoS4ReceiveDaemon(context);
        }

        return instance;
    }

    private void init() {
        this.handler = new Handler();
        this.runnable = new Runnable() {
            @Override
            public void run() {
                // 极端情况下本次循环内可能执行时间超过了时间间隔，此处是防止在前一
                // 次还没有运行完的情况下又重复过劲行，从而出现无法预知的错误
                if (!QoS4ReceiveDaemon.this.isRunning) {
                    QoS4ReceiveDaemon.this.isRunning = true;

                    if (ClientCoreSDK.DEBUG) {
                        Log.d(QoS4ReceiveDaemon.TAG, "【IMCORE】【QoS接收方】++++++++++ START 暂存处理线程正在运行中，当前长度" + QoS4ReceiveDaemon.this.recievedMessages.size() + ".");
                    }

                    for (String key : QoS4ReceiveDaemon.this.recievedMessages.keySet()) {
                        long delta = System.currentTimeMillis() - ((Long) QoS4ReceiveDaemon.this.recievedMessages.get(key)).longValue();

                        if (delta < MESSAGES_VALID_TIME)
                            continue;
                        if (ClientCoreSDK.DEBUG)
                            Log.d(QoS4ReceiveDaemon.TAG, "【IMCORE】【QoS接收方】指纹为" + key + "的包已生存" + delta +
                                    "ms(最大允许" + MESSAGES_VALID_TIME + "ms), 马上将删除之.");
                        QoS4ReceiveDaemon.this.recievedMessages.remove(key);
                    }

                }

                if (ClientCoreSDK.DEBUG) {
                    Log.d(QoS4ReceiveDaemon.TAG, "【IMCORE】【QoS接收方】++++++++++ END 暂存处理线程正在运行中，当前长度" + QoS4ReceiveDaemon.this.recievedMessages.size() + ".");
                }

                QoS4ReceiveDaemon.this.isRunning = false;

                QoS4ReceiveDaemon.this.handler.postDelayed(QoS4ReceiveDaemon.this.runnable, CHECH_INTERVAL);
            }
        };
    }

    public void startup(boolean immediately) {
        stop();

        if ((this.recievedMessages != null) && (this.recievedMessages.size() > 0)) {
            for (String key : this.recievedMessages.keySet()) {
                putImpl(key);
            }

        }

        this.handler.postDelayed(this.runnable, immediately ? 0 : CHECH_INTERVAL);

        this.running = true;
    }

    public void stop() {
        this.handler.removeCallbacks(this.runnable);

        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void addReceived(Message message) {
        if ((message != null) && (message.isQoS()))
            addReceived(message.getFp());
    }

    public void addReceived(String fingerPrintOfProtocal) {
        if (fingerPrintOfProtocal == null) {
            Log.w(TAG, "【IMCORE】无效的 fingerPrintOfProtocal==null!");
            return;
        }

        if (this.recievedMessages.containsKey(fingerPrintOfProtocal)) {
            Log.w(TAG, "【IMCORE】【QoS接收方】指纹为" + fingerPrintOfProtocal +
                    "的消息已经存在于接收列表中，该消息重复了（原理可能是对方因未收到应答包而错误重传导致），更新收到时间戳哦.");
        }

        putImpl(fingerPrintOfProtocal);
    }

    private void putImpl(String fingerPrintOfProtocal) {
        if (fingerPrintOfProtocal != null)
            this.recievedMessages.put(fingerPrintOfProtocal, Long.valueOf(System.currentTimeMillis()));
    }

    public boolean hasReceived(String fingerPrintOfProtocal) {
        return this.recievedMessages.containsKey(fingerPrintOfProtocal);
    }

    public int size() {
        return this.recievedMessages.size();
    }
}