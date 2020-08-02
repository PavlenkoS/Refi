package com.example.Refi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealFieldTypeDTO {
    private Long id;
    private Long fieldId;
    private String type;
    private String label;
}
