package com.example.demo.model;

import java.util.List;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reply pojo.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reply {

    @ApiModelProperty(value = "")
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty(required = true, value = "")
    @NotBlank(message = "Author should not be blank")
    @JsonProperty("author")
    private String author;

    @ApiModelProperty(required = true, value = "")
    @NotBlank(message = "Message should not be blank")
    @JsonProperty("message")
    private String message;

    @ApiModelProperty(value = "")
    @JsonProperty("questionId")
    private Long questionId;

}
