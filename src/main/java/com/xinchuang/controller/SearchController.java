package com.xinchuang.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.xinchuang.base.BaseController;
import com.xinchuang.common.constant.HttpStatus;
import com.xinchuang.core.domain.AjaxResult;
import com.xinchuang.core.page.TableDataInfo;
import com.xinchuang.entity.DocumentSpace;
import com.xinchuang.entity.DocumentSpaceEs;
import com.xinchuang.entity.TFullQueryResult;
import com.xinchuang.entity.TSearchVo;
import com.xinchuang.service.DocumentSpaceEsService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/search")
public class SearchController extends BaseController{
	
	@Autowired
	private DocumentSpaceEsService documentSpaceEsService;


	public static void main(String[] args) {
		String text = "{\"fpath\":\"0/634cc651c52744003b1fc61e\",\"indexid\":\"634cc598c52744004020f336\",\"isAsc\":\"asc\",\"key\":\"pdf\",\"limit\":30,\"offset\":0,\"orderByColumn\":\"initials\",\"rule\":2}";
		TSearchVo searchVo = JSONObject.parseObject(text,TSearchVo.class);
		Map<String, Object> condition = new HashMap<String, Object>();
		List<TFullQueryResult> datalist =new ArrayList<TFullQueryResult>();
		if(StringUtils.isNotBlank(searchVo.getKey())) {
			condition.put("key", searchVo.getKey());
		}
		if(searchVo.getRule()>0) {
			condition.put("rule", searchVo.getRule());
		}
		if(StringUtils.isNotBlank(searchVo.getFname())) {
			condition.put("fname", searchVo.getFname());
		}
		if(searchVo.getTimestart()!=null) {
			condition.put("timestart", searchVo.getTimestart());
		}
		if(searchVo.getTimeend()!=null) {
			condition.put("timeend", searchVo.getTimeend());
		}
		if(searchVo.getFsizestart()!=null) {
			condition.put("fsizestart", searchVo.getFsizestart());
		}
		if(searchVo.getFsizeend()!=null) {
			condition.put("fsizeend", searchVo.getFsizeend());
		}
		if(searchVo.getFtype()!=null) {
			condition.put("ftype", searchVo.getFtype());
		}
		if(StringUtils.isNotBlank(searchVo.getIndexid())) {
			condition.put("indexid", searchVo.getIndexid());
		}
		if(StringUtils.isNotBlank(searchVo.getFpath())) {
			condition.put("fpath", searchVo.getFpath());
		}
		if(StringUtils.isNotBlank(searchVo.getUsername())) {
			condition.put("username", searchVo.getUsername());
		}

		System.out.println(JSONObject.toJSONString(condition));
	}

