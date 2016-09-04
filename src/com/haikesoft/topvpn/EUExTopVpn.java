package com.haikesoft.topvpn;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.*;
import com.haikesoft.topvpn.model.CallJson;
import com.haikesoft.topvpn.vo.AccountInfo;
import com.haikesoft.topvpn.vo.RSAInfo;
import com.haikesoft.topvpn.vo.VPNConfig;
import com.topsec.sslvpn.OnAcceptResultListener;
import com.topsec.sslvpn.OnAcceptSysLogListener;
import com.topsec.sslvpn.datadef.*;
import com.topsec.sslvpn.util.FeatureCodeHelper;
import com.topsec.sslvpn.util.Loger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserActivity;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.platform.push.PushBroadCastReceiver;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.view.View;

import javax.crypto.spec.DHGenParameterSpec;

import android.util.Log;
import com.haikesoft.topvpn.JsConst;
import com.haikesoft.topvpn.TopVpnManager;

import java.io.InvalidObjectException;

/**
 * 天融信VPN插件
 * @author sym
 * @data 20160810
 */
public class EUExTopVpn extends EUExBase
{
    //默认接入为端口转发模式
    public static int m_iWorkModule         = BaseModule.SSLVPN_PORTFORWARDING;
    //
    private static final String TAG 		   = "uexTopVpn";
    /**
     * VPN服务是否正常建立的标志
     */
    private static boolean vpnState  = false;


    /**
     * 1.之后执行
     * 构造函数
     * @param context
     * @param view
     */
    public EUExTopVpn(Context context, EBrowserView view)
    {
        super(context, view);
        //
        //需要先调registerActivityResult才能收到onActivityResult的回调
        //regActivityResult();
        //
        if(null != TopVpnManager.m_ihVPNService && BaseModule.SSLVPN_PORTFORWARDING == this.m_iWorkModule)
        {
            //TODO:通过全网接入建立隧道无需调用该函数，端口转发时必须设置
            int iRet=-1;
            iRet = TopVpnManager.m_ihVPNService.addProxyItem(view);
            Log.i(TAG, "端口转发时设置:AddProxyItem Ret:"+iRet);
        }
    }

