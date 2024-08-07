package beyond.ordersystem.product.service;

import beyond.ordersystem.common.service.StockInventoryService;
import beyond.ordersystem.ordering.dto.StockDecreaseEvent;
import beyond.ordersystem.product.domain.Product;
import beyond.ordersystem.product.dto.ProductCreateReqDto;
import beyond.ordersystem.product.dto.ProductListResDto;
import beyond.ordersystem.product.dto.ProductSearchDto;
import beyond.ordersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final ProductRepository productRepository;
    private final S3Client s3Client;
    private final StockInventoryService stockInventoryService;

    @Autowired
    public ProductService(ProductRepository productRepository, S3Client s3Client, StockInventoryService stockInventoryService) {
        this.productRepository = productRepository;
        this.s3Client = s3Client;
        this.stockInventoryService = stockInventoryService;
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

            // redis를 이용해 분기처리
            if (dto.getName().contains("sale")) { // sale 상품이면
                stockInventoryService.increaseStock(product.getId(), dto.getStockQuantity());
            }

        } catch (IOException e) { // transaction 처리때문에 예외 던짐
            throw new RuntimeException("이미지 저장 실패");
        }

        return product;
    }

    /**
     * 상품 등록 (파일) - aws에 파일처리
     */
    @Transactional
    public Product productAwsCreate(ProductCreateReqDto dto) {

        MultipartFile image = dto.getProductImage(); // 이미지 받아와
        Product product = null;
        try {
            product = productRepository.save(dto.toEntity());
            byte[] bytes = image.getBytes();
            String fileName = product.getId()+ "_" + image.getOriginalFilename();
            Path path = Paths.get("C:/Users/Playtdata/Desktop/tmp/", fileName); // tmp 파일 경로

            // local pc에 임시 저장
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE); //path에다가 bytes를 저장하겠다

            // aws에 pc에 저장된 파일을 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
            String s3Path = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm(); // 이 filename으로 찾아와라
            product.updateImagePath(s3Path); // dirty checking(변경감지)로 사진 저장

        } catch (IOException e) { // transaction 처리때문에 예외 던짐
            throw new RuntimeException("이미지 저장 실패");
        }

        return product;
    }

    /**
     * 상품 목록 -> 검색기능 추가 (ProductSearchDto)
     */
    public Page<ProductListResDto> productList(ProductSearchDto searchDto, Pageable pageable) {

        // 검색을 위해 Specification 객체를 사용하게따
        // Specification 객체는 복잡한 쿼리를 명세를 이용해 정의하는 방식 -> 쿼리를 쉽게 생성
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (searchDto.getSearchName() != null) {
                    // root : 엔티티의 속성을 접근하기 위한 객체, CriteriaBuilder는 쿼리를 생성하기 위한 객체
                    predicates.add(criteriaBuilder.like(root.get("name"), "%"+searchDto.getSearchName() + "%"));
                }
                if (searchDto.getCategory() != null) {
                    predicates.add(criteriaBuilder.like(root.get("category"), "%"+searchDto.getCategory() + "%"));
                }

                Predicate[] predicateArr = new Predicate[predicates.size()];
                for (int i = 0; i < predicateArr.length; i++) {
                    predicateArr[i] = predicates.get(i);
                }
                // 위 if문 두개를 and 조건으로 연결
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };

        Page<Product> products = productRepository.findAll(specification, pageable);

        Page<ProductListResDto> productListResDtos = products.map(a -> a.listFromEntity());

        return productListResDtos;
    }


    /**
     * 재고 감소 -> rabbit mq
     */
    public void stockDecrease(StockDecreaseEvent event) {

        Product product = productRepository.findById(event.getProductId()).orElseThrow(
                () -> new EntityNotFoundException("product없음")
        );
        product.updateStockQuantity(event.getProductCnt());
    }


}
