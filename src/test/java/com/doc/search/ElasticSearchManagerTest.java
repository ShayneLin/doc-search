package com.doc.search;

import com.alibaba.fastjson.JSON;
import com.doc.search.entity.Document;
import com.doc.search.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.doc.search.constant.IndexConst.INDEX_NAME;

@Slf4j
@SpringBootTest
public class ElasticSearchManagerTest {

    @Autowired
    private RestHighLevelClient client;

    @Test
    public void creatIndex() throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX_NAME);
        CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }

    @Test
    public void testIndexExist() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(INDEX_NAME);
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    @Test
    public void delete() throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(INDEX_NAME);
        AcknowledgedResponse delete = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());
    }

    @Test
    public void testAddDocument() throws IOException {
//        File file = new File("E:\\极客时间\\已完结的课程\\23 技术与商业案例解读-徐飞\\180209-056 _ 互联网第一股雅虎的兴衰：没有救世主.html");
        File file = new File("E:\\极客时间\\已完结的课程\\30 软件测试52讲\\html\\29讲聊聊性能测试的基本方法与应用领域.html");
        Document svnDocument = new Document();
        svnDocument.setId(2);
        svnDocument.setName(file.getName());
        svnDocument.setDocUrl(file.getAbsolutePath());
        svnDocument.setDescription("测试一下");
        svnDocument.setModifyTime(new Date(file.lastModified()));

        IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
        indexRequest.id("1")
                .timeout(TimeValue.timeValueSeconds(10))
                .source(JSON.toJSONString(svnDocument), XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(indexResponse.getIndex());
        System.out.println(indexResponse.getResult().getLowercase());

    }

    @Test
    public void testBulkAddDocument() throws IOException {
        List<File> fileList =  FileUtil.getAllFileList("E:\\极客时间\\已完结的课程\\");
        int pageSize = 50;
        List<Document> batchDocument = new ArrayList<>();
        for (int i = 1;i <= fileList.size();i++){
            if (i == fileList.size() || (i % pageSize) == 0){
                //每页50个
                BulkRequest bulkRequest = new BulkRequest();
                bulkRequest.timeout(TimeValue.timeValueSeconds(60));
                for (Document svnDocument : batchDocument){
                    IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
                    indexRequest.id(svnDocument.getId().toString())
                            .source(JSON.toJSONString(svnDocument), XContentType.JSON);
                    bulkRequest.add(indexRequest);
                }
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                if (!bulkResponse.hasFailures()){
                    System.out.println("批量插入成功");
                }

                //插入后需要清空
                batchDocument.clear();
            }else {
                File file = fileList.get(i);
                Document svnDocument = new Document();
                svnDocument.setId(i);
                svnDocument.setName(file.getName());
                svnDocument.setDocUrl(file.getAbsolutePath());
                svnDocument.setDescription(file.getName());
                svnDocument.setModifyTime(new Date(file.lastModified()));
                batchDocument.add(svnDocument);
            }

        }
    }

    @Test
    public void query() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.matchQuery("docUrl", "性能测试"));
        searchSourceBuilder.query(boolQueryBuilder);
        System.out.println("查询语句:" + searchSourceBuilder.toString());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        searchResponse.getHits().forEach(documentFields -> {
            System.out.println("查询结果:" + documentFields.getSourceAsMap());
        });
    }


    @Test
    public void queryWithHighLight() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.matchQuery("docUrl", "性能测试"));
        searchSourceBuilder.query(boolQueryBuilder);

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("docUrl");
        searchSourceBuilder.highlighter(highlightBuilder);

        System.out.println("查询语句:" + searchSourceBuilder.toString());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        searchResponse.getHits().forEach(documentFields -> {
            System.out.println("查询结果:" + documentFields.getSourceAsMap());
            documentFields.getHighlightFields().entrySet().stream().forEach((entry)->{
                System.out.println(entry.getValue());
            });
        });
    }





}
