package com.xinchuang.service;

import java.util.List;
import java.util.Map;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.xinchuang.entity.DocumentSpace;
/*import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;*/
import com.xinchuang.entity.DocumentSpaceEs;
import com.xinchuang.entity.TFullQueryResult;
import com.xinchuang.entity.TSearchVo;

public interface DocumentSpaceEsService{

	/**
	* 删除企业空间索引
	* @return void
	*/
	void deleteEnterpriseSpaceIndex();
	
	/**
	 * 添加数据
	 * @param DocumentSpaceEs bean
	 */
	Object insertDocumentSpaceEs(DocumentSpaceEs bean);


	/**
	 * 添加数据
	 * @param list
	 */
	Object insertTestDocumentSpaceEs(DocumentSpaceEs bean);

	/**
	 * 修改
	 * @param es
	 * @return
	 */
	Object updateDocumentSpaceEs(DocumentSpace bean);
	
	/**
	 * 批量删除
	 * @param fileId
	 * @return
	 */
	boolean deleteDocumentSpaceEs(String fid);
	
	/**
	 * 普通检索
	 * @param condition
	 * @param sort
	 * @param pageable
	 * @return
	 */
	List<TFullQueryResult> findPageByGeneralSearch(Map<String, Object> condition, TSearchVo searchVo);
	
	/**
	 * 修改文件路径
	 * @param es
	 * @return
	 */
	Object updateDocumentSpaceFilPathEs(DocumentSpace bean);
	
}
