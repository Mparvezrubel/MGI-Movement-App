package com.mparvezrubel.mgimovement;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.os.Environment;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.view.View;

public class MainActivity extends Activity {

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView view,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params) {

                filePathCallback = callback;

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                fileIntent.setType("image/*");

                Intent chooser = Intent.createChooser(fileIntent, "ছবি নির্বাচন করুন");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                        new Intent[]{cameraIntent});

                startActivityForResult(chooser, 100);
                return true;
            }
        });

        webView.addJavascriptInterface(new AndroidBridge(), "Android");

        webView.loadUrl("file:///android_asset/index.html");

        setContentView(webView);
    }

    public class AndroidBridge {

        @JavascriptInterface
        public void createPdf() {
            runOnUiThread(() -> {
                try {
                    PrintDocumentAdapter adapter =
                            webView.createPrintDocumentAdapter("MGI-Movement-Backup");

                    PrintAttributes attributes =
                            new PrintAttributes.Builder()
                                    .setMediaSize(
                                            PrintAttributes.MediaSize.ISO_A4)
                                    .setResolution(
                                            new PrintAttributes.Resolution(
                                                    "pdf",
                                                    "PDF",
                                                    300,
                                                    300))
                                    .setMinMargins(
                                            PrintAttributes.Margins.NO_MARGINS)
                                    .build();

                    android.print.PrintManager printManager =
                            (android.print.PrintManager)
                                    getSystemService(PRINT_SERVICE);

                    printManager.print(
                            "MGI Movement Backup",
                            adapter,
                            attributes);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && filePathCallback != null) {

            Uri[] results = null;

            if (resultCode == RESULT_OK) {

                if (data != null && data.getData() != null) {
                    results = new Uri[]{data.getData()};
                }
            }

            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
