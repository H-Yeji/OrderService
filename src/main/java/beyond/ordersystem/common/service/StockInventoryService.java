package beyond.ordersystem.common.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockInventoryService {

    @Qualifier("3") // 주입
    private final RedisTemplate<String, Object> redisTemplate;

    public StockInventoryService( @Qualifier("3") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 상품 등록 시 increaseStock 호출 => 상품 수 증가
     */
    public Long increaseStock(Long itemId, int quantity) {
        // redis가 음수까지 내려갈 경우 -> 추후 재고 upate 상황에서 increase값이 정확하지 않을 수 있음
        // 만약 음수이면, 0으로 세팅하는 로직ㅇㅣ 필요함 ⭐⭐

        // 아래 return 값 => 남아있는 값임. (잔량 값 반환)
        return redisTemplate.opsForValue().increment(String.valueOf(itemId), quantity);
    }

    /**
     * 주문 등록 시 decreaseStock 호출 => 주문 들어오면 상품 수 감소
     */
     public Long decreaseStock(Long itemId, int quantity) {

         // remains : 남아있는 값 (잔량)
         // itemId 상품 몇개 남음 ? -> 꺼내와서 형변환
         Object remains = redisTemplate.opsForValue().get(String.valueOf(itemId)); // redis는 전부 문자열임
         int longRemains = Integer.parseInt(remains.toString());

         // 꺼내온 값과 재고랑 "비교"
         if (longRemains < quantity) { // 꺼내온 값 longRemains와 주문 수량 quantity와 비교하기
             return -1L; // 재고가 더 적으면 -1 반환
         } else {
             // 재고가 더 많으면 => 남아있는 잔량 반환 (주문량 빼려고 검증하는거임 뺄 수 있는지)
             // 요청된 수량만큼 재고가 충분한 경우, redis의 해당 항목의 값으르 1 감소시키고 -> 감소 시킨 후 값을 반환
             return redisTemplate.opsForValue().decrement(String.valueOf(itemId));
         } // => 이 decreaseStock을 호출하는 "주문 등록" 쪽에서 분기처리해서 처리할거임

     }
}
