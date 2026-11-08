package com.banking.wallet.dto;

import java.net.URI;
import java.util.Objects;
import com.banking.wallet.dto.WalletStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * WalletUpdateRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-07T18:58:38.891747100-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class WalletUpdateRequest {

  private UUID linkedDebitCardId;

  private WalletStatus status;

  public WalletUpdateRequest linkedDebitCardId(UUID linkedDebitCardId) {
    this.linkedDebitCardId = linkedDebitCardId;
    return this;
  }

  /**
   * Get linkedDebitCardId
   * @return linkedDebitCardId
  */
  @Valid 
  @Schema(name = "linkedDebitCardId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("linkedDebitCardId")
  public UUID getLinkedDebitCardId() {
    return linkedDebitCardId;
  }

  public void setLinkedDebitCardId(UUID linkedDebitCardId) {
    this.linkedDebitCardId = linkedDebitCardId;
  }

  public WalletUpdateRequest status(WalletStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  @Valid 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public WalletStatus getStatus() {
    return status;
  }

  public void setStatus(WalletStatus status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WalletUpdateRequest walletUpdateRequest = (WalletUpdateRequest) o;
    return Objects.equals(this.linkedDebitCardId, walletUpdateRequest.linkedDebitCardId) &&
        Objects.equals(this.status, walletUpdateRequest.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(linkedDebitCardId, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WalletUpdateRequest {\n");
    sb.append("    linkedDebitCardId: ").append(toIndentedString(linkedDebitCardId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

