package com.java3y.austin.handler.receiver.rabbit;

import com.alibaba.fastjson.JSON;
import com.java3y.austin.common.domain.TaskInfo;
import com.java3y.austin.handler.receiver.service.ConsumeService;
import com.java3y.austin.support.constans.MessageQueuePipeline;
import com.java3y.austin.support.domain.MessageTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author xzcawl
 * @date 23-04-21 10:53:32
 */
@Component
@ConditionalOnProperty(name = "austin.mq.pipeline", havingValue = MessageQueuePipeline.RABBIT_MQ)
public class RabbitMqReceiver {

    @Autowired
    private ConsumeService consumeService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${spring.rabbitmq.queues}", durable = "true"),
            exchange = @Exchange(value = "${austin.rabbitmq.exchange.name}", type = ExchangeTypes.TOPIC),
            key = "${austin.rabbitmq.routing.key}"
    ))
    public void onMessage(Message message) {
        String messageType = message.getMessageProperties().getHeader("messageType");
        byte[] body = message.getBody();
        String messageContent = new String(body);
        if (StringUtils.isBlank(messageContent)) {
            return;
        }
        if ("send".equals(messageType)) {
            // 处理发送消息
            List<TaskInfo> taskInfoLists = JSON.parseArray(messageContent, TaskInfo.class);
            consumeService.consume2Send(taskInfoLists);
        } else if ("recall".equals(messageType)) {
            // 处理撤回消息
            MessageTemplate messageTemplate = JSON.parseObject(messageContent, MessageTemplate.class);
            consumeService.consume2recall(messageTemplate);
        }

    }

}
