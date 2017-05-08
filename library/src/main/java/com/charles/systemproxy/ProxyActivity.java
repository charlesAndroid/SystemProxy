package com.charles.systemproxy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;

public final class ProxyActivity extends Activity {
    private int code;
    public static final int CAMERA = 1;
    public static final int ALBUM = 2;
    public static final int INSTALL = 3;
    public static final int PHONE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        code = getIntent().getIntExtra("code", -1);
        Intent proxyIntent = convert();
        if (proxyIntent != null) {
            startActivityForResult(proxyIntent, code);
        } else {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        code = intent.getIntExtra("code", -1);
        Intent bridge = convert();
        if (bridge != null) {
            startActivityForResult(bridge, code);
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SystemProxy.instance().onActivityResult(resultCode, data);
        finish();
    }


    private Intent convert() {
        Intent proxyIntent = null;
        switch (code) {
            case ALBUM:
                proxyIntent = IntentFactory.createAlbumIntent();
                break;
            case CAMERA:
                proxyIntent = IntentFactory.createCameraIntent();
                String uri = getIntent().getStringExtra("uri");
                if (!TextUtils.isEmpty(uri)) {
                    Uri uriForFile;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uriForFile = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", new File(uri));
                    } else {
                        uriForFile = Uri.fromFile(new File(uri));
                    }

                    proxyIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                }
                break;
            case INSTALL:
                String filePath = getIntent().getStringExtra("apkpath");
                if (!TextUtils.isEmpty(filePath)) {
                    proxyIntent = IntentFactory.createInstallIntent(filePath);
                }
                break;
            case PHONE:
                String phone = getIntent().getStringExtra("phone");
                if (!TextUtils.isEmpty(phone)) {
                    proxyIntent = IntentFactory.createPhoneIntent(phone);
                }
                break;

        }
        return proxyIntent;
    }

    private static class IntentFactory {

        static Intent createAlbumIntent() {
            return new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }

        static Intent createCameraIntent() {
            return new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        }

        static Intent createInstallIntent(String appPath) {
            return new Intent().setDataAndType(Uri.fromFile(new File(appPath)),
                    "application/vnd.android.package-archive");
        }

        static Intent createPhoneIntent(String phone) {
            return new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));

        }
    }

}
