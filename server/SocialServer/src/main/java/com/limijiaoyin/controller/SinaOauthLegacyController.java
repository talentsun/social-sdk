package com.limijiaoyin.controller;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import weibo4j.Oauth;
import weibo4j.http.AccessToken;
import weibo4j.model.User;
import weibo4j.model.WeiboException;
import weibo4j.org.json.JSONObject;

import com.limijiaoyin.dto.Platform;
import com.limijiaoyin.dto.SocialBean;
import com.limijiaoyin.facade.SocialFacade;

@Controller
public class SinaOauthLegacyController {

    private static final Logger LOG = LoggerFactory
            .getLogger(SinaOauthLegacyController.class);

    @Autowired
    private SocialFacade socialFacade;

    @RequestMapping("/web/sina/token")
    public void token(
            @RequestParam(value = "code") String code,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request, HttpServletResponse response)
            throws WeiboException, IOException {
        processCode(code, callback, request, response);
        response.sendRedirect("/bind/success");
    }

    @RequestMapping("/web/sina/authorize")
    public void auth(@RequestParam("deviceId") String deviceId,
            HttpServletRequest request, HttpServletResponse response)
            throws WeiboException, IOException {
        request.getSession().setAttribute("deviceId", deviceId);
        doSinaAuthorize(request, response);
    }

    @RequestMapping("/v1/sina/sso")
    public void sinaSSOAction(@RequestParam("deviceId") String deviceId,
            @RequestParam(value = "accessToken") String accessToken,
            HttpServletRequest request, HttpServletResponse response)
            throws WeiboException, IOException {

        SocialBean socialBean = socialFacade
                .findSocialBeanByDeviceIdAndPlatform(deviceId, Platform.SINA);
        if (socialBean != null) {
            saveAccount(accessToken, socialBean,Platform.SINA);
            LOG.info("sina has bind");
        } else {
            bindAccount(accessToken, deviceId, Platform.SINA);
        }
    }


    private void saveAccount(String accessToken, SocialBean socialBean,Platform platform) {
        socialBean.setAccessToken(accessToken);
        socialBean.setBindTime(new Date());
        socialBean.setPlatform(platform);
        socialFacade.update(socialBean);
    }

    private void doSinaAuthorize(HttpServletRequest request,
            HttpServletResponse response) throws IOException, WeiboException {
        response.sendRedirect(new Oauth().authorize("code", "mobile"));
    }

    private void bindAccount(String accessToken, String deviceId,
            Platform platform) throws WeiboException {
      
        SocialBean socialBean = new SocialBean();
        socialBean.setAccessToken(accessToken);
        socialBean.setBindTime(new Date());
        String fromUid = getUidFromSina(accessToken);
        if(fromUid != null){
            User user = getUserInfo(accessToken, fromUid);
            socialBean.setData(user.toString());
            socialBean.setFromUid(fromUid);
        }
        socialBean.setPlatform(platform);
        socialBean.setDeviceId(deviceId);

        socialFacade.save(socialBean);
    }

    private void processCode(String code, String callback,
            HttpServletRequest request, HttpServletResponse response)
            throws WeiboException, IOException {

        Oauth oauth = new Oauth();
        AccessToken accessTokenObj = oauth.getAccessTokenByCode(code);
        if (accessTokenObj != null) {
            String fromUid = accessTokenObj.getUid();
            String accessToken = accessTokenObj.getAccessToken();
            User user = getUserInfo(accessToken, accessTokenObj.getUid());

            String deviceId = (String) request.getSession().getAttribute(
                    "deviceId");

            SocialBean socialBean = new SocialBean();
            socialBean.setAccessToken(accessToken);
            socialBean.setBindTime(new Date());
            socialBean.setData(user.toString());
            socialBean.setFromUid(fromUid);
            socialBean.setPlatform(Platform.SINA);
            socialBean.setDeviceId(deviceId);

            socialFacade.save(socialBean);
        }
    }

    private String getUidFromSina(String accessToken) {
        weibo4j.Account am = new weibo4j.Account();
        am.client.setToken(accessToken);
        try {
            JSONObject uid = am.getUid();
            return uid.getString("uid");
        } catch (Exception e) {
            LOG.info(e.getMessage());
            return null;
        }
    }

    private User getUserInfo(String accessToken, String uid)
            throws WeiboException {
        weibo4j.Users um = new weibo4j.Users();
        um.client.setToken(accessToken);
        User user = um.showUserById(uid);
        return user;
    }
}
