package beyond.ordersystem.ordering.controller;

import beyond.ordersystem.common.dto.CommonResDto;
import beyond.ordersystem.ordering.domain.Ordering;
import beyond.ordersystem.ordering.dto.OrderCreateReqDto;
import beyond.ordersystem.ordering.dto.OrderListResDto;
import beyond.ordersystem.ordering.service.OrderingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.util.List;

@RestController
@RequestMapping("/order")
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
    public ResponseEntity<?> createOrder(@RequestBody OrderCreateReqDto dto) {

        Ordering ordering = orderingService.createOrder(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "정상완료", ordering.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<?> orderList() {

        List<OrderListResDto> orderListResDtos = orderingService.orderList();

        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "정상 조회 완료", orderListResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


}
