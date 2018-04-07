package com.omycd.factory;

import com.omycd.domain.ProxyIp;
import com.omycd.enhancer.IpProxyPool;
import org.junit.Test;

import static org.junit.Assert.*;

public class IpProxyPoolFactoryTest {

    /**
     * 获取连接ip
     */
    @Test
    public void createIpProxyPool() throws InterruptedException {

        IpProxyPoolFactory ipProxyPoolFactory = new IpProxyPoolFactory();

        IpProxyPool ipProxyPool = ipProxyPoolFactory.createIpProxyPool();

        System.out.println(ipProxyPool);

       ProxyIp proxyIp = ipProxyPool.provideIpAndPort();

        System.out.println(proxyIp);

        Thread.sleep(1000000);

    }
}