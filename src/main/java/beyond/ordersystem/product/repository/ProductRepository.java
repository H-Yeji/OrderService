package beyond.ordersystem.product.repository;

import beyond.ordersystem.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

//    Page<Product> findAll(Pageable pageable); // 이미 구현되어있음 jpa안에
}
