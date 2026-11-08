package com.banking.wallet.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * WalletTransferRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-08T17:28:25.351102900-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class WalletTransferRequest {

  private String targetPhoneNumber;

  private String amount;

  private String currency;

  public WalletTransferRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public WalletTransferRequest(String targetPhoneNumber, String amount, String currency) {
    this.targetPhoneNumber = targetPhoneNumber;
    this.amount = amount;
    this.currency = currency;
  }

  public WalletTransferRequest targetPhoneNumber(String targetPhoneNumber) {
    this.targetPhoneNumber = targetPhoneNumber;
    return this;
  }

  /**
   * Get targetPhoneNumber
   * @return targetPhoneNumber
  */
  @NotNull @Pattern(regexp = "^[0-9]{9,15}$") 
  @Schema(name = "targetPhoneNumber", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("targetPhoneNumber")
  public String getTargetPhoneNumber() {
    return targetPhoneNumber;
  }

  public void setTargetPhoneNumber(String targetPhoneNumber) {
    this.targetPhoneNumber = targetPhoneNumber;
  }

  public WalletTransferRequest amount(String amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Get amount
   * @return amount
  */
  @NotNull @Pattern(regexp = "^(?!0+(\\.0+)?$)[0-9]+(\\.[0-9]{1,2})?$") 
  @Schema(name = "amount", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("amount")
  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  public WalletTransferRequest currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
  */
  @NotNull @Pattern(regexp = "^[A-Z]{3}$") 
  @Schema(name = "currency", requiredMode = Schema.RequiredMode.REQUIRED)
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
    WalletTransferRequest walletTransferRequest = (WalletTransferRequest) o;
    return Objects.equals(this.targetPhoneNumber, walletTransferRequest.targetPhoneNumber) &&
        Objects.equals(this.amount, walletTransferRequest.amount) &&
        Objects.equals(this.currency, walletTransferRequest.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetPhoneNumber, amount, currency);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WalletTransferRequest {\n");
    sb.append("    targetPhoneNumber: ").append(toIndentedString(targetPhoneNumber)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
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

