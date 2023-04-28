package com.google.ads.mediation.topon;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anythink.core.api.AdError;
import com.anythink.nativead.api.ATNative;
import com.anythink.nativead.api.ATNativeNetworkListener;
import com.google.android.gms.ads.mediation.NativeMediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventNative;
import com.google.android.gms.ads.mediation.customevent.CustomEventNativeListener;

import org.json.JSONException;
import org.json.JSONObject;

public class TopOnNativeAdapter extends TopOnBaseAdapter implements CustomEventNative {

    ATNative atNative;

    @Override
    public void requestNativeAd(@NonNull final Context context,
                                @NonNull final CustomEventNativeListener callback,
                                @Nullable String s,
                                @NonNull final NativeMediationAdRequest nativeMediationAdRequest,
                                @Nullable Bundle bundle) {

        if (!nativeMediationAdRequest.isUnifiedNativeAdRequested()) {
            callback.onAdFailedToLoad(generateAdError("Unified Native Ad should be requested."));
            return;
        }
        int adChicesPlacement = -1;
        try {
            adChicesPlacement = nativeMediationAdRequest.getNativeAdRequestOptions().getAdChoicesPlacement();
        } catch (Throwable t) {
        }

        String placementId = null;
        try {
            placementId = getPlacementId(new JSONObject(s));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(placementId)) {
            final int finalAdChicesPlacement = adChicesPlacement;
            atNative = new ATNative(context, placementId, new ATNativeNetworkListener() {
                @Override
                public void onNativeAdLoaded() {
                    new TopOnNativeAdMapper(context,
                            atNative.getNativeAd(),
                            callback,
                            nativeMediationAdRequest,
                            finalAdChicesPlacement).startMapData();
                }

                @Override
                public void onNativeAdLoadFail(AdError adError) {
                    callback.onAdFailedToLoad(generateAdError(adError));
                }
            });
            atNative.setLocalExtra(castBundleToLocalExtra(bundle));
            atNative.makeAdRequest();
        } else {
            callback.onAdFailedToLoad(generateAdError("placementId is empty"));
        }
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }

}
