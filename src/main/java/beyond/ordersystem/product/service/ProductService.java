package beyond.ordersystem.product.service;

import beyond.ordersystem.product.domain.Product;
import beyond.ordersystem.product.dto.ProductCreateReqDto;
import beyond.ordersystem.product.dto.ProductListResDto;
import beyond.ordersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 상품 등록 ⭐ 파일 등록 ⭐⭐⭐
     */
    @Transactional
    public Product createProduct(ProductCreateReqDto dto) {

        MultipartFile image = dto.getProductImage(); // 이미지 받아와
        Product product = null;
        try {
            product = productRepository.save(dto.toEntity());
            byte[] bytes = image.getBytes();
            Path path = Paths.get("C:/Users/Playtdata/Desktop/tmp/"
                    , product.getId()+ "_" + image.getOriginalFilename()); // tmp 파일 경로

            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE); //path에다가 bytes를 저장하겠다
            product.updateImagePath(path.toString()); // dirty checking(변경감지)로 사진 저장
            // => 다시한번 save 할 필요 없어서 위에서 save 먼저함 : product.getId 받아오기 위해

        } catch (IOException e) { // transaction 처리때문에 예외 던짐
            throw new RuntimeException("이미지 저장 실패");
        }

        return product;
    }

    /**
     * 상품 목록
     */
    public Page<ProductListResDto> productList(Pageable pageable) {

        Page<Product> products = productRepository.findAll(pageable);

        Page<ProductListResDto> productListResDtos = products.map(a -> a.listFromEntity());

        return productListResDtos;
    }


}
