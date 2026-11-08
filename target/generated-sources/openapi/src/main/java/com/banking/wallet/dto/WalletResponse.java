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
 * WalletResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-08T17:02:27.470471600-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class WalletResponse {

  private UUID id;

  private String phoneNumber;

  private String email;

  private WalletStatus status;

  private UUID linkedDebitCardId;

  private String balance;

  private String currency;

  public WalletResponse id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  */
  @Valid 
  @Schema(name = "id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public WalletResponse phoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
    return this;
  }

  /**
   * Get phoneNumber
   * @return phoneNumber
  */
  
  @Schema(name = "phoneNumber", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("phoneNumber")
  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public WalletResponse email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
  */
  
  @Schema(name = "email", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public WalletResponse status(WalletStatus status) {
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

  public WalletResponse linkedDebitCardId(UUID linkedDebitCardId) {
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

  public WalletResponse balance(String balance) {
    this.balance = balance;
    return this;
  }

  /**
   * Get balance
   * @return balance
  */
  
  @Schema(name = "balance", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("balance")
  public String getBalance() {
    return balance;
  }

  public void setBalance(String balance) {
    this.balance = balance;
  }

  public WalletResponse currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
  */
  
  @Schema(name = "currency", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("currency")
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WalletResponse walletResponse = (WalletResponse) o;
    return Objects.equals(this.id, walletResponse.id) &&
        Objects.equals(this.phoneNumber, walletResponse.phoneNumber) &&
        Objects.equals(this.email, walletResponse.email) &&
        Objects.equals(this.status, walletResponse.status) &&
        Objects.equals(this.linkedDebitCardId, walletResponse.linkedDebitCardId) &&
        Objects.equals(this.balance, walletResponse.balance) &&
        Objects.equals(this.currency, walletResponse.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, phoneNumber, email, status, linkedDebitCardId, balance, currency);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WalletResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    phoneNumber: ").append(toIndentedString(phoneNumber)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    linkedDebitCardId: ").append(toIndentedString(linkedDebitCardId)).append("\n");
    sb.append("    balance: ").append(toIndentedString(balance)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
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

