package com.google.ads.mediation.topon;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.anythink.core.api.ATAdInfo;
import com.anythink.nativead.api.ATNativeAdView;
import com.anythink.nativead.api.ATNativeDislikeListener;
import com.anythink.nativead.api.ATNativeEventListener;
import com.anythink.nativead.api.NativeAd;
import com.google.android.gms.ads.mediation.NativeMediationAdRequest;
import com.google.android.gms.ads.mediation.UnifiedNativeAdMapper;
import com.google.android.gms.ads.mediation.customevent.CustomEventNativeListener;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TopOnNativeAdMapper extends UnifiedNativeAdMapper {

    private Context mContext;
    private NativeAd mATNativeAd;
    private int mAdChoicePlacement;
    private NativeMediationAdRequest mNativeMediationAdRequest;
    private CustomEventNativeListener mNativeCallback;

    ATNativeAdView mATNativeAdView;
    private TopOnNativeRender mNativeRender;
    private FrameLayout mRenderFragment;

    public TopOnNativeAdMapper(Context context,
                               NativeAd nativeAd,
                               CustomEventNativeListener callback,
                               NativeMediationAdRequest nativeMediationAdRequest,
                               int choicePlacement) {
        this.mContext = context;
        this.mATNativeAd = nativeAd;
        this.mNativeMediationAdRequest = nativeMediationAdRequest;
        this.mNativeCallback = callback;
        this.mAdChoicePlacement = choicePlacement;
    }

    public void startMapData() {
        mRenderFragment = new FrameLayout(mContext);
        mNativeRender = new TopOnNativeRender(mContext,
                mRenderFragment,
                this,
                mNativeCallback,
                !mNativeMediationAdRequest.getNativeAdRequestOptions().shouldReturnUrlsForImageAssets(),
                mNativeMediationAdRequest.isAdMuted(),
                mATNativeAd.getAdInfo().getNetworkFirmId() == 3);
        mATNativeAdView = new ATNativeAdView(mContext);
        mATNativeAd.renderAdView(mATNativeAdView, mNativeRender);
    }

    @Override
    public void trackViews(@NonNull View view, @NonNull Map<String, View> map, @NonNull Map<String, View> map1) {
        if (view instanceof NativeAdView) {

            mRenderFragment.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

            List<View> clickViews = new ArrayList<>();

            NativeAdView nativeAdView = (NativeAdView) view;

            clickViews.addAll(getNativeAllViews(nativeAdView));

            List<View> childViews = new ArrayList<>();
            for (int i = 0; i < nativeAdView.getChildCount(); i++) {
                childViews.add(nativeAdView.getChildAt(i));
            }
            nativeAdView.removeAllViews();
            for (int i = (childViews.size() - 1); i >= 0; i--) {
                View targetView = childViews.get(i);
                if (targetView.getParent() == null) {
                    mRenderFragment.addView(targetView);
                }
            }

            View mateAdIcon = mATNativeAd.getAdMaterial().getAdIconView();
            //only facebook need to replate AdLogoView
            if (mATNativeAd.getAdInfo().getNetworkFirmId() == 1 &&
                    mateAdIcon != null && nativeAdView.getIconView() != null && nativeAdView.getIconView().getParent() != null) {
                View iconView = nativeAdView.getIconView();
                ViewGroup iconParent = ((ViewGroup) iconView.getParent());
                iconParent.addView(mateAdIcon, iconParent.indexOfChild(iconView), nativeAdView.getIconView().getLayoutParams());
                iconView.setVisibility(View.GONE);
            }

            mATNativeAd.setNativeEventListener(new ATNativeEventListener() {
                @Override
                public void onAdImpressed(ATNativeAdView atNativeAdView, ATAdInfo atAdInfo) {
                    if (mNativeCallback != null) {
                        mNativeCallback.onAdImpression();
                    }
                }

                @Override
                public void onAdClicked(ATNativeAdView atNativeAdView, ATAdInfo atAdInfo) {
                    if (mNativeCallback != null) {
                        mNativeCallback.onAdClicked();
                    }
                }

                @Override
                public void onAdVideoStart(ATNativeAdView atNativeAdView) {

                }

                @Override
                public void onAdVideoEnd(ATNativeAdView atNativeAdView) {

                }

                @Override
                public void onAdVideoProgress(ATNativeAdView atNativeAdView, int i) {

                }
            });

            mATNativeAd.setDislikeCallbackListener(new ATNativeDislikeListener() {
                @Override
                public void onAdCloseButtonClick(ATNativeAdView atNativeAdView, ATAdInfo atAdInfo) {
                    if (mNativeCallback != null) {
                        mNativeCallback.onAdClosed();
                    }
                }
            });

            nativeAdView.addView(mATNativeAdView, MATCH_PARENT, MATCH_PARENT);

            FrameLayout.LayoutParams layoutParams = null;
            switch (mAdChoicePlacement) {
                case NativeAdOptions.ADCHOICES_TOP_LEFT:
                    layoutParams = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                    layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
                    break;
                case NativeAdOptions.ADCHOICES_TOP_RIGHT:
                    layoutParams = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                    layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
                    break;
                case NativeAdOptions.ADCHOICES_BOTTOM_LEFT:
                    layoutParams = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                    layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                    break;
                case NativeAdOptions.ADCHOICES_BOTTOM_RIGHT:
                default:
                    layoutParams = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                    layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    break;
            }
            mATNativeAd.prepare(mATNativeAdView, clickViews, layoutParams);
        }
    }

    @Override
    public void untrackView(@NonNull View view) {
        super.untrackView(view);
    }

    private List<View> getNativeAllViews(NativeAdView nativeAdView) {
        List<View> views = new ArrayList<>();
        if (nativeAdView.getHeadlineView() != null) {
            views.add(nativeAdView.getHeadlineView());
        }
        if (nativeAdView.getBodyView() != null) {
            views.add(nativeAdView.getBodyView());
        }
        if (nativeAdView.getCallToActionView() != null) {
            views.add(nativeAdView.getCallToActionView());
        }
        if (nativeAdView.getIconView() != null) {
            views.add(nativeAdView.getIconView());
        }
        if (nativeAdView.getBodyView() != null) {
            views.add(nativeAdView.getBodyView());
        }
        if (nativeAdView.getStoreView() != null) {
            views.add(nativeAdView.getStoreView());
        }
        if (nativeAdView.getPriceView() != null) {
            views.add(nativeAdView.getPriceView());
        }
        if (nativeAdView.getAdvertiserView() != null) {
            views.add(nativeAdView.getAdvertiserView());
        }
        if (nativeAdView.getImageView() != null) {
            views.add(nativeAdView.getImageView());
        }
        if (nativeAdView.getMediaView() != null) {
            views.add(nativeAdView.getMediaView());
        }
        if (nativeAdView.getStarRatingView() != null) {
            views.add(nativeAdView.getStarRatingView());
        }
        if (nativeAdView.getAdChoicesView() != null) {
            views.add(nativeAdView.getAdChoicesView());
        }
        return views;
    }

}
