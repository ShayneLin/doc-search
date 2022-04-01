package com.doc.search.manager;

import com.alibaba.fastjson.JSON;
import com.doc.search.entity.Document;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

@Slf4j
@Component
public class ElasticSearchManager {

    @Autowired
    private RestHighLevelClient client;

    private static final Map<String, Object> KEYWORD_TYPE = new HashMap<>();
    private static final Map<String, Object> TEXT_TYPE = new HashMap<>();
    private static final Map<String, Object> LONG_TYPE = new HashMap<>();
    private static final Map<String, Object> DOUBLE_TYPE = new HashMap<>();
    private static final Map<String, Object> INTEGER_TYPE = new HashMap<>();
    private static final Map<String, Object> DATE_TYPE = new HashMap<>();

    private static final String TYPE_NAME = "type";
    private static final String CREATED = "created";
    private static final String UPDATED = "updated";

    private static final int TIMEOUT = 15;

    private static final int PAGE = 50;

    static {
        //初始化一些类型
        KEYWORD_TYPE.put(TYPE_NAME, FieldType.Keyword.getMappedName());
        INTEGER_TYPE.put(TYPE_NAME, FieldType.Integer.getMappedName());
        TEXT_TYPE.put(TYPE_NAME, FieldType.Text.getMappedName());
        LONG_TYPE.put(TYPE_NAME, FieldType.Long.getMappedName());
        DOUBLE_TYPE.put(TYPE_NAME, FieldType.Double.getMappedName());
        DATE_TYPE.put(TYPE_NAME, FieldType.Date.getMappedName());
        DATE_TYPE.put("format", "yyyy-MM-dd HH:mm:ss");
    }


    /**
     * 创建索引
     * @param index 索引名字
     * @param cls 对象所属的类，用于遍历获得属性名
     * @return
     */
    public boolean createIndex(String index,Class cls) {
        Map<String, Object> setting = getSetting();
        Map<String, Object> indexMapping = new HashMap<>();
        Map<String, Object> properties = parseProperties(cls);
        indexMapping.put("properties", properties);
        boolean exists = existsIndex(index);
        if(exists) {
           log.warn("索引库：{},已经存在!",index);
           return true;
        }
        CreateIndexRequest request = new CreateIndexRequest(index);
        try {
            request.settings(setting);
            request.mapping(indexMapping);
            request.setTimeout(TimeValue.timeValueSeconds(TIMEOUT));
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            return createIndexResponse.isAcknowledged();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
    }


    private Map<String, Object> parseProperties(Class cls) {
        Map<String,Object> properties = new HashMap<>();
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field field : declaredFields){
            if (field.getType().equals(Integer.class)){
                properties.put(field.getName(),INTEGER_TYPE);
            }else if (field.getType().equals(Long.class)){
                properties.put(field.getName(),LONG_TYPE);
            }else if (field.getType().equals(String.class)){
                properties.put(field.getName(),TEXT_TYPE);
            }else if (field.getType().equals(Date.class)){
                properties.put(field.getName(),DATE_TYPE);
            }else if (field.getType().equals(Double.class)){
                properties.put(field.getName(),DOUBLE_TYPE);
            }else {
                //默认是TEXT
                properties.put(field.getName(),TEXT_TYPE);
            }
        }
        return properties;

    }

    public boolean existsIndex(String indexName){
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        try {
            getIndexRequest.setTimeout(TimeValue.timeValueSeconds(TIMEOUT));
            return client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 添加文档
     * @param document
     */
    public boolean addDoc(String indexName,Document document){
        Preconditions.checkNotNull(indexName);
        Preconditions.checkNotNull(document);
        Preconditions.checkState(document.getId() != null);
        IndexRequest indexRequest = new IndexRequest(indexName);
        indexRequest.timeout(TimeValue.timeValueSeconds(TIMEOUT))
                .id(document.getId().toString())
                .source(JSON.toJSONString(document), XContentType.JSON);
        try {
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            return Objects.equals(indexResponse.getResult().getLowercase(),CREATED);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    /**
     * 更新文档
     * @param indexName
     * @param document
     * @return
     */
    public boolean updateDoc(String indexName,Document document){
        Preconditions.checkNotNull(indexName);
        Preconditions.checkNotNull(document);
        Preconditions.checkState(document.getId() != null);
        try {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.index(indexName)
                    .id(document.getId().toString())
                    .timeout(TimeValue.timeValueSeconds(TIMEOUT))
                    .id(document.getId().toString())
                    .doc(JSON.toJSONString(document), XContentType.JSON);
            UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
            return Objects.equals(updateResponse.getResult().getLowercase(),UPDATED);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    private Map<String, Object> getSetting() {
        Map<String, Object> setMapping = new HashMap<>();
        // 分区数、副本数、缓存刷新时间
        setMapping.put("number_of_shards", 10);
        setMapping.put("number_of_replicas", 1);
        setMapping.put("refresh_interval", "5s");
        return setMapping;
    }

    /**
     * 批量添加索引
     * @param indexName
     * @param documents
     * @return
     */
    public boolean bulkIndexRequest(String indexName,List<Document> documents){
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout(TimeValue.timeValueSeconds(60));
        for (Document document : documents){
            IndexRequest indexRequest = new IndexRequest(indexName);
            indexRequest.id(document.getId().toString())
                    .source(JSON.toJSONString(document), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

            System.out.println(bulkResponse.status().getStatus());
            if (bulkResponse.getItems() != null){
                for (BulkItemResponse bulkItemResponse : bulkResponse.getItems()){
                    System.out.println(bulkItemResponse.getFailureMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(),e);
        }
        return !bulkResponse.hasFailures();
    }
}
