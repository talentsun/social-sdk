package com.limijiaoyin.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.AccountException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;

import com.limijiaoyin.dto.Platform;
import com.limijiaoyin.dto.SocialBean;
import com.limijiaoyin.facade.SocialFacade;
import com.limijiaoyin.httpclient.WebHttpsClient;
import com.renren.api.client.RenrenApiConfig;

@Controller
public class RenrenOauthLegacyController {

    @Autowired
    private SocialFacade socialFacade;

    private static final String RENREN_REDIRECT_URL = "http://%s/web/renren/token";

    private static final Logger LOG = LoggerFactory
            .getLogger(RenrenOauthLegacyController.class);

    @RequestMapping("/web/renren/authorize")
    public void auth(
            @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "deviceId") String deviceId,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (callback != null) {
            request.getSession().setAttribute("callback", callback);
        }
        
        request.getSession().setAttribute("deviceId", deviceId);

        String oauthUrl = "https://graph.renren.com/oauth/authorize?client_id="
                + RenrenApiConfig.renrenApiKey
                + "&redirect_uri="
                + URLEncoder.encode(
                        String.format(RENREN_REDIRECT_URL,
                                request.getHeader("Server-Name")), "utf-8")
                + "&response_type=code&scope=read_user_album+read_user_feed+status_update+publish_feed";

        response.sendRedirect(oauthUrl);
    }

    @RequestMapping("/web/renren/token")
    public void token(
            @RequestParam(value = "code", required = false) String code,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, AccountException, ServletException {

        String callback = (String) request.getSession()
                .getAttribute("callback");
        
        String deviceId = (String) request.getSession().getAttribute("deviceId");

        if (StringUtils.isEmpty(code)) {
            request.getRequestDispatcher(
                    "/web/failure").forward(
                    request, response);
            return;
        }

        String authUrl = "https://graph.renren.com/oauth/token?grant_type=authorization_code&client_id="
                + RenrenApiConfig.renrenApiKey
                + "&redirect_uri="
                + URLEncoder.encode(
                        String.format(RENREN_REDIRECT_URL,
                                request.getHeader("Server-Name")), "utf-8")
                + "&client_secret="
                + RenrenApiConfig.renrenApiSecret
                + "&code=" + code;
        HttpClient httpClient = null;
        try {
            httpClient = WebHttpsClient.getInstance();
            String url;
            url = new URI(authUrl).toASCIIString();

            HttpGet authGet = new HttpGet(url);

            String encoding = new String(
                    Base64.encodeBase64((RenrenApiConfig.renrenApiKey + ":" + RenrenApiConfig.renrenApiSecret)
                            .getBytes()));

            authGet.setHeader("Authorization", "Basic " + encoding);
            authGet.setHeader("Content-Type",
                    "application/x-www-form-urlencoded");

            HttpResponse authResponse = httpClient.execute(authGet);
            if (authResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(authResponse.getEntity());
                String key = null;
                if (!StringUtils.isEmpty(result)) {
                    processCode(deviceId, callback, result, key);
                    response.sendRedirect("/bind/success");
                    return;
                }
            } else {
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                LOG.warn("get renren token exception "
                        + EntityUtils.toString(authResponse.getEntity()));
            }
        } catch (KeyManagementException e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOG.warn("get renren token exception " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOG.warn("get renren token exception " + e.getMessage());
        } catch (URISyntaxException e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOG.warn("get renren token exception " + e.getMessage());
        } catch (ParseException e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOG.warn("get renren token exception " + e.getMessage());
        } catch (IOException e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOG.warn("get renren token exception " + e.getMessage());
        } catch (JSONException e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOG.warn("get renren token exception " + e.getMessage());
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().closeIdleConnections(0,
                        TimeUnit.MILLISECONDS);
            }
        }

    }

    private void processCode(String deviceId, String callback, String result,
            String key) throws JSONException, UnsupportedEncodingException,
            IOException {
        JSONObject jsonObj = new JSONObject(result);
        String accessToken = jsonObj.getString("access_token");
        SocialBean socialBean = socialFacade
                .findSocialBeanByDeviceIdAndPlatform(deviceId, Platform.RENREN);
        if (socialBean != null) {
            socialBean.setBindTime(new Date());
            socialBean.setPlatform(Platform.RENREN);
            socialBean.setAccessToken(accessToken);
            socialFacade.update(socialBean);
            return;
        } else {
            JSONObject userObj = jsonObj.getJSONObject("user");
            int fromUid = userObj.getInt("id");

            socialBean = new SocialBean();
            socialBean.setAccessToken(accessToken);
            socialBean.setBindTime(new Date());
            socialBean.setData(userObj.toString());
            socialBean.setFromUid(String.valueOf(fromUid));
            socialBean.setPlatform(Platform.RENREN);
            socialBean.setDeviceId(deviceId);
        }

        socialFacade.save(socialBean);
    }
}
