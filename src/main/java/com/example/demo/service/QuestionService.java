package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.demo.model.Question;
import com.example.demo.model.Reply;

/**
 * {@link Service} class abstracting the datastore interaction logic. A {@link ConcurrentHashMap} is used as an in-memory data store.
 */
@Service
public class QuestionService {

    /**
     * {@link Map} to be used as in memory datastore.
     */
    private ConcurrentHashMap<Long, Question> dataStore = new ConcurrentHashMap<>();

    /**
     * A tracker used to track primary key of {@link Question}s in in-memory datastore.
     */
    private Long QUESTION_ID_TRACKER = 0L;

    /**
     * A tracker used to track primary key of {@link Reply}s in in-memory datastore.
     */
    private Long REPLY_ID_TRACKER = 0L;

    
    /**
     * Saves a new {@link Question} to datastore. As this is update data operation,
     * it is synchronized to prevent multiple threads from interfering with each other
     * and corrupting data.
     *
     * @param question {@link Question} to be asked in the forum
     * @return question saved in dataStore and populated with questionId
     */
    public synchronized Question addQuestion(Question question) {
        Question savedQuestion = Question.builder()
                .id(++QUESTION_ID_TRACKER).author(question.getAuthor()).message(question.getMessage()).replies(new ArrayList<>())
                .build();
        dataStore.put(savedQuestion.getId(), savedQuestion);
        return savedQuestion;
    }

    /**
     * Retrieves a list of all the {@link Question}s from dataStore.
     *
     * @return list of all the {@link Question}s if present in dataStore, or empty list.
     */
    public List<Question> getAllQuestions() {
        Collection<Question> questions = dataStore.values();
        return new ArrayList<>(questions);
    }

    /**
     * Retrieves a {@link Question} from datastore based on its id or null if questionId is missing.
     *
     * @param questionId ID of questionId to return
     * @return question from datastore based on questionId or null if the question corresponding to this id is missing
     */
    public Question getQuestionById(Long questionId) {
        return dataStore.get(questionId);
    }

    /**
     * Add a reply to a particular question. The reply is added to the replies collection of question represented by questionId.
     * 
     * As this is update data operation,
     * it is synchronized to prevent multiple threads from interfering with each other
     * and corrupting data.
     *
     * @param questionId ID of question to which this reply is answered
     * @param reply Reply to the question
     * @return reply saved in dataStore and populated with replyId or null if the question corresponding to questionId doesn't exist
     */
    public synchronized Reply replyToQuestion(Long questionId, Reply reply) {
        
        Question questionToReply = dataStore.get(questionId);
        if(questionToReply == null)
            return null;
        Reply savedReply = Reply.builder()
                .id(++REPLY_ID_TRACKER).author(reply.getAuthor()).message(reply.getMessage()).questionId(questionId)
                .build();
        questionToReply.getReplies().add(savedReply);
        return savedReply;

    }

}
