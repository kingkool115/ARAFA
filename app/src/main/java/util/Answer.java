package util;

/**
 * This class represents an answer instance in DB.
 * */
public class Answer {

    private int id;
    private String questionId;
    private String answer;

    /**
     * Constructor.
     * */
    public Answer(int id, String questionId, String answer) {
        this.id = id;
        this.questionId = questionId;
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public String getQuestionId() {
        return questionId;
    }

    public int getId() {
        return id;
    }
}