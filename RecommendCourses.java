
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.MissingFormatArgumentException;
import java.util.Scanner;

/**
 * Meant to recommend classes for next quarter based on major, current curriculum, unofficial
 * transcripts, and what classes are being offered.
 *
 * @author albiterri
 * @version 1.0
 * Last Edited: 04/27/2020
 */
public class RecommendCourses {

    private ArrayList<String> curriculumCourses = new ArrayList<>();
    private ArrayList<String> takenCourses;
    private ArrayList<String> recommendedCourses = new ArrayList<>();
    private ArrayList<String> remainingCourses = new ArrayList<>();
    private ArrayList<String> electivesNQ = new ArrayList<>();
    private ArrayList<Course> prerequisiteRecommendations = new ArrayList<>();

    /**
     * Constructor that calls all logic that eventually will store necessary recommendations
     * data into recommendedCourses, remainingCourses, electivesNQ, and prerequisitesRecommendations list
     *
     * @param major student's major (CS or SE)
     * @param nextQuarter upcoming quarter (Fall = 1, Winter = 2, Spring = 3)
     * @param curriculumFile student's curriculum (Currently only CS and SE available)
     * @param transcriptFile unofficial transcript from my.msoe.edu
     * @param offeringsFile offerings.csv from SE2800 blackboard
     * @param electivesFile electives.csv from SE800 blackboard
     * @param prerequisitesFile prerequisites.csv from SE800 blackboard
     * @throws IOException for incorrect files or formatting
     */
    public RecommendCourses(String major, String nextQuarter, File curriculumFile, File transcriptFile,
                            File offeringsFile, File electivesFile, File prerequisitesFile) throws IOException {
        if(major.isBlank() | nextQuarter.isBlank()) {
            throw new MissingFormatArgumentException("Your have not input a supported major. Please Try Again!");
        }
        if((major.equals("CS") | major.equals("SE")) &&
                (Integer.parseInt(nextQuarter) > 3 && Integer.parseInt(nextQuarter) < 1)) {
            parseCurriculum(major,curriculumFile);
            takenCourses = new TranscriptParser(transcriptFile).getCoursesTaken();
            recommendCourse(major,nextQuarter,offeringsFile,electivesFile, prerequisitesFile);
        } else {
            throw new IllegalArgumentException("Currently your major (" + major + ") or quarter (" + nextQuarter +
                    ") is not supported for getting recommendations. Please try either CS/SE and quarters 1-3.");
        }
    }

    /**
     * Helper function that will parse the curriculum file in order to get what classes
     * the student has to take overall.
     *
     * @param givenMajor student's major
     * @param curriculumFile curriculum.csv from SE2800 blackboard
     * @throws IOException for incorrect files or formatting
     */
    private void parseCurriculum(String givenMajor, File curriculumFile) throws IOException {
        String line = "";
        String cvsSplitBy = ",";
        int section = 0;
        boolean majorLine = true;

        BufferedReader br = new BufferedReader(new FileReader(curriculumFile));

        while ((line = br.readLine()) != null) {

            // use comma as separator
            String[] currentLine = line.split(cvsSplitBy);
            for(String column: currentLine) {
                if(!column.equals(givenMajor) && majorLine) {
                    section++;
                } else if(column.equals(givenMajor) && majorLine) {
                    break;
                }
                if(!majorLine) {
                    curriculumCourses.add(currentLine[section]);
                    break;
                }
            }
            majorLine = false;
        }
        if(curriculumCourses.isEmpty()) {
            throw new IOException("Current major is not supported. Try CS,SE,CE, or EE");
        }
        br.close();
    }

