package com.imagegrid.pojos;

import java.io.Serializable;

public class SearchResultPOJO implements Serializable {

    private String title;
    private SearchImagePOJO image;
    private String link;
    private int pageNum;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SearchImagePOJO getImage() {
        return image;
    }

    public String getFullImageLink() {
        return link;
    }

    public void setFullImageLink(String link) {
        this.link = link;
    }
}