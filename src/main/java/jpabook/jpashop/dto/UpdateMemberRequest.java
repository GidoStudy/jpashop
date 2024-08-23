package jpabook.jpashop.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateMemberRequest {

    @NotEmpty
    private String name;
}
