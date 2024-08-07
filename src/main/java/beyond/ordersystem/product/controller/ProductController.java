package beyond.ordersystem.product.controller;

import beyond.ordersystem.common.dto.CommonResDto;
import beyond.ordersystem.product.domain.Product;
import beyond.ordersystem.product.dto.ProductCreateReqDto;
import beyond.ordersystem.product.dto.ProductListResDto;
import beyond.ordersystem.product.dto.ProductSearchDto;
import beyond.ordersystem.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController (ProductService productService) {
        this.productService = productService;
    }

    /**
     * 상품 등록
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    // 파일도 받는건데 다시 공부 ㄱㄱ (requestBody로 받지 않는 이유 -> 파일때문인데 확인)
    public ResponseEntity<?> createProduct(ProductCreateReqDto dto) {

        Product product = productService.productAwsCreate(dto);

        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "member created successfully", product.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }

//    /**
//     * 상품 목록 조회
//     */
//    @GetMapping("/list")
//    public ResponseEntity<?> productList(Pageable pageable) {
//
//        Page<ProductListResDto> productListResDtos = productService.productList(pageable);
//
//        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "정상조회 완료", productListResDtos);
//        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//
//    }

    /**
     * 상품 목록 조회 -> 검색기능 추가
     */
    @GetMapping("/list")
    public ResponseEntity<?> productList(ProductSearchDto searchDto, Pageable pageable) {

        Page<ProductListResDto> productListResDtos = productService.productList(searchDto, pageable);

        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "정상조회 완료", productListResDtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);

    }
}
