package com.omycd.domain;

public class ProxyIp {
    //ID代号
    private String id ;
    //IP地址
    private String ip;
    //端口号
    private String port;
    //协议类型:http.https
    private String protocol;
    //协议等级Level
    private String level;
    //使用状态,1:可使用，0:不可使用
    private String status;

    public ProxyIp() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
