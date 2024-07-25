package beyond.ordersystem.member.domain;

import beyond.ordersystem.common.domain.Address;
import beyond.ordersystem.common.domain.BaseTimeEntity;
import beyond.ordersystem.member.dto.MemberListResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

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
    private Role role;
    //@ColumnDefault("'USER'")
    //@Builder.Default()
    //private Role role = Role.valueOf("USER");

    public MemberListResDto listFromEntity() {

        MemberListResDto memberListResDto = MemberListResDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .address(this.address)
                .build();

        return memberListResDto;
    }

}