    /**
     * Helper function to help breakdown recommendations data including classes for next
     * quarter, electives for next quarter, prerequisites for recommended classes, and classes that the student
     * has yet to take
     *
     * @param studentMajor student's major
     * @param nextQuarter upcoming quarter
     * @param offeringsFile offerings.csv from SE2800 blackboard
     * @param electivesFile electives.csv from SE800 blackboard
     * @param prerequisitesFile prerequisites.csv from SE800 blackboard
     * @throws IOException for incorrect files or formatting
     */
    private void recommendCourse(String studentMajor, String nextQuarter, File offeringsFile
            , File electivesFile, File prerequisitesFile) throws IOException {

        CSVParser recommendParser = new CSVParser();
        ArrayList<String> nextQuarterCourses = recommendParser.quarterMajorCourses(offeringsFile
                ,studentMajor,nextQuarter);
        CSVPrerequisite prerequisiteParser = new CSVPrerequisite();
        prerequisiteParser.parse(prerequisitesFile);
        ArrayList<Course> prerequisiteCourses = prerequisiteParser.getCourses();

        for(String course: curriculumCourses) { //Curriculum - Taken = Remaining Classes
            if(!takenCourses.contains(course)) {
                remainingCourses.add(course);
            }
        }
        for(String course: nextQuarterCourses) { //NextQ - Taken = Recommendations for Next Quarter
            if(!takenCourses.contains(course)) {
                recommendedCourses.add(course);
            }
        }

        for(String course: recommendedCourses) { //Find prerequisites for recommended courses
            Course foundCourse = hasPrerequisite(course,prerequisiteCourses);
            if(!foundCourse.getName().equals("BlankCourse")) {
                prerequisiteRecommendations.add(foundCourse);
            }
        }

        ArrayList<String> allQuarterClasses = recommendParser.quarterMajorCourses(offeringsFile,
                "all",nextQuarter);
        ArrayList<String> allElectives = new CSVParser().getElectives(electivesFile,studentMajor,"all");

        for(String elective: allElectives) { //Find electives offered next quarter for student's major
            if(allQuarterClasses.contains(elective) && !electivesNQ.contains(elective)) {
                electivesNQ.add(elective);
            }
        }

        for(String course: electivesNQ) { //Find electives that have prerequisites
            Course foundCourse = hasPrerequisite(course,prerequisiteCourses);
            if(!foundCourse.getName().equals("BlankCourse")) {
                prerequisiteRecommendations.add(foundCourse);
            }
        }
    }

    /**
     * Helper function used to search through list of courses with prerequisites to find out
     * if the given course has any prerequisites.
     *
     * @param course given course to check for prerequisites
     * @param prerequisiteCourses parsed list of prerequisite data
     * @return either a blank course or a course with prerequisite datat
     */
    private Course hasPrerequisite(String course, ArrayList<Course> prerequisiteCourses) {

        Course prereqCourse = new Course("BlankCourse");
        for(Course c: prerequisiteCourses) {
            if(c.getName().equals(course) && !c.getPrerequisites().isBlank()) {
                return c;
            }
        }
        return prereqCourse;
    }

    public ArrayList<String> getRecommendations() {
        return recommendedCourses;
    }

    public ArrayList<String> getRemainingCourses() {
        return remainingCourses;
    }

    public ArrayList<String> getElectivesNQ() {
        return electivesNQ;
    }

    public ArrayList<Course> getPrerequisiteRecommendations() {
        return prerequisiteRecommendations;
    }


    public static void main(String[] args) throws IOException {

        File curriculum = new File(System.getProperty("user.dir") +
                "\\docs\\newcurriculum.csv");
        File transcriptAlbiter = new File(System.getProperty("user.dir") +
                "\\docs\\TranscriptFiles\\transcript.pdf");
        File transcriptChapman = new File(System.getProperty("user.dir") +
                "\\docs\\TranscriptFiles\\Chapmann_transcript.pdf");
        File offerings = new File(System.getProperty("user.dir") +
                "\\docs\\offerings.csv");
        File electives = new File(System.getProperty("user.dir") +
                "\\docs\\electives.csv");
        File prerequisites = new File(System.getProperty("user.dir") +
                "\\docs\\prerequisites.csv");

        Scanner in = new Scanner(System.in);
        System.out.print("Enter major you would like to see courses for (Ex: CS, SE, etc): ");
        String major = in.nextLine().toUpperCase();
        System.out.print("Enter your upcoming quarter (Ex: Fall = 1, Winter = 2, Spring = 3): ");
        String nextQuarter = in.nextLine();
        RecommendCourses r = new RecommendCourses(major,nextQuarter,curriculum,transcriptChapman
                ,offerings,electives,prerequisites);

        switch (nextQuarter) {
            case "1":
                nextQuarter = "Fall";
                break;
            case "2":
                nextQuarter = "Winter";
                break;
            case "3":
                nextQuarter = "Spring";
                break;
            default:
                break;
        }

        System.out.println("\nAs an " + major + " you should take these classes for " + nextQuarter + " quarter:" +
                " (Classes may be year specific)");
        for(String recommended: r.getRecommendations()) {
            System.out.println(recommended);
        }

        System.out.println("\n\nHere are some electives you can take next quarter: ");
        for(String elective: r.getElectivesNQ()) {
            System.out.println(elective);
        }

        System.out.println("\n\nThe following recommended courses have prereqs: ");
        for(Course c: r.getPrerequisiteRecommendations()) {
            System.out.println(c.getName() + "-- " + c.getPrerequisites());
        }

        System.out.println("\n\nThese are classes you have yet to take: ");
        for(String remaining: r.getRemainingCourses()) {
            System.out.println(remaining);
        }
    }
}
