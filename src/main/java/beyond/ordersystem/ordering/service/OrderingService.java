package beyond.ordersystem.ordering.service;

import beyond.ordersystem.common.service.StockInventoryService;
import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.member.repository.MemberRepository;
import beyond.ordersystem.ordering.domain.OrderDetail;
import beyond.ordersystem.ordering.domain.OrderStatus;
import beyond.ordersystem.ordering.domain.Ordering;
import beyond.ordersystem.ordering.dto.OrderCreateReqDto;
import beyond.ordersystem.ordering.dto.OrderListResDto;
import beyond.ordersystem.ordering.dto.OrderResDto;
import beyond.ordersystem.ordering.dto.StockDecreaseEvent;
import beyond.ordersystem.ordering.repository.OrderDetailRepository;
import beyond.ordersystem.ordering.repository.OrderingRepository;
import beyond.ordersystem.product.domain.Product;
import beyond.ordersystem.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final StockInventoryService stockInventoryService;
    private final StockDecreaseEventHandler stockDecreaseEventHandler;


    public OrderingService(OrderingRepository orderingRepository, OrderDetailRepository orderDetailRepository, MemberRepository memberRepository, ProductRepository productRepository, StockInventoryService stockInventoryService, StockDecreaseEventHandler stockDecreaseEventHandler) {
        this.orderingRepository = orderingRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.stockInventoryService = stockInventoryService;
        this.stockDecreaseEventHandler = stockDecreaseEventHandler;
    }

    /**
     * 주문 등록
     */
    // public Synchronize Ordering createOrder() 걸어보기 -> 한번에 하나씩 접근하게
    // 근데 이렇게 설정한다고 하더라도, 재고 감소가 db에 반영되는 시점은 트랜잭션이 커밋되고 종료되는 시점
    @Transactional
    public Ordering createOrder(List<OrderCreateReqDto> dtoList) {
        // 방법2 => jpa 최적화 방식 (orderDetailRepository 사용하지 않은 버전 => cascade.persist랑 같이확인)
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(() -> new EntityNotFoundException("없음"));
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(
                () -> new EntityNotFoundException("없음")
        );

        // orderStatus는 초기화했고, orderDetail은 없다고 가정 (아래서 add하는 방식 사용하기 위해)
        // 즉, member만 builder에 넣어주면 됨 => 이렇게 ordering 객체 생성
        Ordering ordering = Ordering.builder()
                .member(member)
                //.orderDetails()
                .build();


        for(OrderCreateReqDto orderCreateReqDto : dtoList){
            Product product = productRepository.findById(orderCreateReqDto.getProductId()).orElse(null);
            int quantity = orderCreateReqDto.getProductCnt();

            // 구매 가능한지 재고 비교
            if (product.getName().contains("sale")) { // sale인 상품일 때만 redis를 통해 재고관리
                // 동시성 해결 => redis를 통한 재고관리 및 재고 잔량 확인
                int newQuantity = stockInventoryService.decreaseStock(orderCreateReqDto.getProductId(), orderCreateReqDto.getProductCnt()).intValue();
                // 여기서 분기처리 ㄱㄱ
                if (newQuantity < 0) { // 재고가 더 부족할 때 -1L 반환한거
                    throw new IllegalArgumentException("재고 부족");
                }
                // rdb에 재고 업데이트 (product 테이블에 업데이트) => 이전까진 100개수량에서 마이너스가 안되고 있었음
                // rabbitmq를 통해 비동기적으로 이벤트 처리
                stockDecreaseEventHandler.publish(new StockDecreaseEvent(product.getId(), orderCreateReqDto.getProductCnt()));

            } else {
                if (product.getStockQuantity() < quantity) {
                    throw new IllegalArgumentException("재고 부족");
                }
                log.info("재고 확인 (전) : " + product.getStockQuantity());
                product.updateStockQuantity(quantity);
                log.info("재고 확인 (후) : " + product.getStockQuantity());

            }
            // 구매 가능하면 진행
            OrderDetail orderDetail =  OrderDetail.builder()
                    .product(product)
                    .quantity(quantity)
                    // 아직 save가 안됐는데 어떻게 이 위의 ordering이 들어가나? => jpa가 알 아 서 해줌⭐
                    .ordering(ordering)
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        Ordering savedOrdering = orderingRepository.save(ordering);
        return savedOrdering;
    }

    /**
     * 주문 목록
     */
    public List<OrderListResDto> orderList() {

        List<Ordering> orderDetailList = orderingRepository.findAll();

        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for (Ordering ordering : orderDetailList) { // 전체 order 내역에서 진행
            orderListResDtos.add(ordering.listFromEntity());
        }

        return orderListResDtos;
    }

    /**
     * 내 주문 조회
     */
    public List<OrderListResDto> myOrders() {

        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member myMember = memberRepository.findByEmail(myEmail).orElseThrow(
                () -> new EntityNotFoundException("no email")
        );
        log.info("myMember");

        List<Ordering> myOrders = orderingRepository.findByMember(myMember);

        log.info("order Detail list : " + myOrders);
        List<OrderListResDto> orderResDtos = new ArrayList<>();
        for (Ordering ordering : myOrders) { // 본인 주문 내역에서만 진행
            orderResDtos.add(ordering.listFromEntity());
        }
        return orderResDtos;
    }

    /**
     * 주문 취소
     */
    @Transactional
    public Ordering orderCancel(Long id) {

        Ordering ordering = orderingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("not found")
        );

        ordering.updateStatus(OrderStatus.CANCELD); // update로 ordered > canceld로 변경

        return ordering;
    }


//         createOrder에서 방법(1)
//        // 방법1 => 쉬운방식
//        // Ordering생성 : member_id, status
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()->new EntityNotFoundException("없음"));
//        Ordering ordering = orderingRepository.save(dto.toEntity(member));
//
//        // OrderDetail생성 : order_id, product_id, quantity
//        for(OrderCreateReqDto.OrderDto orderDto : dto.getOrderDtos()){
//            Product product = productRepository.findById(orderDto.getProductId()).orElse(null);
//            int quantity = orderDto.getProductCnt();
//            OrderDetail orderDetail =  OrderDetail.builder()
//                    .product(product)
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    .build();
//            orderDetailRepository.save(orderDetail);
//        }
//        return ordering;
}
