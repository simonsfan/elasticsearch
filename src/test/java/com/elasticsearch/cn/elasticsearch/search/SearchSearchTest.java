package com.elasticsearch.cn.elasticsearch.search;

import com.elasticsearch.cn.elasticsearch.ElasticsearchApplicationTests;
import com.elasticsearch.cn.elasticsearch.service.search.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SearchSearchTest extends ElasticsearchApplicationTests {

    @Autowired
    private SearchService searchService;

    @Test
    public void testSearch(){
        long houseId = 15l;
        searchService.index(houseId);
    }

}
