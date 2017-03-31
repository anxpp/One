package com.anxpp.one.plus;

import android.support.annotation.NonNull;

import com.yalantis.filter.model.FilterModel;

/**
 * 标签
 * Created by anxpp.com on 16.09.16.
 */
public class Tag implements FilterModel {
    private String text;
    private int color;

    public Tag(String text, int color) {
        this.text = text;
        this.color = color;
    }

    @NonNull
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Tag)) return false;
//
//        Tag tag = (Tag) o;
//
//        if (getColor() != tag.getColor()) return false;
//        return getText().equals(tag.getText())&& color==tag.getColor();
//
//    }
//
//    @Override
//    public int hashCode() {
//        int result = getText().hashCode();
//        result = 31 * result + getColor();
//        return result;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;

        Tag tag = (Tag) o;

        return getColor() == tag.getColor() && getText().equals(tag.getText());

    }

    @Override
    public int hashCode() {
        int result = getText().hashCode();
        result = 31 * result + getColor();
        return result;
    }
}
