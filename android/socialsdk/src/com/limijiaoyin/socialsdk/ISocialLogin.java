package com.limijiaoyin.socialsdk;

public interface ISocialLogin {
  public void onLoginSuccess(Platform platform);

  public void onLoginFailure();
}
