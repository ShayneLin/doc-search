package com.doc.search.init;

import com.doc.search.service.IDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ElasticSearchIndexInitRunner implements CommandLineRunner {


    @Autowired
    private IDocumentService documentService;


    @Override
    public void run(String... args) {
        for (String s : args){
            System.out.println(s);
        }
        log.info("初始化索引...");
        if (!documentService.existIndex()){
            log.info("创建索引...");
            documentService.createIndex();

        }

    }
}
