package com.brotherjing.client.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.brotherjing.client.R;

public class GithubActivity extends AppCompatActivity {


    private static final String TAG = GithubActivity.class.getSimpleName();
    private WebView mWebView;
    private final String mUrl = "https://github.com/gongkechuang3C/smartcar";

    @SuppressWarnings("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_github);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Github");


        mWebView = (WebView) findViewById(R.id.webViewGithub);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public boolean shouldOverrideLoading(WebView view, String url) {
                return false;
            }
        });
        mWebView.loadUrl(mUrl);

    }

}
