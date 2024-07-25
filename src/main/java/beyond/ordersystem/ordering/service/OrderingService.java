package beyond.ordersystem.ordering.service;

import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.member.repository.MemberRepository;
import beyond.ordersystem.ordering.domain.OrderDetail;
import beyond.ordersystem.ordering.domain.OrderStatus;
import beyond.ordersystem.ordering.domain.Ordering;
import beyond.ordersystem.ordering.dto.OrderCreateReqDto;
import beyond.ordersystem.ordering.repository.OrderDetailRepository;
import beyond.ordersystem.ordering.repository.OrderingRepository;
import beyond.ordersystem.product.domain.Product;
import beyond.ordersystem.product.repository.ProductRepository;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;


    public OrderingService(OrderingRepository orderingRepository, OrderDetailRepository orderDetailRepository, MemberRepository memberRepository, ProductRepository productRepository) {
        this.orderingRepository = orderingRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
    }

    /**
     * 주문 등록
     */
    @Transactional
    public Ordering createOrder(OrderCreateReqDto dto) {

        // ========================(1) ordering========================
        // createOrder에서 toEntity하는 대신 여기서 (복잡해서)
        // builder에 memberm 채우기
        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(() -> new EntityNotFoundException("not found"));

        // builder 생성
        Ordering ordering = Ordering.builder()
                .member(member)
                .orderStatus(OrderStatus.ORDERED)
                .build();

        // ========================(2) orderDetail========================
        for (OrderCreateReqDto.OrderProductInfo orderDto : dto.getOrderProducts()) {

            Product product = productRepository.findById(orderDto.getProductId()).orElseThrow(()->new EntityNotFoundException("not found"));

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .quantity(orderDto.getProductCnt())
                    .build();

            ordering.getOrderDetails().add(orderDetail);

        }

        // 생성한 ordering 객체 save하기
        Ordering savedOrdering = orderingRepository.save(ordering);




        return savedOrdering;

    }
}
