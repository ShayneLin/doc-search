package com.doc.search.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.doc.search.entity.Document;
import com.doc.search.mapper.DocumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DocumentManager {

    @Autowired
    private DocumentMapper documentMapper;

    public int addDocument(Document document){
        return documentMapper.insert(document);
    }

    /**
     * 根据唯一标识查找记录
     * @param entityId
     * @return
     */
    public Document getDocumentByEntityId(String entityId){
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.eq("entity_id", entityId);
        return documentMapper.selectOne(wrapper);
    }

}
