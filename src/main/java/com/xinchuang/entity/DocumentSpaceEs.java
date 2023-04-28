package com.xinchuang.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;
import com.xinchuang.common.constant.EsConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @author d'm'l
 * 企业空间
 */

/*@Document(indexName = EsConstants.ES_INDEX_NAME_ENTERPRISE, type = EsConstants.ES_TYPE_ENTERPRISE)  
@Setting(settingPath = "/es-mapping/es-setting.json")
@Mapping(mappingPath = "/es-mapping/enterprise-mapping.json")*/
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Document(indexName = EsConstants.ES_INDEX_NAME_DOCUMENT_SPACE, type = EsConstants.ES_TYPE_DOCUMENT_SPACE, shards = 1, replicas = 0,createIndex = true)
public class DocumentSpaceEs implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private String id;
	 
	private String fid;//文件ID
	
	private int type;//协议命令字
	
	//@Field(type = FieldType.Text, analyzer = "ik_max_word")
	@Field(type = FieldType.Keyword)
	private String fname;//文件名分词
	 
	@Field(type = FieldType.Keyword)
	private String fnameStr;//文件不分词
	
	@Field(type = FieldType.Integer)
	private int ftype;//文件编码类型
	
	@Field(type = FieldType.Integer)
	private int ftime;//文件时间
	
	@Field(type = FieldType.Integer)
	private int fsize;//文件大小
	
	@Field(type = FieldType.Integer)
	private int ver;//文件版本号
	
	@Field(type = FieldType.Keyword)
	private String uid;//用户ID
	
    //@Field(type = FieldType.Text, analyzer = "ik_max_word")
	@Field(type = FieldType.Keyword)
	private String fullText;//全文内容分词
	
//	@Field(type = FieldType.Keyword)
//	private String fullTextStr;//全文内容不分词
	
	@Field(type = FieldType.Keyword)
	private String indexid;//文件所属索引空间ID
	
	private String sky;
	
	private String mod;
	
	private String fpath;//文件路径
	
	private String username;//创建者/修改者
	
	private String initials;
	

	
}
