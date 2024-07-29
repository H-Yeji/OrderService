package beyond.ordersystem.ordering.dto;

import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.ordering.domain.OrderDetail;
import beyond.ordersystem.ordering.domain.OrderStatus;
import beyond.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateReqDto {


    private Long productId;

    private Integer productCnt;

//    private Long memberId;
//
//    private List<OrderDto> orderDtos;
//
//    @Data
//    @Builder
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class OrderDto { // class 내에 만들거라 static, 직접 접근 가능해야해서 public
//        private Long productId;
//        private Integer productCnt;
//    }

    public Ordering toEntity(Member member){
        return Ordering.builder()
                .member(member)
                .orderStatus(OrderStatus.ORDERED)
                .build();
    }
}
