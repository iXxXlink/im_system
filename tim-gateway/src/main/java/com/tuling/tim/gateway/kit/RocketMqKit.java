package com.tuling.tim.gateway.kit;
import java.io.IOException;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocketMqKit {
    private static final Logger logger = LoggerFactory.getLogger(ProducerExample.class);
    private String endpoint = "localhost:8081";
    private String topic = "YourTopic";
    private ClientServiceProvider provider;
    private ClientConfigurationBuilder builder;
    private ClientConfiguration configuration;
    private Producer producer;
    private String key = "messageKey";
    private String tag = "messageTag";
    private void initMq(){
        provider = ClientServiceProvider.loadService();
        builder = ClientConfiguration.newBuilder().setEndpoints(endpoint);
        configuration = builder.build();
        producer = provider.newProducerBuilder()
                .setTopics(topic)
                .setClientConfiguration(configuration)
                .build();
    }
    private void sendMsgToMq(String msg){
        Message message = provider.newMessageBuilder()
                .setTopic(topic)
                .setKeys(key)
                .setTag(tag)
                .setBody(msg.getBytes())
                .build();
        try {
            SendReceipt sendReceipt = producer.send(message);
            logger.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        } catch (ClientException e) {
            logger.error("Failed to send message", e);
        }
    }
}

