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
    // size 어노테이션에서 길이 조건을 걸어놔서 handler에서 MethodArgumentNotValidException 에러가 터짐
    // 주석처리하면 비밀번호가 너무 짧습니다 (service에서 걸어둔거)가 나옴
    private String password;
    private String city;
    private String street;
    private String zipcode;
    private Address address;
    private Role role = Role.USER;

    public Member toEntity(String encodedPassword) {
        Member member = Member.builder()
                .name(this.name)
                .email(this.email)
                .password(encodedPassword)
//                .address(Address.builder()
//                        .city(this.city)
//                        .street(this.street)
//                        .zipcode(this.zipcode)
//                        .build())
                .address(this.address)
                .role(this.role)
                .build();
        return member;
    }


}
