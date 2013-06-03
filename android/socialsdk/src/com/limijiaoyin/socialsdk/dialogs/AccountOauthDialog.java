package com.limijiaoyin.socialsdk.dialogs;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.limijiaoyin.socialsdk.ISocialLogin;
import com.limijiaoyin.socialsdk.Platform;

public class AccountOauthDialog extends Dialog {

  static final int BLUE = 0xFF6D84B4;

  static final float[] DIMENSIONS_DIFF_LANDSCAPE = {
        20, 60
    };

  static final float[] DIMENSIONS_DIFF_PORTRAIT = {
        40, 60
    };

  static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);

  static final int MARGIN = 4;

  static final int PADDING = 2;

  private final String mUrl;

  // private DialogListener mListener;
  private ProgressDialog mSpinner;

  private WebView mWebView;

  private LinearLayout mContent;

  private TextView mTitle;

  private ISocialLogin mSocialLoginListener;

  private Platform mPlatform;

  public AccountOauthDialog(Context context, String url, Platform platform,
            ISocialLogin socialLoginListener) {
    super(context);
    mUrl = url;
    this.mPlatform = platform;
    mSocialLoginListener = socialLoginListener;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN,
                LayoutParams.FLAG_FULLSCREEN);
    mSpinner = new ProgressDialog(getContext());
    mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
    mSpinner.setMessage("Loading...");

    mContent = new LinearLayout(getContext());
    mContent.setOrientation(LinearLayout.VERTICAL);
    setUpTitle();
    setUpWebView();
    Display display = getWindow().getWindowManager().getDefaultDisplay();
    final float scale = getContext().getResources().getDisplayMetrics().density;
    int orientation = getContext().getResources().getConfiguration().orientation;
    float[] dimensions =
        (orientation == Configuration.ORIENTATION_LANDSCAPE) ? DIMENSIONS_DIFF_LANDSCAPE
                : DIMENSIONS_DIFF_PORTRAIT;
    addContentView(
                mContent,
                new LinearLayout.LayoutParams(display.getWidth()
                        - ((int) (dimensions[0] * scale + 0.5f)), display
                        .getHeight() - ((int) (dimensions[1] * scale + 0.5f))));
  }

  private void setUpTitle() {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    mTitle = new TextView(getContext());
    mTitle.setText("Website");
    mTitle.setTextColor(Color.WHITE);
    mTitle.setTypeface(Typeface.DEFAULT_BOLD);
    mTitle.setBackgroundColor(BLUE);
    mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
    mContent.addView(mTitle);
  }

  private void clearCookie() {
    CookieSyncManager.createInstance(getContext());
    CookieSyncManager.getInstance().startSync();
    CookieManager.getInstance().removeSessionCookie();
    CookieManager.getInstance().removeAllCookie();

    mWebView.clearCache(true);
    mWebView.clearHistory();
  }

  private void setUpWebView() {
    mWebView = new WebView(getContext());
    mWebView.setVerticalScrollBarEnabled(true);
    mWebView.setHorizontalScrollBarEnabled(true);
    mWebView.setWebViewClient(new AccountOauthDialog.DialogWebViewClient());
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebView.getSettings().setBuiltInZoomControls(true);

    clearCookie();

    mWebView.loadUrl(mUrl);
    mWebView.setLayoutParams(FILL);
    mContent.addView(mWebView);
  }

  private class DialogWebViewClient extends WebViewClient {

    @TargetApi(8)
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler,
                SslError error) {
      handler.proceed();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      view.loadUrl(url);
      return true;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
      Log.d("account", "errorCode=" + errorCode + " description= "
                    + description + "  failingUrl= " + failingUrl);
      super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      if (url.endsWith("/bind/success")) {
        mSocialLoginListener.onLoginSuccess(mPlatform);
        mSpinner.dismiss();
        AccountOauthDialog.this.dismiss();
        return;
      }
      if (!mSpinner.isShowing()) {
        mSpinner.show();
      }
      super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
      String title = mWebView.getTitle();
      if (title != null && title.length() > 0) {
        mTitle.setText(title);
      }
      try {
        if (mSpinner.isShowing())
                    mSpinner.dismiss();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
