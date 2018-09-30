package com.elasticsearch.cn.elasticsearch.service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 类描述：kafka测试
 * 创建人：simonsfan
 * 创建时间：2018/9/29 17:33
 */
@Service
public class KafkaServiceImpl implements KafkaService{

    private static final Logger logger = LoggerFactory.getLogger(KafkaServiceImpl.class);

    private static final String TEST_TOPIC = "test_kafka_topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = TEST_TOPIC)
    public void messageConsumerHandler(String content){

        logger.info("进入kafka消费队列==========content：{}",content);

    }

    @Override
    public void messageSend(){
        logger.info("开始给kafka发送==========");

        kafkaTemplate.send(TEST_TOPIC,"我是推送给kafka的消息~");
    }


}
