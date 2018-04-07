package com.omycd.test;

import com.omycd.domain.ProxyIp;
import com.omycd.enhancer.impl.DefaultIpProxyPool;

public class DefaultIpProxyPoolTest {

    public static void main(String[] args) {

        initPool();
    }


    public  static void initPool(){
          DefaultIpProxyPool defaultIpProxyPool = new DefaultIpProxyPool();
        defaultIpProxyPool.initPool();
         /*   ProxyIp proxyIp = new ProxyIp();
            proxyIp.setIp("220.162.75.49");
            proxyIp.setPort("4560");
          defaultIpProxyPool.proxyIpConnectionTest(proxyIp);*/
    }

}
