package com.charles.systemproxy;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

public class SystemProxy {
    private static SystemProxy systemProxy = new SystemProxy();
    private PublishSubject<Proxy> mSubject;
    private Activity mActivity;

    private SystemProxy() {
    }

    public static SystemProxy instance() {
        return systemProxy;
    }

    public Observable<String> camera(Activity activity, final String uri) {
        mActivity = activity;
        return new RxPermissions(mActivity)
                .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .filter(new Predicate<Boolean>() {
                    @Override
                    public boolean test(Boolean aBoolean) throws Exception {
                        return aBoolean;
                    }
                })
                .doOnNext(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        Utils.createFile(uri);
                    }
                })
                .map(new Function<Boolean, Intent>() {
                    @Override
                    public Intent apply(Boolean aBoolean) throws Exception {
                        return intent().putExtra("code", ProxyActivity.CAMERA).putExtra("uri", uri);
                    }
                })
                .flatMap(new Function<Intent, ObservableSource<Proxy>>() {
                    @Override
                    public ObservableSource<Proxy> apply(Intent intent) throws Exception {
                        return request(intent);
                    }
                })
                .filter(new Predicate<Proxy>() {
                    @Override
                    public boolean test(Proxy proxy) throws Exception {
                        return proxy != null && proxy.code == Activity.RESULT_OK;
                    }
                })
                .map(new Function<Proxy, String>() {
                    @Override
                    public String apply(Proxy proxy) throws Exception {
                        return uri;
                    }
                });
    }


    public Observable<String> album(final Activity activity) {
        mActivity = activity;
        return new RxPermissions(mActivity)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .filter(new Predicate<Boolean>() {
                    @Override
                    public boolean test(Boolean aBoolean) throws Exception {
                        return aBoolean;
                    }
                })
                .flatMap(new Function<Boolean, ObservableSource<Proxy>>() {
                    @Override
                    public ObservableSource<Proxy> apply(Boolean aBoolean) throws Exception {
                        return request(intent().putExtra("code", ProxyActivity.ALBUM));
                    }
                })
                .filter(new Predicate<Proxy>() {
                    @Override
                    public boolean test(Proxy proxy) throws Exception {
                        return proxy != null && proxy.code == Activity.RESULT_OK;
                    }
                })
                .map(new Function<Proxy, String>() {
                    @Override
                    public String apply(Proxy proxy) throws Exception {
                        return Utils.getImageAbsolutePath(activity.getApplicationContext(), proxy.data.getData());
                    }
                });
    }

    public Observable<Boolean> install(Activity activity, String apkpath) {
        mActivity = activity;
        return request(intent().putExtra("code", ProxyActivity.INSTALL).putExtra("apkpath", apkpath))
                .map(new Function<Proxy, Boolean>() {
                    @Override
                    public Boolean apply(Proxy proxy) throws Exception {
                        return proxy != null && proxy.code == Activity.RESULT_OK;
                    }
                });
    }

    public void phone(Activity activity, final String phone) {
        mActivity = activity;
        new RxPermissions(mActivity)
                .request(Manifest.permission.CALL_PHONE)
                .filter(new Predicate<Boolean>() {
                    @Override
                    public boolean test(Boolean aBoolean) throws Exception {
                        return aBoolean;
                    }
                })
                .map(new Function<Boolean, Intent>() {


                    @Override
                    public Intent apply(Boolean aBoolean) throws Exception {
                        return intent().putExtra("code", ProxyActivity.PHONE).putExtra("phone", phone);
                    }
                })
                .flatMap(new Function<Intent, ObservableSource<Proxy>>() {
                    @Override
                    public ObservableSource<Proxy> apply(Intent intent) throws Exception {
                        return request(intent);
                    }
                })
                .subscribe(new TestObserver<Proxy>());
    }

    private Observable<Proxy> request(Intent intent) {
        mSubject = PublishSubject.create();
        intent.setClass(mActivity, ProxyActivity.class);
        mActivity.startActivity(intent);
        return Observable.concat(Observable.just(mSubject));
    }

    void onActivityResult(int resultCode, Intent data) {
        if (mSubject != null) {
            mSubject.onNext(new Proxy(resultCode, data));
            mSubject.onComplete();

        }
    }

    private Intent intent() {
        return new Intent(mActivity, ProxyActivity.class);
    }


}
