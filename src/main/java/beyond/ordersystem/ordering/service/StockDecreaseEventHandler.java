package beyond.ordersystem.ordering.service;

import beyond.ordersystem.common.configs.RabbitMqConfig;
import beyond.ordersystem.ordering.dto.StockDecreaseEvent;
import beyond.ordersystem.product.domain.Product;
import beyond.ordersystem.product.repository.ProductRepository;
import beyond.ordersystem.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class StockDecreaseEventHandler {

    @Autowired
    private RabbitTemplate rabbitTemplate; // 만든 rabbitTemplate 주입
    @Autowired
    private ProductService productService;

    public void publish(StockDecreaseEvent event) { // q에 넣기
        // 큐이름, 몇개 뺄건지 형식의 내용 -> 괄호 안에 들어갈 것
        rabbitTemplate.convertAndSend(RabbitMqConfig.STOCK_DECREASE_QUEUE, event);
    }

    // 트랜잭션이 완료된 이후에 그 다음 메시지 수신하므로, 동시셩 이슈 발생x
    @Transactional
    @RabbitListener(queues = RabbitMqConfig.STOCK_DECREASE_QUEUE)
    public void listen(Message message) throws JsonProcessingException { // 요청이 들어오면 여기서 rdb 업데이트
        String messageBody = new String(message.getBody());
        log.info("messageBody : " + messageBody); // messageBody : {"productId":2,"productCnt":1}

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // json 메시지를 parsing (직렬화 -> 객체로 만들어줌)
            StockDecreaseEvent stockDecreaseEvent = objectMapper.readValue(messageBody, StockDecreaseEvent.class);
            // 재고 update
            productService.stockDecrease(stockDecreaseEvent);
        } catch (JsonProcessingException e) { // 트랜잭션이 터지면 -> 큐에 다시 집어넣어짐 (자동으로 넣어줌)
            throw new RuntimeException(e); // unchecked
        }
    }
}
