
/*
 * Course: SE2800 A
 * Winter 2019 - 2020
 * Name: Trenton Bowser
 * Created: 3/29/2020
 */

import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the creation and maintenance of the flowcharts
 */
public class FlowChartHandler {
    private ScrollPane pane;
    private Canvas canvas;
    private Map<String, int[]> courses;
    private Map<String, String> prereqs;
    private ArrayList<ArrayList<String>> major;
    private Color[] colors = {Color.BLACK, Color.GREEN, Color.RED, Color.BLUE};
    boolean showPrereqs; //decides whether or not prereqs will be shown for courses
    boolean showMajors; //tells whether or not the flowchart is displaying a major

    private final static int BOXHEIGHT = 60; //dimension of the box
    private Map<String, Integer> taken;

    /**
     * Constructor for creation and maintenance of the flowcharts
     *
     * @param pane - the scroll pane in which the canvas sits
     */
    public FlowChartHandler(ScrollPane pane) {
        canvas = new Canvas(870, 735);
        this.pane = pane;
        pane.setContent(canvas);
        courses = new HashMap<>();
        prereqs = new HashMap<>();
    }

    /**
     * Displays the graduation plan based on the student's transcript
     *
     * @param majorNorm  - list of major courses
     * @param courses    - map of all offerings
     * @param major      - student's major
     * @param prereqData - prerequisite and other data
     */
    public void displayGraduation(ArrayList<ArrayList<String>> majorNorm,
                                  Map<String, String> courses, String major,
                                  ArrayList<Course> prereqData) {
        GraduationHandler graduationHandler = new GraduationHandler(pane);
        graduationHandler.setCourses(taken, majorNorm, courses, major, prereqData);
    }

    /**
     * Displays all courses in a major flowchart.
     *
     * @param list list of lists that represent the courses in the major
     */
    public void displayMajor(ArrayList<ArrayList<String>> list) {
        showPrereqs = true;
        showMajors = true;
        clearFlowChart();
        major = list;
        int x = 20;
        int y = 40;
        for (ArrayList<String> strings : list) {
            displayVerticalList(x, y, strings, true);
            x += 120;
        }
    }

    /**
     * displays all given courses from a Map of Strings representing the courses offered in all term
     *
     * @param courses offered for all terms
     */
    public void displayAllCourses(Map<String, String> courses) {
        clearFlowChart();
        showPrereqs = true;
        showMajors = false;
        int x = 100;
        int y = 40;
        // Fills arraylists
        ArrayList<String> fallCourses = new ArrayList<>();
        ArrayList<String> winterCourses = new ArrayList<>();
        ArrayList<String> springCourses = new ArrayList<>();
        for (String name : courses.keySet()) {
            String[] values = name.split(" ");
            String course = values[values.length - 1];
            int quarter = Integer.parseInt(courses.get(name));
            switch (quarter) {
                case (1):
                    if (!fallCourses.contains(course)) {
                        fallCourses.add(course);
                    }
                    break;
                case (2):
                    if (!winterCourses.contains(course)) {
                        winterCourses.add(course);
                    }
                    break;
                case (3):
                    if (!springCourses.contains(course)) {
                        springCourses.add(course);
                    }
                    break;
            }
        }
        // Displays courses
        displayCourseRect(20, y, "Fall", true, false);
        displayHorizontalList(x, y, fallCourses, false);
        y += 200;
        displayCourseRect(20, y, "Winter", true, false);
        displayHorizontalList(x, y, winterCourses, false);
        y += 200;
        displayCourseRect(20, y, "Spring", true, false);
        displayHorizontalList(x, y, springCourses, false);
    }

    /**
     * displays all courses available in the fall
     *
     * @param courses courses to display
     */
    public void displayFallCourses(Map<String, String> courses) {
        clearFlowChart(); //clear chart
        showPrereqs = false;
        showMajors = false;
        //set initial position
        int x = 100;
        int y = 40;

        //fall courses
        ArrayList<String> fallCourses = new ArrayList<>();

        for (String name : courses.keySet()) {
            String[] values = name.split(" ");
            String course = values[values.length - 1];
            int quarter = Integer.parseInt(courses.get(name));
            if (!fallCourses.contains(course) && quarter == 1) {
                fallCourses.add(course);
            }
        }
        // Displays courses
        displayCourseRect(20, y, "Fall", true, false);
        displayHorizontalList(x, y, fallCourses, false);
    }

