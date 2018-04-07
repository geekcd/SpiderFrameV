package com.omycd.enhancer.impl;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.omycd.domain.ProxyIp;
import com.omycd.enhancer.IpProxyPool;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * @author  chendong
 * @vesion 1.0
 */
public class DefaultIpProxyPool implements IpProxyPool {

    //使用c3p0连接池建立连接，DBUTils进行连接
    private ComboPooledDataSource ds = null;

    private QueryRunner queryRunner =null;

    @Override
    public  void initPool() {
        ds = new ComboPooledDataSource();
        queryRunner =new QueryRunner(ds);
        //执行策略
        System.out.println("11");
        //读取配置文件内的策略配置

        //如果没能读取到配置，则启用默认配置
        if(ds!=null) {
            defaultIpHandlePolicy();
        }
    }

    @Override
    public ProxyIp provideIpAndPort() {
        ProxyIp proxyIp = null;
        try {
            proxyIp = queryRunner.query("select * from proxyip where status like '1%'  limit  1", new BeanHandler<ProxyIp>(ProxyIp.class));
        }catch (Exception e) {
            throw new RuntimeException("get Exception");
           }
        if(proxyIp==null){
            System.out.println("等待ip连接池更新可用的ip");
            //启动更新
            updatePool();
            //完成后再次调用自己
            provideIpAndPort();

        }
        return proxyIp;
    }

    @Override
    public void updatePool() {
        System.out.println("-->         IP代理池资源不足，开始启动更新       <--");
        List<ProxyIp> effectiveData =new ArrayList<>();
        int enoughTime=1;
        //当有效数据小于100个时候，则循环爬取有效数据
        while(effectiveData.size()<2) {
            //开启下一次下载
            System.out.println("第"+enoughTime+"次下载");
            List<ProxyIp> ipList = parseWebsiteProxyIp(enoughTime++);
            for (ProxyIp proxyIp : ipList) {
                //如果连接经过测试为有效
                if(proxyIpConnectionTest(proxyIp)){
                    //设置状态为可用，并添加到有效数据集合中
                    proxyIp.setStatus("1");
                    effectiveData.add(proxyIp);
                }
            }
        }
        for (ProxyIp proxyIp : effectiveData) {
            String sql = "insert into proxyip values (null,?,?,?,?,?)";
            try {
                ProxyIp proxyIp1 = queryRunner.query("select * from proxyip where ip =? ",new BeanHandler<ProxyIp>(ProxyIp.class),proxyIp.getIp());
                //如果找不到这元素才存入数据库
                if (proxyIp1==null) {
                    queryRunner.update(sql, proxyIp.getIp(), proxyIp.getPort(), proxyIp.getProtocol(), proxyIp.getLevel(), proxyIp.getStatus());
                }
            }catch (Exception e){
                throw  new RuntimeException("出入数据异常");
            }

        }
        //调节满足后，则开始存入数据到数据库
    }


    @Override
    public void callbackToOutmode(ProxyIp proxyIp) {
        try {
            queryRunner.update("update  proxyip set status  = 0 where ip = ?", proxyIp.getIp());
        }catch (Exception e ){
            throw  new RuntimeException("callback to outmode meet a Exception");
        }

    }


    /**
     *默认的更新策略 ，10 分钟一次检查数据库内部的IP有效数量，如果数量小于50 则启动更新操作
     *
     */
    public void defaultIpHandlePolicy(){
        //开启定时器
        Timer defaultConfigTimer = new Timer();
        //设定间隔时间
        long regularTime = 1000*60*15L;
        defaultConfigTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //查询数据库里的IP信息
                List<ProxyIp>  ipList = null;
                try {
                    ipList =  queryRunner.query("select  *  from  proxyip  where  status like '%1'   ", new BeanListHandler<ProxyIp>(ProxyIp.class));
                }catch (Exception e){
                    e.printStackTrace();
                    throw  new RuntimeException("[erro]"+this.getClass().getName()+" at initpool()  query Exception");
                }
                if(ipList.size()<10){
                    //更新数据库
                    updatePool();
                    System.out.println("122");
                }
            }
        }, new Date(), regularTime);

    }







    /**
     * 解析网站上的代理服务器IP地址，这个是网站1
     * @return
     */
    public List<ProxyIp> parseWebsiteProxyIp(int enoughTime) {
        List<ProxyIp> list = new ArrayList<>();
        //获取西刺的网址，进行解析
        Document document = null;
        try {
            //获取网页文件
            System.out.println("1");
            document = Jsoup.connect("http://www.xicidaili.com/nn/"+enoughTime).get();
            Elements tbody = document.select("tbody");
            System.out.println("2");
            for (Element element : tbody) {
                Elements children = element.children();
                for (Element child : children) {
                    //获取IP和port端口，以及等级协议
                  String ip = child.child(1).text();
                  String port=child.child(2).text();
                  String level = child.select("td[class=country]").text();
                  String  protocol  =child.child(5).text();
                  //排除干扰数据
                  if(!"端口".equalsIgnoreCase(port)) {
                        //封装数据
                        ProxyIp proxyIp = new ProxyIp();
                        proxyIp.setIp(ip);
                        proxyIp.setPort(port);
                        proxyIp.setLevel(level);
                        proxyIp.setProtocol(protocol);
                        //存入集合中
                        list.add(proxyIp);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("");
        }
        return list;

    }


    /**
     * ip代理网址的解析方式不同，这里提供了两个网站，这个是网站2
     * @param enoughTime
     * @return
     */
  public List<ProxyIp> parseWebsiteProxyIp2(int enoughTime) {
        List<ProxyIp> list = new ArrayList<>();
        //获取西刺的网址，进行解析
        Document document = null;
        try {
            //获取网页文件
            document = Jsoup.connect("https://www.kuaidaili.com/free/inha/"+enoughTime).get();
            Elements tbody = document.select("tbody");
            for (Element element : tbody) {
                Elements children = element.children();
                for (Element child : children) {
                    //获取IP和port端口，以及等级协议
                  String ip = child.child(0).text();
                  String port=child.child(1).text();
                  String level = child.child(2).text();
                  String  protocol  =child.child(3).text();
                    System.out.println(ip+port+level+protocol);
                  //排除干扰数据
                  if(!"端口".equalsIgnoreCase(port)) {
                        //封装数据
                        ProxyIp proxyIp = new ProxyIp();
                        proxyIp.setIp(ip);
                        proxyIp.setPort(port);
                        proxyIp.setLevel(level);
                        proxyIp.setProtocol(protocol);
                        //存入集合中
                        list.add(proxyIp);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("");
        }
        return list;

    }


    /**
     * 测试网址的可用性
     * @param proxyIp
     * @return
     */
    public boolean proxyIpConnectionTest(ProxyIp proxyIp){

        try {
            //URL连接百度
            URL url = new URL("https://www.baidu.com");
            //设置代理测试
            SocketAddress addr = new InetSocketAddress(proxyIp.getIp(), Integer.parseInt(proxyIp.getPort()));

            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
            //开始连接
            URLConnection conn = url.openConnection(proxy);
            //设置1秒超时
            System.out.println(proxyIp.getIp()+" 开始连接。。。");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1500);
            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/59.0");
            conn.setRequestProperty("Accept", "  text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "zh-cn,zh;q=0.5");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.connect();
            int connectTimeout = conn.getConnectTimeout();

            System.out.println("connection Timeout:"+connectTimeout+"-->> effective proxyIp:"+proxyIp.getIp());
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return  false;
        } catch (IOException e) {
            System.out.println(proxyIp.getIp()+"Connetion Timeout ");
            return  false;
        }

    }



}
