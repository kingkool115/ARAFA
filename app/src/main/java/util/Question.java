package util;

/**
 * This class represents an question entry in DB.
 * */
public class Question {

    private int id;
    private int lectureId;
    private String name;
    private boolean isTr;
    private boolean isAnswered;
    private String imagePath;

    /**
     * constructor
     * */
    public Question(int id, int lectureId, String name, boolean isTr, boolean isAnswered,
                                                                            String imagePath) {
        this.id = id;
        this.lectureId = lectureId;
        this.name = name;
        this.isTr = isTr;
        this.isAnswered = isAnswered;
        this.imagePath = imagePath;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLectureId() {
        return lectureId;
    }

    public boolean isTr() {
        return isTr;
    }

    public boolean isAnswered() {
        return isAnswered;
    }

    public String getImagePath() {
        return imagePath;
    }
}