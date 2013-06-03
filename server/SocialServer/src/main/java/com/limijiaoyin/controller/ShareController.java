package com.limijiaoyin.controller;import java.io.IOException;import java.net.URLEncoder;import java.util.Date;import java.util.List;import javax.servlet.http.HttpServletRequest;import javax.servlet.http.HttpServletResponse;import org.apache.commons.httpclient.HttpStatus;import org.json.simple.JSONObject;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Controller;import org.springframework.web.bind.annotation.RequestMapping;import org.springframework.web.bind.annotation.RequestParam;import weibo4j.Timeline;import weibo4j.http.ImageItem;import weibo4j.model.Status;import weibo4j.model.WeiboException;import com.limijiaoyin.dto.Platform;import com.limijiaoyin.dto.SocialBean;import com.limijiaoyin.facade.SocialFacade;import com.limijiaoyin.utils.FileUtils;import com.qq.connect.QQConnectException;import com.renren.api.client.RenrenApiClient;import com.renren.api.client.param.impl.AccessToken;@Controller@RequestMapping("v1")public class ShareController {    private static final Logger LOG = LoggerFactory            .getLogger(ShareController.class);        private static final String IMAGE_PATH= "/home/huwei/share/baiduss.png";    @Autowired    SocialFacade socialFacade;    @RequestMapping("/share")    public void share(@RequestParam(value = "platform") String platform,            @RequestParam(value = "content") String content,            @RequestParam(value = "deviceId") String deviceId,            HttpServletRequest request, HttpServletResponse response)            throws IOException {        String[] platformList = platform.split(",");        boolean shareSuccess = true;        String unbindPlatform = "";        LOG.info("share to " + platform);        byte[] imageContent = FileUtils.readFileImage(IMAGE_PATH);        for (String plat: platformList) {            if (plat.equals("sina")) {                SocialBean socialBean = socialFacade                        .findSocialBeanByDeviceIdAndPlatform(deviceId,                                Platform.SINA);                if (socialBean != null) {                    String accessToken = socialBean.getAccessToken();                    Timeline tm = new Timeline();                    tm.client.setToken(accessToken);                    try {                        ImageItem pic = new ImageItem("pic", imageContent);                        Status status = tm.UploadStatus(URLEncoder.encode(content,"utf-8"),pic);                        if (status.getId() != null) {                            LOG.info("send weibo success");                        } else {                            LOG.info("user " + deviceId + " share sina failure ");                        }                    } catch (WeiboException e) {                        shareSuccess = false;                        int error = e.getErrorCode();                        LOG.info("user " + deviceId + " share sina failure "                                + e.getError());                        switch (error) {                        // accesstoken has expired                            case 20003:                            case 21314:                            case 21315:                            case 21316:                            case 21317:                            case 21330:                            case 21319:                            case 21327:                            case 21332:                            case 20034:                                unbind(Platform.SINA, deviceId);                                response.setStatus(HttpStatus.SC_BAD_REQUEST);                                unbindPlatform = unbindPlatform + "sina,";                                break;                            default:                                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);                        }                    }                }            } else if (plat.equals("tqq")) {                SocialBean socialBean = socialFacade                        .findSocialBeanByDeviceIdAndPlatform(deviceId,                                Platform.QQ);                if (socialBean != null) {                    String accessToken = socialBean.getAccessToken();                    String openId;                    try {                        com.qq.connect.api.OpenID qqOpenId = new com.qq.connect.api.OpenID(                                accessToken);                        openId = qqOpenId.getUserOpenID();                        com.qq.connect.api.weibo.Weibo weibo = new com.qq.connect.api.weibo.Weibo(                                accessToken, openId);//                        weibo.addWeibo(content);                        weibo.addPicWeibo(content, imageContent);                    } catch (QQConnectException e) {                        LOG.info("user " + deviceId + " share tqq failure "                                + e.getMessage());                    }                }            } else if (plat.equals("qzone")) {                SocialBean socialBean = socialFacade                        .findSocialBeanByDeviceIdAndPlatform(deviceId,                                Platform.QQ);                if (socialBean != null) {                    String accessToken = socialBean.getAccessToken();                    String openId;                    com.qq.connect.api.OpenID qqOpenId = new com.qq.connect.api.OpenID(                            accessToken);                    try {                        openId = qqOpenId.getUserOpenID();                        com.qq.connect.api.qzone.Share qqShare = new com.qq.connect.api.qzone.Share(                                accessToken, openId);                        String comment = "comment:" + content;                        qqShare.addShare("寻找搜神",                                "http://as.baidu.com/a/item?docid=3199169",                                "http://sou.baidu.com", "http://sou.baidu.com",                                comment, "nswb:1","images:http://social.limijiaoyin.com/share/baiduss.png");                    } catch (QQConnectException e) {                        LOG.info("user " + deviceId + " share qzone failure "                                + e.getMessage());                    }                }            } else if (plat.equals("renren")) {                SocialBean socialBean = socialFacade                        .findSocialBeanByDeviceIdAndPlatform(deviceId,                                Platform.RENREN);                if (socialBean != null) {                    JSONObject result = RenrenApiClient                            .getInstance()                            .getFeedService()                            .publicFeed(                                    "寻找搜神",//                                    "在互联网高速发展的今天，搜索成为了一种生活方式。而搜商也渐渐成为考量人们互联网生活品质的工具。搜商即通过搜索，获取知识，解决问题的能力。你想与万千搜商达人成为朋友吗？你想提高自己的搜商吗？你想成为搜商超高的搜神吗？快下载APP寻找搜神，与万千搜商达人一决高下。",                                    content,                                    "http://as.baidu.com/a/item?docid=3199169",                                    "",                                    "",                                    "",                                    "http://as.baidu.com/a/item?docid=3199169",                                    "分享",                                    new AccessToken(socialBean.getAccessToken()));                    LOG.info(result.toString());                    // int result = RenrenApiClient                    // .getInstance()                    // .getStatusService()                    // .setStatus(                    // content,                    // new AccessToken(socialBean.getAccessToken()));                    // if (result != 1) {                    // LOG.info("user " + deviceId + " share renren failure "                    // + result);                    // shareSuccess = false;                    // switch (result) {                    // case 2001:                    // case 202:                    // unbind(Platform.SINA, deviceId);                    // unbindPlatform = unbindPlatform + "renren,";                    // response.setStatus(HttpStatus.SC_BAD_REQUEST);                    // break;                    // default:                    // response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);                    // }                    // }                }            }        }        response.setStatus(HttpStatus.SC_OK);        if (!shareSuccess) {            response.getWriter().write(unbindPlatform);        } else {            response.setStatus(HttpStatus.SC_OK);        }    }    @RequestMapping("/profile")    public void bindPlatform(@RequestParam(value = "deviceId") String deviceId,            HttpServletRequest request, HttpServletResponse response)            throws IOException {        List<SocialBean> socialBeanList = socialFacade                .findSocialByDeviceId(deviceId);        if (socialBeanList.isEmpty()) {            response.getWriter().write("");        } else {            String platform = "";            for (SocialBean socialBean: socialBeanList) {                if (socialBean.getPlatform().equals(Platform.SINA)) {                    platform = platform + "sina,";                } else if (socialBean.getPlatform().equals(Platform.QQ)) {                    platform = platform + "qq,";                } else if (socialBean.getPlatform().equals(Platform.RENREN)) {                    platform = platform + "renren,";                }            }            response.getWriter().write(platform);        }    }    private void unbind(Platform platform, String deviceId) {        SocialBean socialBean = socialFacade                .findSocialBeanByDeviceIdAndPlatform(deviceId, platform);        socialFacade.deleteSocial(socialBean.getId());    }    @RequestMapping("/v1/social/accessToken")    public void v1SocialClientAction(@RequestParam("deviceId") String deviceId,            @RequestParam(value = "accessToken") String accessToken,            @RequestParam(value = "platform") String platform,            HttpServletRequest request, HttpServletResponse response)            throws WeiboException, IOException {        if (platform.equals("sina")) {            SocialBean socialBean = socialFacade                    .findSocialBeanByDeviceIdAndPlatform(deviceId,                            Platform.SINA);            if (socialBean != null) {                saveAccount(accessToken, socialBean, Platform.SINA);                LOG.info("sina has bind");            } else {                bindAccount(accessToken, deviceId, Platform.SINA);            }        } else if (platform.equals("qq")) {            SocialBean socialBean = socialFacade                    .findSocialBeanByDeviceIdAndPlatform(deviceId, Platform.QQ);            if (socialBean != null) {                saveAccount(accessToken, socialBean, Platform.QQ);                LOG.info("qq has bind");            } else {                bindAccount(accessToken, deviceId, Platform.QQ);            }        }    }    @RequestMapping("/social/accessToken")    public void socialClientAction(@RequestParam("deviceId") String deviceId,            @RequestParam(value = "accessToken") String accessToken,            @RequestParam(value = "platform") String platform,            HttpServletRequest request, HttpServletResponse response)            throws WeiboException, IOException {        if (platform.equals("sina")) {            SocialBean socialBean = socialFacade                    .findSocialBeanByDeviceIdAndPlatform(deviceId,                            Platform.SINA);            if (socialBean != null) {                saveAccount(accessToken, socialBean, Platform.SINA);                LOG.info("sina has bind");            } else {                bindAccount(accessToken, deviceId, Platform.SINA);            }        } else if (platform.equals("qq")) {            SocialBean socialBean = socialFacade                    .findSocialBeanByDeviceIdAndPlatform(deviceId, Platform.QQ);            if (socialBean != null) {                saveAccount(accessToken, socialBean, Platform.QQ);                LOG.info("qq has bind");            } else {                bindAccount(accessToken, deviceId, Platform.QQ);            }        }    }    private void bindAccount(String accessToken, String deviceId,            Platform platform) throws WeiboException {        // String fromUid = getUidFromSina(accessToken);        // User user = getUserInfo(accessToken, fromUid);        SocialBean socialBean = new SocialBean();        socialBean.setAccessToken(accessToken);        socialBean.setBindTime(new Date());        // socialBean.setData(user.toString());        // socialBean.setFromUid(fromUid);        socialBean.setPlatform(platform);        socialBean.setDeviceId(deviceId);        socialFacade.save(socialBean);    }    private void saveAccount(String accessToken, SocialBean socialBean,            Platform platform) {        socialBean.setAccessToken(accessToken);        socialBean.setBindTime(new Date());        socialBean.setPlatform(platform);        socialFacade.update(socialBean);    }    public static void main(String[] args) throws IOException {                JSONObject result = RenrenApiClient                .getInstance()                .getFeedService()                .publicFeed(                        "寻找搜神",//                        "在互联网高速发展的今天，搜索成为了一种生活方式。而搜商也渐渐成为考量人们互联网生活品质的工具。搜商即通过搜索，获取知识，解决问题的能力。你想与万千搜商达人成为朋友吗？你想提高自己的搜商吗？你想成为搜商超高的搜神吗？快下载APP寻找搜神，与万千搜商达人一决高下。",                        "分享",                        "http://as.baidu.com/a/item?docid=3199169",                        "",                        "",                        "",                        "http://as.baidu.com/a/item?docid=3199169",                        "如今xxxxxxx",                        new AccessToken("235614|6.fc215abb02486fb1349ca22f5315361a.2592000.1372345200-530113648"));                System.out.println(result);//        byte[] imageContent = FileUtils.readFileImage(IMAGE_PATH);//        String accessToken = "2.00bnueJCqLZKgE80f10966b4r1Br8C";//        Timeline tm = new Timeline();//        tm.client.setToken(accessToken);//        try {//            ImageItem pic = new ImageItem("pic", imageContent);//            Status status = tm.UploadStatus(URLEncoder.encode("中文tetere","utf-8"),pic);//            if (status.getId() != null) {//                LOG.info("send weibo success");//            } else {////                LOG.info("user " + deviceId + " share sina failure ");//            }//        } catch (WeiboException e) {//        }    }}