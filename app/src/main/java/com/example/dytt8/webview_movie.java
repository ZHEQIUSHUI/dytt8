package com.example.dytt8;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class webview_movie extends AppCompatActivity {

    public String web_txt;
    WebView v;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_movie);
        v = findViewById(R.id.web_view);

        web_txt = getIntent().getStringExtra("web_txt");
//        System.out.println(web_txt);
        v.setInitialScale(260);
        v.loadData(web_txt,"text/html","GB2312");

    }
}