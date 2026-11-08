package com.banking.wallet.dto;

import java.net.URI;
import java.util.Objects;
import com.banking.wallet.dto.DocumentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * WalletCreateRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-08T08:23:06.302411700-05:00[America/Lima]", comments = "Generator version: 7.6.0")
public class WalletCreateRequest {

  private DocumentType documentType;

  private String documentNumber;

  private String phoneNumber;

  private String imei;

  private String email;

  public WalletCreateRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public WalletCreateRequest(DocumentType documentType, String documentNumber, String phoneNumber, String email) {
    this.documentType = documentType;
    this.documentNumber = documentNumber;
    this.phoneNumber = phoneNumber;
    this.email = email;
  }

  public WalletCreateRequest documentType(DocumentType documentType) {
    this.documentType = documentType;
    return this;
  }

  /**
   * Get documentType
   * @return documentType
  */
  @NotNull @Valid 
  @Schema(name = "documentType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("documentType")
  public DocumentType getDocumentType() {
    return documentType;
  }

  public void setDocumentType(DocumentType documentType) {
    this.documentType = documentType;
  }

  public WalletCreateRequest documentNumber(String documentNumber) {
    this.documentNumber = documentNumber;
    return this;
  }

  /**
   * Get documentNumber
   * @return documentNumber
  */
  @NotNull @Size(min = 5, max = 20) 
  @Schema(name = "documentNumber", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("documentNumber")
  public String getDocumentNumber() {
    return documentNumber;
  }

  public void setDocumentNumber(String documentNumber) {
    this.documentNumber = documentNumber;
  }

  public WalletCreateRequest phoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
    return this;
  }

  /**
   * Get phoneNumber
   * @return phoneNumber
  */
  @NotNull @Pattern(regexp = "^[0-9]{9,15}$") 
  @Schema(name = "phoneNumber", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("phoneNumber")
  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public WalletCreateRequest imei(String imei) {
    this.imei = imei;
    return this;
  }

  /**
   * Opcional, no confiable como credencial
   * @return imei
  */
  
  @Schema(name = "imei", description = "Opcional, no confiable como credencial", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("imei")
  public String getImei() {
    return imei;
  }

  public void setImei(String imei) {
    this.imei = imei;
  }

  public WalletCreateRequest email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
  */
  @NotNull @jakarta.validation.constraints.Email 
  @Schema(name = "email", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WalletCreateRequest walletCreateRequest = (WalletCreateRequest) o;
    return Objects.equals(this.documentType, walletCreateRequest.documentType) &&
        Objects.equals(this.documentNumber, walletCreateRequest.documentNumber) &&
        Objects.equals(this.phoneNumber, walletCreateRequest.phoneNumber) &&
        Objects.equals(this.imei, walletCreateRequest.imei) &&
        Objects.equals(this.email, walletCreateRequest.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(documentType, documentNumber, phoneNumber, imei, email);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WalletCreateRequest {\n");
    sb.append("    documentType: ").append(toIndentedString(documentType)).append("\n");
    sb.append("    documentNumber: ").append(toIndentedString(documentNumber)).append("\n");
    sb.append("    phoneNumber: ").append(toIndentedString(phoneNumber)).append("\n");
    sb.append("    imei: ").append(toIndentedString(imei)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
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

