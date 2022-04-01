package com.doc.search.service;

import com.doc.search.dto.CrawlResourceDTO;
import com.doc.search.entity.Document;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lcs
 * @since 2022-03-31
 */
public interface IDocumentService extends IService<Document> {

    /**
     * 爬取数据
     * @param crawlResourceDTO
     */
    void crawlDoc(CrawlResourceDTO crawlResourceDTO);

    boolean existIndex();

    boolean createIndex();



}
