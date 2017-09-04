package util;

/**
 * This class represents an question entry in DB.
 * */
public class Question {

    private String id;
    private int questionIdLars;
    private int lectureId;
    private String sessionId;
    private String question;
    private boolean isTr;
    private boolean isMultiSelect;
    private boolean isAnswered;
    private String imageUrl;

    /**
     * Constructor.
     * */
    public Question(String id, int questionIdLars, int lectureId, String sessionId, String question, boolean isTr,
                    boolean isMultiSelect, boolean isAnswered, String imageUrl) {
        this.id = id;
        this.questionIdLars = questionIdLars;
        this.lectureId = lectureId;
        this.sessionId = sessionId;
        this.question = question;
        this.isTr = isTr;
        this.isMultiSelect = isMultiSelect;
        this.isAnswered = isAnswered;
        this.imageUrl = imageUrl;
    }

    /**
     * Constructor.
     * */
    public Question(String id, int lectureId, String question, String imageUrl, boolean isTr) {
        this.id = id;
        this.lectureId = lectureId;
        this.question = question;
        this.imageUrl = imageUrl;
        this.isTr = isTr;
    }

    public String getId() {
        return id;
    }

    public String getSessionId() {return  sessionId; }

    public int getQuestionIdLarsId() {
        return questionIdLars;
    }

    public String getQuestion() {
        return question;
    }

    public int getLectureId() {
        return lectureId;
    }

    public boolean isTr() {
        return isTr;
    }

    public boolean isMultiSelect() {return isMultiSelect; }

    public boolean isAnswered() {
        return isAnswered;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}