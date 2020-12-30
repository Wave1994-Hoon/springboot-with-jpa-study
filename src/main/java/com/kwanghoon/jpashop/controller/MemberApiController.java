package com.kwanghoon.jpashop.controller;

import com.kwanghoon.jpashop.domain.Member;
import com.kwanghoon.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController // RestController = @Controller + @ResponseBody
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /*
    * 문제점
    * - 화면에 종속적인 엔티티를 갖음 ex) @NotEmpty
    * - 엔티티 스펙이 변경되면 API 도 변경된다.
    * - 해결방안: DTO 필요
    */
    @PostMapping("api/v1/members")
    public CreateMemberResponse saveMemberV1(
        @RequestBody @Valid Member member
    ) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /*
    * 문제점
    * - 로직은 간단하지만, 조회 시 모든 데이터가 외부에 노출된다.
    * - 실무에서는 같은 엔티티에 대해 API 가 용도에 따라 다양하게 만들어짐
    * - 엔티티를 직접 반환하게 될 경우, 한 엔티티에 각각의 APIf 를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
    * - 칼렉샨을 직ㅈ버 반환하면 향후 API 스팩을 변경히기 어렵다. ex) [{},{}, ... ] -> 값을 추가 하기 어려움, { a:[], b:[], ... } 이런식으로 해야함
    */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    /*
    * 장점
    * - 엔티티가 바뀌어도 API 에 입력받는 파라미터 스펙은 바뀌지 않는다.
    * - V1 과 달리 원하는 값을 파라미타로 받을 수 있다.
    */
    @PostMapping("api/v2/members")
    public CreateMemberResponse saveMemberV2(
        @RequestBody @Valid CreateMemberRequest request
    ) {
        Member member = new Member();
        member.setName(request.getName());

        Long memberId =  memberService.join(member);
        return new CreateMemberResponse(memberId);
    }

    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
            .map(m -> new MemberDto(m.getName()))
            .collect(Collectors.toList());

        return new Result(collect);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMeberV2(
        @PathVariable("id") Long id,
        @RequestBody @Valid UpdateMemberRequest request
    ) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);

        return new UpdateMemberResponse(findMember.getId(), findMember.getName());

    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
