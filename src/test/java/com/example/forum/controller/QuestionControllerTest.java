package com.example.forum.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.example.forum.controller.QuestionController;
import com.example.forum.exception.ExceptionAdvice;
import com.example.forum.model.Error;
import com.example.forum.model.Question;
import com.example.forum.model.Reply;
import com.example.forum.service.QuestionService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QuestionControllerTest {

    private MockMvc mockMvc;
    private QuestionService questionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Setup {@link MockMvc} to test {@link QuestionController} and
     * added {@link ExceptionAdvice} as a {@link ControllerAdvice}.
     */
    @BeforeEach
    public void setup() {
        questionService = new QuestionService();
        mockMvc = MockMvcBuilders.standaloneSetup(new QuestionController(questionService))
                .setControllerAdvice(new ExceptionAdvice())
                .build();
    }

    /**
     * Given:
     *      The datastore contains questions.
     * 
     * When:
     *      The GET request is sent to /v2/questions.
     * 
     * Then: 
     *      The request completes with 200 OK status and
     *      returns all the questions in response body.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("questions present | GET /questions | 200 OK and list of questions")
    public void shouldReturnAllQuestionsWhenQuestionsPresent() throws Exception {
        
        questionService.addQuestion(Question.builder().author("John").message("Hello").build());

        MvcResult result = mockMvc.perform(get("/v2/questions"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        List<Question> questions = Arrays.asList(objectMapper.readValue(result.getResponse().getContentAsString(), Question[].class));
        assertThat(questions.get(0).getId()).isEqualTo(1L);
        assertThat(questions.get(0).getAuthor()).isEqualTo("John");
        assertThat(questions.get(0).getMessage()).isEqualTo("Hello");
        assertThat(questions.get(0).getReplies()).isEmpty();

    }

    /**
     * Given:
     *      There are no questions in datastore.
     * 
     * When:
     *      The GET request is sent to /v2/questions.
     * 
     * Then: 
     *      The request completes with 204 No Content status and empty response.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("questions missing | GET /questions | 204 No Content and empty response")
    public void shouldReturnEmptyResponseWhenQuestionsMissing() throws Exception {
        
        MvcResult result = mockMvc.perform(get("/v2/questions"))
            .andDo(print())
            .andExpect(status().isNoContent())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("[]");

    }

    /**
     * When:
     *      The DELETE request is sent to /v2/questions.
     * 
     * Then: 
     *      The request completes with 405 Method Not Allowed status and
     *      returns array of errors in response describing the problem.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("DELETE /questions | 405 Method Not Allowed and error array in response")
    public void shouldReturn405MethodNotFoundWhenDeleteRequestIsSent() throws Exception {
        
        MvcResult result = mockMvc.perform(delete("/v2/questions"))
            .andDo(print())
            .andExpect(status().isMethodNotAllowed())
            .andReturn();

        List<Error> errors = Arrays.asList(objectMapper.readValue(result.getResponse().getContentAsString(), Error[].class));
        assertThat(errors.get(0).getRejectedValue()).isEqualTo(HttpMethod.DELETE.toString());
        assertThat(errors.get(0).getExpectedValue()).contains(Arrays.asList(HttpMethod.GET.toString(), HttpMethod.POST.toString()));

    }

    /**
     * When:
     *      The GET request is sent to /v2/questions with "application/xml" in Accept header.
     * 
     * Then: 
     *      The request completes with 406 Not Acceptable status
     *      and returns missing response body.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("GET /questions Accept:'application/xml' | 406 Not Acceptable and missing response body")
    public void shouldReturn406NotAcceptableWhenGetRequestIsSentWithApplicationXmlAcceptHeader() throws Exception {
        
        MvcResult result = mockMvc.perform(get("/v2/questions")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE))
            .andDo(print())
            .andExpect(status().isNotAcceptable())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("");

    }

    /**
     * When:
     *      The POST request is sent to /v2/questions with {@link Question} in request body.
     * 
     * Then: 
     *      The request completes with 201 Created status and
     *      returns the {@link Question} populated with id in response body.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("POST /questions body:Question | 201 Created and Question with id")
    public void shouldReturnQuestionWithIdWhenPostQuestionRequestIsSent() throws Exception {

        Question question = Question.builder().author("John").message("Hello").build();

        MvcResult result = mockMvc
            .perform(post("/v2/questions").content(objectMapper.writeValueAsString(question))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isCreated())
            .andReturn();

        Question savedQuestion = objectMapper.readValue(result.getResponse().getContentAsString(), Question.class);
        assertThat(savedQuestion.getId()).isEqualTo(1L);
        assertThat(savedQuestion.getAuthor()).isEqualTo("John");
        assertThat(savedQuestion.getMessage()).isEqualTo("Hello");
        assertThat(savedQuestion.getReplies()).isEmpty();

    }

    /**
     * When:
     *      The POST request is sent to /v2/questions with {@link Question}
     *      in request body and Content-Type header missing.
     * 
     * Then: 
     *      The request completes with 415 Unsupported Media Type status
     *      and returns array of errors in response describing the problem.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("POST /questions body:Question Content-Type missing | 415 Unsupported Media Type and error array in response")
    public void shouldReturn415UnsupportedMediaTypeWhenContentTypeHeaderIsMissing() throws Exception {

        Question question = Question.builder().author("John").message("Hello").build();

        MvcResult result = mockMvc
            .perform(post("/v2/questions").content(objectMapper.writeValueAsString(question)))
            .andDo(print())
            .andExpect(status().isUnsupportedMediaType())
            .andReturn();

        List<Error> errors = Arrays.asList(objectMapper.readValue(result.getResponse().getContentAsString(), Error[].class));
        assertThat(errors.get(0).getRejectedValue()).isNull();
        assertThat(errors.get(0).getExpectedValue()).contains(Arrays.asList(MediaType.APPLICATION_JSON_VALUE));

    }

    /**
     * When:
     *      The POST request is sent to /v2/questions with {@link Question}
     *      in request body and invalid Content-Type header.
     * 
     * Then: 
     *      The request completes with 415 Unsupported Media Type status
     *      and returns array of errors in response describing the problem.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("POST /questions body:Question Content-Type application/xml | 415 Unsupported Media Type and error array in response")
    public void shouldReturn415UnsupportedMediaTypeWhenContentTypeHeaderIsInvalid() throws Exception {

        Question question = Question.builder().author("John").message("Hello").build();

        MvcResult result = mockMvc
            .perform(post("/v2/questions").content(objectMapper.writeValueAsString(question))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE))
            .andDo(print())
            .andExpect(status().isUnsupportedMediaType())
            .andReturn();

        List<Error> errors = Arrays.asList(objectMapper.readValue(result.getResponse().getContentAsString(), Error[].class));
        assertThat(errors.get(0).getRejectedValue()).isEqualTo(MediaType.APPLICATION_XML_VALUE);
        assertThat(errors.get(0).getExpectedValue()).contains(Arrays.asList(MediaType.APPLICATION_JSON_VALUE));

    }

    /**
     * When:
     *      The POST request is sent to /v2/questions with {@link Question}
     *      in request body with invalid Accept header.
     * 
     * Then: 
     *      The request completes with 406 Not Acceptable status
     *      and missing response body.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("POST /questions body:Question Accept application/xml | 406 Not Acceptable and missing response body")
    public void shouldReturn406NotAcceptableWhenAcceptHeaderIsInvalid() throws Exception {

        Question question = Question.builder().author("John").message("Hello").build();

        MvcResult result = mockMvc
            .perform(post("/v2/questions").content(objectMapper.writeValueAsString(question))
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isNotAcceptable())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("");

    }

    /**
     * When:
     *      The POST request is sent to /v2/questions with request body missing.
     * 
     * Then: 
     *      The request completes with 400 Bad Request status and
     *      returns array of errors in response describing the problem.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("POST /questions body:missing | 400 Bad Request and error array in response")
    public void shouldReturn400BadRequestWhenQuestionIsMissingInPostRequest() throws Exception {

        MvcResult result = mockMvc
            .perform(post("/v2/questions").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

        List<Error> errors = Arrays.asList(objectMapper.readValue(result.getResponse().getContentAsString(), Error[].class));
        assertThat(errors.get(0).getObjectName()).isEqualTo("Request Body");

    }

    /**
     * When:
     *      The POST request is sent to /v2/questions with {@link Question}
     *      having blank author and message.
     * 
     * Then: 
     *      The request completes with 400 Bad Request status and
     *      returns array of errors in response describing which fields in request failed validation.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("POST /questions body:author and message blank | 400 Bad Request and error array in response")
    public void shouldReturn400BadRequestWhenQuestionAuthorAndMessageAreBlankInRequestBody() throws Exception {

        Question question = Question.builder().author("").message("").build();
        MvcResult result = mockMvc
            .perform(post("/v2/questions").content(objectMapper.writeValueAsString(question))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

        List<Error> errors = Arrays.asList(objectMapper.readValue(result.getResponse().getContentAsString(), Error[].class));
        assertThat(errors.get(0).getFieldName()).isIn("message", "author");
        assertThat(errors.get(0).getObjectName()).isEqualTo("question");
        assertThat(errors.get(0).getRejectedValue()).isEqualTo("");
        assertThat(errors.get(0).getMessage()).contains("should not be blank");
        assertThat(errors.get(1).getFieldName()).isIn("message", "author");
        assertThat(errors.get(1).getObjectName()).isEqualTo("question");
        assertThat(errors.get(1).getRejectedValue()).isEqualTo("");
        assertThat(errors.get(1).getMessage()).contains("should not be blank");

    }

    /**
     * Given:
     *      The datastore contains question with id 1.
     * 
     * When:
     *      The GET request is sent to /v2/questions/{questionId}.
     * 
     * Then: 
     *      The request completes with 200 OK status and
     *      returns the questions with all replies in response body.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("question present | GET /questions/{questionId} | 200 OK and question with all replies")
    public void shouldReturnQuestionWithAllRepliesWhenQuestionWithIdPresent2() throws Exception {

        questionService.addQuestion(Question.builder().author("John").message("Hello").build());
        questionService.replyToQuestion(1L, Reply.builder().id(1L).author("Jane").message("Hi").questionId(1L).build());
        questionService.replyToQuestion(1L, Reply.builder().id(2L).author("Alice").message("Howdy!").questionId(1L).build());

        MvcResult result = mockMvc.perform(get("/v2/questions/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        Question question = objectMapper.readValue(result.getResponse().getContentAsString(), Question.class);
        assertThat(question.getId()).isEqualTo(1L);
        assertThat(question.getAuthor()).isEqualTo("John");
        assertThat(question.getMessage()).isEqualTo("Hello");
        assertThat(question.getReplies().get(0).getId()).isEqualTo(1L);
        assertThat(question.getReplies().get(0).getAuthor()).isEqualTo("Jane");
        assertThat(question.getReplies().get(0).getMessage()).isEqualTo("Hi");
        assertThat(question.getReplies().get(0).getQuestionId()).isEqualTo(1L);
        assertThat(question.getReplies().get(1).getId()).isEqualTo(2L);
        assertThat(question.getReplies().get(1).getAuthor()).isEqualTo("Alice");
        assertThat(question.getReplies().get(1).getMessage()).isEqualTo("Howdy!");
        assertThat(question.getReplies().get(1).getQuestionId()).isEqualTo(1L);

    }

    /**
     * When:
     *      The GET request is sent to /v2/questions/{questionId}
     *      and questionId is not number.
     * 
     * Then: 
     *      The request completes with 400 Bad Request status and
     *      returns array of errors in response describing the problem.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("GET /questions/{questionId} questionId is not number | 400 Bad Request and error array in response")
    public void shouldReturn400BadRequestWhenRequestedQuestionIdIsNotNumber() throws Exception {

        MvcResult result = mockMvc.perform(get("/v2/questions/abc"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

        List<Error> errors = Arrays.asList(objectMapper.readValue(result.getResponse().getContentAsString(), Error[].class));
        assertThat(errors.get(0).getFieldName()).isEqualTo("questionId");
        assertThat(errors.get(0).getRejectedValue()).isEqualTo("abc");

    }

    /**
     * When:
     *      The GET request is sent to /v2/questions/{questionId}
     *      and {@link Question} with that questionId doesn't exist.
     * 
     * Then: 
     *      The request completes with 404 Not Found status and
     *      empty response body.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("GET /questions/{questionId} questionId doesn't exist | 404 Not Found and empty response")
    public void shouldReturn404NotFoundWhenRequestedQuestionIdDoesntExist() throws Exception {

        MvcResult result = mockMvc.perform(get("/v2/questions/1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("");

    }

    /**
     * Given:
     *      The {@link Question} with questionId exist.
     *      
     * When:
     *      The POST request is sent to /v2/questions/{questionId}/reply
     *      with {@link Reply} in request body.
     * 
     * Then: 
     *      The request completes with 201 Created status and
     *      returns the {@link Reply} populated with id in response body.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("POST /questions/{questionId}/reply body:Reply | 201 Created and Reply with id")
    public void shouldReturnReplyWithIdWhenPostReplyRequestIsSent() throws Exception {

        Question savedQuestion = questionService.addQuestion(Question.builder().author("John").message("Hello").build());
        Reply reply = Reply.builder().author("Jane").message("Hi").build();

        MvcResult result = mockMvc
            .perform(post("/v2/questions/" + savedQuestion.getId() + "/reply").content(objectMapper.writeValueAsString(reply))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isCreated())
            .andReturn();

        Reply savedReply = objectMapper.readValue(result.getResponse().getContentAsString(), Reply.class);
        assertThat(savedReply.getId()).isEqualTo(1L);
        assertThat(savedReply.getAuthor()).isEqualTo("Jane");
        assertThat(savedReply.getMessage()).isEqualTo("Hi");
        assertThat(savedReply.getQuestionId()).isEqualTo(savedQuestion.getId());

    }

    /**
     * Given:
     *      The {@link Question} with questionId exist.
     *      
     * When:
     *      The POST request is sent to /v2/questions/{questionId}/reply
     *      with {@link Reply} in request body which has blank author and message.
     * 
     * Then: 
     *      The request completes with 400 Bad Request status and
     *      returns array of errors in response describing which fields in request failed validation.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("POST /questions/{questionId}/reply body:author and message blank | 400 Bad Request and error array in response")
    public void shouldReturn400BadRequestWhenReplyAuthorAndMessageAreBlankInRequestBody() throws Exception {

        Question savedQuestion = questionService.addQuestion(Question.builder().author("John").message("Hello").build());
        Reply reply = Reply.builder().author("").message("").build();

        MvcResult result = mockMvc
            .perform(post("/v2/questions/" + savedQuestion.getId() + "/reply").content(objectMapper.writeValueAsString(reply))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

        List<Error> errors = Arrays.asList(objectMapper.readValue(result.getResponse().getContentAsString(), Error[].class));
        assertThat(errors.get(0).getFieldName()).isIn("message", "author");
        assertThat(errors.get(0).getObjectName()).isEqualTo("reply");
        assertThat(errors.get(0).getRejectedValue()).isEqualTo("");
        assertThat(errors.get(0).getMessage()).contains("should not be blank");
        assertThat(errors.get(1).getFieldName()).isIn("message", "author");
        assertThat(errors.get(1).getObjectName()).isEqualTo("reply");
        assertThat(errors.get(1).getRejectedValue()).isEqualTo("");
        assertThat(errors.get(1).getMessage()).contains("should not be blank");

    }

    /**
     * Given:
     *      The {@link Question} with questionId doesn't exist.
     * 
     * When:
     *      The POST request is sent to /v2/questions/{questionId}/reply
     *      with {@link Reply} in request body.
     * 
     * Then: 
     *      The request completes with 404 Not Found status and
     *      returns array of errors in response describing the problem.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("POST /questions/{questionId}/reply questionId doesn't exist | 404 Not Found and empty response")
    public void shouldReturn404NotFoundWhenSendingReplyToNonExistentQuestionId() throws Exception {

        Reply reply = Reply.builder().author("Jane").message("Hi").build();

        MvcResult result = mockMvc
            .perform(post("/v2/questions/1/reply").content(objectMapper.writeValueAsString(reply))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("");

    }

    /**
     * When:
     *      The POST request is sent to /v2/questions/{questionId}/reply
     *      and questionId is not number.
     * 
     * Then: 
     *      The request completes with 400 Bad Request status and
     *      returns array of errors in response describing the problem.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("POST /questions/{questionId}/reply questionId is not number | 400 Bad Request and error array in response")
    public void shouldReturn400BadRequestWhenReplyingToQuestionWithNonNumberQuestionId() throws Exception {

        Reply reply = Reply.builder().author("Jane").message("Hi").build();

        MvcResult result = mockMvc
            .perform(post("/v2/questions/abc/reply").content(objectMapper.writeValueAsString(reply))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

        List<Error> errors = Arrays.asList(objectMapper.readValue(result.getResponse().getContentAsString(), Error[].class));
        assertThat(errors.get(0).getFieldName()).isEqualTo("questionId");
        assertThat(errors.get(0).getRejectedValue()).isEqualTo("abc");

    }

    /**
     * Given:
     *      The {@link Question} with questionId exist.
     *      
     * When:
     *      The POST request is sent to /v2/questions/{questionId}/reply with request body missing.
     * 
     * Then: 
     *      The request completes with 400 Bad Request status and
     *      returns array of errors in response describing the problem.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("POST /questions/{questionId}/reply body:missing | 400 Bad Request and error array in response")
    public void shouldReturn400BadRequestWhenReplyIsMissingInPostRequest() throws Exception {

        Question savedQuestion = questionService.addQuestion(Question.builder().author("John").message("Hello").build());

        MvcResult result = mockMvc
            .perform(post("/v2/questions/" + savedQuestion.getId() + "/reply").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();

        List<Error> errors = Arrays.asList(objectMapper.readValue(result.getResponse().getContentAsString(), Error[].class));
        assertThat(errors.get(0).getObjectName()).isEqualTo("Request Body");

    }

    /**
     * When:
     *      The GET request is sent to a non existent resource /v2/abc/def.
     * 
     * Then: 
     *      The request completes with 404 Not Found status and
     *      missing response.
     *      
     * @throws Exception
     */
    @Test
    @DisplayName("GET /abc/def | 404 Not Found and empty response")
    public void shouldReturn404NotFoundWhenRequestingResourceNotImplementedAsEndpoint() throws Exception {

        MvcResult result = mockMvc
            .perform(get("/v2/abc/def"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("");

    }

}
