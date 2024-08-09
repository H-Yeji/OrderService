package beyond.ordersystem.ordering.controller;

import beyond.ordersystem.ordering.dto.OrderListResDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 여기는 서버가 요청을 받으면 유저a와 연결맺어주는 코드 => 연결 요청은 프론트에서 진행
 */
@RestController
@Slf4j
public class SSEController implements MessageListener {

    // sseEmitter : 연결된 사용자의 정보(ip, 위치정보 등) 의미 => 사용자와 서버와 연결되어야함
    // ConcurrentHashMap : thread-safe한 map (멀티스레드 상황에서 동시성 이슈가 발생하지 않음)
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 여러번 구독을 방지하기 위한 concurrentHashSet 변수 생성
    private Set<String> subscribeList = ConcurrentHashMap.newKeySet();

    @Qualifier("4")
    private final RedisTemplate<String, Object> sseRedisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    public SSEController(@Qualifier("4") RedisTemplate<String, Object> sseRedisTemplate, RedisMessageListenerContainer redisMessageListenerContainer) {
        this.sseRedisTemplate = sseRedisTemplate;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }

    /**
     * email에 해당되는 메시지를 listen 하는 listener를 추가함
     */
    public void subscribeChannel(String email) {
        // 이미 구독한 email일 경우 더이상 구독하지 않도록 분기처리
        if (!subscribeList.contains(email)) {
            MessageListenerAdapter listenerAdapter = createListenerAdapter(this);
            redisMessageListenerContainer.addMessageListener(listenerAdapter, new PatternTopic(email));
            subscribeList.add(email);
        }
    }

    private MessageListenerAdapter createListenerAdapter(SSEController sseController) {
        return new MessageListenerAdapter(sseController, "onMessage");
    }


    @GetMapping("/subscribe") // a : 나 너랑 연결하고싶어 (서버한테)
    public SseEmitter subscribe() {
        // 사용자 정보 emitter에 담기
        SseEmitter emitter = new SseEmitter(14400 * 60 * 1000L); // sse정보가 유효한 유효시간 설정
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // 로그인한 사용자 정보 겟
        emitters.put(email, emitter); //email을 key로해서 emitter(사용자정보)를 put함
        // emiitters는 사용자의 정보 "목록"을 가지고 있는 것

        // remove
        emitter.onCompletion(()->emitters.remove(email));
        emitter.onTimeout(()->emitters.remove(email));

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected!!!!!"));
            // a한테 서버가 "연결됐엉" 하고 알려줌 => 실질적인 메시지 : data(object)부분

        } catch(IOException e) {
            e.printStackTrace();
        }
        subscribeChannel(email);
        // a가 프론트앤드 로그인하면 /subscribe -> connect 됐어 알려줌
        // redis에 해당 email을 listen하겠다 선언 (email들어오면 listen 하려고 email만 바라보는중)
        // 근데 리슨하면 onmessage 실행되게 해둠
        // b가 로그인하고나서 주문을 넣었음 > a:주문목록 이라고 redis에 메시지 발행

        return emitter;
    }

    public void publishMessage(OrderListResDto dto, String email) {

        SseEmitter emitter = emitters.get(email); // emitter정보 get (admin인 사용자a의 정보 찾기 -> admin이 요청했으니까)
//        if (emitter != null) {
//            try {
//                emitter.send(SseEmitter.event().name("ordered").data(dto));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        } else { // emitter를 안가지고 있으면 redis에 던지겠다
            // redis를 활용해 pub/sub
            sseRedisTemplate.convertAndSend(email, dto); // 직렬화해서 보내겠다
//        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) { // implements method한 부분
        // message 내용 parsing
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            OrderListResDto dto = objectMapper.readValue(message.getBody(), OrderListResDto.class);
            log.info("리 슨 중 ㅋ");
            log.info("dto: {}", dto);

            String email = new String(pattern, StandardCharsets.UTF_8); // pattern 안에 들어있는 email
            SseEmitter emitter = emitters.get(email);

            if (emitter != null) {
                emitter.send(SseEmitter.event().name("ordered").data(dto));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
