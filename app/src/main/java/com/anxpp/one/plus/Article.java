package com.anxpp.one.plus;

import java.util.List;

/**
 * Created by anxpp.com on 16.09.16.
 */
public class Article {
    private String title;
    private String authorJobTitle;
    private String img;
    private String date;
    private String text;
    private String url;
    private List<Tag> tags;

    public Article(){
    }

    public Article(String title, String authorJobTitle, String img, String date, String text, List<Tag> tags) {
        this.title = title;
        this.authorJobTitle = authorJobTitle;
        this.img = img;
        this.date = date;
        this.text = text;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorJobTitle() {
        return authorJobTitle;
    }

    public void setAuthorJobTitle(String authorJobTitle) {
        this.authorJobTitle = authorJobTitle;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean hasTag(String string) {
        for (Tag tag : tags) {
            if (tag.getText().equals(string)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article)) return false;

        Article article = (Article) o;

        if (getTitle() != null ? !getTitle().equals(article.getTitle()) : article.getTitle() != null)
            return false;
        if (getAuthorJobTitle() != null ? !getAuthorJobTitle().equals(article.getAuthorJobTitle()) : article.getAuthorJobTitle() != null)
            return false;
        if (getImg() != null ? !getImg().equals(article.getImg()) : article.getImg() != null)
            return false;
        if (getDate() != null ? !getDate().equals(article.getDate()) : article.getDate() != null)
            return false;
        if (getText() != null ? !getText().equals(article.getText()) : article.getText() != null)
            return false;
        return getTags() != null ? getTags().equals(article.getTags()) : article.getTags() == null;

    }

    @Override
    public int hashCode() {
        int result = getTitle() != null ? getTitle().hashCode() : 0;
        result = 31 * result + (getAuthorJobTitle() != null ? getAuthorJobTitle().hashCode() : 0);
        result = 31 * result + (getImg() != null ? getImg().hashCode() : 0);
        result = 31 * result + (getDate() != null ? getDate().hashCode() : 0);
        result = 31 * result + (getText() != null ? getText().hashCode() : 0);
        result = 31 * result + (getTags() != null ? getTags().hashCode() : 0);
        return result;
    }
}