    @RequestMapping(value = "/generalsearch/list", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
	public Object getGeneralSearch(HttpServletRequest request,TSearchVo searchVo) {
		try {
			log.info("SearchController--->getGeneralSearch,请求参数为:{}", JSONObject.toJSONString(searchVo));
			Map<String, Object> condition = new HashMap<String, Object>();
			List<TFullQueryResult> datalist =new ArrayList<TFullQueryResult>();
			if(StringUtils.isNotBlank(searchVo.getKey())) {
				condition.put("key", searchVo.getKey());
			}else {
				return AjaxResult.error("请输入检索关键字");
			}
			if(searchVo.getRule()>0) {
				condition.put("rule", searchVo.getRule());
			}
			if(StringUtils.isNotBlank(searchVo.getFname())) {
				condition.put("fname", searchVo.getFname());
			}
			if(searchVo.getTimestart()!=null) {
				condition.put("timestart", searchVo.getTimestart());
			}
			if(searchVo.getTimeend()!=null) {
				condition.put("timeend", searchVo.getTimeend());
			}
			if(searchVo.getFsizestart()!=null) {
				condition.put("fsizestart", searchVo.getFsizestart());
			}
			if(searchVo.getFsizeend()!=null) {
				condition.put("fsizeend", searchVo.getFsizeend());
			}
			if(searchVo.getFtype()!=null) {
				condition.put("ftype", searchVo.getFtype());
			}
			if(StringUtils.isNotBlank(searchVo.getIndexid())) {
				condition.put("indexid", searchVo.getIndexid());
			}
			if(StringUtils.isNotBlank(searchVo.getFpath())) {
				condition.put("fpath", searchVo.getFpath());
			}
			if(StringUtils.isNotBlank(searchVo.getUsername())) {
				condition.put("username", searchVo.getUsername());
			}
			List<TFullQueryResult> list = Lists.newArrayList();
			if(condition.isEmpty()) {
				return getDataTable(list);
			}
//			Map<String, SortOrder> sortMap = new HashMap<>();
//			if(!Strings.isNullOrEmpty(searchVo.getOrderByColumn())) {	
//				if("desc".equals(searchVo.getOrderByColumn().toLowerCase())) {
//					sortMap.put(searchVo.getOrderByColumn(), SortOrder.DESC);
//				}else {
//					sortMap.put(searchVo.getOrderByColumn(), SortOrder.ASC);
//				}
//			}else {
//				sortMap.put("fname", SortOrder.ASC);
//			}
			list=documentSpaceEsService.findPageByGeneralSearch(condition, searchVo);
			TableDataInfo rspData = new TableDataInfo();
	        rspData.setCode(HttpStatus.SUCCESS);
	        rspData.setMsg("查询成功");	        
	        rspData.setRows(list);
	        rspData.setTotal(list.size());
			log.info("SearchController--->getGeneralSearch,请求参数为:{},返回结果:{}", JSONObject.toJSONString(searchVo),JSONObject.toJSONString(rspData));
	        return rspData;
			
		} catch (Exception e) {
			log.error("获取列表出现异常，",e);
			return AjaxResult.error("查询异常");
		}
	}
	
    /**
     * 删除索引空间
     * @param request
     * @param
     * @return
     */
    @RequestMapping(value = "/deleteSpace", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
	public Object deleteSpace(HttpServletRequest request) {
		try {
			
			documentSpaceEsService.deleteEnterpriseSpaceIndex();
			TableDataInfo rspData = new TableDataInfo();
	        rspData.setCode(HttpStatus.SUCCESS);
	        rspData.setMsg("删除空间成功");	      
	        return rspData;
		} catch (Exception e) {
			log.error("删除空间出现异常，",e);
			return AjaxResult.error("删除空间异常");
		}
	}

	/**
	 * 新增索引
	 * @param request
	 * @param
	 * @return
	 */
	@RequestMapping(value = "/insertTestIndex", method = {RequestMethod.GET,RequestMethod.POST})
	@ResponseBody
	public Object insertTestIndex(HttpServletRequest request,DocumentSpaceEs bean) {
		try {
			if(StringUtils.isBlank(bean.getId())||StringUtils.isBlank(bean.getFname())) {
				return AjaxResult.error("主键Id不能为空||文件名称不能为空");
			}
			return documentSpaceEsService.insertTestDocumentSpaceEs(bean);
		} catch (Exception e) {
			log.error("新增索引失败出现异常，",e);
			return AjaxResult.error("新增索引失败");
		}
	}
    
    /**
     * 新增索引
     * @param request
     * @param
     * @return
     */
    @RequestMapping(value = "/insertIndex", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
	public Object insertIndex(HttpServletRequest request,DocumentSpaceEs bean) {
		try {
			log.info("SearchController-->insertIndex bean:{}",JSONObject.toJSONString(bean));
			if(StringUtils.isBlank(bean.getId())) {
				return AjaxResult.error("主键Id不能为空");
			}
			TableDataInfo rspData = new TableDataInfo();
			rspData=(TableDataInfo) documentSpaceEsService.insertDocumentSpaceEs(bean);
			log.info("SearchController-->insertIndex bean:{},rspData:{}",JSONObject.toJSONString(bean),JSONObject.toJSONString(rspData));
	        return rspData;
		} catch (Exception e) {
			log.error("新增索引失败出现异常，bean:{}",JSONObject.toJSONString(bean),e);
			return AjaxResult.error("新增索引失败");
		}
	}
    
    /**
     * 删除索引
     * @param request
     * @param
     * @return
     */
    @RequestMapping(value = "/deleteIndex", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
	public Object deleteIndex(HttpServletRequest request,TSearchVo bean) {
		try {
			if(StringUtils.isBlank(bean.getFid())) {
				return AjaxResult.error("文件id不能为空");
			}
			boolean flag=documentSpaceEsService.deleteDocumentSpaceEs(bean.getFid());
			TableDataInfo rspData = new TableDataInfo();
			if(flag) {
				 rspData.setCode(HttpStatus.SUCCESS);
			     rspData.setMsg("删除索引成功");	 
			}else {
				rspData.setCode(HttpStatus.ERROR);
			    rspData.setMsg("删除索引成功");	 
			}
	        return rspData;
		} catch (Exception e) {
			log.error("删除索引出现异常，",e);
			return AjaxResult.error("删除异常");
		}
	}
    
    /**
     * 修改索引
     * @param request
     * @param
     * @return
     */
    @RequestMapping(value = "/updateIndex", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
	public Object updateIndex(HttpServletRequest request,DocumentSpace bean) {
		try {
			TableDataInfo rspData = new TableDataInfo();
			if(StringUtils.isBlank(bean.getFid())) {
				return AjaxResult.error("文件id不能为空");
			}
			if(StringUtils.isBlank(bean.getIndexid())) {
				return AjaxResult.error("空间id不能为空");
			}
			rspData=(TableDataInfo) documentSpaceEsService.updateDocumentSpaceEs(bean); 
	        return rspData;
		} catch (Exception e) {
			log.error("修改索引出现异常，",e);
			return AjaxResult.error("修改索引出现异常");
		}
	}
	
    /**
     * 修改索引
     * @param request
     * @param
     * @return
     */
    @RequestMapping(value = "/updateFpath", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
	public Object updateFpath(HttpServletRequest request,DocumentSpace bean) {
		try {
			if(StringUtils.isBlank(bean.getFpath())) {
				return AjaxResult.error("文件路径不能为空！");
			}
			if(StringUtils.isBlank(bean.getIndexid())) {
				return AjaxResult.error("空间id不能为空！");
			}
			if(StringUtils.isBlank(bean.getNewFpath())) {
				return AjaxResult.error("新路径不能为空！");
			}
			TableDataInfo rspData = new TableDataInfo();
			rspData=(TableDataInfo) documentSpaceEsService.updateDocumentSpaceFilPathEs(bean); 
	        return rspData;
		} catch (Exception e) {
			log.error("修改索引出现异常，",e);
			return AjaxResult.error("修改索引出现异常");
		}
	}
}
