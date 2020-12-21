package com.kwanghoon.jpashop;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;


// Transactional : Spring에서 지원하는걸 쓰는걸 권장 -> 제공하는 옵션 더 많음

@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @Transactional
    public void testMember() throws Exception{
        // Given
        Member member = new Member();
        member.setUsername("memberA");

        // When
        Long saveId = memberRepository.save(member);
        Member findMember = memberRepository.find(saveId);

        // Then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member); // 같은 트랜잭션내에 있어서 1차 캐시, 같음

    }
}