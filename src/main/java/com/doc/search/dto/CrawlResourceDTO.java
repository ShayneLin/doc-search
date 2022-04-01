package com.doc.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 爬取的数据源信息
 * 包含：类型、路径
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResourceDTO {

    /**
     * 资源类型
     */
    private Integer resourceType;

    /**
     * 资源的位置
     */
    private String resourceURI;

}
