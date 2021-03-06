package com.example.forum.model;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Pojo holding the properties of an error and describing error in detail.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Error {

    @ApiModelProperty(value = "")
    @JsonProperty("fieldName")
    String fieldName;

    @ApiModelProperty(value = "")
    @JsonProperty("objectName")
    String objectName;

    @ApiModelProperty(value = "")
    @JsonProperty("rejectedValue")
    String rejectedValue;

    @ApiModelProperty(value = "")
    @JsonProperty("expectedValue")
    String expectedValue;

    @ApiModelProperty(required = true, value = "")
    @NotBlank(message = "Message should not be blank")
    @JsonProperty("message")
    String message;
}
