package com.anxpp.one.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.anxpp.one.R;
import com.anxpp.one.utils.Global;
import com.dd.CircularProgressButton;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 首次启动界面
 */
public class StartActivity extends AppCompatActivity {

    private String TAG = this.getClass().getSimpleName();

    //    private TagGroup tagGroup;
    private Call call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //提示标题
        new Shimmer().setStartDelay(100).start((ShimmerTextView) findViewById(R.id.shimmer_tv1));
        new Shimmer().setStartDelay(500).start((ShimmerTextView) findViewById(R.id.shimmer_tv2));
        new Shimmer().setStartDelay(1100).start((ShimmerTextView) findViewById(R.id.shimmer_tv3));
        new Shimmer().setStartDelay(1300).start((ShimmerTextView) findViewById(R.id.shimmer_tv4));

        //输入框
        final MaterialEditText materialEditText = (MaterialEditText) findViewById(R.id.editUser);

        //进度按钮
        final CircularProgressButton circularProgressButton = (CircularProgressButton) findViewById(R.id.btnWithText);
        circularProgressButton.setIndeterminateProgressMode(true); // turn on indeterminate progress
        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = materialEditText.getText().toString().trim();
                Log.i("username", username);
                Toast.makeText(StartActivity.this, username, Toast.LENGTH_LONG).show();
                if(username.isEmpty()){
                    Toast.makeText(StartActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }else if(username.length()<6){
                    Toast.makeText(StartActivity.this, "昵称不能少于6个字符！", Toast.LENGTH_SHORT).show();
                    return;
                }else if(username.length()>32){
                    Toast.makeText(StartActivity.this, "昵称不能多于32个字符！", Toast.LENGTH_SHORT).show();
                    return;
                }
                circularProgressButton.setProgress(50);
                OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(2, TimeUnit.SECONDS).readTimeout(2, TimeUnit.SECONDS).writeTimeout(2, TimeUnit.SECONDS).build();
                Request request = new Request.Builder().url(Global.URL_USER_REGISTER).post(new FormBody.Builder().add("username",username).build()).build();
                call = okHttpClient.newCall(request);               // step 3：创建 Call 对象
                call.enqueue(new Callback() {                       // step 4: 开始异步请求
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "onClick::onFailure:" + e.getMessage());
                        StartActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                circularProgressButton.setProgress(-1);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.e(TAG, "onClick::onResponse:" + response.body().string());
                        StartActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                circularProgressButton.setProgress(100);
                            }
                        });
                    }
                });
                //请求服务器登陆
//                new Handler().postDelayed(new Runnable() {
//                    public void run() {
//                        //模拟数据加载完成
////                        circularProgressButton.setProgress(0);
//                        String username = materialEditText.getText().toString().trim();
//                        Toast.makeText(StartActivity.this,username,Toast.LENGTH_LONG).show();
//                        Log.i("username", username);
//                        circularProgressButton.setProgress(100);
//                    }
//                }, 1800);
            }
        });

//        tagGroup = (TagGroup) findViewById(R.id.tag_group);
//        tagGroup.setTags("Tag1", "Tag2", "Tag3");
    }

    @Override
    protected void onPause() {
        if(call!=null){
            if(call.isExecuted())
                call.cancel();
        }
        super.onPause();
    }
}