    /**
     * displays all courses available in Winter term
     *
     * @param courses - map of courses to find available in the winter
     */
    public void displayWinterCourses(Map<String, String> courses) {
        clearFlowChart(); //clear chart
        showPrereqs = false;
        showMajors = false;
        //set initial position
        int x = 100;
        int y = 40;

        //winter courses
        ArrayList<String> winterCourses = new ArrayList<>();

        for (String name : courses.keySet()) {
            String[] values = name.split(" ");
            String course = values[values.length - 1];
            int quarter = Integer.parseInt(courses.get(name));
            if (!winterCourses.contains(course) && quarter == 2) {
                winterCourses.add(course);
            }
        }
        // Displays courses
        displayCourseRect(20, y, "Winter", true, false);
        displayHorizontalList(x, y, winterCourses, false);
    }

    /**
     * displays all courses offered in the spring term
     *
     * @param courses to show for the spring term
     */
    public void displaySpringCourses(Map<String, String> courses) {
        clearFlowChart(); //clear chart
        showPrereqs = false;
        showMajors = false;
        //set initial position
        int x = 100;
        int y = 40;

        //spring courses
        ArrayList<String> springCourses = new ArrayList<>();

        for (String name : courses.keySet()) {
            String[] values = name.split(" ");
            String course = values[values.length - 1];
            int quarter = Integer.parseInt(courses.get(name));
            if (!springCourses.contains(course) && quarter == 3) {
                springCourses.add(course);
            }
        }
        // Displays courses
        displayCourseRect(20, y, "Spring", true, false);
        displayHorizontalList(x, y, springCourses, false);
    }

    /**
     * Displays the courses for the given elective type and major from the electives.csv file onto UI
     *
     * @param output            Parser for given electives file
     * @param electivesFile     electives.csv file
     * @param givenMajor        Chosen major
     * @param givenElectiveType Elective type either SCIEL, MASCIEL, TECHEL, or FREE
     */
    private static void displayElectives(CSVParser output, File electivesFile, String givenMajor,
                                         String givenElectiveType) throws IOException {

        ArrayList<String> desiredCourses = new ArrayList<>();
        Map<String, String> termMap = output.parse(electivesFile);
        ArrayList<String> keys = output.getKeys();
        for (String k : keys) {
            int majorCourseSplit = findMajorCourseSplit(k);
            String getMajor = k.substring(0, majorCourseSplit); //Gets major ex: AE, CE, etc
            String getCourse = k.substring(majorCourseSplit); //Gets course ex: CS1021, EE2070
            String getElectiveType = termMap.get(k); //Gets elective type SCIEL, MASCIEL, FREE, etc
            if (givenMajor.equals(getMajor) && givenElectiveType.equals(getElectiveType)) {
                desiredCourses.add(getCourse);
            }
        }

        System.out.println(givenElectiveType);
        for (String c : desiredCourses) {
            System.out.println(c);
        }
        System.out.println("*********************************");
    }

    /**
     * Helper method for displayMajorCourses in order to split the major from the course in given key.
     * Helpful for when the major isn't standard 2 letter such as "BSE PT" or "ME A"
     *
     * @param key given key that is in format (Major Course)
     */
    private static int findMajorCourseSplit(String key) {
        String regexThreeNum = ".\\w\\w\\d\\d\\d.*"; //Ex: MA136 or CE1011
        Pattern threeNumPattern = Pattern.compile(regexThreeNum);
        Matcher matcher = threeNumPattern.matcher(key);

        if (matcher.find()) {
            return matcher.start();
        }
        return 0;
    }

