package com.haikesoft.topvpn.vo;

import java.io.Serializable;

/**
 * 实体类：证书认证的用户信息(暂未使用)
 *
 * @author sym
 * @data 20160812
 */
public class RSAInfo implements Serializable
{
    private static final long      serialVersionUID = -3925738954223902510L;
    //
    /**
     * .p12证书文件名(只包含文件名，有默认值，要求该文件必须在sdcard的指定目录下)
     * 相当于加密后的用户名
     */
    private              String    p12              = "sdk.p12";
    /**
     * .p12证书的密码
     */
    private              String    pwd              = "";
    //
    public String getP12()
    {
        return p12;
    }

    public void setP12(String p12)
    {
        this.p12 = p12;
    }

    public String getPwd()
    {
        return pwd;
    }

    public void setPwd(String pwd)
    {
        this.pwd = pwd;
    }
}
