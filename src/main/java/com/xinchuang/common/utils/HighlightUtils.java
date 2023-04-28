package com.xinchuang.common.utils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.xinchuang.common.constant.EsConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HighlightUtils {

	/**
	 * 
	 * @param hlmstr
	 * @return
	 */
	public static String formatField(String hlmstr) {
		int totalidx = hlmstr.length();
		int startidx = hlmstr.indexOf(EsConstants.ES_PRE_TAGS);
		int endidx = hlmstr.lastIndexOf(EsConstants.ES_POST_TAGS);
		
		int fsidx = 0;
		if(startidx > EsConstants.ES_HIGHLIGHT_TEXT) {
			fsidx = startidx - EsConstants.ES_HIGHLIGHT_TEXT;
		}else {
			fsidx = startidx;
		}
		int feidx = 0;
		if(totalidx - endidx > EsConstants.ES_HIGHLIGHT_TEXT) {
			feidx = totalidx - EsConstants.ES_HIGHLIGHT_TEXT;
		}else {
			feidx = totalidx;
		}
		hlmstr = hlmstr.substring(fsidx,feidx);
		return hlmstr;
	}
	
	/**
	 * 
	 * @param fieldName
	 * @return
	 */
	public static String parSetName(String fieldName) {
		if (null == fieldName || "".equals(fieldName)) {
			return null;
		}
		int startIndex = 0;
		if (fieldName.charAt(0) == '_')
			startIndex = 1;
		return "set" + fieldName.substring(startIndex, startIndex + 1).toUpperCase()
				+ fieldName.substring(startIndex + 1);
	}
	
	/**
	 * <strong class="zhsjFont">&#<strong style='color:red'>x9ef5</strong>;</strong>齒。有聖德。年十五而佐顓頊。
	 * <strong style='color:red'>僧</strong>&#<strong style='color:red'>xf</strong>2<strong style='color:red'>cf</strong>4;&#<strong style='color:red'>xf</strong>2<strong style='color:red'>cf</strong>4;、鳳
	 * @param hightStr
	 * @return
	 */
	public static String replaceSpan(String hightStr,List<String> bigwordUicode) {
		String handleStr = standardHightLight(hightStr);
		//log.info(hightStr);			
		String patstr = "(&#x[0-9a-z;]{6})";
		Pattern p = Pattern.compile( patstr );
	    Matcher m = p.matcher(handleStr);
	    //String uc6 = null;
	    StringBuilder wordstr = new StringBuilder();
	    String result = handleStr;
	    while( m.find()){	      
	       String waizi = m.group();
	       //log.info(waizi);
	       if(bigwordUicode.contains(waizi)) {
	    	   wordstr.append(EsConstants.ES_PRE_TAGS).append(waizi).append(EsConstants.ES_POST_TAGS);
	    	   result = handleStr.replace(waizi, wordstr.toString());
	    	   wordstr.setLength(0);
	       }	       	      
	    }
	    //log.info("==>处理结果：{}",result);
		return result;
	}
	
	private static String standardHightLight(String hightStr)  {
		StringBuilder finalbuf = new StringBuilder();
		try {
			//log.info("startstr=>"+hightStr);
			//String patstr = "(<strong style='color:red'>[0-9a-z]{1,5}</strong>)";
			String patstr = "("+EsConstants.ES_PRE_TAGS+"[0-9a-z]{1,6}"+EsConstants.ES_POST_TAGS+")";
			Pattern p = Pattern.compile(patstr);
		    Matcher m = p.matcher(hightStr);
		   
		    StringBuilder allbuf = new StringBuilder();
		   
		    int startIdx = 0;
		    int endIdx = 0;
		    while( m.find()){	      
		    	
		       String waiziStr = m.group();
		       //log.info("匹配外字："+waiziStr);	       
		      
		       //截取除外字外的标签位置
		       endIdx = hightStr.indexOf(waiziStr,startIdx);
		       
		       String otherStr = hightStr.substring(startIdx,endIdx);		       
		       finalbuf.append(otherStr);		       
		       String waizire = waiziStr.replace(EsConstants.ES_PRE_TAGS, "").replace(EsConstants.ES_POST_TAGS, "");
			   finalbuf.append(waizire);
			   
		       //log.info("total temp=>"+finalbuf.toString());
		       allbuf.append(otherStr);
		       allbuf.append(waiziStr);	       
		       //计算下一个的开始坐标
		       startIdx = endIdx + waiziStr.length();
		    }
		    String last_temp = hightStr.substring(allbuf.length(), hightStr.length());
		    finalbuf.append(last_temp);
		   // log.info("finalbuf=>"+finalbuf.toString());
		}catch(Exception e) {
			log.error("异常，",e);
		}
	    return finalbuf.toString();
	}
	
	public static String formatCorrectHight(String hightStr,String queryKey) {
		String result = hightStr;
		String patstr = "("+EsConstants.ES_PRE_TAGS+".?"+EsConstants.ES_POST_TAGS+")";
		Pattern p = Pattern.compile(patstr);
	    Matcher m = p.matcher(hightStr);
	   
	    //StringBuilder allbuf = new StringBuilder();
	    
	    boolean iscorrect = true;
	    while( m.find()){	      	    	
	       String hs = m.group();
	       //log.info(hs);
	       String hsf = hs.replace(EsConstants.ES_PRE_TAGS, "").replace(EsConstants.ES_POST_TAGS, "");
	       //log.info(hsf);
	       if(!queryKey.contains(hsf)) {
	    	   iscorrect  = false;	    	  
	       }
	    }
	    
	    if(!iscorrect) {//错误转换
	    	String hsf = hightStr.replace(EsConstants.ES_PRE_TAGS, "").replace(EsConstants.ES_POST_TAGS, "");
	    	//log.info(hsf);
	    	StringBuilder sb = new StringBuilder();//构造一个StringBuilder对象
	    	String[] querys = hsf.split(queryKey);
	    	for(String query : querys) {
	    		//log.info(query);
	    		sb.append(query).append(EsConstants.ES_PRE_TAGS).append(queryKey).append(EsConstants.ES_POST_TAGS);
	    	}
	        //sb.insert(1, "正在学习");//在指定的位置1，插入指定的字符串
	    	result = sb.toString();
	    }
		
		return result;
	}
	
	
	public static String addBigwordSpan(String hightStr) {
		String handleStr = standardHightLight(hightStr);
		//log.info(hightStr);			
		String patstr = "(&#x[0-9a-z;]{6})";
		Pattern p = Pattern.compile( patstr );
	    Matcher m = p.matcher(handleStr);
	    //String uc6 = null;
	    Map<String,String> wordMap = Maps.newHashMap();
	    String result = handleStr;
	    while( m.find()){	      
	       String waizi = m.group();
	       wordMap.put(waizi, waizi);	       
	    }
	    
	    log.info("==>处理结果：{}",wordMap);
	    List<String> list = wordMap.values().stream().collect(Collectors.toList());
	    for(String wz : list) {
	    	String fonttype = "";
	    	if(wz.startsWith("&#x2")) {
	    		fonttype = "big02";
	    	}else if(wz.startsWith("&#xf")) {
	    		fonttype = "big15";
	    	}
	    	String replacement = "<span class='"+fonttype+"'>"+wz+"</span>";
	    	result = StringUtils.replace(result, wz, replacement);
	    }
	    //log.info(result);
		return result;
	}
	public static void main(String[] args) {
	    //String hightStr = "狀&#<strong style='color:red'>x</strong>6<strong style='color:red'>ebc</strong>;靈龜三關電掃。劉禪入臣。天符人事。於是信矣。[祭文]&#<strong style='color:red'>x</strong>6<strong style='color:red'>ebc</strong>;能";
//		String hightStr = "<strong style='color:red'>僧</strong>&#<strong style='color:red'>xf</strong>2<strong style='color:red'>cf</strong>4;靈龜三關電掃。劉禪入臣。天符人事。於是信矣&#<strong style='color:red'>xf</strong>2<strong style='color:red'>cf</strong>4;、鳳";
//		//String hightStr = "<strong style='color:red'>僧</strong>是大幅度发放";
//		List<String> uicodelist = Lists.newArrayList();
//		uicodelist.add("&#xf2cf4;");
//		uicodelist.add("&#x6ebc;");
//		
//		replaceSpan(hightStr,uicodelist);
		
		//standardHightLight(hightStr);
//		String test = "<strong style='color:red'>分</strong><strong style='color:red'>自</strong>魏始，官氏志只言『勳品流外位卑不載』。（通典拾玖『大唐……又置勳品九品，……謂之流外。流外自此始』。自注云：『勳品自齊梁卽有之』，而不及齊隋所承襲之北魏，何也）？而不及視品。魏<strong style='color:red'>書</strong><strong style='color:red'>刑</strong>法志：舊制直閤直後直齋武官隊主隊副等以比視官，至於犯譴不得除罪。尙書令任城王澄奏：案諸州中正亦非品令所載，又無祿恤，先朝以來皆得當刑。直閤等禁直上下，有宿衞之勤，理不應異。靈太后令準中正。是魏有比視官之證。......宣陽門雖然是北魏時新改的城門名，但漢代的<strong style='color:red'>宮</strong><strong style='color:red'>城</strong>不必和北魏的宮城一致。宣陽門旣已決<strong style='color:red'>定</strong><strong style='color:red'>，</strong>那就北魏宮城正門的地位也可以決定<strong style='color:red'>了</strong><strong style='color:red'>，</strong>但其四方基址地址還要決定的，現在可以先根據洛陽伽藍記的以下一段：永寧寺熙平元年靈太后胡氏所立也，在宮前閶闔門南一里，御道西、其寺東有太尉府，西對永康里，南界昭玄曹：北鄰御史臺。閶闔門御道東有左衞府，府南有司徒府，南有國子學堂，內有孔丘";
//		replaceSpan(test,uicodelist);
//		
//		String result = formatCorrectHight(test,"北魏");
//		log.info(result);
		
		//String txt = TxtUtil.readTxtFile("C:\\Users\\ThinkPad\\Desktop\\testpattern.txt");
		
		//addBigwordSpan(txt);
	}
}
