package beyond.ordersystem.ordering.controller;

import beyond.ordersystem.common.dto.CommonResDto;
import beyond.ordersystem.ordering.domain.Ordering;
import beyond.ordersystem.ordering.dto.OrderCreateReqDto;
import beyond.ordersystem.ordering.dto.OrderListResDto;
import beyond.ordersystem.ordering.dto.OrderResDto;
import beyond.ordersystem.ordering.service.OrderingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.util.List;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderingController {

    private final OrderingService orderingService;

    @Autowired
    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }

    /**
     * 주문 등록
     */
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody List<OrderCreateReqDto> dto) {

        Ordering ordering = orderingService.createOrder(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "정상완료", ordering.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> orderList() {

        List<OrderListResDto> orderListResDtos = orderingService.orderList();

        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "정상 조회 완료", orderListResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    /**
     * 내 주문만 볼 수 있는 myOrders : order/myorders
     */
    @GetMapping("/myorders")
    public ResponseEntity<?> myOrders() {

        log.info("컨트롤러 시작");
        List<OrderListResDto> orderResDtos = orderingService.myOrders();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "정상 조회 완료", orderResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    /**
     * admin 사용자가 주문 취소
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/cancel") // id = 주문id
    public ResponseEntity<?> orderCancel(@PathVariable Long id) {
        Ordering ordering = orderingService.orderCancel(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "정상 취소", ordering.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

}
