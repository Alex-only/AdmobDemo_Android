package com.google.ads.mediation.topon;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;
import com.anythink.interstitial.api.ATInterstitial;
import com.anythink.interstitial.api.ATInterstitialListener;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

public class TopOnInterstitialAdapter extends TopOnBaseAdapter implements MediationInterstitialAd {

    ATInterstitial atInterstitial;
    private MediationInterstitialAdCallback mInterstitalAdCallback;

    @Override
    public void loadInterstitialAd(@NonNull MediationInterstitialAdConfiguration mediationInterstitialAdConfiguration,
                                   @NonNull final MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> callback) {
        String placementId = null;
        try {
            placementId = getPlacementId(new JSONObject(mediationInterstitialAdConfiguration.getServerParameters().getString("parameter")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(placementId)) {
            atInterstitial = new ATInterstitial(mediationInterstitialAdConfiguration.getContext(), placementId);
            atInterstitial.setLocalExtra(castBundleToLocalExtra(mediationInterstitialAdConfiguration.getMediationExtras()));
            atInterstitial.setAdListener(new ATInterstitialListener() {
                @Override
                public void onInterstitialAdLoaded() {
                    mInterstitalAdCallback = callback.onSuccess(TopOnInterstitialAdapter.this);
                }

                @Override
                public void onInterstitialAdLoadFail(AdError adError) {
                    callback.onFailure(generateAdError(adError));
                }

                @Override
                public void onInterstitialAdClicked(ATAdInfo atAdInfo) {
                    mInterstitalAdCallback.reportAdClicked();
                }

                @Override
                public void onInterstitialAdShow(ATAdInfo atAdInfo) {
                    mInterstitalAdCallback.onAdOpened();
                    mInterstitalAdCallback.reportAdImpression();
                }

                @Override
                public void onInterstitialAdClose(ATAdInfo atAdInfo) {
                    mInterstitalAdCallback.onAdClosed();
                }

                @Override
                public void onInterstitialAdVideoStart(ATAdInfo atAdInfo) {

                }

                @Override
                public void onInterstitialAdVideoEnd(ATAdInfo atAdInfo) {

                }

                @Override
                public void onInterstitialAdVideoError(AdError adError) {
                    mInterstitalAdCallback.onAdFailedToShow(generateAdError(adError));
                }
            });
            atInterstitial.load();
        } else {
            callback.onFailure(generateAdError("placementId is empty"));
        }
    }

    @Override
    public void showAd(@NonNull Context context) {
        if (atInterstitial.isAdReady()) {
            if (context instanceof Activity) {
                atInterstitial.show((Activity) context);
            } else {
                this.mInterstitalAdCallback.onAdFailedToShow(generateAdError("Context is not instanceof Activity."));
            }
        } else {
            this.mInterstitalAdCallback.onAdFailedToShow(generateAdError("Ad is unavailable to show."));
        }
    }
}
