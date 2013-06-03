package com.limijiaoyin.socialsdk.social;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.limijiaoyin.socialsdk.Const;
import com.limijiaoyin.socialsdk.ISocialLogin;
import com.limijiaoyin.socialsdk.Platform;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

public class TencentSSOListener implements IUiListener {
  private String mAccessToken;
  private String mDeviceId;
  private ISocialLogin mLoginListener;

  public TencentSSOListener(String deviceId, ISocialLogin loginListener) {
    mDeviceId = deviceId;
    mLoginListener = loginListener;
  }

  @Override
  public void onCancel() {

  }

  @Override
  public void onComplete(JSONObject arg0) {
    try {
      mAccessToken = arg0.getString("access_token");
      if (!TextUtils.isEmpty(mAccessToken)) {
        new PostTencentSSOTokenThread().start();
        Log.d("qq", mAccessToken);
      } else {
        Log.d("qq", "accessToken is invalid");
      }
    } catch (JSONException e) {
      e.printStackTrace();
      Log.d("qq", "login response error");
    }

  }

  @Override
  public void onError(UiError arg0) {
    if (arg0 != null && !TextUtils.isEmpty(arg0.errorMessage)) {
      Log.d("qq", arg0.errorMessage);
    }
  }

  class PostTencentSSOTokenThread extends Thread {

    @Override
    public void run() {
      sendTencentSSOToken(mAccessToken);
    }
  }

  private void sendTencentSSOToken(String accessToken) {
    HttpPost request = new HttpPost(Const.ACCESS_TOKEN_URL);
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("deviceId", mDeviceId));
    params.add(new BasicNameValuePair("accessToken", accessToken));
    params.add(new BasicNameValuePair("platform", "qq"));

    try {
      HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
      request.setEntity(entity);
      DefaultHttpClient client = new DefaultHttpClient();
      HttpContext context = new BasicHttpContext();
      HttpResponse response = client.execute(request, context);
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        mLoginListener.onLoginSuccess(Platform.QQ);
      } else {}
    } catch (Exception e) {
      Log.d("qq", e.getMessage());
    }
  }

}
