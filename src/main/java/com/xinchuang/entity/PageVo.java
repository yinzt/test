package com.xinchuang.entity;


import lombok.Data;

/**
 * @author gulian.chensr
 */
@Data
public class PageVo{

    
    private int offset;
    
    private int limit;
   
    private String orderByColumn;
    
    private String isAsc;
    
}
