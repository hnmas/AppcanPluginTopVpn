package com.haikesoft.topvpn.vo;

import java.io.Serializable;

/**
 * 实体类：用户名密码认证的用户信息(默认认证方式)
 *
 * @author sym
 * @data 20160812
 */
public class AccountInfo implements Serializable
{
    private static final long   serialVersionUID = 7628994007795436932L;
    //
    /**
     * vpn认证登录的用户名
     */
    private              String name             = "";
    /**
     * vpn认证登录的密码
     */
    private              String pwd              = "";
    /**
     * vpn认证登录的验证码
     */
    private              String code             = "";

    //
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPwd()
    {
        return pwd;
    }

    public void setPwd(String pwd)
    {
        this.pwd = pwd;
    }
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }
}
