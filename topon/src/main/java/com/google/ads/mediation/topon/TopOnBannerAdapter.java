package com.google.ads.mediation.topon;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.anythink.banner.api.ATBannerListener;
import com.anythink.banner.api.ATBannerView;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

public class TopOnBannerAdapter extends TopOnBaseAdapter implements MediationBannerAd {

    ATBannerView atBannerView;
    MediationBannerAdCallback bannerAdCallback;

    @Override
    public void loadBannerAd(@NonNull MediationBannerAdConfiguration mediationBannerAdConfiguration,
                             @NonNull final MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback) {

        String placementId = null;
        try {
            placementId = getPlacementId(new JSONObject(mediationBannerAdConfiguration.getServerParameters().getString("parameter")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(placementId)) {
            atBannerView = new ATBannerView(mediationBannerAdConfiguration.getContext());
            atBannerView.setPlacementId(placementId);
            atBannerView.setLocalExtra(castBundleToLocalExtra(mediationBannerAdConfiguration.getMediationExtras()));
            atBannerView.setBannerAdListener(new ATBannerListener() {
                @Override
                public void onBannerLoaded() {
                    bannerAdCallback = callback.onSuccess(TopOnBannerAdapter.this);
                }

                @Override
                public void onBannerFailed(AdError adError) {
                    callback.onFailure(generateAdError(adError));
                }

                @Override
                public void onBannerClicked(ATAdInfo atAdInfo) {
                    bannerAdCallback.reportAdClicked();
                }

                @Override
                public void onBannerShow(ATAdInfo atAdInfo) {
                    bannerAdCallback.onAdOpened();
                    bannerAdCallback.reportAdImpression();
                }

                @Override
                public void onBannerClose(ATAdInfo atAdInfo) {
                    bannerAdCallback.onAdClosed();
                }

                @Override
                public void onBannerAutoRefreshed(ATAdInfo atAdInfo) {
                    bannerAdCallback.reportAdImpression();
                }

                @Override
                public void onBannerAutoRefreshFail(AdError adError) {

                }
            });
            atBannerView.loadAd();
        } else {
            callback.onFailure(generateAdError("placementId is empty"));
        }
    }

    @NonNull
    @Override
    public View getView() {
        return atBannerView;
    }
}
