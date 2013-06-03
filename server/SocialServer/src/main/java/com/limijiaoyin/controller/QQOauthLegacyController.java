package com.limijiaoyin.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.security.auth.login.AccountException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.limijiaoyin.dto.Platform;
import com.limijiaoyin.dto.SocialBean;
import com.limijiaoyin.facade.SocialFacade;
import com.qq.connect.QQConnectException;
import com.qq.connect.api.OpenID;
import com.qq.connect.api.qzone.UserInfo;
import com.qq.connect.javabeans.AccessToken;
import com.qq.connect.oauth.Oauth;

@Controller
public class QQOauthLegacyController {

    @Autowired
    private SocialFacade socialFacade;

    private static final Logger LOG = LoggerFactory
            .getLogger(QQOauthLegacyController.class);

    @RequestMapping("/web/qq/authorize")
    public void auth(
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "callback", required = false) String callback,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, QQConnectException {
        if (deviceId != null) {
            request.getSession().setAttribute("deviceId", deviceId);
        }

        request.getSession().setAttribute("callback", callback);

        String url = new Oauth().getAuthorizeURL(request);
        response.sendRedirect(url);
    }

    @RequestMapping("/web/qq/token")
    public void token(HttpServletRequest request, HttpServletResponse response)
            throws IOException, QQConnectException, AccountException,
            ServletException {

        Oauth oauth = new Oauth();
        AccessToken accessTokenObj = oauth.getAccessTokenByRequest(request);

        if (StringUtils.isEmpty(accessTokenObj.getAccessToken())) {
            processCode(request, response, accessTokenObj);
        } 
    }

    private void processCode(HttpServletRequest request,
            HttpServletResponse response, AccessToken accessTokenObj)
            throws QQConnectException, UnsupportedEncodingException,
            IOException, AccountException {
        String accessToken = accessTokenObj.getAccessToken();
        String openID = new OpenID(accessToken).getUserOpenID();

        UserInfo qzoneUserInfo = new UserInfo(accessToken, openID);

        String deviceId = (String) request.getAttribute("deviceId");

        SocialBean socialBean = new SocialBean();
        socialBean.setAccessToken(accessToken);
        socialBean.setBindTime(new Date());
        socialBean.setData(qzoneUserInfo.toString());
        socialBean.setFromUid(openID);
        socialBean.setPlatform(Platform.QQ);
        socialBean.setDeviceId(deviceId);

        socialFacade.save(socialBean);

        String callback = (String) request.getSession()
                .getAttribute("callback");
        if (!StringUtils.isEmpty(callback)) {
            response.sendRedirect(callback);
        }
        LOG.info("bind qq success");
    }
}
