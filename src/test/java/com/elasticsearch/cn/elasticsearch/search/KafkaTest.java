package com.elasticsearch.cn.elasticsearch.search;

import com.elasticsearch.cn.elasticsearch.ElasticsearchApplicationTests;
import com.elasticsearch.cn.elasticsearch.service.kafka.KafkaService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 项目名称：elasticsearch
 * 类名称：com.elasticsearch.cn.elasticsearch.search
 * 类描述：
 * 创建人：simonsfan
 * 创建时间：2018/9/29 17:48
 */
public class KafkaTest extends ElasticsearchApplicationTests {

    @Autowired
    private KafkaService kafkaService;

    @Test
    public void testKafka(){
        kafkaService.messageSend();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {

        }
    }

}
