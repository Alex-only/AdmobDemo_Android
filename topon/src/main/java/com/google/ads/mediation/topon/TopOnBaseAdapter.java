package com.google.ads.mediation.topon;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.anythink.sdk.core.BuildConfig;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.VersionInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopOnBaseAdapter extends Adapter {

    static final String APP_ID_PARAMETER = "app_id";
    static final String APP_KEY_PARAMETER = "app_key";
    static final String PLACEMENT_ID_PARAMETER = "placement_id";

    @NonNull
    @Override
    public VersionInfo getSDKVersionInfo() {
        String versionString = BuildConfig.SDK_VERSION_NAME;
        String splits[] = versionString.split("\\.");
        int major = Integer.parseInt(splits[0]);
        int minor = Integer.parseInt(splits[1]);
        int micro = Integer.parseInt(splits[2]) * 100 + 1;
        return new VersionInfo(major, minor, micro);
    }

    @NonNull
    @Override
    public VersionInfo getVersionInfo() {
        String versionString = BuildConfig.SDK_VERSION_NAME;
        String splits[] = versionString.split("\\.");
        int major = Integer.parseInt(splits[0]);
        int minor = Integer.parseInt(splits[1]);
        int micro = Integer.parseInt(splits[2]);
        return new VersionInfo(major, minor, micro);
    }

    @Override
    public void initialize(@NonNull Context context, @NonNull InitializationCompleteCallback initializationCompleteCallback, @NonNull List<MediationConfiguration> list) {
        if (list.size() > 0) {
            Bundle bundle = list.get(0).getServerParameters();
            String params = bundle.getString("parameter");
            String appId = null;
            try {
                appId = getAppId(new JSONObject(params));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String appKey = null;
            try {
                appKey = getAppKey(new JSONObject(params));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appKey)) {
                initializationCompleteCallback.onInitializationFailed("Please check " + APP_ID_PARAMETER + " and " + APP_KEY_PARAMETER + " is valid.By this info:" + params);
            } else {
                TopOnInitializer.getInstance().initSDK(context, appId, appKey);
                initializationCompleteCallback.onInitializationSucceeded();
            }
        } else {
            initializationCompleteCallback.onInitializationFailed("no mediationconfiguration list");
        }
    }

    protected String getAppId(JSONObject jsonObject) {
        String appId = "";
        try {
            appId = jsonObject.optString(APP_ID_PARAMETER, "");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return appId;
    }

    protected String getAppKey(JSONObject jsonObject) {
        String appKey = "";
        try {
            appKey = jsonObject.optString(APP_KEY_PARAMETER, "");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return appKey;
    }

    protected String getPlacementId(JSONObject jsonObject) {
        String placementId = "";
        try {
            placementId = jsonObject.optString(PLACEMENT_ID_PARAMETER, "");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return placementId;
    }

    protected static AdError generateAdError(String errorMsg) {
        return new AdError(-1, errorMsg, "");
    }

    protected static AdError generateAdError(com.anythink.core.api.AdError adError) {
        try {
            return new AdError(Integer.parseInt(adError.getCode()), adError.getDesc(), "");
        } catch (Throwable t) {
            return new AdError(-1, adError.getDesc(), "");
        }
    }


    protected Map<String, Object> castBundleToLocalExtra(Bundle bundle) {
        Map<String, Object> localExtras = new HashMap<>();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                localExtras.put(key, bundle.get(key));
            }
        }
        return localExtras;
    }
}
