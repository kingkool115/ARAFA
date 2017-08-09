package util;

/**
 * This class represents an answer instance in DB.
 * */
public class Answer {

    private int id;
    private int questionId;
    private String answer;

    /**
     * Constructor.
     * */
    public Answer(int id, int questionId, String answer) {
        this.id = id;
        this.questionId = questionId;
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getId() {
        return id;
    }
}