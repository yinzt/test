package com.xinchuang.entity;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TFullQueryResult implements Serializable{
	
	private String fid;//文件ID
	private int type;//协议命令字
	private String fname;//文件名分词
	private int ftype;//文件编码类型
	private int ftime;//文件时间
	private int fsize;//文件大小
	private int ver;//文件版本号
	private String uid;//用户ID
	private String fullText;//全文内容分词  
	private String indexid;
	private String id;
	private String sky;
	private String username;
	private String fpath;

     
}