    /**
     * Displays a course block on the canvas at the given coordinates
     *
     * @param x          - x coordinate
     * @param y          - y coordinate
     * @param label      - text label being displayed in the block
     * @param header     - true if the block is a list header
     * @param horizontal - true if the courses should be connected horizontally
     */
    private void displayCourseRect(int x, int y, String label, boolean header, boolean horizontal) {

        canvas.getGraphicsContext2D().setTextAlign(TextAlignment.CENTER);
        if (header) {

            canvas.getGraphicsContext2D().setFill(Color.color(.9, .9, .9)); //color of the rect
            if (label.startsWith("Fall")) { //set color of the edge
                canvas.getGraphicsContext2D().setStroke(Color.DARKORANGE);
            } else if (label.startsWith("Winter")) {
                canvas.getGraphicsContext2D().setStroke(Color.DARKCYAN);
            } else if (label.startsWith("Spring")) {
                canvas.getGraphicsContext2D().setStroke(Color.DARKGREEN);
            }
            canvas.getGraphicsContext2D().setLineWidth(3);
            canvas.getGraphicsContext2D().strokeRect(x - 5, y - 5, 60, 50);
            canvas.getGraphicsContext2D().fillRect(x - 5, y - 5, 60, 50);
            canvas.getGraphicsContext2D().setFill(Color.BLACK);
            canvas.getGraphicsContext2D().fillText(label, x + 25, y + 25, 50);
            canvas.getGraphicsContext2D().setLineWidth(1.0);
        } else {
            if (label.length() < 5 || label.equals("HU/SS") || label.equals("MA/SCI")) { //dual electives
                canvas.getGraphicsContext2D().setFill(Color.LIGHTSALMON);
            } else if ((label.startsWith("CS") || label.startsWith("SE") || label.startsWith("CE") || label.startsWith("EE"))) { //EE/CS department majors
                canvas.getGraphicsContext2D().setFill(Color.LIGHTBLUE);
            } else if (label.startsWith("MA")) { //math
                canvas.getGraphicsContext2D().setFill(Color.LIGHTYELLOW);
            } else if (label.startsWith("PH") || label.startsWith("CH")) { //electives
                canvas.getGraphicsContext2D().setFill(Color.LIGHTGREEN);
            } else if (label.endsWith("GS")) { //general studies
                canvas.getGraphicsContext2D().setFill(Color.LIGHTCORAL);
            } else {
                canvas.getGraphicsContext2D().setFill(Color.LIGHTGRAY);
            }
            canvas.getGraphicsContext2D().fillRect(x, y, 50, 40);
            canvas.getGraphicsContext2D().setFill(Color.BLACK);
            canvas.getGraphicsContext2D().fillText(label, x + 25, y + 25, 40);

            if (taken != null) {
                if (taken.containsKey(label)) {
                    canvas.getGraphicsContext2D().setFill(Color.GREEN);
                    canvas.getGraphicsContext2D().setStroke(Color.BLACK);
                    canvas.getGraphicsContext2D().strokeOval(x + 40, y + 30, 10, 10);
                    canvas.getGraphicsContext2D().fillOval(x + 40, y + 30, 10, 10);
                    canvas.getGraphicsContext2D().stroke();
                }
            }
            if (showPrereqs) {
                showPrereqs(x, y, label, horizontal);
            }
        }
    }

    private void displayHorizontalList(int x, int y, List<String> tempList, boolean header) {
        for (String data : tempList) {
            // Scales canvas size up to fit new elements on the screen
            if (x > canvas.getWidth() - BOXHEIGHT) {
                canvas.setWidth(x + BOXHEIGHT);
            }
            if (header) {
                displayCourseRect(x, y, data, true, true);
                header = false;
            } else {
                displayCourseRect(x, y, data, false, true);
            }
            int[] coordinates = new int[2];
            coordinates[0] = x + 25;
            coordinates[1] = y + 40;
            courses.put(data, coordinates);
            x += BOXHEIGHT;
        }
    }

