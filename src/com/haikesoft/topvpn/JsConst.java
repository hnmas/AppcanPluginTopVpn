package com.haikesoft.topvpn;

/**
 * 插件的回调方法定义
 * on类定义 ：表示app请求插件方法后，直接返回的回调(表示当前方法直接执行后的返回结果，不异常的话都会成功)
 * cb类定义 ：表示vpn功能功能异步处理后，异步产生的回调(表示当前方法直接执行后，隔一段时间，服务器主动发起的回调结果)
 */
public class JsConst
{
    public static final String ON_CALL_INIT          = "uexTopVpn.onInit";
    //public static final String CB_CALLBACK_INIT             = "uexTopVpn.cbInit";
    /**
     * 设置VPN参数回调，包括：工作模式, 认证模式(用户名密码认证)
     */
    public static final String ON_CALL_SETCONFIGINFO = "uexTopVpn.onSetConfigInfo";
    //public static final String CB_CALLBACK_SETCONFIGINFO = "uexTopVpn.cbSetConfigInfo";

    /**
     * 获取服务器配置
     */
    public static final String CB_CALLBACK_GETSERVERCFG = "uexTopVpn.cbGetService";
    /**
     * 生成验证码异步回调
     */
    public static final String CB_CALLBACK_GETCAPTCHA    = "uexTopVpn.cbGetCaptcha";
    /**
     * 登录到vpn服务器(用户名密码，证书，硬件)
     */
    public static final String ON_CALL_LOGINVONE         = "uexTopVpn.onLogin";
    public static final String CB_CALLBACK_LOGINVONE     = "uexTopVpn.cbLogin";
    /**
     * 退出vpn登录
     */
    public static final String ON_CALL_LOGOUTVONE        = "uexTopVpn.onLogout";
    public static final String CB_CALLBACK_LOGOUTVONE    = "uexTopVpn.cbLogout";
    /**
     * 获取当前VPN状态
     */
    public static final String ON_CALL_QUERYSTATE        = "uexTopVpn.onQueryState";
    public static final String CB_CALLBACK_QUERYSTATE    = "uexTopVpn.cbQueryState";
    /**
     * 获取vpn资源信息
     */
    public static final String ON_CALL_GETVPNRESINFO     = "uexTopVpn.onGetResInfo";
    public static final String CB_CALLBACK_GETVPNRESINFO = "uexTopVpn.cbGetResInfo";
    /**
     * 启动vpn后台服务
     */
    public static final String ON_CALL_STARTSERVICE      = "uexTopVpn.onStartService";
    public static final String CB_CALLBACK_STARTSERVICE  = "uexTopVpn.cbStartService";
    /**
     * 关闭vpn后台服务
     */
    public static final String ON_CALL_CLOSESERVICE       = "uexTopVpn.onCloseService";
    public static final String CB_CALLBACK_CLOSESERVICE  = "uexTopVpn.cbCloseService";

    /**
     * 未处理的回调
     */
    public static final String CB_CALLBACK_OTHER            = "uexTopVpn.cbOther";

    //------------------------------------------------------------------------------------------------------------------
    //VPN的方法异步处理主动发起的异步回调
}