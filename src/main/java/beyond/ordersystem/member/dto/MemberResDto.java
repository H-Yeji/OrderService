package beyond.ordersystem.member.dto;

import beyond.ordersystem.common.domain.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberResDto {

    private Long id;
    private String email;
    private String name;
    private int orderCount;
    private Address address;

}
