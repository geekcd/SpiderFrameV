package com.omycd.factory;

import com.omycd.enhancer.IpProxyPool;
import com.omycd.enhancer.impl.DefaultIpProxyPool;

public class IpProxyPoolFactory {

    private IpProxyPool ipProxyPool;

     public  IpProxyPool createIpProxyPool(){

         DefaultIpProxyPool defaultIpProxyPool = new DefaultIpProxyPool();
         //对象创建好后，立即开始加载初始化方法
         defaultIpProxyPool.initPool();

         return defaultIpProxyPool;
     }

}
