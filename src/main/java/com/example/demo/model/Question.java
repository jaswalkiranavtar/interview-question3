package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Question pojo.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question  {

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
    @JsonProperty("replies")
    @Valid
    private List<Reply> replies = null;

}
