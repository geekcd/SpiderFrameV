package com.omycd.enhancer;


import com.omycd.domain.ProxyIp;

/**
 *
 * Ip 代理池，从免费公开的IP代理服务器网站上爬取IP到本地列表中，
 * 然后通过验证和去重，保留有效的IP。
 * 所以步骤：
 * 1.获取IP代理爬取网址，选取西刺网站作为IP耕取地
 * 2.jsoup直接获取document文件
 * 3.然后解析数据，保存在内存中
 * 4.验证数据是否有效，通过访问百度，查看是否能获取连接。
 * 5.把有效的数保存到数据库中
 *
 * 数据获取后，需要为调用者提供一个IP获取的接口
 *
 * @author  chendong
 * @version  1.0
 * @date 2018.3
 */
public interface IpProxyPool {

    /**
     * 1-1.初始化后,需要加载数据进入内存，为了网络分布式的使用，IP数据需要存储在数据库中，
     * 为了适应不同的数据库，这里采用原始的数据库连接方式，不采用任何数据库连接工具。
     *
     * 注意：
     * 通过读取配置文件，选定数据库的连接，数据库的配置文件名应该为：
     * 这里采用c3p0进行配置，不同的实现类组件选择不用的数据源
     *
     * 这个方法是用来，连接数据库，开启定时爬虫功能，爬取定时策略应该由配置文件指出，如果
     * 配置文件中没有指定，则采用默认规则，“10分钟读取一次，如果IP池里面的数据小于50条，则
     * 再次开启爬取，爬取目标是200条”
     */
    public   void initPool();

    /**
     * 2-1提供IP和Port端口数据，
     */
    public ProxyIp provideIpAndPort();

    /**
     * 3-1更新池内数据,根据初始化时的策略进行执行
     */
    public void updatePool();

    /**
     * 4-1.设置回调函数使当前使用的ip设置为过时状态,当无法通过当前的IP获取
     */
    public void  callbackToOutmode(ProxyIp proxyIp);



}
