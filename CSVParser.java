
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to parse the given csv file, offerings.csv/electives.csv, in order to extract the
 * courses majors and which terms/electives that they are available to take
 *
 * @author albiterri
 * @version 1.0
 * Last Edited: 05/06/2020
 */
public class CSVParser {

    private ArrayList<String> keys = new ArrayList<>();
    private ArrayList<String> majorsList = new ArrayList<>();
    private ArrayList<String> courseList = new ArrayList<>();

    public CSVParser() { }

    /**
     * Parser that puts extracted courses, majors, and terms/electives into a list or map
     * with the given CSV file, offerings.csv/electives.csv
     *
     * @param csvFile    CSV file
     * @return Map where key is (major | course) and value is (term offered or elective offered)
     * @throws IOException Trouble reading in file
     */
    public Map<String,String> parse(File csvFile) throws IOException {
        String line = "";
        String cvsSplitBy = ",";
        int lineCount = 0;
        int section = 0;
        boolean courseTitle = true;
        Map<String, String> majorCourseMap = new LinkedHashMap<>();

        BufferedReader br = new BufferedReader(new FileReader(csvFile));

        while ((line = br.readLine()) != null) {

            // use comma as separator
            String[] currentLine = line.split(cvsSplitBy);
            if(emptyCSVRowCheck(currentLine)) {
                throw new NoSuchElementException("Empty Row. Fix CSV file format");
            }
            String firstWord = currentLine[0];
            String majorCourseKey = "";

            //Populate course list
            if (courseTitle) {
                firstWord = currentLine[1];
                courseTitle = false;
            } else {
                if(firstWord.isBlank()) {
                    throw new MissingFormatArgumentException("Missing course/s in 1st col");
                } else {
                    courseList.add(section, firstWord);
                }
            }
            if (lineCount == 0) { //Row containing Title and Majors
                courseTitle = true;
                for(int i = 0; i < currentLine.length; i++) {
                    if(i == 0 & (currentLine[0].isBlank() | courseTitle)) {
                        courseTitle = false;
                    } else {
                        if(currentLine[i].isBlank()) { //Any majors should not be missing (blank)
                            throw new MissingFormatArgumentException("Missing major/s in 1st row");
                        } else {
                            majorsList.add(i - 1, currentLine[i]);
                        }
                    }
                }
            } else if (lineCount > 0) { //Start adding keys and values to majorCourseMap
                //Data in currentLine can be either numbers for terms or elective being offered
                for (String majorCourseItem : currentLine) {
                    if (!majorCourseItem.equals(firstWord)) {
                        majorCourseKey = majorsList.get(section) + " " + firstWord;
                        if (!majorCourseItem.isBlank()) {
                            majorCourseMap.put(majorCourseKey, majorCourseItem);
                            keys.add(majorCourseKey);
                        }
                        section++;
                    }
                }
            }
            lineCount++; //Next line
            section = 0; //Resets section
        }
        br.close();
        return majorCourseMap;
    }

    public ArrayList<String> getKeys() {
        return this.keys;
    }

    public ArrayList<String> getMajors() {
        return this.majorsList;
    }

    public ArrayList<String> getCourse() {
        return this.courseList;
    }

    /**
     * Parser method used to get electives list data extracted from electives.csv.
     *
     * @param electivesFile electives.csv or similar
     * @param givenMajor student's major
     * @param givenElectiveType type of elective student desires to see (ex: MASCIEL, SCIEL, TECH, FREE)
     * @return list of electives
     * @throws IOException for errors in reading the electives.csv file
     */
    public ArrayList<String> getElectives(File electivesFile, String givenMajor,
                                          String givenElectiveType) throws IOException {

        ArrayList<String> desiredCourses = new ArrayList<>();
        Map<String, String> termMap = this.parse(electivesFile);
        ArrayList<String> keys = this.getKeys();
        for(String k: keys) {
            int majorCourseSplit = findMajorCourseSplit(k);
            String getMajor = k.substring(0, majorCourseSplit).trim(); //Gets major ex: AE, CE, etc
            String getCourse = k.substring(majorCourseSplit).trim(); //Gets course ex: CS1021, EE2070
            String getElectiveType = termMap.get(k); //Gets elective type SCIEL, MASCIEL, FREE, etc
            if(givenMajor.equals(getMajor) && givenElectiveType.equals("all") && !desiredCourses.contains(getCourse)) {
                desiredCourses.add(getCourse);
            } else if(givenMajor.equals(getMajor) && givenElectiveType.equals(getElectiveType)) {
                desiredCourses.add(getCourse);
            }
        }

        return desiredCourses;
    }

    /**
     * Parser method used to get list of courses for the quarter based off student's major
     *
     * @param offeringsFile offerings.csv file
     * @param givenMajor student's major
     * @param givenQuarter Quarter in number code such as Fall = 1, Winter = 2, Spring = 3
     * @return list of courses for the given quarter and major
     */
    public ArrayList<String> quarterMajorCourses(File offeringsFile, String givenMajor,
                                                 String givenQuarter) throws IOException {

        ArrayList<String> desiredCourses = new ArrayList<>();
        Map<String, String> termMap = this.parse(offeringsFile);
        ArrayList<String> keys = this.getKeys();
        for(String k: keys) {
            int majorCourseSplit = findMajorCourseSplit(k);
            String getMajor = k.substring(0, majorCourseSplit).trim(); //Gets major ex: AE, CE, etc
            String getCourse = k.substring(majorCourseSplit).trim(); //Gets course ex: CS1021, EE2070
            String getQuarter = termMap.get(k); //Gets quarter ex: Fall = 1, Winter = 2, Spring = 3
            if(givenMajor.equals("all") && givenQuarter.equals(getQuarter) && !desiredCourses.contains(getCourse)) {
                desiredCourses.add(getCourse);
            } else if(givenMajor.equals(getMajor) && givenQuarter.equals(getQuarter)) {
                desiredCourses.add(getCourse);
            }
        }

        return desiredCourses;
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

        if(matcher.find()) {
            return matcher.start();
        }
        return 0;
    }

    /**
     * Helper method used to check whether or not a row is empty in order to
     * throw IOException.
     *
     * @param line current line in file
     * @return whether or not the row is empty
     */
    private static boolean emptyCSVRowCheck(String[] line) {

        for (String s : line) {
            if (!s.isBlank()) {
                return false;
            }
        }
        return true;
    }
}