    /**
     * 需要先调registerActivityResult才能收到onActivityResult的回调
     */
    public  void regActivityResult()
    {
        Log.d(TAG, "VPN执行registerActivityResult为以后onActivityResult做准备");
        //this.registerActivityResult();
        //
        EUExTopVpn.this.registerActivityResult();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, "VPN执行onActivityResult结果:requestCode" + requestCode + ", resultCode:" + resultCode);
        //
        // TODO:可以在resultCode返回成功调用(仅限全网接入)
        TopVpnManager.m_ihVPNService.toGrantStartVpnService(resultCode);
        //
        super.onActivityResult(requestCode, resultCode, data);
    }
    /**
     * 0.最先执行
     * app的application中的onCreate时执行本方法
     * @param context
     */
    public static void onApplicationCreate(Context context)
    {
        Log.d(TAG, "VPN执行onApplicationCreate准备实例化VPN实例");
        //
        TopVpnManager.getInstance().initVpnContext(context);
    }

    /**
     * 初始化VPN服务的全局单例，这个方法必须最先调用，否则VPN服务不能实例化
     * 本方法没有异步回调信息
     */
    public void init(String[] parm)
    {
        Log.e(TAG, "开始初始化VPN服务的全局单例...");
        //
        CallJson json = new CallJson();
        //
        try
        {
            //初始化SVN服务的全局惟一单例
            json = TopVpnManager.getInstance().initVpnService();
            if(json.isResult())
            {
                vpnState = true;
            }
            //
            if(vpnState)
            {
                initVPNServiceListener();
            }
            //
            Log.e(TAG, "初始化VPN服务的全局实例成功...");
        }
        catch(Exception ex)
        {
            json.setResult(false);
            //
            Log.e(TAG, "初始化VPN服务的全局实例失败,原因:" + ex.toString());
        }
        //
        callBackPluginJs(JsConst.ON_CALL_INIT, json.toString());
    }

    /**
     * 设置VPN认证服务器基础信息
     * 本方法有异步回调，回调入口在：MyOnAcceptResultListener
     * 回调1：OPERATION_GET_SERVERCFG（返回服务器配置信息）
     * 回调2:OPERATION_GET_CAPTCHA（获取当前操作的验证码）
     * 回调时获取服务器配置，如果有验证码，直接获取验证码
     * @param parm:传入json格式参数
     */
    public void setConfigInfo(String[] parm)
    {
        CallJson json = new CallJson();
        //
        if (parm.length < 1)
        {
            json.setMsg("传入参数为空，VPN服务器参数设置失败。");
        }
        else
        {
            json.setResult(true);
            //这里认为传过来的参数格式一定是正确的，不再重复校验
            VPNConfig vpn = DataHelper.gson.fromJson(parm[0], VPNConfig.class);
            //
            Log.e(TAG, "VPN认证:"+ vpn.getProtocol() + "" + vpn.getAddr() + ":" + vpn.getPort());
            //
            //设置VPN配置信息
            BaseConfigInfo bciInfo =new BaseConfigInfo();
            //LogLevel.LOG_LEVEL_DEBUG,LogLevel.LOG_LEVEL_NONE;
            bciInfo.m_iLogLevel= LogLevel.LOG_LEVEL_DEFAULT;//发布版本请把日志级别设置为LogLevel.LOG_LEVEL_NONE或不设置
            bciInfo.m_blAutoReConnect = true;
            bciInfo.m_iRetryCount = 10;
            bciInfo.m_iTimeOut = 5;     //单位为秒
            bciInfo.m_iEnableModule = BaseModule.SSLVPN_NETACCESS;//配置工作模块-全网接入等
            //bciInfo.m_iEnableModule = BaseModule.SSLVPN_PORTFORWARDING;//配置工作模块-端口转发
            //
            //这里可能有问题
            bciInfo.m_aMainActivity = (EBrowserActivity)mContext;//用于接收用户返回选择结果的Activity（目前为全网接入模块使用，其他忽略）

            bciInfo.m_strVPNIP = vpn.getAddr();
            bciInfo.m_iServerPort = vpn.getPort();
            bciInfo.m_iWorkMode= WorkModel.WORKMODE_DEFAULT;//默认参数 WorkModel.WORKMODE_DEFAULT
            TopVpnManager.m_ihVPNService.setConfigInfo(bciInfo);
            //
            m_iWorkModule= bciInfo.m_iEnableModule;//控制DemoBroswer页面AddProxyItem接口的调用（全网接入不需要调用）
        }
        //
        Log.e(TAG, "VPN服务器参数设置结束...");
        //
        callBackPluginJs(JsConst.ON_CALL_SETCONFIGINFO, json.toString());
    }


    /**
     * VPN用户登录
     * 本方法有异步回调，回调入口在：MyOnAcceptResultListener
     * 回调方法有两个，分别为验证成功或失败和登录成功或失败，如果验证失败会请求验证码
     * @param parm:1表示认证类型(用户名密码方式/证书方式/硬件方式)，2认证参数json格式
     */
    public void login(String[] parm)
    {
        CallJson json = new CallJson();
        //
        if(TopVpnManager.m_ihVPNService == null)
        {
            json.setMsg("VPN 实例已经初始化失败改操作无法执行！");
        }
        else
        {
            //入参有二个参数，1表示认证类型，2认证参数json格式
            if (parm.length < 1)
            {
                json.setMsg("传入参数为空，用户登录初始化失败。");
            }
            else
            {
                //认证方式,0:用户名和密码认证, 1：证书认证， 2：硬件认证
                int type = (int) Double.parseDouble(parm[0]);
                //
                boolean canLogin = false;
                BaseAccountInfo arg0 = new BaseAccountInfo();
                String strPackageName = mContext.getPackageName();
                //
                if(type == 0)
                {
                    canLogin = true;
                    //传入用户名和密码
                    AccountInfo acc = DataHelper.gson.fromJson(parm[1], AccountInfo.class);
                    //
                    Log.e(TAG, "VPN认证(用户名密码方式认证):name:"+ acc.getName() + ", pwd:" + acc.getPwd());
                    //
                    arg0.m_iLoginType = eLoginType.LOGIN_TYPE_CODEWORD.value();
                    //
                    arg0.m_strAccount        = acc.getName();    //用户名
                    arg0.m_strLoginPasswd   = acc.getPwd();      //密码
                    arg0.m_strCerPasswd     ="";
                    arg0.m_strCaptcha       ="";                //验证码
                }
                else if (type == 1)
                {
                    canLogin = true;
                    //国际(rsa)软证书方式：要求一个证书文件(p12格式)，和对应的证书密码
                    RSAInfo rsa = DataHelper.gson.fromJson(parm[1], RSAInfo.class);
                    Log.e(TAG, "VPN认证(软件证书认证):p12:"+ rsa.getP12() + ", pwd:" + rsa.getPwd());
                    //
                    arg0.m_iLoginType   = eLoginType.LOGIN_TYPE_CERT.value();
    				arg0.m_iAuthType    = eVertifyType.VERTIFY_TYPE_SOFTCERT.value();
                    //
                    arg0.m_strAccount        = "";
                    arg0.m_strLoginPasswd   = "";
                    arg0.m_strCerPasswd     = rsa.getPwd();     //证书密码
                    arg0.m_strCaptcha       = "";               //验证码
                }
                else if (type == 2)
                {
                    canLogin = true;
                    //国际(rsa)硬证书方式
                    RSAInfo rsa = DataHelper.gson.fromJson(parm[1], RSAInfo.class);
                    Log.e(TAG, "VPN认证(硬件证书认证):pwd:" +  rsa.getPwd());
                    //
                    arg0.m_iLoginType       = eLoginType.LOGIN_TYPE_CERT.value();
    				arg0.m_iAuthType        = eVertifyType.VERTIFY_TYPE_HARDCERT.value();
    				arg0.m_strPackageName  = strPackageName;
                    //
                    arg0.m_strAccount        = "";
                    arg0.m_strLoginPasswd   = "";
                    arg0.m_strCerPasswd     = rsa.getPwd();     //证书密码
                    arg0.m_strCaptcha       = "";               //验证码
                }
                else if(type == 3)
                {
                    canLogin = true;
                    //国密(sm)硬证书
                    RSAInfo rsa = DataHelper.gson.fromJson(parm[1], RSAInfo.class);
                    Log.e(TAG, "VPN认证(国密(sm)硬证书):pwd:" +  rsa.getPwd());
                    //
                    arg0.m_iLoginType       = eLoginType.LOGIN_TYPE_CERT.value();
    				arg0.m_iAuthType        = eVertifyType.VERTIFY_TYPE_HARDCERT.value();
    				arg0.m_iProtocolType    = eProtocolType.PROTOCOL_TYPE_CLOSE.value();
    				arg0.m_strPackageName   = strPackageName;
                    //
                    arg0.m_strAccount        = "";
                    arg0.m_strLoginPasswd   = "";
                    arg0.m_strCerPasswd     = rsa.getPwd();     //证书密码
                    arg0.m_strCaptcha       = "";               //验证码
                }
                else
                {
                    canLogin = false;
                    Log.e(TAG, "VPN认证:不支持的认证参数，请检查传入的认证方式参数。");
                }
                //
                if(canLogin)
                {
                    Log.e(TAG, "VPN认证开始执行...");
                    if(null == arg0.m_strPhoneFeatureCode)
                    {
                        arg0.m_strPhoneFeatureCode= FeatureCodeHelper.getPhoneFeatureCode(mContext.getApplicationContext());
                    }
                    TopVpnManager.m_ihVPNService.loginVOne(arg0);
                }
            }
        }
        Log.e(TAG, "VPN认证执行结束...");
        //
        callBackPluginJs(JsConst.ON_CALL_LOGINVONE, json.toString());
    }

    /**
     * 发请求:VPN用户退出登录
     * 本方法有异步回调，回调入口在：MyOnAcceptResultListener
     * 回调时请求验证码
     */
    public void logout(String[] parm)
    {
        CallJson json = new CallJson();
        //
        json.setResult(true);
        TopVpnManager.m_ihVPNService.logoutVOne();
        //
        Log.e(TAG, "VPN退出执行结束...");
        //
        callBackPluginJs(JsConst.ON_CALL_LOGOUTVONE, json.toString());
    }

    /**
     * 发请求:获取VPN资源
     * 本方法有异步回调，回调入口在：MyOnAcceptResultListener
     * 回调时获取当前用户可使用的资源列表
     */
    public void getResInfo(String[] parm)
    {
        CallJson json = new CallJson();

        json.setResult(true);
        TopVpnManager.m_ihVPNService.requestVPNResInfo();
        //同步回调
        callBackPluginJs(JsConst.ON_CALL_GETVPNRESINFO, json.toString());
    }

    /**
     * 发请求:启动VPN服务(成功后业务资源可用)
     * 本方法有异步回调，回调入口在：MyOnAcceptResultListener
     *
     */
    public void startService(String[] parm)
    {
        CallJson json = new CallJson();
        //
        json.setResult(true);
        //
        //需要先调registerActivityResult才能收到onActivityResult的回调
        regActivityResult();
        //
        if(BaseModule.SSLVPN_NETACCESS == m_iWorkModule)
        {
            Log.e(TAG, "全网接入启动VPN服务...");
            //全网接入
            //调用该函数启动全网接入服务(Activity) context;
            //第一个参数用于接收用户返回选择结果的Activity，用户需要在该类中实现onActivityResult函数
            TopVpnManager.m_ihVPNService.startService((EBrowserActivity)mContext, null);
        }
        else
        {
            Log.e(TAG, "端口转发启动VPN服务...");
            //端口转发
            TopVpnManager.m_ihVPNService.startService();
        }
        //同步回调
        callBackPluginJs(JsConst.ON_CALL_STARTSERVICE, json.toString());
    }

    /**
     * 发请求:关闭VPN服务(关闭后业务资源不可用)
     * 本方法有异步回调，回调入口在：MyOnAcceptResultListener
     */
    public void closeService(String[] parm)
    {
        CallJson json = new CallJson();
        //
        json.setResult(true);
        TopVpnManager.m_ihVPNService.closeService();
        //同步回调
        callBackPluginJs(JsConst.ON_CALL_CLOSESERVICE, json.toString());
    }

    /**
     * 发请求:查询VPN状态(异步)
     * 本方法有异步回调，回调入口在：MyOnAcceptResultListener
     */
    public void queryState(String[] parm)
    {
        CallJson json = new CallJson();
        //
        json.setResult(true);
        TopVpnManager.m_ihVPNService.queryVPNRunningState();
        //同步回调
        callBackPluginJs(JsConst.ON_CALL_QUERYSTATE, json.toString());
    }

    private void initVPNServiceListener()
    {
        Log.e(TAG, "VPN服务分别实现执行结果处理和日志处理监听...");
        //
        TopVpnManager.m_ihVPNService.setOnAcceptSysLogListener(new MyOnAcceptSysLogListener());
        TopVpnManager.m_ihVPNService.setOnAcceptResultListener(new MyOnAcceptResultListener());
    }
    /**
     * VPN日志回调
     */
    private class MyOnAcceptSysLogListener implements OnAcceptSysLogListener
    {
        @Override
        public void onAcceptSysLogInfo(int level,  String tag, String info)
        {
            Log.i(TAG, tag + ":" + info);
            //
            try
            {
                Loger.WriteLog(level, tag, info);
            }
            catch (InvalidObjectException e)
            {
                Log.i(TAG, "VPN服务记录日志时发生异常，异常信息为:");
                e.printStackTrace();
            }
        }
    }

    /**
     * VPN请求结果回调
     */
    private class  MyOnAcceptResultListener implements OnAcceptResultListener
    {
        @Override
        public void onAcceptExecResultListener(int iOperationID, int iRetValue, Object objExtralInfo, Object objReserved)
        {
            Log.i(TAG, "VPN基础库执行结果回调信息iOperationID:" + eOperateType.valueOf(iOperationID) + ", iRetValue:" + iRetValue);
            //
            String strNotice = null;
            //
            switch (eOperateType.valueOf(iOperationID))
            {
                case OPERATION_GET_RESOURCE:
                    {
                        CallJson json = new CallJson();
                        if(0 != iRetValue)
                        {
                            strNotice="获取资源数据失败，原因："+TopVpnManager.m_ihVPNService.getErrorInfoByCode(iRetValue);
                        }
                        else
                        {
                            TopVpnManager.m_briArrayResInfo=(BaseResourceInfo[])objExtralInfo;
                            if(null!=TopVpnManager.m_briArrayResInfo)
                            {
                                strNotice="获取资源数据成功，总共"+TopVpnManager.m_briArrayResInfo.length+"个";
                            }
                            else
                            {
                                strNotice="暂无可用资源数据";
                            }
                            json.setResult(true);
                            json.setCount(TopVpnManager.m_briArrayResInfo.length);
                        }
                        //
                        json.setMsg(strNotice);
                        callBackPluginJs(JsConst.CB_CALLBACK_GETVPNRESINFO, json.toString());
                    }
                    break;
                case OPERATION_GET_CAPTCHA:
                    {
                        CallJson json = new CallJson();
                        //2.当服务器启用验证码时，返回验证码信息
                        //Log.d(TAG, "VPN执行结果回调验证码:OPERATION_GET_CAPTCHA");
                        BaseCaptchaInfo bciTmp =(BaseCaptchaInfo)objExtralInfo;
                        Bitmap          btMap  = BitmapFactory.decodeByteArray(bciTmp.m_btData, 0,bciTmp.m_iLength);
                        //
                        if(null==btMap)
                        {
                            strNotice="验证码格式错误!";
                            break;
                        }
                        strNotice="获取验证码成功!";
                        //
                        //将验证码保存到sd卡上，然后返回保存后的文件名
                        Log.i(TAG, "验证码信息:Width:" + ((Bitmap) btMap).getWidth()+", Height:"+((Bitmap) btMap).getHeight());
                        //
                        json.setResult(true);
                        json.setMsg(strNotice);
                        callBackPluginJs(JsConst.CB_CALLBACK_GETCAPTCHA, json.toString());
                    }
                    break;
                case OPERATION_GET_SERVERCFG:
                    {
                        CallJson json = new CallJson();
                        //1.返回获取服务器配置状态(当配置启用验证码时，会接着回调验证码)
                        if(iRetValue==0)
                        {
                            if(null!=objExtralInfo)
                            {
                                TopVpnManager.m_sacAuthCfgInfo=(ServiceAuthCfg)objExtralInfo;
                                switch(TopVpnManager.m_sacAuthCfgInfo.m_ectCaptchaType)
                                {
                                    case GID_TYPE_OFF:
                                        strNotice="获取服务器配置成功，无验证码";
                                        break;
                                    case GID_TYPE_ON:
                                        strNotice="获取服务器配置成功，有验证码";
                                        break;
                                    case GID_TYPE_AUTO :
                                        strNotice="获取服务器配置成功，验证码自动启用";
                                        break;
                                }
                            }
                            else
                            {
                                strNotice="获取服务器配置成功!";
                            }
                            json.setResult(true);
                        }
                        else
                        {
                            strNotice="获取服务端配置失败，返回："+TopVpnManager.m_ihVPNService.getErrorInfoByCode(iRetValue);
                        }
                        json.setMsg(strNotice);
                        callBackPluginJs(JsConst.CB_CALLBACK_GETSERVERCFG, json.toString());
                    }
                    break;
                case OPERATION_AUTH_LOGININFO:
                    {
                        if(iRetValue==0)
                        {
                            strNotice="验证用户账户信息成功! ";
                        }
                        else
                        {
                            strNotice="验证失败，错误原因："+TopVpnManager.m_ihVPNService.getErrorInfoByCode(iRetValue);
                        }
                    }
                    break;
                case OPERATION_UPLOAD_FEATURECODE:
                    Log.d(TAG, "VPN执行结果回调:OPERATION_UPLOAD_FEATURECODE");
                    break;
                case OPERATION_LOGIN_SYSTEM:
                    {
                        CallJson json = new CallJson();
                        if(iRetValue==0)
                        {
                            json.setResult(true);
                            strNotice="成功登入VPN系统! ";
                        }
                        else
                        {
                            //登录失败时重新获取验证码
                            if(null!=TopVpnManager.m_sacAuthCfgInfo)
                            {
                                if(ServiceAuthCfg.eCaptchaType.GID_TYPE_OFF != TopVpnManager.m_sacAuthCfgInfo.m_ectCaptchaType)
                                {
                                    TopVpnManager.m_ihVPNService.requestCaptcha();
                                }
                            }
                            //
                            strNotice="登入VPN系统失败，返回：" + TopVpnManager.m_ihVPNService.getErrorInfoByCode(iRetValue);
                        }
                        json.setMsg(strNotice);
                        callBackPluginJs(JsConst.CB_CALLBACK_LOGINVONE, json.toString());
                    }
                    break;
                case OPERATION_START_PFPROXY:
                    Log.d(TAG, "VPN执行结果回调:OPERATION_START_PFPROXY");
                    break;
                case OPERATION_STOP_PFPROXY:
                    //目前版本可以不使用
                    Log.d(TAG, "VPN执行结果回调:OPERATION_STOP_PFPROXY");
                    break;
                case OPERATION_LOGOUT_SYSTEM:
                    {
                        CallJson json = new CallJson();
                        //退出成功后重新请求验证码，准备下次登录(VPN服务仍在)
                        if(iRetValue==0)
                        {
                            json.setResult(true);
                            //
                            strNotice="成功退出VPN系统! ";
                            if(null!=TopVpnManager.m_sacAuthCfgInfo)
                            {
                                if(ServiceAuthCfg.eCaptchaType.GID_TYPE_OFF!=TopVpnManager.m_sacAuthCfgInfo.m_ectCaptchaType)
                                {
                                    TopVpnManager.m_ihVPNService.requestCaptcha();
                                }
                            }
                            else
                            {
                                TopVpnManager.m_ihVPNService.requestCaptcha();
                            }
                        }
                        else
                        {
                            strNotice="退出VPN系统失败，原因："+TopVpnManager.m_ihVPNService.getErrorInfoByCode(iRetValue);
                        }
                        json.setMsg(strNotice);
                        callBackPluginJs(JsConst.CB_CALLBACK_LOGOUTVONE, json.toString());
                    }
                    break;
                case OPERATION_GET_KEEPSTATUS:
                    {
                        strNotice="当前VPN状态：";
                        strNotice+=VPNStaus.IsUserLoggedin(iRetValue)?"用户已成功登入VPN系统":"用户尚未登入VPN系统";
                        strNotice+="，";
                        strNotice+=VPNStaus.IsVPNServiceRunning(iRetValue)?"VPN服务正在运行":"VPN服务尚未启动";
                        strNotice+=("(当前系统返回："+iRetValue+")");
                        //
                        CallJson json = new CallJson();
                        json.setResult(true);
                        json.setMsg(strNotice);
                        callBackPluginJs(JsConst.CB_CALLBACK_QUERYSTATE, json.toString());
                    }
                    break;
                case OPERATION_START_SERVICE:
                    {
                        CallJson json = new CallJson();
                        //
                        if(iRetValue==0)
                        {
                            json.setResult(true);
                            //
                            strNotice="启动VPN服务成功! ";
                            if(BaseModule.SSLVPN_PORTFORWARDING == m_iWorkModule)
                            {
                                String strExData = TopVpnManager.m_ihVPNService.getExchangeDataFromMode(WorkModel.WORKMODE_MASTER);
                                Log.i(TAG, "端口转发成功后参数:" + strExData);
                            }
                        }
                        else
                        {
                            strNotice="启动VPN服务失败，原因：" + TopVpnManager.m_ihVPNService.getErrorInfoByCode(iRetValue);
                        }
                        //
                        json.setMsg(strNotice);
                        callBackPluginJs(JsConst.CB_CALLBACK_STARTSERVICE, json.toString());
                    }
                    break;
                case OPERATION_STOP_SERVICE:
                    Log.d(TAG, "VPN执行结果回调:OPERATION_STOP_SERVICE");
                    break;
                case OPERATION_CLOSE_SERVICE:
                    {
                        CallJson json = new CallJson();
                        if(iRetValue==0)
                        {
                            json.setResult(true);
                            //
                            strNotice="VPN服务已成功关闭! ";
                        }
                        else
                        {
                            strNotice="关闭VPN失败，原因："+TopVpnManager.m_ihVPNService.getErrorInfoByCode(iRetValue);
                        }
                        //
                        json.setMsg(strNotice);
                        callBackPluginJs(JsConst.CB_CALLBACK_CLOSESERVICE, json.toString());
                    }
                    break;
                case OPERATION_CHECK_NETSTATUS:
                    Log.d(TAG, "VPN执行结果回调:OPERATION_CHECK_NETSTATUS");
                    break;
                case OPERATION_PREPARE_NETACCESS:
                    Log.d(TAG, "VPN执行结果回调:OPERATION_PREPARE_NETACCESS");
                    break;
                case OPERATION_START_NETACCESS:
                    Log.d(TAG, "VPN执行结果回调:OPERATION_START_NETACCESS");
                    break;
                case OPERATION_STOP_NETACCESS:
                    Log.d(TAG, "VPN执行结果回调:OPERATION_STOP_NETACCESS");
                    break;
                case OPERATION_NETCONFIG_NETACCESS:
                    Log.d(TAG, "VPN执行结果回调:OPERATION_NETCONFIG_NETACCESS");
                    break;
                default:
                {
                    CallJson json = new CallJson();
                    //
                    if(iRetValue==0)
                    {
                        json.setResult(true);
                        //
                        strNotice="执行操作"+iOperationID+"成功完成! "+objReserved;
                    }
                    else
                    {
                        strNotice="执行操作"+iOperationID+"失败，返回："+TopVpnManager.m_ihVPNService.getErrorInfoByCode(iRetValue);
                    }
                    //
                    json.setMsg(strNotice);
                    callBackPluginJs(JsConst.CB_CALLBACK_OTHER, json.toString());
                }
                break;
            }
            //
            Log.d(TAG, "VPN执行结果回调结果:" + strNotice);
        }
    }


    /**
     * 回调指定的JS方法
     * @param methodName:js方法名
     * @param jsonData:传递的json参数
     */
    private void callBackPluginJs(String methodName, String jsonData)
    {
        String js = SCRIPT_HEADER + "if(" + methodName + "){" + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }

    @Override
    protected boolean clean()
    {
        return true;
    }


}
