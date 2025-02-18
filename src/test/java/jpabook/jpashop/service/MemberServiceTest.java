package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    /**
     * 테스트 요구 사항
     * 1. 회원가입을 성공해야 한다.
     * 2. 회원가입할 때 같은 이름이 있으면 예외가 발생해야 한다.
     */

    @Test
    void 회원가입() {
        //given
        Member member = new Member();
        member.setName("kim");
        // when
        Long id = memberService.join(member);
        // then
        assertEquals(member, memberService.findOne(id));
    }

    @Test
    void 중복_회원_예외() {
        //given
        String name = "kim";
        Member member1 = new Member();
        member1.setName(name);

        Member member2 = new Member();
        member2.setName(name);
        // when
        memberService.join(member1);
        Throwable catchThrow = catchThrowable(() -> memberService.join(member2));
        // then
        assertThat(catchThrow).isInstanceOf(IllegalArgumentException.class);
    }
}