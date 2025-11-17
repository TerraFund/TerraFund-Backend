package com.example.TerraFund.dto.requests;

import com.example.TerraFund.dto.enums.RoleEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChooseRoleRequest {
    private RoleEnum role;
}
