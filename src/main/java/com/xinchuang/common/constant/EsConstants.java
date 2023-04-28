package com.xinchuang.common.constant;

public class EsConstants {
	//普通，不支持同义词
	public static final	String ES_INDEX_NAME_DOCUMENT_SPACE = "db_document_space";
	public static final	String ES_TYPE_DOCUMENT_SPACE = "t_db_document_space";
	
	
	public static final String ES_PRE_TAGS="<strong style='color:red' id='top'>";
    public static final String ES_POST_TAGS="</strong>";
	
    public static final Integer ES_HIGHLIGHT_TEXT=20;//es高亮前后保留字数
    
    public static final String INDEX_ENTERPRISE="1";//企业空间
    public static final String INDEX_PERSONAL="2";//个人空间
    public static final String INDEX_GROUP="3";//群组空间
    
    public static final int RULE_1=1;//精确
    public static final int RULE_2=2;//模糊
    
}
