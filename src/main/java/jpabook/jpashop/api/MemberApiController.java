package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.dto.MemberListResponseDto;
import jpabook.jpashop.dto.MemberResult;
import jpabook.jpashop.dto.UpdateMemberRequest;
import jpabook.jpashop.dto.UpdateMemberResponse;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public ResponseEntity<MemberResult> membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberListResponseDto> list = findMembers.stream()
                .map(m -> new MemberListResponseDto(m.getName())).toList();
        return ResponseEntity.ok(new MemberResult<>(list.size(),list));
    }

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public ResponseEntity<UpdateMemberResponse> updateMemberV1(@PathVariable Long id,
                                                               @RequestBody @Valid UpdateMemberRequest request) {
        memberService.updateMember(id, request.getName());
        Member findMember = memberService.findOne(id);
        UpdateMemberResponse response = new UpdateMemberResponse(findMember.getId(), findMember.getName());
        return ResponseEntity.ok(response);
    }

    @Data
    static class CreateMemberRequest{
        @NotEmpty
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
