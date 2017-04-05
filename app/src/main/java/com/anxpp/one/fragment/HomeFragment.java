package com.anxpp.one.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anxpp.one.R;
import com.anxpp.one.activity.MainActivity;
import com.anxpp.one.activity.ArticleDetailsActivity;
import com.anxpp.one.activity.ToDoListActivity;
import com.anxpp.one.plus.Article;
import com.anxpp.one.plus.ArticleAdapter;
import com.anxpp.one.plus.Tag;
import com.anxpp.one.utils.Global;
import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.wang.avi.AVLoadingIndicatorView;
import com.yalantis.filter.adapter.FilterAdapter;
import com.yalantis.filter.animator.FiltersListItemAnimator;
import com.yalantis.filter.listener.FilterListener;
import com.yalantis.filter.widget.Filter;
import com.yalantis.filter.widget.FilterItem;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    //下拉刷新、上拉加载
    private SwipeToLoadLayout swipeToLoadLayout;

    //activity引用
    private MainActivity activity;

    private RecyclerView mRecyclerView;

    private final String TAG = HomeFragment.class.getSimpleName();

    private List<Article> mAllArticles = new ArrayList<>();

    private Filter<Tag> mFilter;

    private ArticleAdapter articleAdapter;

    private final List<Tag> tagsForFilter = new ArrayList<>();

    private AVLoadingIndicatorView avLoadingIndicatorView;

    private int page = 55;

    public HomeFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;//保存Context引用
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //文章筛选器
        FilterListener<Tag> filterListener = new FilterListener<Tag>() {
            @Override
            public void onFiltersSelected(@NotNull ArrayList<Tag> filters) {
                Log.i(TAG, "onFiltersSelected");
                List<Article> newArticles = findByTags(filters);
                List<Article> oldArticles = articleAdapter.getArticles();
                articleAdapter.setArticles(newArticles);
                calculateDiff(oldArticles, newArticles);
            }

            @Override
            public void onNothingSelected() {
                if (mRecyclerView != null) {
                    articleAdapter.setArticles(mAllArticles);
                    articleAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFilterSelected(Tag tag) {
                Log.i(TAG, "onFilterSelected");
                if (tag.getText().equals(tagsForFilter.get(0).getText())) {
                    mFilter.deselectAll();
                    mFilter.collapse();
                }
            }

            @Override
            public void onFilterDeselected(Tag tag) {
                Log.i(TAG, "onFilterDeselected");
            }
        };
        mFilter = (Filter<Tag>) activity.findViewById(R.id.filter);
        tagsForFilter.add(new Tag("全部", -456356));
        tagsForFilter.add(new Tag("csdn干货", 456412));
        tagsForFilter.add(new Tag("第54期", 456412));
        mFilter.setAdapter(new SimpleFilterAdapter(tagsForFilter));
        mFilter.setListener(filterListener);
        mFilter.setNoSelectedItemText(getString(R.string.str_all_selected));
        mFilter.build();

        //TO DO LIST
        activity.findViewById(R.id.btn_alarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity, ToDoListActivity.class));
            }
        });

        //刷新与加载
        swipeToLoadLayout = (SwipeToLoadLayout) activity.findViewById(R.id.swipeToLoadLayout);
        //下拉刷新
        swipeToLoadLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i("setOnRefreshListener", "onRefresh");
                mAllArticles.clear();
                page = 55;
                getArticles();
            }
        });
        //上拉加载
        swipeToLoadLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Log.i("setOnLoadMoreListener", "onLoadMore");
                getArticles();
            }
        });

        //Loading
        avLoadingIndicatorView = (AVLoadingIndicatorView) activity.findViewById(R.id.avi);

        ImagePipelineConfig config = ImagePipelineConfig
                .newBuilder(activity)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(activity, config);

        mRecyclerView = (RecyclerView) activity.findViewById(R.id.swipe_target);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(articleAdapter = new ArticleAdapter(activity, mAllArticles));
        mRecyclerView.setItemAnimator(new FiltersListItemAnimator());
        articleAdapter.setOnItemClickListener(new ArticleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.i(TAG, "articleAdapter.setOnItemClickListener:" + position);
                Intent intent = new Intent(activity, ArticleDetailsActivity.class);
                Article article = mAllArticles.get(position);
                intent.putExtra("text", article.getText());
                intent.putExtra("img", article.getImg());
                intent.putExtra("title", article.getTitle());
                intent.putExtra("url", article.getUrl());
                startActivity(intent);
            }
        });
        getArticles();
    }

    @Override
    public void onPause() {
        if (call != null)
            call.cancel();
        if (swipeToLoadLayout != null) {
            if (swipeToLoadLayout.isRefreshing()) {
                swipeToLoadLayout.setRefreshing(false);
            }
            if (swipeToLoadLayout.isLoadingMore()) {
                swipeToLoadLayout.setLoadingMore(false);
            }
        }
        super.onPause();
    }

    private void calculateDiff(final List<Article> oldList, final List<Article> newList) {
        DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }
        }).dispatchUpdatesTo(articleAdapter);
    }

    /**
     * 获取文章列表
     */
    private Call call;

    private void getArticles() {
        avLoadingIndicatorView.show();
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Global.URL_HOME_ARTICLE_BASE + (--page)).method("GET", null).build();
        call = okHttpClient.newCall(request);               // step 3：创建 Call 对象
        call.enqueue(new Callback() {                       // step 4: 开始异步请求
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "getArticles::onFailure:" + e.getMessage());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeToLoadLayout.isLoadingMore())
                            swipeToLoadLayout.setLoadingMore(false);
                        if (swipeToLoadLayout.isRefreshing())
                            swipeToLoadLayout.setRefreshing(false);
                        avLoadingIndicatorView.hide();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    onArticleResponse(response.body().string());
                    afterArticleResponse();
                } catch (IOException ignored) {
                }
            }
        });
    }

    private void onArticleResponse(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Article article = new Article();
                article.setImg(jsonObject.getString("img"));
                article.setAuthorJobTitle(jsonObject.getString("authorJobTitle"));
                article.setTitle(jsonObject.getString("title"));
                article.setDate(jsonObject.getString("date"));
                article.setText(jsonObject.getString("text"));
                article.setUrl(jsonObject.getString("url"));
                JSONArray jsonArrayTags = new JSONArray(jsonObject.getString("tags"));
                List<Tag> tags = new ArrayList<>();
                for (int j = 0; j < jsonArrayTags.length(); j++) {
                    JSONObject jsonObjectTag = jsonArrayTags.getJSONObject(j);
                    Tag tag = new Tag(jsonObjectTag.getString("text"), jsonObjectTag.getInt("color"));
                    tags.add(tag);
                    if (!tagsForFilter.contains(tag))
                        tagsForFilter.add(tag);
                }
                article.setTags(tags);
                mAllArticles.add(article);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知 UI 文章变更
     */
    private void afterArticleResponse() {
        //请求成功
        Log.i(TAG, "getArticles::afterArticleResponse");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (swipeToLoadLayout.isLoadingMore())
                    swipeToLoadLayout.setLoadingMore(false);
                if (swipeToLoadLayout.isRefreshing())
                    swipeToLoadLayout.setRefreshing(false);
                avLoadingIndicatorView.hide();
                Log.i(TAG, "length of tagsForFilter:" + tagsForFilter.size());
                articleAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 查找标签
     *
     * @param tags 标签
     * @return 结果
     */
    private List<Article> findByTags(List<Tag> tags) {
        List<Article> articles = new ArrayList<>();
        for (Article article : mAllArticles) {
            for (Tag tag : tags) {
                if (article.hasTag(tag.getText()) && !articles.contains(article)) {
                    articles.add(article);
                }
            }
        }

        return articles;
    }

    /**
     * 标签适配器
     */
    class SimpleFilterAdapter extends FilterAdapter<Tag> {
        SimpleFilterAdapter(@NotNull List<? extends Tag> items) {
            super(items);
        }

        @NotNull
        @Override
        public FilterItem createView(int position, Tag item) {
            FilterItem filterItem = new FilterItem(activity);
            filterItem.setStrokeColor(ContextCompat.getColor(activity, R.color.colorAll));
            filterItem.setTextColor(ContextCompat.getColor(activity, R.color.colorAll));
            filterItem.setCheckedTextColor(ContextCompat.getColor(activity, android.R.color.white));
            filterItem.setColor(ContextCompat.getColor(activity, android.R.color.white));
            filterItem.setCheckedColor(getItems().get(position).getColor());
            filterItem.setText(item.getText());
            filterItem.deselect();
            return filterItem;
        }
    }

}
