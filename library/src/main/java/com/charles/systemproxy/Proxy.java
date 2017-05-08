package com.charles.systemproxy;

import android.content.Intent;
import android.support.annotation.Keep;

@Keep
class Proxy {
    public Intent data;
    int code;

    Proxy(int resultCode, Intent data) {
        this.data = data;
        this.code = resultCode;
    }
}