    /**
     * Displays the given list in a vertical orientation starting at the given coordinates
     *
     * @param x        - the x coordinate
     * @param y        - the y coordinate
     * @param tempList - the list being displayed
     * @param header   - true if the boxes should appear as headers
     */
    private void displayVerticalList(int x, int y, List<String> tempList, boolean header) {

        if (x > canvas.getWidth() - BOXHEIGHT) {
            canvas.setWidth(x + BOXHEIGHT);
        }
        for (String data : tempList) {
            // Scales canvas size up to fit new elements on the screen
            if (y > canvas.getHeight() - BOXHEIGHT) {
                canvas.setHeight(y + BOXHEIGHT);
            }
            if (header) {
                displayCourseRect(x, y, data, true, false);
                header = false;
            } else {
                displayCourseRect(x, y, data, false, false);
            }
            int[] array = new int[2];
            array[0] = x + 50;
            array[1] = y + 20;
            courses.put(data, array);
            y += BOXHEIGHT;
        }
    }

    /**
     * Helper function to clear the flowchart window
     */
    private void clearFlowChart() {
        canvas = new Canvas(870, 735);
        pane.setContent(canvas);
        courses = new HashMap<>();
    }

    /**
     * Sets the preq map
     *
     * @param prereqs the map from a string of one course to the prerequisites of that course
     */
    public void setPrereqs(Map<String, String> prereqs) {
        this.prereqs = prereqs;
    }

    /**
     * Checks the prereqs of the classes, and prints a line if the prereq is already displayed
     *
     * @param x          current coordinate
     * @param y          current coordinate
     * @param label      the label of the current node
     * @param horizontal if the list is horizontal
     */
    private void showPrereqs(int x, int y, String label, boolean horizontal) {
        String prereq = prereqs.get(label);
        if (prereq != null) {
            String[] list = prereq.split(","); //split by boxes
            for (String course : list) {
                if (course.contains("|") && course.contains(" ")) { //course contains both an or and and case
                    String[] options = course.split(" ");
                    for (String option : options) { //look through each prerequisite
                        if (option.contains("|")) {
                            canvas.getGraphicsContext2D().setStroke(Color.DARKBLUE);

                            String[] line = course.split("\\|");
                            for (String c : line) { //look through each prerequisite

                                //check if course is in the current major list
                                int[] prev = courses.get(c);
                                if (showMajors) { //if we are displaying a major
                                    if (prev != null && inMajor(option)) { //check if the course is in the major
                                        int x1 = prev[0];
                                        int y1 = prev[1];
                                        drawTo(x1, y1, x, y, horizontal);
                                    }
                                } else { //otherwise, we are showing all courses or terms
                                    if (prev != null) { //check if the previous course exists
                                        int x1 = prev[0];
                                        int y1 = prev[1];
                                        drawTo(x1, y1, x, y, horizontal);
                                    }
                                }
                            }
                        } else {
                            canvas.getGraphicsContext2D().setStroke(Color.DARKRED);
                            //check if course is in the current major list
                            int[] prev = courses.get(option);
                            if (showMajors) { //if we are displaying a major
                                if (prev != null && inMajor(option)) { //check if the course is in the major
                                    int x1 = prev[0];
                                    int y1 = prev[1];
                                    drawTo(x1, y1, x, y, horizontal);
                                }
                            } else { //otherwise, we are showing all courses or terms
                                if (prev != null) { //check if the previous course exists
                                    int x1 = prev[0];
                                    int y1 = prev[1];
                                    drawTo(x1, y1, x, y, horizontal);
                                }
                            }
                        }
                    }
                } else if (course.contains("|") && !course.contains(" ")) { //or case
                    canvas.getGraphicsContext2D().setStroke(Color.BLUE);
                    String[] options = course.split("\\|");
                    for (String option : options) { //look through each prerequisite

                        //check if course is in the current major list
                        int[] prev = courses.get(option);
                        if (showMajors) { //if we are displaying a major
                            if (prev != null && inMajor(option)) { //check if the course is in the major
                                int x1 = prev[0];
                                int y1 = prev[1];
                                drawTo(x1, y1, x, y, horizontal);
                            }
                        } else { //otherwise, we are showing all courses or terms
                            if (prev != null) { //check if the previous course exists
                                int x1 = prev[0];
                                int y1 = prev[1];
                                drawTo(x1, y1, x, y, horizontal);
                            }
                        }
                    }
                } else if (!course.contains("|") && course.contains(" ")) { //and case
                    canvas.getGraphicsContext2D().setStroke(Color.RED);
                    String[] options = course.split(" ");
                    for (String option : options) {
                        int[] prev = courses.get(option);
                        if (showMajors) { //if we are displaying a major
                            if (prev != null && inMajor(option)) { //check if the course is in the major
                                int x1 = prev[0];
                                int y1 = prev[1];
                                drawTo(x1, y1, x, y, horizontal);
                            }
                        } else { //otherwise, we are showing all courses or terms
                            if (prev != null) { //check if the previous course exists
                                int x1 = prev[0];
                                int y1 = prev[1];
                                drawTo(x1, y1, x, y, horizontal);
                            }
                        }
                    }
                } else { //single elective
                    canvas.getGraphicsContext2D().setStroke(Color.BLACK);
                    int[] prev = courses.get(course);
                    if (showMajors) { //if we are displaying a major
                        if (prev != null && inMajor(course)) { //check if the course is in the major
                            int x1 = prev[0];
                            int y1 = prev[1];
                            drawTo(x1, y1, x, y, horizontal);
                        }
                    } else { //otherwise, we are showing all courses or terms
                        if (prev != null) { //check if the previous course exists
                            int x1 = prev[0];
                            int y1 = prev[1];
                            drawTo(x1, y1, x, y, horizontal);
                        }
                    }
                }
            }
        }
    }

