package beyond.ordersystem.member.domain;

import beyond.ordersystem.common.domain.Address;
import beyond.ordersystem.common.domain.BaseTimeEntity;
import beyond.ordersystem.member.dto.MemberResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    public MemberResDto listFromEntity() {

        MemberResDto memberResDto = MemberResDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .address(this.address)
                .build();

        return memberResDto;
    }

    public MemberResDto myDetailFromEntity(String myEmail, String myName, Address myAddress) {
        MemberResDto memberResDto = MemberResDto.builder()
                .name(myName)
                .email(myEmail)
                .address(myAddress)
                .build();

        return memberResDto;
    }

}
