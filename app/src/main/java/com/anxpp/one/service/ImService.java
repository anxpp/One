package com.anxpp.one.service;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.anxpp.one.activity.MainActivity;
import com.anxpp.one.tinyim.IMClientManager;
import com.anxpp.one.tinyim.client.conf.ConfigEntity;
import com.anxpp.one.tinyim.client.core.MessageSender;
import com.anxpp.one.tinyim.client.core.SocketProvider;

import java.util.Observable;
import java.util.Observer;

/**
 * IM Service
 * Created by anxpp.com on 2017/4/1.
 */
public class ImService extends Service {

    private final static String TAG = ImService.class.getSimpleName();

    /**
     * 当另一个组件（如 Activity）通过调用 startService() 请求启动服务时
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG,"onStartCommand");
        Toast.makeText(this,TAG+":onStartCommand",Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 首次创建服务
     */
    @Override
    public void onCreate() {
        Log.w(TAG,"onCreate");
        Toast.makeText(this,TAG+":onCreate",Toast.LENGTH_LONG).show();
        super.onCreate();
        initIMClient();
    }

    /**
     * 服务不再使用且将被销毁时
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG,"onDestroy");
    }

    private void initIMClient() {
        //初始化
        IMClientManager.getInstance(this).initMobileIMSDK();
        doLogin();
    }

    /**
     * 登陆处理。
     */
    private void doLogin() {
        if (!CheckNetworkState())
            return;

        // 设置服务器地址和端口号
        String serverIP = "10.0.2.2";
        String serverPort = "1114";
        if (!(serverIP.trim().length() <= 0)
                && !(serverPort.trim().length() <= 0)) {
            // 无条件重置socket，防止首次登陆时用了错误的ip或域名，下次登陆时sendData中仍然使用老的ip
            // 说明：本行代码建议仅用于Demo时，生产环境下是没有意义的，因为你的APP里不可能连IP都搞错了
            SocketProvider.getInstance().closeLocalUDPSocket();

            ConfigEntity.serverIP = serverIP.trim();
            try {
                ConfigEntity.serverUDPPort = Integer.parseInt(serverPort.trim());
            } catch (Exception e2) {
                Toast.makeText(getApplicationContext(), "请输入合法的端口号！", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(getApplicationContext(), "请确保服务端地址和端口号都不为空！", Toast.LENGTH_SHORT).show();
            return;
        }

        // 发送登陆数据包
//        doLoginImpl();
    }

    /**
     * 真正的登陆信息发送实现方法。
     */
    private void doLoginImpl() {
        // * 立即显示登陆处理进度提示（并将同时启动超时检查线程）
        // * 设置好服务端反馈的登陆结果观察者（当客户端收到服务端反馈过来的登陆消息时将被通知）
        IMClientManager.getInstance(this).getBaseEventListener().setLoginOkForLaunchObserver(new Observer() {
            @Override
            public void update(Observable o, Object data) {
                // 服务端返回的登陆结果值
                int code = (Integer) data;
                // 登陆成功
                if (code == 0) {
                    //** 提示：登陆MobileIMSDK服务器成功后的事情在此实现即可

                    // 进入主界面
                    startActivity(new Intent(ImService.this, MainActivity.class));
                }
                // 登陆失败
                else {
                    new AlertDialog.Builder(ImService.this).setTitle("友情提示").setMessage("Sorry，登陆失败，错误码=" + code).setPositiveButton("知道了", null).show();
                }
            }
        });

        // 异步提交登陆名和密码
        new MessageSender.SendLoginDataAsync(this, "username", "password") {
            /**
             * 登陆信息发送完成后将调用本方法（注意：此处仅是登陆信息发送完成
             * ，真正的登陆结果要在异步回调中处理哦）。
             *
             * @param code 数据发送返回码，0 表示数据成功发出，否则是错误码
             */
            @Override
            protected void fireAfterSendLogin(int code) {
                if (code == 0) {
                    //
                    Toast.makeText(getApplicationContext(), "数据发送成功！", Toast.LENGTH_SHORT).show();
                    Log.d(MainActivity.class.getSimpleName(), "登陆信息已成功发出！");
                } else {
                    Toast.makeText(getApplicationContext(), "数据发送失败。错误码是：" + code + "！", Toast.LENGTH_SHORT).show();

                }
            }
        }.execute();
    }

    private boolean CheckNetworkState() {
        boolean flag = false;
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager.getActiveNetworkInfo() != null) {
            flag = manager.getActiveNetworkInfo().isAvailable();
        }
        if (!flag) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("Network not avaliable");//
            builder.setMessage("Current network is not avaliable, set it?");//
            builder.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); //直接进入手机中的wifi网络设置界面
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create();
            builder.show();
        }

        return flag;
    }

}
