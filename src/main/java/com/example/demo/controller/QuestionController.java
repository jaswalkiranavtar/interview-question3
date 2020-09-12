package com.example.demo.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Error;
import com.example.demo.model.Question;
import com.example.demo.model.Reply;
import com.example.demo.service.QuestionService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Validated
@RestController
@RequestMapping("/v2")
public class QuestionController {

    QuestionService questionService;

    public QuestionController(final QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * POST /questions : Create a new question
     *
     * @param body Question to be asked in the forum (required)
     * @return question created (status code 201)
     *         or Bad Request (status code 400)
     */
    @ApiOperation(value = "Create a new question", nickname = "addQuestion", notes = "", response = Question.class, tags = {"question"})
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "question created", response = Question.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Error.class)})
    @PostMapping(value = "/questions", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<Question> addQuestion(@ApiParam(value = "Question to be asked in the forum", required = true) @Valid @RequestBody Question body) {

        return new ResponseEntity<>(questionService.addQuestion(body), HttpStatus.CREATED);
    }

    /**
     * GET /questions : Get a list of questions
     *
     * @return successful operation (status code 200)
     *         or No Content (status code 204)
     */
    @ApiOperation(value = "Get a list of questions", nickname = "getAllQuestions", notes = "", response = Question.class, responseContainer = "List", tags = {"question"})
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = Question.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = Error.class),
        @ApiResponse(code = 204, message = "No Content")})
    @GetMapping(value = "/questions", produces = {"application/json"})
    public ResponseEntity<List<Question>> getAllQuestions() {

        List<Question> allQuestions = questionService.getAllQuestions();
        HttpStatus status = HttpStatus.OK;
        if(allQuestions.isEmpty())
            status = HttpStatus.NO_CONTENT;
        return new ResponseEntity<>(allQuestions, status);

    }

    /**
     * GET /questions/{questionId} : Find thread by ID
     * Returns the question along with all its replies
     *
     * @param questionId ID of questionId to return (required)
     * @return question created (status code 200)
     *         or Not Found (status code 404)
     */
    @ApiOperation(value = "Find thread by ID", nickname = "getQuestionById", notes = "Returns the question along with all its replies", response = Question.class, tags = {"question"})
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "question created", response = Question.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Error.class),
        @ApiResponse(code = 404, message = "Not Found")})
    @GetMapping(value = "/questions/{questionId}", produces = {"application/json"})
    public ResponseEntity<Question> getQuestionById(@ApiParam(value = "ID of questionId to return", required = true) @PathVariable("questionId") Long questionId) {

        Question question = questionService.getQuestionById(questionId);
        HttpStatus status = HttpStatus.OK;
        if(question == null)
            status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(question, status);

    }

    /**
     * POST /questions/{questionId}/reply : Post a reply to a message
     *
     * @param questionId ID of question to which this reply is answered (required)
     * @param body Reply to the question (required)
     * @return reply added to question (status code 201)
     *         or Bad Request (status code 400)
     *         or Not Found (status code 404)
     */
    @ApiOperation(value = "Post a reply to a message", nickname = "replyToQuestion", notes = "", response = Reply.class, tags = {"question"})
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created", response = Reply.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Error.class),
        @ApiResponse(code = 404, message = "Not Found") })
    @PostMapping(value = "/questions/{questionId}/reply", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<Reply> replyToQuestion(@ApiParam(value = "ID of question to which this reply is answered", required=true) @PathVariable("questionId") Long questionId,
            @ApiParam(value = "Reply to the question", required = true) @Valid @RequestBody Reply body) {

        Reply savedReply = questionService.replyToQuestion(questionId, body);
        HttpStatus status = HttpStatus.CREATED;
        if(savedReply == null)
            status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(savedReply, status);

    }

}
