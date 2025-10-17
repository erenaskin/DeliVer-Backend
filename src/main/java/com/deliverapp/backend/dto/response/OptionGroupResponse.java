package com.deliverapp.backend.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionGroupResponse {
    private Long id;
    private Long productId;
    private String name;
    private String description;
    private String optionType;
    private Boolean isRequired;
    private Boolean isActive;
    private Integer sortOrder;
    private Integer minSelections;
    private Integer maxSelections;
    private List<OptionValueResponse> options;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}