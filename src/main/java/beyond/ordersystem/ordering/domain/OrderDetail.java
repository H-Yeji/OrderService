package beyond.ordersystem.ordering.domain;

import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.ordering.dto.OrderListResDto;
import beyond.ordersystem.ordering.dto.OrderResDto;
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
