package com.elasticsearch.cn.elasticsearch.search;

import com.elasticsearch.cn.elasticsearch.ElasticsearchApplicationTests;
import com.elasticsearch.cn.elasticsearch.service.search.SearchService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SearchSearchTest extends ElasticsearchApplicationTests {

    @Autowired
    private SearchService searchService;

    @Test
    public void testSearch() throws InterruptedException {
/*        long houseId = 15l;
        searchService.index(houseId);*/
        long houseId = 15l;
        searchService.remove(houseId);
    }

}
