package com.example.Refi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealTypeDTO {

  private Long id;
  private String dealTypeName;
  private String internalDealIdentifier;
  private String description;
  private LocalDateTime configuredOn;
  private Long baseId;
  private boolean enabled;
  private boolean isDefault;
  private Set<DealTypeFieldDTO> dealTypeField = new HashSet<>();



}
