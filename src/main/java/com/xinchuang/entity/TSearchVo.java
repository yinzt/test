package com.xinchuang.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TSearchVo extends PageVo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("key")
	private String key;//关键词组
	
	private int rule;//搜索规则，包括模糊（任意匹配），精准（全部匹配）

	private String indexid;//空间ID（可选，如果没有则搜索全部空间）
	
	private String fname;//指定文件名
	
	private Integer timestart;//文件开始时间
	
	private Integer timeend;//文件结束时间
	
	private Integer fsizestart;//文件开始大小
	
	private Integer fsizeend;//文件结束大小
	
	private String fid;
	
    private String fpath;//文件路径
    
    private Integer ftype;//文件类型
	
	private String username;//创建者/修改者

	
}
