package com.kwanghoon.jpashop.service;

import com.kwanghoon.jpashop.domain.Member;
import com.kwanghoon.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)  // 읽기 전용으로 약건의 최적화 효과를 얻을 수 있음 ex) 더티 체킹 같은 것
@RequiredArgsConstructor
public class MemberService {

//    @Autowired // 변경할 수 있는 방법이 없음
    private final MemberRepository memberRepository;

    /* setter injection */
//    public void setMemberRepository(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    /* constructor injection, 스프링 최신버젼에서는 @Autowired 안붙여도  */
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    /* 회원 가입 */
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member); // 중복 회원 검증
        memberRepository.save(member);

        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        // Exception
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /* 회원 전체 조회 */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