    /**
     * searches for a course within a major
     * if it is found, it returns true and allows the showPrereqs method to draw a connection
     *
     * @param course to find in the major
     * @return true if the course is in the major's curriculum, false if it is not found
     */
    private boolean inMajor(String course) {
        for (ArrayList<String> maj : major) {
            for (String c : maj) {
                if (course.equals(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *                   Draws a line from one node to another representing that it is a prereq
     * @param x1         x for prereq
     * @param y1         y for prereq
     * @param x          x for current
     * @param y          y for current
     * @param horizontal if the list is horizontal
     */
    private void drawTo(int x1, int y1, int x, int y, boolean horizontal) {

        canvas.getGraphicsContext2D().moveTo(x1, y1); //move the canvas to the prerequisite position

        if (horizontal) { //horizontal line: All Courses selected
            x += 30;
            //check specific or equal cases
            if (Math.abs(y1 - y) < 200 && Math.abs(y1 - y) > 60) { //one term below
                canvas.getGraphicsContext2D().lineTo(x, y);

            } else if (y1 == y) { //same term
                canvas.getGraphicsContext2D().lineTo(x1, y1 + 20);
                canvas.getGraphicsContext2D().lineTo(x, y1 + 20);

            } else if (x1 == x) { //same column

                //go down, right by approx 35, up/down to y +- 20, move to x, down
                if (y1 < y) { //prerequisite is above
                    y1 += 40;
                    canvas.getGraphicsContext2D().lineTo(x1, y1);
                    canvas.getGraphicsContext2D().lineTo(x1 + 35, y1);
                    canvas.getGraphicsContext2D().lineTo(x1 + 35, y - 20);
                    canvas.getGraphicsContext2D().lineTo(x, y - 20);

                } else if (y < y1) { //below, reverse the signs
                    y += 40;
                    canvas.getGraphicsContext2D().lineTo(x1, y1 - 20);
                    canvas.getGraphicsContext2D().lineTo(x1 - 35, y1 - 20);
                    canvas.getGraphicsContext2D().lineTo(x1 - 35, y + 20);
                    canvas.getGraphicsContext2D().lineTo(x, y + 20);

                }
            } else { //two terms apart
                //check if the prerequisite is above or below
                if (y1 < y) { //prerequisite is above the course
                    y1 += 40;
                    //move down a little
                    canvas.getGraphicsContext2D().lineTo(x1, y1 + 20);
                    //check horizontal positions
                    if (x1 < x) { //prerequisite is behind the course
                        //move to x +/- 35
                        canvas.getGraphicsContext2D().lineTo(x - 35, y1 + 20);
                        //move up or down
                        canvas.getGraphicsContext2D().lineTo(x - 35, y - 20);
                        //move to x
                        canvas.getGraphicsContext2D().lineTo(x, y - 20);

                    } else { //prerequisite is in front of the course
                        //move to x+/- 40
                        canvas.getGraphicsContext2D().lineTo(x + 25, y1 + 20);
                        //move up or down
                        canvas.getGraphicsContext2D().lineTo(x + 25, y - 20);
                        //move to x
                        canvas.getGraphicsContext2D().lineTo(x, y - 20);
                    }
                } else if (y < y1) { //prerequisite is below the course
                    y += 40;
                    //move down a little
                    canvas.getGraphicsContext2D().lineTo(x1, y1 + 20);

                    if (x1 < x) { //prerequisite is behind the course
                        //move to x +/- 35
                        canvas.getGraphicsContext2D().lineTo(x - 35, y1 + 20);
                        //move up or down
                        canvas.getGraphicsContext2D().lineTo(x - 35, y + 20);
                        //move to x
                        canvas.getGraphicsContext2D().lineTo(x, y + 20);

                    } else { //prerequisite is in front of the course
                        //move to x+/- 40
                        canvas.getGraphicsContext2D().lineTo(x + 35, y1 - 20);
                        //move up or down
                        canvas.getGraphicsContext2D().lineTo(x + 35, y + 20);
                        //move to x
                        canvas.getGraphicsContext2D().lineTo(x, y + 20);
                    }
                }
            }
            //move the rest of the way
            canvas.getGraphicsContext2D().lineTo(x, y);
        } else {
            //vertical lines: major or MyFlowchart was selected
            //get to the middle of both vertical positions of the prerequisite and course box
            y += 40;
            x += 30;
            canvas.getGraphicsContext2D().lineTo(x1 + 30, y1); //move forward a little

            if (x1 + 120 > x) { //check if the box is a column behind
                if (y1 + 40 != y) { // the box is either above or below
                    canvas.getGraphicsContext2D().lineTo(x1 + 30, y + 10); //draw a straight line going up or down
                    canvas.getGraphicsContext2D().lineTo(x, y + 10); //get underneath the box
                    canvas.getGraphicsContext2D().lineTo(x, y); //go the rest of the way

                } else {  //box is just a column behind
                    canvas.getGraphicsContext2D().lineTo(x, y);
                }
            } else { //the box is further behind
                if (y1 + 40 == y) { //prerequisite is in the same row as the course
                    canvas.getGraphicsContext2D().lineTo(x1 + 30, y + 10); //draw a straight line going up or down
                    canvas.getGraphicsContext2D().lineTo(x, y + 10); //get underneath the box
                    canvas.getGraphicsContext2D().lineTo(x, y); //go the rest of the way

                } else if (y < y1) { //prerequisite is above the course
                    canvas.getGraphicsContext2D().lineTo(x1 + 30, y + 10); //draw a straight line going up or down
                    canvas.getGraphicsContext2D().lineTo(x, y + 10); //straight line to horizontal coordinate
                    canvas.getGraphicsContext2D().lineTo(x, y); //finish drawing

                } else { //prerequisite is below the course
                    canvas.getGraphicsContext2D().lineTo(x1 + 30, y - 50); //straight line up or down
                    canvas.getGraphicsContext2D().lineTo(x, y - 50); //straight line to horizontal coordinate
                    canvas.getGraphicsContext2D().lineTo(x, y - 40); //finish drawing
                }
            }
        }
        canvas.getGraphicsContext2D().stroke();
    }

    public void setTaken(List<String> taken) {
        this.taken = new HashMap<>();
        for (String course : taken) {
            int index = course.indexOf(":");
            this.taken.put(course.substring(0, index), Integer.parseInt(course.substring(index + 1)));
        }
    }
}
