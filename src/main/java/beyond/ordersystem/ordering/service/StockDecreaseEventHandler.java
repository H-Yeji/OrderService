package beyond.ordersystem.ordering.service;

import beyond.ordersystem.common.configs.RabbitMqConfig;
import beyond.ordersystem.ordering.dto.StockDecreaseEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StockDecreaseEventHandler {

    @Autowired
    private RabbitTemplate rabbitTemplate; // 만든 rabbitTemplate 주입

    public void publish(StockDecreaseEvent event) { // q에 넣기
        // 큐이름, 몇개 뺄건지 형식의 내용 -> 괄호 안에 들어갈 것
        rabbitTemplate.convertAndSend(RabbitMqConfig.STOCK_DECREASE_QUEUE, event);
    }

//    @Transactional
//    @RabbitListener(queues = RabbitMqConfig.STOCK_DECREASE_QUEUE)
//    public void listen() { // 요청이 들어오면 여기서 rdb 업데이트
//
//    }
}
