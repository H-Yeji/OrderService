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

    private Long memberId;

    private List<OrderProductInfo> orderProducts;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderProductInfo {
        private Long productId;
        private Integer productCnt;
    }

//    public Ordering toEntity() {
//
//        Ordering ordering = Ordering.builder()
//                .orderStatus(OrderStatus.ORDERED)
//                .member()
//                .build();
//    }

//    public Ordering orderingToEntity(Member member) {

//        Ordering ordering = Ordering.builder()
//                .member(member)
//                .orderStatus(OrderStatus.ORDERED)
//                .orderDetails()
//                .build();
//        return ordering;
//    }
//    public OrderDetail orderDetilToEntity() {
//        OrderDetail orderDetail = OrderDetail.builder()
//
//                .build();
//
//        return orderDetail;
//    }
}
