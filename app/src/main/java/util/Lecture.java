package util;

/**
 * Created by gest3747 on 19.06.17.
 */

public class Lecture {

    private String name;
    private int id;


    /**
     * Constructor used to display list view.
     * */
    public Lecture(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
