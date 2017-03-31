package com.anxpp.one.plus;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anxpp.one.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

/**
 * 文章列表适配置
 * Created by anxpp.com on 16.09.16.
 */
public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private List<Article> mArticles;
    private Context mContext;
    private OnItemClickListener onItemClickListener;

    public ArticleAdapter(Context context, List<Article> articles) {
        mContext = context;
        mArticles = articles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_home, null, false));
    }

    public List<Article> getArticles() {
        return mArticles;
    }

    public void setArticles(List<Article> articles) {
        this.mArticles = articles;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Article article = mArticles.get(position);

        if(onItemClickListener!=null)
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(position);
                }
            });

        holder.avatar.setImageURI(article.getImg());
        holder.textAuthorName.setText(article.getTitle());
        holder.textJobTitle.setText(article.getAuthorJobTitle());
        holder.textDate.setText(article.getDate());
        holder.textQuestion.setText(article.getText());
        Tag firstTag = article.getTags().get(0);
        holder.firstFilter.setText(firstTag.getText());
        Tag secondTag = article.getTags().get(1);
        holder.secondFilter.setText(secondTag.getText());

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(1000);
        drawable.setColor(firstTag.getColor());
        holder.firstFilter.setBackground(drawable);
        GradientDrawable drawable1 = new GradientDrawable();
        drawable1.setCornerRadius(1000);
        drawable1.setColor(secondTag.getColor());
        holder.secondFilter.setBackground(drawable1);
    }

    private int getColor(int color) {
        return ContextCompat.getColor(mContext, color);
    }

    @Override
    public int getItemCount() {
        return mArticles.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textAuthorName;
        TextView textJobTitle;
        TextView textDate;
        TextView textQuestion;
        TextView firstFilter;
        TextView secondFilter;
        SimpleDraweeView avatar;

        ViewHolder(View itemView) {
            super(itemView);
            textAuthorName = (TextView) itemView.findViewById(R.id.text_name);
            textJobTitle = (TextView) itemView.findViewById(R.id.text_job_title);
            textDate = (TextView) itemView.findViewById(R.id.text_date);
            textQuestion = (TextView) itemView.findViewById(R.id.text_question);
            firstFilter = (TextView) itemView.findViewById(R.id.filter_first);
            secondFilter = (TextView) itemView.findViewById(R.id.filter_second);
            avatar = (SimpleDraweeView) itemView.findViewById(R.id.avatar);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

}
