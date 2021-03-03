import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * a course has:
 * available terms
 * a name
 * perquisites
 * credits
 * description
 * terms available
 */
public class Course {

    private ArrayList<Integer> terms; //terms a course is available
    private String prerequisites; //prerequisites for this course
    private int credits; //the number of credits for a given course
    private String name; //course code name i.e. BI1010
    private String description; //course description

    public Course(String courseName){ //ensures that a course at least has a name and exists
        this(courseName, 0);
    }

    /**
     * creates a Course object with a name and credits
     * @param courseName - the course name
     * @param credits - the number of credits
     */
    public Course(String courseName, int credits){
        this.name = courseName;
        this.credits = credits;
        prerequisites = "";
        description = "";
    }

    public void setDescription(String describe){
        this.description = describe;
    }

    public void setPerquisites(String prereq){
        prerequisites = prereq;
    }

    public void setCredits(int credits){
        this.credits = credits;
    }

    public String getName(){
        return this.name;
    }
    public int getCredits() {
        return this.credits;
    }
    public String getPrerequisites(){
        return prerequisites;
    }

    public String getDescription() {
        return description;
    }
}
