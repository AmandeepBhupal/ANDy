package com.andy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FeedsWebViewActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds_web_view);

        webView = (WebView) findViewById(R.id.webView);
        String url = getIntent().getStringExtra("URI");

        //Navigate to previous activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Instantiate web view
        webView.setWebViewClient(new webViewClient());
        webView.getSettings().setJavaScriptEnabled(true);



        String viewType = "";boolean loadData = true;

        if(url.length() > 4)
        {
            String substring = url.substring(0,4);
            substring = substring.toLowerCase();
            if(substring.contains("http")|| substring.contains("file")){

                webView.getSettings().setLoadWithOverviewMode(true);
                webView.getSettings().setUseWideViewPort(true);
                webView.getSettings().setSupportZoom(true);

                /*String pdfUrl = "http://drive.google.com/viewerng/viewer?embedded=true&url=";
                String extension = url.substring(url.lastIndexOf('.') + 1);
                if( !extension.isEmpty() && extension.compareTo("pdf") == 0){
                    url = pdfUrl + url;
                }*/

                webView.loadUrl(url); // load a web page in a web view

                loadData = false;
            }
        }
        if(loadData) {
            webView.loadData(url, "text/html; charset=utf-8", "utf-8");
        }


    }

}

    class webViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
}
