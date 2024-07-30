package beyond.ordersystem.product.service;

import beyond.ordersystem.common.service.StockInventoryService;
import beyond.ordersystem.product.domain.Product;
import beyond.ordersystem.product.dto.ProductCreateReqDto;
import beyond.ordersystem.product.dto.ProductListResDto;
import beyond.ordersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

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
     * 상품 목록
     */
    public Page<ProductListResDto> productList(Pageable pageable) {

        Page<Product> products = productRepository.findAll(pageable);

        Page<ProductListResDto> productListResDtos = products.map(a -> a.listFromEntity());

        return productListResDtos;
    }


}
