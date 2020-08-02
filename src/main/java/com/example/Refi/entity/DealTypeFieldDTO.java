package com.example.Refi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealTypeFieldDTO {
  private Long id;
  private Long order;
  private Boolean editable;
  private Boolean mandatoryVisible;
  private Boolean hidden;
  private DealFieldTypeDTO dealFieldType;
}
