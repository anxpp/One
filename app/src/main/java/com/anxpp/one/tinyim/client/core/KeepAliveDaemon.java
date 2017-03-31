package com.anxpp.one.tinyim.client.core;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.anxpp.one.tinyim.client.ClientCoreSDK;

import java.util.Observer;

/**
 * 心跳线程
 */
public class KeepAliveDaemon {
    private static final String TAG = KeepAliveDaemon.class.getSimpleName();

    public static int NETWORK_CONNECTION_TIME_OUT = 10000;

    public static int KEEP_ALIVE_INTERVAL = 3000;
    private static KeepAliveDaemon instance = null;
    private Handler handler = null;
    private Runnable runnable = null;
    private boolean keepAliveRunning = false;
    private long lastGetKeepAliveResponseFromServerTimeStrap = 0L;
    private Observer networkConnectionLostObserver = null;
    private boolean isRunning = false;
    private Context context = null;

    private KeepAliveDaemon(Context context) {
        this.context = context;
        init();
    }

    public static KeepAliveDaemon getInstance(Context context) {
        if (instance == null)
            instance = new KeepAliveDaemon(context);
        return instance;
    }

    private void init() {
        this.handler = new Handler();
        this.runnable = new Runnable() {
            public void run() {
                // 极端情况下本次循环内可能执行时间超过了时间间隔，此处是防止在前一
                // 次还没有运行完的情况下又重复过劲行，从而出现无法预知的错误
                if (!KeepAliveDaemon.this.isRunning) {
                    new AsyncTask<Object, Integer, Integer>() {
                        private boolean willStop = false;

                        protected Integer doInBackground(Object[] params) {
                            KeepAliveDaemon.this.isRunning = true;
                            if (ClientCoreSDK.DEBUG)
                                Log.d(KeepAliveDaemon.TAG, "【IMCORE】心跳线程执行中...");
                            return MessageSender.getInstance(KeepAliveDaemon.this.context).sendKeepAlive();
                        }

                        protected void onPostExecute(Integer code) {
                            boolean isInitialedForKeepAlive = KeepAliveDaemon.this.lastGetKeepAliveResponseFromServerTimeStrap == 0L;
                            if ((code == 0)
                                    && (KeepAliveDaemon.this.lastGetKeepAliveResponseFromServerTimeStrap == 0L)) {
                                KeepAliveDaemon.this.lastGetKeepAliveResponseFromServerTimeStrap = System.currentTimeMillis();
                            }

                            if (!isInitialedForKeepAlive) {
                                long now = System.currentTimeMillis();

                                // 当当前时间与最近一次服务端的心跳响应包时间间隔>= 10秒就判定当前与服务端的网络连接已断开
                                if (now - KeepAliveDaemon.this.lastGetKeepAliveResponseFromServerTimeStrap
                                        >= KeepAliveDaemon.NETWORK_CONNECTION_TIME_OUT) {
                                    KeepAliveDaemon.this.stop();

                                    if (KeepAliveDaemon.this.networkConnectionLostObserver != null) {
                                        KeepAliveDaemon.this.networkConnectionLostObserver.update(null, null);
                                    }
                                    this.willStop = true;
                                }
                            }

                            KeepAliveDaemon.this.isRunning = false;
                            if (!this.willStop) {
                                // 开始下一个心跳循环
                                KeepAliveDaemon.this.handler.postDelayed(
                                        KeepAliveDaemon.this.runnable
                                        , KeepAliveDaemon.KEEP_ALIVE_INTERVAL);
                            }
                        }
                    }.execute();
                }
            }
        };
    }

    public void stop() {
        this.handler.removeCallbacks(this.runnable);
        this.keepAliveRunning = false;
        this.lastGetKeepAliveResponseFromServerTimeStrap = 0L;
    }

    public void start(boolean immediately) {
        stop();
        this.handler.postDelayed(this.runnable, immediately ? 0 : KEEP_ALIVE_INTERVAL);
        this.keepAliveRunning = true;
    }

    public boolean isKeepAliveRunning() {
        return this.keepAliveRunning;
    }

    public void updateGetKeepAliveResponseFromServerTimstamp() {
        this.lastGetKeepAliveResponseFromServerTimeStrap = System.currentTimeMillis();
    }

    public void setNetworkConnectionLostObserver(Observer networkConnectionLostObserver) {
        this.networkConnectionLostObserver = networkConnectionLostObserver;
    }
}