package com.kwanghoon.jpashop.Controller;

import com.kwanghoon.jpashop.domain.Member;
import com.kwanghoon.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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
