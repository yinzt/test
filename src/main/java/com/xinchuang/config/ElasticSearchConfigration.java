package com.xinchuang.config;

import java.util.ArrayList;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.fastjson.JSONObject;
import com.xinchuang.common.utils.TikaUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ElasticSearchConfigration {
	
	@Autowired
	private SerarchDownloadProperties serarchDownloadProperties;
	
	
	@Value("${spring.elasticsearch.address}")
	private String address;

	/**
	 * 连接超时时间
	 */
	@Value("${spring.elasticsearch.connect-timeout}")
	private int connectTimeOut = 1000;

	/**
	 * 连接超时时间
	 */
	@Value("${spring.elasticsearch.socket-timeout}")
	private int socketTimeOut = 30000;

	/**
	 * 获取连接的超时时间
	 */
	@Value("${spring.elasticsearch.connection-request-timeout}")
	private int connectionRequestTimeOut = 500;

	/**
	 * 最大连接数
	 */
	@Value("${spring.elasticsearch.max-connect-num}")
	private int maxConnectNum = 100;

	/**
	 * 最大路由连接数
	 */
	@Value("${spring.elasticsearch.max-connect-per-route}")
	private int maxConnectPerRoute = 100;

	@Bean
	RestHighLevelClient restHighLevelClient() {
		ArrayList<HttpHost> hostList = new ArrayList<>();
		//String[] addrss = address.split(",");
		
		String configStr=serarchDownloadProperties.getConfigPath();
		String configPath=TikaUtils.getContext(configStr);
	    JSONObject json = JSONObject.parseObject(configPath);
	    String esearch_ip=(String) json.get("ESearch_ip");
	    String[] addrss = esearch_ip.split(",");
		for (String addr : addrss) {
			String[] arr = addr.split(":");
			hostList.add(new HttpHost(arr[1].replace("//", ""), Integer.parseInt(arr[2]), arr[0]));
		}

		RestClientBuilder builder = RestClient.builder(hostList.toArray(new HttpHost[0]));
		// 异步httpclient连接延时配置
		builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
			@Override
			public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
				requestConfigBuilder.setConnectTimeout(connectTimeOut);
				requestConfigBuilder.setSocketTimeout(socketTimeOut);
				requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
				return requestConfigBuilder;
			}
		});
		// 异步httpclient连接数配置
		builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
			@Override
			public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
				httpClientBuilder.setMaxConnTotal(maxConnectNum);
				httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
				return httpClientBuilder;
			}
		});

		RestHighLevelClient client = new RestHighLevelClient(builder);
		return client;
	}

	/*
	 * @Value("${spring.elasticsearch.host}") private String esHost;
	 * 
	 * @Value("${spring.elasticsearch.port}") private int esPort;
	 * 
	 * @Value("${spring.elasticsearch.clusterName}") private String esClusterName;
	 * 
	 * private TransportClient client;
	 * 
	 * @PostConstruct public void initialize() throws Exception {
	 * System.setProperty("es.set.netty.runtime.available.processors", "false");
	 * Settings esSettings = Settings.builder() .put("cluster.name", esClusterName)
	 * .put("client.transport.sniff", true).build(); client = new
	 * PreBuiltTransportClient(esSettings);
	 * 
	 * String[] esHosts = esHost.trim().split(","); for (String host : esHosts) {
	 * client.addTransportAddress(new TransportAddress(InetAddress.getByName(host),
	 * esPort)); } }
	 * 
	 * @Bean public Client client() { return client; }
	 * 
	 * 
	 * @Bean public ElasticsearchTemplate elasticsearchTemplate() throws Exception {
	 * return new ElasticsearchTemplate(client); }
	 * 
	 * // //避免TransportClient每次使用创建和释放 // public Client esTemplate() { // if(
	 * StringUtils.isEmpty(client) || StringUtils.isEmpty(client.admin())) { //
	 * client = client(); // return client; // } // return client; // }
	 * 
	 * 
	 * @PreDestroy public void destroy() { if (client != null) { client.close(); } }
	 */

}
