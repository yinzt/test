package com.xinchuang.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import lombok.Data;


@Data
public class DocumentSpace implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private String id;
	 
	private String fid;//文件ID
	
	private int type;//协议命令字
	
	private String fname;//文件名分词
	 
	private String fnameStr;//文件不分词
	
	private int ftype;//文件编码类型
	
	private int ftime;//文件时间
	
	private int fsize;//文件大小
	
	private int ver;//文件版本号
	
	private String uid;//用户ID
	
	private String fullText;//全文内容分词
	
	//private String fullTextStr;//全文内容不分词
	
	private String indexid;//文件所属索引空间ID
	
	private String sky;
	
	private String mod;
	
	private String fpath;//文件路径
	
	private String username;//创建者/修改者
	
	private String newFpath;//新路径
	

	
}
