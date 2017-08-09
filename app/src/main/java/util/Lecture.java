package util;

/**
 * Created by gest3747 on 19.06.17.
 */

public class Lecture {

    private String name;
    private String id;


    /**
     * Constructor used to display list view.
     * */
    public Lecture(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
