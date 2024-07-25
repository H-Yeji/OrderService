package beyond.ordersystem.ordering.controller;

import beyond.ordersystem.ordering.dto.OrderCreateReqDto;
import beyond.ordersystem.ordering.service.OrderingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public void createOrder(OrderCreateReqDto dto) {

        orderingService.createOrder(dto);
    }

}
