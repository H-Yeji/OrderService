package beyond.ordersystem.member.dto;

import beyond.ordersystem.common.domain.Address;
import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberCreateReqDto {

    private String name;
    @NotEmpty(message = "email is essential") // 에러 터뜨림
    private String email;
    @NotEmpty(message = "password is essential")
    @Size(min = 8, message = "password minimum length is 8")
    private String password;
    private String city;
    private String street;
    private String zipcode;
    private Address address;
    //private Role role;

    public Member toEntity() {
        Member member = Member.builder()
                .name(this.name)
                .email(this.email)
                .password(this.password)
//                .address(Address.builder()
//                        .city(this.city)
//                        .street(this.street)
//                        .zipcode(this.zipcode)
//                        .build())
                .address(this.address)
                .role(Role.USER) // 어노테이션 default가 안먹어서 이걸로 대체(admin 안들어감)
                .build();
        return member;
    }


}
