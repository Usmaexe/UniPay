package com.unipay.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class BaseEntityDto {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
