package com.innotion.vpillreminders;


import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class Help extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.help);
	    
	    WebView webview = new WebView(this);

	    webview.getSettings().setJavaScriptEnabled(true);
	    webview.getSettings().setBuiltInZoomControls(true);

	    setContentView(webview);
        try {
                webview.loadUrl("file:///android_asset/help.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}