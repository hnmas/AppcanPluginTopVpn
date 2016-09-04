package com.haikesoft.topvpn.vo;

import java.io.Serializable;

/**
 * 实体类：VPN服务器的配置信息
 * @author sym
 * @data 20160812
*/
public class VPNConfig implements Serializable
{
    private static final long   serialVersionUID = 8182247805485663069L;
    //
    /**
     * VPN服务器IP地址
     */
    private              String addr             = "";
    /**
     * VPN的承载协议，目前只支持HTTPS，只读属性
     */
    private              String protocol         = "https://";
    /**
     * 默认端口为443
     */
    private              int    port             = 443;
    //
    public String getAddr()
    {
        return addr;
    }

    public void setAddr(String addr)
    {
        this.addr = addr;
    }
    public String getProtocol()
    {
        return protocol;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

}
