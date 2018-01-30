package com.imagegrid.network;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SearchAsyncHttp {
    private static final String BASE_URL = "https://www.googleapis.com/customsearch/v1?";
    private static final String API_KEY = "AIzaSyCqRZgz6YJJwCclmfNX__L74mpLf_LFiOQ";
    private static final String SE_ID = "013079244253699854793:seggni-nrhs";

    private AsyncHttpClient client;

    public SearchAsyncHttp() {
        this.client = new AsyncHttpClient();
    }

    public void startSearchAsync(final String textToSearch, int startPage, JsonHttpResponseHandler handler) {
        try {
            String url = BASE_URL + "q=" + URLEncoder.encode(textToSearch, "utf-8") + "&start=" + startPage +
                    "&cx=" + SE_ID + "&searchType=image&key=" + API_KEY;
            client.get(url, handler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}