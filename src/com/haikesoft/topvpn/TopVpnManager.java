package com.haikesoft.topvpn;


import android.widget.Button;
import com.haikesoft.topvpn.model.CallJson;
import com.topsec.sslvpn.IVPNHelper;
import com.topsec.sslvpn.OnAcceptResultListener;
import com.topsec.sslvpn.OnAcceptSysLogListener;
import com.topsec.sslvpn.datadef.BaseResourceInfo;
import com.topsec.sslvpn.datadef.ServiceAuthCfg;
import com.topsec.sslvpn.lib.VPNService;
import com.topsec.sslvpn.util.CrashHandler;
import com.topsec.sslvpn.util.Loger;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import android.content.Context;
import android.content.ContextWrapper;

import java.io.InvalidObjectException;

/**
 * VPN插件管理类(单例模式)
 *
 * @author sym
 * @data 20160810
 */
public class TopVpnManager
{
    private static final String TAG 		              = "TopVpnManager";

    //topvpn相关参数----------------------------------------------------------------------------------------------------
    //VPN服务核心类
    public static IVPNHelper         m_ihVPNService    = null;
    //VPN配置信息类
    public static ServiceAuthCfg     m_sacAuthCfgInfo  = null;
    //VPN服务端资源类
    public static BaseResourceInfo[] m_briArrayResInfo = null;
    //------------------------------------------------------------------------------------------------------------------
    //当前VPN管理的单例类
    private static TopVpnManager instance               = null;
    //当前app的上下文对象
    private        Context       mContext;

    /**
     * initVpnContext必须是第一个执行的方法，一般放到application事件中执行
     */
    public void initVpnContext(Context context)
    {
        this.mContext = context;
    }
    public static synchronized TopVpnManager getInstance()
    {
        if (instance == null)
        {
            instance = new TopVpnManager();
        }
        //
        return instance;
    }
    /**
     * 初始化VPN服务的全局唯一实例(这个必须最先执行，否则VPN服务不能建立)
     */
    public CallJson initVpnService()
    {
        CallJson json = new CallJson();
        //
        try
        {
            Loger.Initialize(this.mContext, null, 0,  2048, 81920, "yyyy-MM-dd HH:mm:ss", false);
            m_ihVPNService = VPNService.getVPNInstance(this.mContext.getApplicationContext());
            //
            json.setResult(true);
        }
        catch(Exception ex)
        {
            json.setResult(false);
            //
            Log.i(TAG, "从VPN服务中获取VPN全局实例异常,原因:" + ex.getLocalizedMessage());
        }
        //
        return json;
    }



}