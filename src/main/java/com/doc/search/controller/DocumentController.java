package com.doc.search.controller;

import com.doc.search.dto.CrawlResourceDTO;
import com.doc.search.dto.resp.Result;
import com.doc.search.service.IDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lcs
 * @since 2022-03-31
 */
@Slf4j
@Controller
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    private IDocumentService documentService;

    /**
     * 提交需要爬取的数据
     * @return
     */
    @ResponseBody
    @PostMapping("/crawl")
    public Result<?> crawl(@RequestBody CrawlResourceDTO resourceDTO){
        /**
         * 生成爬取数据的任务，提交到任务队列里
         * 后续逐步进行爬取。
         */
        try{
            documentService.crawlDoc(resourceDTO);
            return Result.success();
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return Result.fail();
        }
    }

}
