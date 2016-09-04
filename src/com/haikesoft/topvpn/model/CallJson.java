package com.haikesoft.topvpn.model;

/**
 * js前台调用的操作结果
 *
 * @author sym
 * @data 20160811
 */
public class CallJson
{


    /**
     * 处理成功成功或失败的标识
     */
    private boolean result = false;
    /**
     * 计数变量
     */
    private int    count = 0;
    /**
     * 失败时的原因
     */
    private String msg   = "";
    //
    public CallJson(boolean result, String msg)
    {
        this.result = result;
        this.msg = msg;
    }

    public CallJson(String msg)
    {
        this.result = false;
        this.msg = msg;
    }
    public CallJson()
    {
        this.result = false;
    }

    @Override
    public String toString()
    {
        return "{\"result\":"+ this.isResult() + ", \"count\":\""+ this.getCount() +"\"" +", \"msg\":\""+ this.getMsg() +"\"}";
    }

    public boolean isResult()
    {
        return result;
    }

    public void setResult(boolean result)
    {
        this.result = result;
    }
    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }
    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }
}
