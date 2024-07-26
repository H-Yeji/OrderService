package beyond.ordersystem.ordering.domain;

import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.ordering.dto.OrderListResDto;
import beyond.ordersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id")
    private Ordering ordering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;


    // ⭐⭐⭐⭐ OrderListResDto 내부에 "OrderListDto" 함수를 static으로 생성했을 때,
    // 내부 메서드를 먼저 builder로 만들고, 그 결과를 반환 타입을 맞춰준 다음, 그걸 사용해서 (넣어서)
    // OrderListResDto 객체를 builder로 최종 생성
    public OrderListResDto listFromEntity() {
        // static 함수 변수 사용한 builder
        OrderListResDto.OrderDetailDto orderListDto = OrderListResDto.OrderDetailDto.builder()
                .id(this.ordering.getId())
                .productName(this.product.getName())
                .count(this.quantity)
                .build();

        // 반환을 List 형태로 해야해서 new ArrayList로 객체 생성
        List<OrderListResDto.OrderDetailDto> orderListDtoList = new ArrayList<>();
        orderListDtoList.add(orderListDto);

        // 최종 OrderListResDto 객체를 builder로 생성
        OrderListResDto orderListResDto = OrderListResDto.builder()
                .id(this.id)
                .memberEmail(this.ordering.getMember().getEmail())
                .orderStatus(OrderStatus.ORDERED)
                .orderDetailDtos(orderListDtoList)
                .build();

        return orderListResDto;
    }


    /**
     * 목록 service에서 Ordering을 받아 사용했을 때 사용하는 코드
     */
    public OrderListResDto.OrderDetailDto fromEntity() {
        OrderListResDto.OrderDetailDto orderDetailDto = OrderListResDto.OrderDetailDto
                .builder()
                .id(this.id)
                .productName(this.product.getName())
                .count(this.quantity)
                .build();
        return orderDetailDto;
    }
}
