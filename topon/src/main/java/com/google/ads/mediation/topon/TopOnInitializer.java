package com.google.ads.mediation.topon;

import android.content.Context;

import com.anythink.core.api.ATSDK;

public class TopOnInitializer {
    private static TopOnInitializer sInstance;

    public synchronized static TopOnInitializer getInstance() {
        if (sInstance == null) {
            sInstance = new TopOnInitializer();
        }
        return sInstance;
    }

    private boolean hasInited = false;

    public void initSDK(Context context, String appId, String appKey) {
        if (!hasInited) {
            hasInited = true;
            ATSDK.init(context, appId, appKey);
        }
    }
}
