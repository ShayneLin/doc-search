package com.doc.search.service.impl;

import com.doc.search.constant.ResourceTypeConst;
import com.doc.search.dto.CrawlResourceDTO;
import com.doc.search.entity.Document;
import com.doc.search.manager.DocumentManager;
import com.doc.search.manager.ElasticSearchManager;
import com.doc.search.mapper.DocumentMapper;
import com.doc.search.service.IDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.doc.search.util.CryptoUtil;
import com.doc.search.util.FileUtil;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.doc.search.constant.IndexConst.BATCH_PAGE_SIZE;
import static com.doc.search.constant.IndexConst.INDEX_NAME;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lcs
 * @since 2022-03-31
 */
@Slf4j
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements IDocumentService {




    @Autowired
    private ElasticSearchManager elasticSearchManager;

    @Autowired
    private DocumentManager documentManager;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void crawlDoc(CrawlResourceDTO crawlResourceDTO){

        if (Objects.equals(crawlResourceDTO.getResourceType(), ResourceTypeConst.FILE_SYSTEM)){

            File file = new File(crawlResourceDTO.getResourceURI());
            Preconditions.checkState(file.isDirectory(),"URI不是目录");
            log.info("文件系统数据爬取...");
            List<Document> batchDocument = new ArrayList<>();
            List<File> allFileList = FileUtil.getAllFileList(crawlResourceDTO.getResourceURI());
            for (int i = 1;i <= allFileList.size();i++) {
                File actualFile = allFileList.get(i - 1);
                if (i == allFileList.size() || (i % BATCH_PAGE_SIZE) == 0) {
                    this.batchSaveDocument(batchDocument);
                    boolean result = elasticSearchManager.bulkIndexRequest(INDEX_NAME, batchDocument);
                    log.info("批量插入文档结果:{}",result);
                    //插入后需要清空
                    batchDocument.clear();
                } else {
                    Document document = new Document();
                    document.setId(i);
                    document.setName(actualFile.getName());
                    document.setDocUrl(actualFile.getAbsolutePath());
                    document.setDescription(actualFile.getName());
                    document.setModifyTime(new Date(actualFile.lastModified()));
                    batchDocument.add(document);
                }
            }

        }else if (Objects.equals(crawlResourceDTO.getResourceType(),ResourceTypeConst.SVN)){

            //TODO:
            log.info("SVN数据爬取...");

        }else {
            throw new RuntimeException("资源类型不支持...");
        }
    }

    private void batchSaveDocument(List<Document> batchDocument) {

        for (Document document: batchDocument){
            String docUrl = document.getDocUrl();
            String entityId = CryptoUtil.md5(docUrl);
            document.setEntityId(entityId);
            Document documentPO = documentManager.getDocumentByEntityId(entityId);
            if (documentPO == null){
                //未插入才需要保存
                documentManager.addDocument(document);
            }
        }
    }


    @Override
    public boolean existIndex() {
        System.out.println(this.getClass().getGenericSuperclass().getTypeName());
        System.out.println(this.getClass().getGenericSuperclass().getClass());
        return elasticSearchManager.existsIndex(INDEX_NAME);
    }

    @Override
    public boolean createIndex() {
        return elasticSearchManager.createIndex(INDEX_NAME,Document.class);
    }
}
