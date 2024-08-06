package beyond.ordersystem.member.domain;

import beyond.ordersystem.common.domain.Address;
import beyond.ordersystem.common.domain.BaseTimeEntity;
import beyond.ordersystem.member.dto.MemberResDto;
import beyond.ordersystem.member.dto.MemberUpdatePwdDto;
import beyond.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.w3c.dom.stylesheets.LinkStyle;

import javax.persistence.*;
import java.util.List;

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

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Ordering> orderingList;

    public MemberResDto listFromEntity() {

        MemberResDto memberResDto = MemberResDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .orderCount(this.orderingList.size())
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

    public Member updateFromEntity(String updatePassword) {

        this.password = updatePassword; // 변경 비밀번호로 업데이트
        return this;
    }

}
