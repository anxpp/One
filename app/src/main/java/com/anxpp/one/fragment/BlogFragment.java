package com.anxpp.one.fragment;

import android.content.Context;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.anxpp.one.R;
import com.anxpp.one.activity.MainActivity;
import com.anxpp.one.plus.HProgressBarLoading;
import com.anxpp.one.utils.TinyUtil;

public class BlogFragment extends Fragment {

    private WebView mWebView;
    private HProgressBarLoading mTopProgress;
    private TextView mTvCenterBadnet;
    private boolean isContinue = false;
    private MainActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;//保存Context引用
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mTopProgress = (HProgressBarLoading) activity.findViewById(R.id.top_progress);
        mTvCenterBadnet = (TextView) activity.findViewById(R.id.tv_center_badnet);

        //初始化webView
        initWebView();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initWebView() {
        mWebView = (WebView) activity.findViewById(R.id.mWebView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        //有限使用缓存
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.loadUrl("http://blog.anxpp.com");
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {  //表示按返回键 时的操作
                        mWebView.goBack();   //后退
                        return true;        //已处理
                    }
                }
                return false;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                //如果没有网络直接跳出方法
                if (!TinyUtil.isNetworkAvailable(activity)) {
                    return;
                }
                //如果进度条隐藏则让它显示
                if (View.INVISIBLE == mTopProgress.getVisibility()) {
                    mTopProgress.setVisibility(View.VISIBLE);
                }
                //大于80的进度的时候,放慢速度加载,否则交给自己加载
                if (newProgress >= 80) {
                    //拦截webView自己的处理方式
                    if (isContinue) {
                        return;
                    }
                    mTopProgress.setCurProgress(100, 3000, new HProgressBarLoading.OnEndListener() {
                        @Override
                        public void onEnd() {
                            finishOperation(true);
                            isContinue = false;
                        }
                    });

                    isContinue = true;
                } else {
                    mTopProgress.setNormalProgress(newProgress);
                }
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            //连接调用自己
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            //https的处理方式
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            //错误页面的逻辑处理
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                errorOperation();
            }
        });
    }

    /**
     * 错误的时候进行的操作
     */
    private void errorOperation() {
        //隐藏webview
        mWebView.setVisibility(View.INVISIBLE);

        if (View.INVISIBLE == mTopProgress.getVisibility()) {
            mTopProgress.setVisibility(View.VISIBLE);
        }
        //3.5s 加载 0->80 进度的加载 为了实现,特意调节长了事件
        mTopProgress.setCurProgress(80, 3500, new HProgressBarLoading.OnEndListener() {
            @Override
            public void onEnd() {
                //3.5s 加载 80->100 进度的加载
                mTopProgress.setCurProgress(100, 3500, new HProgressBarLoading.OnEndListener() {
                    @Override
                    public void onEnd() {
                        finishOperation(false);
                    }
                });
            }
        });
    }

    /**
     * 结束进行的操作
     */
    private void finishOperation(boolean flag) {
        //最后加载设置100进度
        mTopProgress.setNormalProgress(100);
        //显示网络异常布局
        mTvCenterBadnet.setVisibility(flag ? View.INVISIBLE : View.VISIBLE);
        //点击重新连接网络
        mTvCenterBadnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvCenterBadnet.setVisibility(View.INVISIBLE);
                //重新加载网页
                mWebView.reload();
            }
        });
        hideProgressWithAnim();
    }

    /**
     * 隐藏加载对话框
     */
    private void hideProgressWithAnim() {
        AnimationSet animation = getDismissAnim(activity);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTopProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mTopProgress.startAnimation(animation);
    }

    /**
     * 获取消失的动画
     *
     * @param context
     * @return
     */
    private AnimationSet getDismissAnim(Context context) {
        AnimationSet dismiss = new AnimationSet(context, null);
        AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
        alpha.setDuration(1000);
        dismiss.addAnimation(alpha);
        return dismiss;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return activity.onKeyDown(keyCode, event);
    }
}
