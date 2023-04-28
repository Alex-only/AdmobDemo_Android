package com.google.ads.mediation.topon;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATAdInfo;
import com.anythink.rewardvideo.api.ATRewardVideoAd;
import com.anythink.rewardvideo.api.ATRewardVideoListener;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

public class TopOnRewardedAdAdapter extends TopOnBaseAdapter implements MediationRewardedAd {

    ATRewardVideoAd atRewardVideoAd;
    private MediationRewardedAdCallback rewardedAdCallback;

    @Override
    public void loadRewardedAd(MediationRewardedAdConfiguration mediationRewardedAdConfiguration, final MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> callback) {
        String placementId = null;
        try {
            placementId = getPlacementId(new JSONObject(mediationRewardedAdConfiguration.getServerParameters().getString("parameter")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(placementId)) {
            atRewardVideoAd = new ATRewardVideoAd(mediationRewardedAdConfiguration.getContext(), placementId);
            atRewardVideoAd.setLocalExtra(castBundleToLocalExtra(mediationRewardedAdConfiguration.getMediationExtras()));
            atRewardVideoAd.setAdListener(new ATRewardVideoListener() {
                @Override
                public void onRewardedVideoAdLoaded() {
                    rewardedAdCallback = callback.onSuccess(TopOnRewardedAdAdapter.this);
                }

                @Override
                public void onRewardedVideoAdFailed(com.anythink.core.api.AdError adError) {
                    callback.onFailure(generateAdError(adError));
                }

                @Override
                public void onRewardedVideoAdPlayStart(ATAdInfo atAdInfo) {
                    rewardedAdCallback.onAdOpened();
                    rewardedAdCallback.onVideoStart();
                    rewardedAdCallback.reportAdImpression();
                }

                @Override
                public void onRewardedVideoAdPlayEnd(ATAdInfo atAdInfo) {
                    rewardedAdCallback.onVideoComplete();
                }

                @Override
                public void onRewardedVideoAdPlayFailed(com.anythink.core.api.AdError adError, ATAdInfo atAdInfo) {
                    rewardedAdCallback.onAdFailedToShow(generateAdError(adError));
                }

                @Override
                public void onRewardedVideoAdClosed(ATAdInfo atAdInfo) {
                    rewardedAdCallback.onAdClosed();
                }

                @Override
                public void onRewardedVideoAdPlayClicked(ATAdInfo atAdInfo) {
                    rewardedAdCallback.reportAdClicked();
                }

                @Override
                public void onReward(ATAdInfo atAdInfo) {
                    rewardedAdCallback.onUserEarnedReward(new TopOnRewardItem());
                }
            });
            try {
                atRewardVideoAd.load();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else {
            callback.onFailure(generateAdError("placementId is empty"));
        }
    }

    @Override
    public void showAd(Context context) {
        if (atRewardVideoAd.isAdReady()) {
            if (context instanceof Activity) {
                atRewardVideoAd.show((Activity) context);
            } else {
                this.rewardedAdCallback.onAdFailedToShow(generateAdError("Context is not instanceof Activity."));
            }
        } else {
            this.rewardedAdCallback.onAdFailedToShow(generateAdError("Ad is unavailable to show."));
        }
    }
}
