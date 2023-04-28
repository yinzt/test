package com.xinchuang.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiTermQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SpanMultiTermQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.xinchuang.common.constant.EsConstants;
import com.xinchuang.common.constant.HttpStatus;
import com.xinchuang.common.utils.HighlightUtils;
import com.xinchuang.common.utils.IDUtil;
import com.xinchuang.common.utils.PinYinUtil;
import com.xinchuang.common.utils.TikaUtils;
import com.xinchuang.config.SerarchDownloadProperties;
import com.xinchuang.core.page.TableDataInfo;
import com.xinchuang.entity.DocumentSpace;
import com.xinchuang.entity.DocumentSpaceEs;
import com.xinchuang.entity.TFullQueryResult;
import com.xinchuang.entity.TSearchVo;
import com.xinchuang.service.DocumentSpaceEsService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("elasticsearchService")
public class DocumentSpaceEsServiceImpl implements DocumentSpaceEsService {

	@Autowired
	private RestHighLevelClient client;

	@Autowired
	private SerarchDownloadProperties serarchDownloadProperties;

	@PostConstruct
	public void init() {
		System.setProperty("es.set.netty.runtime.available.processors", "false");

		try {
			GetIndexRequest request = new GetIndexRequest(EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE);
			boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
			if (!exists) {
				CreateIndexRequest createRequest = new CreateIndexRequest(EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE);
				CreateIndexResponse response = client.indices().create(createRequest, RequestOptions.DEFAULT);
				log.info(JSON.toJSONString(response));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void deleteEnterpriseSpaceIndex() {

		try {
			GetIndexRequest request = new GetIndexRequest(EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE);
			boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
			if (exists) {
				DeleteIndexRequest deleteRequest = new DeleteIndexRequest(EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE);
				AcknowledgedResponse response = client.indices().delete(deleteRequest, RequestOptions.DEFAULT);
				//System.out.println(JSON.toJSONString(response));
				log.info("删除索引请求：" + JSON.toJSONString(response));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * 
	 * @param data
	 * @param searchHit
	 * @return
	 */
	public static TFullQueryResult setEnterpriseSpaceEs(TFullQueryResult data, SearchHit searchHit, Integer rule) {
		data.setFid(String.valueOf(searchHit.getSourceAsMap().get("fid")));
		data.setId(searchHit.getId());
		data.setSky(String.valueOf(searchHit.getSourceAsMap().get("sky")));
		data.setType(Integer.parseInt(String.valueOf(searchHit.getSourceAsMap().get("type"))));
		data.setFtype(Integer.parseInt(String.valueOf(searchHit.getSourceAsMap().get("ftype"))));
		data.setFtime(Integer.parseInt(String.valueOf(searchHit.getSourceAsMap().get("ftime"))));
		data.setFsize(Integer.parseInt(String.valueOf(searchHit.getSourceAsMap().get("fsize"))));
		data.setVer(Integer.parseInt(String.valueOf(searchHit.getSourceAsMap().get("ver"))));
		data.setIndexid(String.valueOf(searchHit.getSourceAsMap().get("indexid")));
		data.setUid(String.valueOf(searchHit.getSourceAsMap().get("uid")));
		if (rule == EsConstants.RULE_1) {// 精确
			data.setFname(String.valueOf(searchHit.getSourceAsMap().get("fnameStr")));
			String ftStr = String.valueOf(searchHit.getSourceAsMap().get("fullTextStr") == null ? ""
					: searchHit.getSourceAsMap().get("fullTextStr"));
			data.setFullText(ftStr);
		} else {// 模糊
			data.setFname(String.valueOf(searchHit.getSourceAsMap().get("fname")));
			String ft = String.valueOf(searchHit.getSourceAsMap().get("fullText") == null ? ""
					: searchHit.getSourceAsMap().get("fullText"));
			data.setFullText(ft);
		}
		data.setUsername(String.valueOf(searchHit.getSourceAsMap().get("username")));
		data.setFpath(String.valueOf(searchHit.getSourceAsMap().get("fpath")));

		return data;
	}

	/**
	 * 查询条件处理
	 * 
	 * @param condition
	 * @return
	 */
	private BoolQueryBuilder buildGeneralSearchQuery(Map<String, Object> condition) {
		BoolQueryBuilder boolQueryBuilder = boolQuery();
		if (condition.containsKey("rule") && !Objects.isNull(condition.get("rule"))) {
			String rule = condition.get("rule").toString();
			if ("1".equals(rule)) {//精确 不分词
				if (condition.containsKey("fname") && !Objects.isNull(condition.get("fname"))) {// 开始时间
					String fname = condition.get("fname").toString();
					String[] keys = fname.split("\\+");
					for (String _key : keys) {
						if(_key.matches("[0-9]+") || _key.matches("[a-zA-Z]+")){
						    //纯数字或纯字母
							boolQueryBuilder.must(QueryBuilders.wildcardQuery("fname","*"+_key.trim()+"*"));
						}else {
							boolQueryBuilder.should(QueryBuilders.termQuery("fname", _key));
						}
						
					}
				}
			} else {//模糊
				if (condition.containsKey("fname") && !Objects.isNull(condition.get("fname"))) {// 开始时间
					String fname = condition.get("fname").toString();
					BoolQueryBuilder queryBuilder = boolQuery();
					String[] keys = fname.split("\\+");
					for (String _key : keys) {
						if(_key.matches("[0-9]+") || _key.matches("[a-zA-Z]+")){
						    //纯数字或纯字母
						    queryBuilder.must(QueryBuilders.wildcardQuery("fname","*"+_key.trim()+"*"));
						}else {
							queryBuilder.must(QueryBuilders.matchPhraseQuery("fname",_key.trim()).boost(100).slop(0));
						}
						
					}
					boolQueryBuilder.must(queryBuilder);
				}
			}
		}
		if (condition.containsKey("key") && !Objects.isNull(condition.get("key"))) {
			String keyword = condition.get("key").toString();
			String[] keys = keyword.split("\\+");
			BoolQueryBuilder queryBuilder = boolQuery();
			for (String _key : keys) {
				if(_key.matches("[0-9]+") || _key.matches("[a-zA-Z]+")){
				    //纯数字或纯字母
				    queryBuilder.must(QueryBuilders.wildcardQuery("fullText","*"+_key.trim()+"*"));
				}else {
					queryBuilder.must(QueryBuilders.matchPhraseQuery("fullText",_key.trim()).boost(100).slop(0));
				}
				
			}
			boolQueryBuilder.must(queryBuilder);
		}
		if (condition.containsKey("timestart") && !Objects.isNull(condition.get("timestart"))) {// 开始时间
			int timestart = (int) condition.get("timestart");
			boolQueryBuilder.must(QueryBuilders.rangeQuery("ftime").gte(timestart));
		}

		if (condition.containsKey("timeend") && !Objects.isNull(condition.get("timeend"))) {// 结束时间
			int timeend = (int) condition.get("timeend");
			boolQueryBuilder.must(QueryBuilders.rangeQuery("ftime").lte(timeend));
		}

		if (condition.containsKey("fsizestart") && !Objects.isNull(condition.get("fsizestart"))) {// 文件大小开始
			int fsizestart = (int) condition.get("fsizestart");
			boolQueryBuilder.must(QueryBuilders.rangeQuery("fsize").gte(fsizestart));
		}

		if (condition.containsKey("fsizeend") && !Objects.isNull(condition.get("fsizeend"))) {// 文件大小结束
			int fsizeend = (int) condition.get("fsizeend");
			boolQueryBuilder.must(QueryBuilders.rangeQuery("fsize").lte(fsizeend));
		}
		
		if (condition.containsKey("ftype") && !Objects.isNull(condition.get("ftype"))) {// 文件类型
			int ftype = (int) condition.get("ftype");
			boolQueryBuilder.filter(QueryBuilders.termQuery("ftype", ftype));
		}
		
		if (condition.containsKey("indexid") && !Objects.isNull(condition.get("indexid"))) {// 文件大小结束
			String indexid = condition.get("indexid").toString();
			boolQueryBuilder.filter(QueryBuilders.termQuery("indexid", indexid));
		}
		
		if (condition.containsKey("fpath") && !Objects.isNull(condition.get("fpath"))) {// 文件路径
			String fpath = condition.get("fpath").toString();
			boolQueryBuilder.must(QueryBuilders.wildcardQuery("fpath", fpath+"*"));
		}
		
		if (condition.containsKey("username") && !Objects.isNull(condition.get("username"))) {// 创建者/修改者
			String username = condition.get("username").toString();
			boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("username",username.trim()).boost(100).slop(0));
		}
		return boolQueryBuilder;
	}

	public static boolean saveUrlAs(String fileUrl, String filePath) {
		// 此方法只能用户HTTP协议
		try {
			log.info("下载文件地址:"+fileUrl);
			File file=new File(filePath); 
			if (!file .exists() ) {
				log.info("创建目录:"+filePath);
				file.getParentFile().mkdir();
			}
			URL url = new URL(fileUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			DataInputStream in = new DataInputStream(connection.getInputStream());
			DataOutputStream out = new DataOutputStream(new FileOutputStream(filePath));
			byte[] buffer = new byte[4096];
			int count = 0;
			while ((count = in.read(buffer)) > 0) {
				out.write(buffer, 0, count);
			}
			out.close();
			in.close();
			return true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public Object insertTestDocumentSpaceEs(DocumentSpaceEs bean) {
		TableDataInfo rspData = new TableDataInfo();
		try {
			String configStr=serarchDownloadProperties.getConfigPath();
			String configPath=TikaUtils.getContext(configStr);
			log.info("zyc----配置参数："+configPath);
			JSONObject json = JSONObject.parseObject(configPath);
			String downPath=json.getString("cache_dir");
			String ocrUrl=json.getString("ocr_url");
			String filePath = downPath+bean.getFname();
			bean.setId(bean.getId());
			int type=bean.getFtype();  //86  图片读取文字

			String wordStr=TikaUtils.parsePicture(filePath,ocrUrl);
			bean.setFullText(wordStr);

			//bean.setFullTextStr(wordStr);
			bean.setInitials(PinYinUtil.getFullSpell(bean.getFname()));
			//删除当前文件
			File file = new File(filePath);
			file.delete();
			IndexRequest request = new IndexRequest(EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE);
			request.id(bean.getId());
			log.error("zyc-新增数据", bean);
			request.source(JSON.toJSONString(bean), XContentType.JSON);
			//request.setRefreshPolicy(RefreshPolicy.WAIT_UNTIL);
			IndexResponse response = client.index(request, RequestOptions.DEFAULT);
			response.setForcedRefresh(true);
			log.info("fileId: {} 插入zyc-----es数据库成功", JSON.toJSONString(response));
			rspData.setCode(HttpStatus.SUCCESS);
			rspData.setMsg("zyc-新增数据成功");
			return rspData;

		} catch (Exception e) {
			log.error("fileId: {}新增数据入es库异常", bean);
			log.error(e.getMessage(), e);
			rspData.setCode(HttpStatus.ERROR);
			rspData.setMsg("新增数据失败");
			return rspData;
		}
	}

	@Override
	public Object insertDocumentSpaceEs(DocumentSpaceEs bean) {
		TableDataInfo rspData = new TableDataInfo();
		try {
			bean.setId(bean.getId());
			String configStr=serarchDownloadProperties.getConfigPath();
			String configPath=TikaUtils.getContext(configStr);
			log.info("配置参数："+configPath);
		    JSONObject json = JSONObject.parseObject(configPath);
		    String downPath=json.getString("cache_dir");
		    String ocrUrl=json.getString("ocr_url");
			String filePath = downPath+bean.getFname();
			String downloadUrl=json.getString("manage_ip");
			StringBuffer sb = new StringBuffer();
			sb.append(downloadUrl+"core/index.php?");
			sb.append("mod=searchdownload");
			sb.append("&fd=" + bean.getFid());
			sb.append("&ver=" + bean.getVer());
			sb.append("&uid=" + bean.getUid());
			sb.append("&sky=" +bean.getSky());
			log.info("下载链接", sb.toString());
			List<Integer> fileFormat=new ArrayList<Integer>();
			fileFormat.add(81);
			fileFormat.add(82);
			fileFormat.add(83);
			fileFormat.add(84);
			fileFormat.add(85);
			int type=bean.getFtype();
			log.info("下载文件类型Ftype:[{}]", type);
			for (int i=21;i<30;i++){
				fileFormat.add(i);
			}
			//ftype是20-30的就是图片
			if(!fileFormat.contains(type)) {
				 rspData.setCode(HttpStatus.ERROR);
			     rspData.setMsg("不支持该文件类型解析！");	
				 return rspData;
			}
			String wordStr=null;
			//下载文件
			boolean flag=saveUrlAs(sb.toString(), filePath);
			log.info("zyc====001====:{}", flag);
			if(flag) {
				switch (type) {
					case 81://txt
						wordStr=TikaUtils.getContext(filePath);
						break;
					case 82://ppt  pptx
						wordStr=TikaUtils.getPPTContent(filePath);
						break;
					case 83://doc docx
						wordStr=TikaUtils.parseWord(filePath);
						break;
					case 84:// pdf
						wordStr=TikaUtils.parsePdf(filePath,ocrUrl);
						break;
					case 85://xls xlsx
						wordStr=TikaUtils.parseExcel(filePath);
						break;
					default:
						break;
				}
				if(type>20 && type<30){
					//wordStr=TikaUtils.parsePicture(filePath,ocrUrl);
					wordStr=TikaUtils.parsePicture(filePath,ocrUrl);
				}
				bean.setFullText(wordStr);
				if(StringUtils.isBlank(wordStr)) {
					rspData.setCode(HttpStatus.ERROR);
				    rspData.setMsg("解析文件为空！");	
				    return rspData;
				}
				//bean.setFullTextStr(wordStr);
				bean.setInitials(PinYinUtil.getFullSpell(bean.getFname()));
				//删除当前文件
				File file = new File(filePath);
				file.delete();
				IndexRequest request = new IndexRequest(EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE);
				request.id(bean.getId());
				log.info("新增数据:{}", bean.toString());
				request.source(JSON.toJSONString(bean), XContentType.JSON);
				//request.setRefreshPolicy(RefreshPolicy.WAIT_UNTIL);
				IndexResponse response = client.index(request, RequestOptions.DEFAULT);
				response.setForcedRefresh(true);
				log.info("fileId: {} 插入es数据库成功", JSON.toJSONString(response));
				rspData.setCode(HttpStatus.SUCCESS);
			    rspData.setMsg("新增数据成功");
			    return rspData;
			}else {
				//return "文件下载失败";
				 rspData.setCode(HttpStatus.ERROR);
			     rspData.setMsg("文件下载失败！");	
			     return rspData;
			}
		} catch (Exception e) {
			log.error("fileId: {}新增数据入es库异常", bean);
			log.error(e.getMessage(), e);
			rspData.setCode(HttpStatus.ERROR);
		    rspData.setMsg("新增数据失败");	
		    return rspData;
		}
	}

	
	@Override
	public Object updateDocumentSpaceEs(DocumentSpace bean) {
		TableDataInfo rspData = new TableDataInfo();
		try {
			BoolQueryBuilder boolQueryBuilder = boolQuery();
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("fid", bean.getFid());
			TermQueryBuilder indexidBuilder = QueryBuilders.termQuery("indexid", bean.getIndexid());
			boolQueryBuilder.filter(termQueryBuilder);
			boolQueryBuilder.filter(indexidBuilder);
			sourceBuilder.query(boolQueryBuilder);
			SearchRequest searchRequest = new SearchRequest(EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE)
					.types(EsConstants.ES_TYPE_DOCUMENT_SPACE).source(sourceBuilder); // 索引
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			SearchHits hits = response.getHits(); // SearchHits提供有关所有匹配的全局信息，例如总命中数或最高分数：
			SearchHit[] searchHits = hits.getHits();
			List<DocumentSpaceEs> updataList=setUpdateList(searchHits,bean);//批量修改es
			
			if(updataList.size()<=0) {
				rspData.setCode(HttpStatus.ERROR);
			    rspData.setMsg("未查出相关数据！");	
			    return rspData;
			}
			BulkRequest bulkRequest = new BulkRequest();
			updataList.forEach(
					doc -> bulkRequest.add(new UpdateRequest(EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE, doc.getId())
							.doc(JSON.toJSONString(doc), XContentType.JSON)));
			//bulkRequest.setRefreshPolicy(RefreshPolicy.WAIT_UNTIL);
			client.bulk(bulkRequest, RequestOptions.DEFAULT);
			rspData.setCode(HttpStatus.SUCCESS);
		    rspData.setMsg("修改成功");	
		    return rspData;
		} catch (Exception e) {
			log.error("fileId: {}修改es库异常", bean.getFid());
			log.error(e.getMessage(), e);
			rspData.setCode(HttpStatus.ERROR);
		    rspData.setMsg("修改es库发生异常");	
		    return rspData;
		}
		
	}

	/**
	 * 批量修改es
	 * @param searchHits
	 * @param bean
	 * @return
	 */
	private List<DocumentSpaceEs> setUpdateList(SearchHit[] searchHits,DocumentSpace bean) {
		List<DocumentSpaceEs> updataList = new ArrayList<DocumentSpaceEs>();
		DocumentSpaceEs ds = null;
		for (SearchHit hit : searchHits) {
			ds = new DocumentSpaceEs();
			ds.setId(String.valueOf(hit.getSourceAsMap().get("id")));
			ds.setFid(String.valueOf(hit.getSourceAsMap().get("fid")));
			ds.setFname(bean.getFname());
			ds.setFnameStr(bean.getFname());
			ds.setInitials(PinYinUtil.getFullSpell(bean.getFname()));
			ds.setFtype(Integer.parseInt(String.valueOf(hit.getSourceAsMap().get("ftype"))));
			ds.setFtime(Integer.parseInt(String.valueOf(hit.getSourceAsMap().get("ftime"))));
			ds.setFsize(Integer.parseInt(String.valueOf(hit.getSourceAsMap().get("fsize"))));
			ds.setVer(Integer.parseInt(String.valueOf(hit.getSourceAsMap().get("ver"))));
			ds.setFullText(String.valueOf(hit.getSourceAsMap().get("fullText")));
			//ds.setFullTextStr(String.valueOf(hit.getSourceAsMap().get("fullTextStr")));
			ds.setUid(String.valueOf(hit.getSourceAsMap().get("uid")));
			ds.setIndexid(String.valueOf(hit.getSourceAsMap().get("indexid")));
			ds.setFpath(String.valueOf(hit.getSourceAsMap().get("fpath")));
			ds.setUsername(String.valueOf(hit.getSourceAsMap().get("username")));
			updataList.add(ds);
		}
		return updataList;
		
	}
	
	@Override
	public boolean deleteDocumentSpaceEs(String fid) {
		try {
			DeleteByQueryRequest request = new DeleteByQueryRequest(EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE);
			request.setDocTypes(EsConstants.ES_TYPE_DOCUMENT_SPACE);
			request.setQuery(new TermQueryBuilder("fid", fid));
			request.setRefresh(true);
			BulkByScrollResponse resp = client.deleteByQuery(request, RequestOptions.DEFAULT);
			
			log.info("fileId: {} 删除es数据库成功", JSON.toJSONString(resp));
			return true;
		} catch (Exception e) {
			log.error("fileId: {}删除es库异常", fid);
			log.error(e.getMessage(), e);
			return false;
		}
	}

	public static void main(String[] args) {
		String text = "{\"rule\":2,\"indexid\":\"634cc598c52744004020f336\",\"fpath\":\"0/634cc651c52744003b1fc61e\",\"key\":\"pdf\"}";
		String text2 = "{\"fpath\":\"0/634cc651c52744003b1fc61e\",\"indexid\":\"634cc598c52744004020f336\",\"isAsc\":\"asc\",\"key\":\"pdf\",\"limit\":30,\"offset\":0,\"orderByColumn\":\"initials\",\"rule\":2}";
		TSearchVo tSearchVo = JSONObject.parseObject(text2,TSearchVo.class);
		Map<String,Object> map = JSONObject.parseObject(text,Map.class);

		String text3 = "{\"clusters\":{\"fragment\":true,\"skipped\":0,\"successful\":0,\"total\":0},\"failedShards\":0,\"fragment\":false,\"hits\":{\"fragment\":true,\"hits\":[{\"fields\":{},\"fragment\":false,\"highlightFields\":{\"fullText\":{\"fragment\":true,\"fragments\":[{\"fragment\":true}],\"name\":\"fullText\"}},\"id\":\"634fb234c5274402834a7205\",\"matchedQueries\":[],\"primaryTerm\":0,\"rawSortValues\":[],\"score\":null,\"seqNo\":-2,\"sortValues\":[\"0\"],\"sourceAsMap\":{\"fid\":\"634fb234c5274402834a7206\",\"ftime\":1666167348,\"sky\":\"-1\",\"fname\":\"00数据安全防护盾测试用例初稿v1.5-190317.xlsx\",\"ver\":1,\"initials\":\"0\",\"fullText\":\", 存储空间防护, 基于本地目录和网络存储路径的防护, v1.0, , ], [, , , adobe reader pdf, 1.业务服务器运行正常\\n2.数据防护盾安装成功\\n3.策略下发成功, 1.验证虚拟磁盘文件文件是否可以打开, , ], [, , , adobe reader pdf, 1.业务服务器运行正常\\\\n2.数据防护盾安装成功\\\\n3.策略下发成功, 1.验证虚拟磁盘文件文件是否可以打开, , t47\\n3.6.2 发起流程\\t48\\功能。\\\\n管理员也可以在控制台，进入【安全策略】->【客户端策略】页面中，为所有用户启动自动清除缓存功能。\\\\n清除缓存，只是清除从云端已缓存到您本地的文件，但是并没删除该云端文件。清除缓存后，您仍然可以访问该文件。\\\\n\\\\n\\\\n\\\\n\\\\n\\\\n    \\\\n\\\\n\\\",\\\"id\\\":\\\"634fafd6c5274400351a91d9\\\",\\\"indexid\\\":\\\"634cc598c52744004020f336\\\",\\\"initials\\\":\\\"A\\\",\\\"sky\\\":\\\"-1\\\",\\\"type\\\":0,\\\"uid\\\":\\\"634cc598c52744004020f336\\\",\\\"username\\\":\\\"suser1\\\",\\\"ver\\\":1}\",\"sourceRef\":{\"fragment\":true},\"type\":\"t_db_document_space\",\"version\":-1}],\"maxScore\":null,\"totalHits\":{\"relation\":\"EQUAL_TO\",\"value\":2}},\"internalResponse\":{\"fragment\":true,\"numReducePhases\":1},\"numReducePhases\":1,\"profileResults\":{\"$ref\":\"$.hits.hits[0].fields\"},\"shardFailures\":[],\"skippedShards\":0,\"successfulShards\":1,\"timedOut\":false,\"took\":{\"days\":0,\"daysFrac\":1.6203703703703703E-7,\"hours\":0,\"hoursFrac\":3.888888888888889E-6,\"micros\":14000,\"microsFrac\":14000.0,\"millis\":14,\"millisFrac\":14.0,\"minutes\":0,\"minutesFrac\":2.3333333333333333E-4,\"nanos\":14000000,\"seconds\":0,\"secondsFrac\":0.014,\"stringRep\":\"14ms\"},\"totalShards\":1}";

		SearchResponse searchResponse = JSONObject.parseObject(text3,SearchResponse.class);
		DocumentSpaceEsServiceImpl documentSpaceEsService = new DocumentSpaceEsServiceImpl();
		documentSpaceEsService.findPageByGeneralSearch(map,tSearchVo);
	}

	@Override
	public List<TFullQueryResult> findPageByGeneralSearch(Map<String, Object> condition,TSearchVo searchVo) {
		try {
			BoolQueryBuilder boolBuilder = buildGeneralSearchQuery(condition);
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

			sourceBuilder.query(boolBuilder);

			Integer from = searchVo.getOffset() <= 0 ? 0 : searchVo.getOffset() * searchVo.getLimit();
			// 设置确定结果要从哪个索引开始搜索的from选项，默认为0
			sourceBuilder.from(from);
			sourceBuilder.size(searchVo.getLimit());
			if(!Strings.isNullOrEmpty(searchVo.getOrderByColumn())) {	
				if("desc".equals(searchVo.getIsAsc().toLowerCase())) {
					sourceBuilder.sort(new FieldSortBuilder(searchVo.getOrderByColumn()).order(SortOrder.DESC));
				}else {
					sourceBuilder.sort(new FieldSortBuilder(searchVo.getOrderByColumn()).order(SortOrder.ASC));
				}
			}else {
				sourceBuilder.sort(new FieldSortBuilder("fname").order(SortOrder.ASC));
			}
			
			Integer rule = (Integer) condition.get("rule");
			// 高亮
			List<String> highlightFields = new ArrayList<String>();
			HighlightBuilder highlight = new HighlightBuilder();
			if (rule == EsConstants.RULE_1) {// 精确
				highlight.field("fnameStr");
				highlight.field("fullText");
				highlightFields.clear();
				highlightFields.add("fnameStr");
				highlightFields.add("fullText");
			} else {//模糊
				highlight.field("fname");
				highlight.field("fullText");
				highlightFields.clear();
				highlightFields.add("fname");
				highlightFields.add("fullText");
			}
			highlight.preTags(EsConstants.ES_PRE_TAGS);
			highlight.postTags(EsConstants.ES_POST_TAGS);
			highlight.fragmentSize(100);
			highlight.numOfFragments(2);
			sourceBuilder.highlighter(highlight);
			SearchRequest searchRequest = new SearchRequest(EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE)
					.types(EsConstants.ES_TYPE_DOCUMENT_SPACE).source(sourceBuilder); // 索引
			log.info("DocumentSpaceEsServiceImpl-->findPageByGeneralSearch 请求参数:{}",JSONObject.toJSONString(searchRequest));
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			log.info("DocumentSpaceEsServiceImpl-->findPageByGeneralSearch 请求参数:{},返回结果:{}",JSONObject.toJSONString(searchRequest),JSONObject.toJSONString(response));

			SearchHits hits = response.getHits(); // SearchHits提供有关所有匹配的全局信息，例如总命中数或最高分数：
			List<TFullQueryResult> datas = new ArrayList<TFullQueryResult>();
			if (response.status().getStatus() == 200) {
				TFullQueryResult data = null;
				for (SearchHit searchHit : hits) {
					data = new TFullQueryResult();
					setEnterpriseSpaceEs(data, searchHit, rule);
					// 高亮设置
					if (!searchHit.getHighlightFields().isEmpty()) {
						StringBuilder buf = new StringBuilder();
						for (String field : highlightFields) {
							HighlightField highLightMessage = searchHit.getHighlightFields().get(field);
							if (!Objects.isNull(highLightMessage)) {
								// 反射调用set方法将高亮内容设置进去
								try {
									String setMethodName = HighlightUtils.parSetName(field);
									Class<? extends TFullQueryResult> dataClazz = data.getClass();
									Method setMethod = dataClazz.getMethod(setMethodName, String.class);
									buf.setLength(0);
									if (highLightMessage.fragments().length > 1) {
										for (int i = 0; i < highLightMessage.fragments().length; i++) {
											buf.append(highLightMessage.fragments()[i].toString()).append("");
											// buf.append(highLightMessage.fragments()[1].toString());
										}
									} else {
										buf.append(highLightMessage.fragments()[0].toString());
									}
									String htmlstr = buf.toString();
									// 外字需要加入样式
									htmlstr = HighlightUtils.addBigwordSpan(htmlstr);

									setMethod.invoke(data, htmlstr);
								} catch (Exception e) {
									log.error("error", e);
								}
							}
						}
					}
					datas.add(data);
				}
			}
			return datas;
		} catch (Exception e) {
			log.error("查询异常，", e);
		}

		return null;
	}

	/**
	 * 高亮结果集 特殊处理 map转对象 JSONObject.parseObject(JSONObject.toJSONString(map),
	 * Content.class)
	 * 
	 * @param searchResponse
	 * @param highlightField
	 */
	private List<Map<String, Object>> setSearchResponse(SearchResponse searchResponse, String highlightField) {
		// 解析结果
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			Map<String, HighlightField> high = hit.getHighlightFields();
			HighlightField title = high.get(highlightField);
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();// 原来的结果
			// 解析高亮字段,将原来的字段换为高亮字段
			if (title != null) {
				Text[] texts = title.fragments();
				String nTitle = "";
				for (Text text : texts) {
					nTitle += text;
				}
				// 替换
				sourceAsMap.put(highlightField, nTitle);
			}
			list.add(sourceAsMap);
		}
		return list;
	}

	@Override
	public Object updateDocumentSpaceFilPathEs(DocumentSpace bean) {
		TableDataInfo rspData = new TableDataInfo();
		
		try {
			BoolQueryBuilder boolQueryBuilder = boolQuery();
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			boolQueryBuilder.must(QueryBuilders.wildcardQuery("fpath", bean.getFpath()+"*"));
			TermQueryBuilder indexidBuilder = QueryBuilders.termQuery("indexid", bean.getIndexid());
			boolQueryBuilder.filter(indexidBuilder);
			sourceBuilder.query(boolQueryBuilder);
			SearchRequest searchRequest = new SearchRequest(EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE)
					.types(EsConstants.ES_TYPE_DOCUMENT_SPACE).source(sourceBuilder); // 索引
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			SearchHits hits = response.getHits(); // SearchHits提供有关所有匹配的全局信息，例如总命中数或最高分数：
			SearchHit[] searchHits = hits.getHits();
			List<DocumentSpaceEs> updataList=setUpdateList(searchHits,bean);//批量修改es
			
			if(updataList.size()<=0) {
				rspData.setCode(HttpStatus.ERROR);
			    rspData.setMsg("未查出相关数据！");	
			    return rspData;
			}
			String  path="";
			for (DocumentSpaceEs documentSpaceEs : updataList) {
				path=null;
				path=documentSpaceEs.getFpath().replace(bean.getFpath(), bean.getNewFpath());
				documentSpaceEs.setFpath(path);
			}
			BulkRequest bulkRequest = new BulkRequest();
			updataList.forEach(
					doc -> bulkRequest.add(new UpdateRequest(EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE, doc.getId())
							.doc(JSON.toJSONString(doc), XContentType.JSON)));
			
			client.bulk(bulkRequest, RequestOptions.DEFAULT);
			rspData.setCode(HttpStatus.SUCCESS);
		    rspData.setMsg("修改成功");	
		    return rspData;
		} catch (Exception e) {
			log.error("fileId: {}修改es库异常", bean.getFid());
			log.error(e.getMessage(), e);
			rspData.setCode(HttpStatus.ERROR);
		    rspData.setMsg("修改es库发生异常");	
		    return rspData;
		}
	}

}
