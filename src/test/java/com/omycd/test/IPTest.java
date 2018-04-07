package com.omycd.test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.swing.text.html.HTML;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.omycd.domain.ProxyIp;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.jsoup.Jsoup;



    public class IPTest {
        /**
         * 动态IP代理测试，生效，测试地址返回的ip为如下所示
         * @param args
         */

	/*<body>
	  <div align="center">
	   CDN节点iP:[1.27.51.46]
	  </div>
	  <a href="http://user.ip138.com/ip/" target="_blank">iP查询接口</a> |
	  <a href="http://user.ip138.com/ip/lib/" target="_blank"><font color="red">离线iP数据库</font></a>
	 </body>
	</html>
	*/

        /**
         * 有效的ip:
         * 218.73.239.226:61202
         * 180.115.245.22:61202
         * @param args
         */


        public static void main(String[] args) throws SQLException {

           // String html = getHtml("https://zhuanlan.zhihu.com/p/25285987");
           /* String html1  = getHtml("http://user.ip138.com/ip/");
            System.out.println(html1);*/
            QueryRunner queryRunner =new QueryRunner(new ComboPooledDataSource());
            List<ProxyIp> list= queryRunner.query("select * from proxyip where status like '%1' ",new BeanListHandler<ProxyIp>(ProxyIp.class));
            for (ProxyIp proxyIp : list) {
                proxyIpConnectionTest(proxyIp);
            }


        }

        /**
         * 获得页面信息
         * @param address
         * @return
         */
        private static String getHtml(String address){
            java.lang.String ipString = "218.73.239.226:61202";
            java.lang.String ip = ipString.substring(0,ipString.indexOf(":"));
            java.lang.String port = ipString.substring(ipString.indexOf(":")+1);
            System.out.println(ip+"---"+port);
            SocketAddress addr = new InetSocketAddress(ip, Integer.parseInt(port));
            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
            System.out.println(proxy);
            StringBuffer html = new StringBuffer();
            String result = null;
            try{
                URL url = new URL(address);
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/59.0");
                conn.setRequestProperty("Accept", "  text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                conn.setRequestProperty("Accept-Language", "zh-cn,zh;q=0.5");
                conn.setReadTimeout(1000*4);
                // conn.setRequestProperty();
                conn.setRequestProperty("Connection", "keep-alive");

                Map<String,List<String>> headerMap = conn.getHeaderFields();
                for(Map.Entry<String, List<String>> entry : headerMap.entrySet()){
                    String key = entry.getKey();
                    List<String> values = entry.getValue();
                    StringBuilder sb = new StringBuilder();
                    int size=values==null?0:values.size();
                    for(int i=0;i<size;i++){
                        if(i>0){
                            sb.append(",");
                        }
                        sb.append(values.get(i));
                    }
                    System.out.println(key+":"+sb.toString());
                }


                BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                try{
                    String inputLine;
                    byte[] buf = new byte[4096];
                    int bytesRead = 0;
                    while (bytesRead >= 0) {
                        inputLine = new String(buf, 0, bytesRead, "utf-8");
                        html.append(inputLine);
                        bytesRead = in.read(buf);
                        inputLine = null;
                    }
                    buf = null;
                }finally{
                    in.close();
                    conn = null;
                    url = null;
                }
                org.jsoup.nodes.Document re = Jsoup.parse(html.toString());
                System.out.println(re.html());

            }catch (Exception e) {
                e.printStackTrace();
                return null;
            }finally{
                html = null;
            }
            return result;
        }



        public static  boolean proxyIpConnectionTest(ProxyIp proxyIp){

            try {
                //URL连接百度
                URL url = new URL("http://fuliba.net/?qqdrsign=0671a");
                //设置代理测试
                SocketAddress addr = new InetSocketAddress(proxyIp.getIp(), Integer.parseInt(proxyIp.getPort()));
                System.out.println(proxyIp.getIp()+"======"+proxyIp.getPort());

                Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
                //开始连接
                URLConnection conn = url.openConnection();
                //设置1秒超时
                System.out.println(proxyIp.getIp()+" 开始连接。。。");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2500);
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




