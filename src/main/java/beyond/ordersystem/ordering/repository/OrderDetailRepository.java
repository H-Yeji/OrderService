package beyond.ordersystem.ordering.repository;

import beyond.ordersystem.ordering.domain.OrderDetail;
import beyond.ordersystem.ordering.domain.Ordering;
import beyond.ordersystem.ordering.dto.OrderResDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrdering(Ordering ordering);
}
