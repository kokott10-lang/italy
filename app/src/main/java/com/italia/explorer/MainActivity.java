package com.italia.explorer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.content.FileProvider;

import java.io.File;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> chooser;
    private Uri camUri;
    private PermissionRequest pendingPerm;
    private static final int REQ = 1, REQ_CAM = 2, REQ_PERM = 3;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        web.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void print() {
                runOnUiThread(() -> {
                    PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                    pm.print("이탈리아 탐험대", web.createPrintDocumentAdapter("이탈리아_탐험대"),
                            new PrintAttributes.Builder().build());
                });
            }
        }, "AndroidPrint");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, android.webkit.WebResourceRequest r) {
                Uri u = r.getUrl();
                if ("file".equals(u.getScheme())) return false;
                startActivity(new Intent(Intent.ACTION_VIEW, u));
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            // 페이지 안 카메라(getUserMedia) 권한
            @Override
            public void onPermissionRequest(PermissionRequest req) {
                runOnUiThread(() -> {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        req.grant(req.getResources());
                    } else {
                        pendingPerm = req;
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_PERM);
                    }
                });
            }

            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (chooser != null) chooser.onReceiveValue(null);
                chooser = cb;
                try {
                    if (p.isCaptureEnabled()) {
                        File dir = new File(getCacheDir(), "photos");
                        dir.mkdirs();
                        File f = new File(dir, "cam_" + System.currentTimeMillis() + ".jpg");
                        camUri = FileProvider.getUriForFile(MainActivity.this, "com.italia.explorer.files", f);
                        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        i.putExtra(MediaStore.EXTRA_OUTPUT, camUri);
                        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(i, REQ_CAM);
                    } else {
                        startActivityForResult(p.createIntent(), REQ);
                    }
                } catch (Exception e) {
                    chooser = null;
                    return false;
                }
                return true;
            }
        });
        if (b == null) web.loadUrl("file:///android_asset/index.html");
        else web.restoreState(b);
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == REQ_PERM && pendingPerm != null) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                pendingPerm.grant(pendingPerm.getResources());
            } else {
                pendingPerm.deny();
            }
            pendingPerm = null;
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (chooser == null) return;
        if (req == REQ_CAM) {
            chooser.onReceiveValue(res == RESULT_OK && camUri != null ? new Uri[]{camUri} : null);
            chooser = null;
        } else if (req == REQ) {
            chooser.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data));
            chooser = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle o) {
        super.onSaveInstanceState(o);
        web.saveState(o);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
