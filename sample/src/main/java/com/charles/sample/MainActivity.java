package com.charles.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.charles.systemproxy.SystemProxy;

import io.reactivex.observers.TestObserver;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemProxy.instance()
                        .camera(MainActivity.this, "sdcard/123.jpeg")
                        .subscribe(new TestObserver<String>() {

                            @Override
                            public void onNext(String s) {
                                Log.e("SystemProxy", "path:" + s);
                            }
                        });
            }
        });
        findViewById(R.id.btn_album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemProxy.instance()
                        .album(MainActivity.this)
                        .subscribe(new TestObserver<String>() {
                            @Override
                            public void onNext(String s) {
                                Log.e("SystemProxy", "path:" + s);
                            }
                        });
            }
        });


    }

}
