package com.banking.wallet.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonValue;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets DocumentType
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-08T08:23:06.302411700-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public enum DocumentType {
  
  DNI("DNI"),
  
  CEX("CEX"),
  
  PASSPORT("PASSPORT");

  private String value;

  DocumentType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static DocumentType fromValue(String value) {
    for (DocumentType b : DocumentType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

