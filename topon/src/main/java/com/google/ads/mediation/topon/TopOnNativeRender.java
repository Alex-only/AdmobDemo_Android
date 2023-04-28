package com.google.ads.mediation.topon;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.anythink.nativead.api.ATNativeAdRenderer;
import com.anythink.nativead.api.ATNativeImageView;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.mediation.customevent.CustomEventNativeListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TopOnNativeRender implements ATNativeAdRenderer<CustomNativeAd> {
    Context mContext;
    View adView;
    TopOnNativeAdMapper nativeAdMapper;
    boolean onlySupportDrawable;
    boolean isAdMuted;
    boolean isInmobi;
    private CustomEventNativeListener mNativeCallback;


    HashMap<String, URL> imageDownloadMap = new HashMap<>();
    URL imageURL = null;
    Uri imageUri = null;

    URL iconURL = null;
    Uri iconUri = null;

    public TopOnNativeRender(Context context,
                             View view,
                             TopOnNativeAdMapper mapper,
                             CustomEventNativeListener mNativeCallback,
                             boolean onlyDrawable,
                             boolean muted,
                             boolean isInmobi) {
        mContext = context;
        adView = view;
        this.nativeAdMapper = mapper;
        this.mNativeCallback = mNativeCallback;
        this.onlySupportDrawable = onlyDrawable;
        this.isAdMuted = muted;
        this.isInmobi = isInmobi;
    }

    @Override
    public View createView(Context context, int i) {
        return adView;
    }

    @Override
    public void renderAdView(View view, final CustomNativeAd atNativeAd) {

        //override click and impression handle
        nativeAdMapper.setOverrideClickHandling(true);
        nativeAdMapper.setOverrideImpressionRecording(true);

        atNativeAd.setVideoMute(isAdMuted);


        nativeAdMapper.setHeadline(atNativeAd.getTitle());
        nativeAdMapper.setBody(atNativeAd.getDescriptionText());

        View mediaView = atNativeAd.getAdMediaView();
        if (mediaView != null) {
            nativeAdMapper.setHasVideoContent(true);
            nativeAdMapper.setMediaView(mediaView);
        } else if (isInmobi) {
            //only for inmobi mediaView
            final RelativeLayout placeHolderView = new ClickInterceptorRelativeLayout(mContext);
            placeHolderView.setLayoutParams(
                    new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            placeHolderView.setGravity(Gravity.CENTER);
            placeHolderView.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            final View primaryView = atNativeAd.getAdMediaView(placeHolderView, placeHolderView.getWidth());
                            if (primaryView == null) {
                                return;
                            }
                            placeHolderView.addView(primaryView);
                            int viewHeight = primaryView.getLayoutParams().height;
                            if (viewHeight > 0) {
                                nativeAdMapper.setMediaContentAspectRatio((float) primaryView.getLayoutParams().width / viewHeight);
                            }
                        }
                    });
            nativeAdMapper.setHasVideoContent(true);
            nativeAdMapper.setMediaView(placeHolderView);
        } else if (!TextUtils.isEmpty(atNativeAd.getMainImageUrl())) {
            try {
                imageURL = new URL(atNativeAd.getMainImageUrl());
                imageUri = Uri.parse(imageURL.toURI().toString());
            } catch (Throwable exception) {
            }
            if (onlySupportDrawable) {
                imageDownloadMap.put(ImageDownloaderAsyncTask.KEY_IMAGE, imageURL);
            } else {
                if (imageUri != null) {
                    List<NativeAd.Image> images = new ArrayList<>();
                    images.add(new TopOnAdapterNativeAdImage(imageUri));
                    nativeAdMapper.setImages(images);
                }
            }
        }

        if (!TextUtils.isEmpty(atNativeAd.getIconImageUrl())) {
            try {
                iconURL = new URL(atNativeAd.getIconImageUrl());
                iconUri = Uri.parse(iconURL.toURI().toString());
            } catch (Throwable exception) {
            }
            if (onlySupportDrawable) {
                imageDownloadMap.put(ImageDownloaderAsyncTask.KEY_ICON, iconURL);
            } else {
                if (iconUri != null) {
                    nativeAdMapper.setIcon(new TopOnAdapterNativeAdImage(iconUri));
                }
            }
        } else {
            nativeAdMapper.setIcon(new TopOnAdapterNativeAdImage());
        }
        nativeAdMapper.setCallToAction(atNativeAd.getCallToActionText());

        nativeAdMapper.setStarRating(atNativeAd.getStarRating());
        nativeAdMapper.setAdvertiser(atNativeAd.getAdFrom());

        if (!TextUtils.isEmpty(atNativeAd.getAdChoiceIconUrl())) {
            ATNativeImageView imageView = new ATNativeImageView(mContext);
            imageView.setImage(atNativeAd.getAdChoiceIconUrl());
            nativeAdMapper.setAdChoicesContent(imageView);
        }

        if (onlySupportDrawable) {
            //Admob Image only support Drawable Return
            new ImageDownloaderAsyncTask(
                    new ImageDownloaderAsyncTask.DrawableDownloadListener() {
                        @Override
                        public void onDownloadSuccess(HashMap<String, Drawable> drawableMap) {
                            if (imageDownloadMap.containsKey(ImageDownloaderAsyncTask.KEY_ICON)) {
                                Drawable iconDrawable = drawableMap.get(ImageDownloaderAsyncTask.KEY_ICON);

                                if (iconDrawable == null) {
                                    mNativeCallback.onAdFailedToLoad(TopOnBaseAdapter.generateAdError("Failed to download icon assets."));
                                    return;
                                }

                                nativeAdMapper.setIcon(new TopOnAdapterNativeAdImage(iconDrawable, iconUri));
                            }
                            if (imageDownloadMap.containsKey(ImageDownloaderAsyncTask.KEY_IMAGE)) {
                                Drawable imageDrawable = drawableMap.get(ImageDownloaderAsyncTask.KEY_IMAGE);

                                if (imageDrawable == null) {
                                    mNativeCallback.onAdFailedToLoad(TopOnBaseAdapter.generateAdError("Failed to download image assets."));
                                    return;
                                }

                                List<NativeAd.Image> imagesList = new ArrayList<>();
                                imagesList.add(new TopOnAdapterNativeAdImage(imageDrawable, imageUri));
                                nativeAdMapper.setImages(imagesList);
                            }

                            mNativeCallback.onAdLoaded(nativeAdMapper);
                        }

                        @Override
                        public void onDownloadFailure() {
                            mNativeCallback.onAdFailedToLoad(TopOnBaseAdapter.generateAdError("Failed to download image or icon assets."));
                        }
                    })
                    .execute(imageDownloadMap);
        } else {
            mNativeCallback.onAdLoaded(nativeAdMapper);
        }
    }


    private class TopOnAdapterNativeAdImage extends
            NativeAd.Image {

        private Drawable mDrawable;

        private Uri mUri;

        public TopOnAdapterNativeAdImage() {
        }

        public TopOnAdapterNativeAdImage(Drawable drawable, Uri uri) {
            this.mDrawable = drawable;
            this.mUri = uri;
        }

        public TopOnAdapterNativeAdImage(Uri uri) {
            this.mUri = uri;
        }

        @NonNull
        @Override
        public Drawable getDrawable() {
            return mDrawable;
        }

        @NonNull
        @Override
        public Uri getUri() {
            return mUri;
        }

        @Override
        public double getScale() {
            return 1;
        }
    }
}
