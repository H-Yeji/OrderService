package beyond.ordersystem.ordering.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 여기는 서버가 요청을 받으면 유저a와 연결맺어주는 코드 => 연결 요청은 프론트에서 진행
 */
@RestController
public class SSEController {

    // sseEmitter : 연결된 사용자의 정보(ip, 위치정보 등) 의미 => 사용자와 서버와 연결되어야함
    // ConcurrentHashMap : thread-safe한 map (멀티스레드 상황에서 동시성 이슈가 발생하지 않음)
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

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
        return emitter;
    }
}
