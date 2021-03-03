import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;

/**
 * parses through prerequisites.csv to create a map of courses mapped to prerequisites
 * required to take the class. This will also serve to create a Course object that will
 * potentially store information.
 * <p>
 * Each Prerequisite file has up to 4 section; Course, Credits, Prerequisites and Description
 * <p>
 * Parent class of other prerequisite parsers
 *
 * @author Trenton Bowser and bunalesj
 */
public class CSVPrerequisite {

    private Map<String, String> prerequisites = new LinkedHashMap<>();
    private ArrayList<Course> courseList = new ArrayList<>(); //list of courses
    private static final List<String> VALID_HEADERS = new ArrayList<>(
            Arrays.asList("course", "credits", "prereqs", "description"));

    /**
     * Parses the given file for prerequisites
     *
     * @param file - the given csv file
     * @throws IOException - If the given file cannot be read
     */
    public void parse(File file) throws IOException {
        Course course;

        BufferedReader br = new BufferedReader(new FileReader(file));
        Map<String, Integer> indexes = getIndexes(br.readLine());
        String line = br.readLine();
        while (line != null) {
            String[] lineArr = split(line); //split the csv line by commas
            if (indexes.get("course") != null && lineArr[indexes.get("course")] != null &&
                    indexes.get("prereqs") != null && lineArr[indexes.get("prereqs")] != null) {
                course = new Course(lineArr[indexes.get("course")]); //create a new course
                course.setPerquisites(lineArr[indexes.get("prereqs")]);
                Integer credits = indexes.get("credits");
                if (credits != null && lineArr[credits] != null) {
                    course.setCredits(Integer.parseInt(lineArr[credits]));
                }
                Integer desc = indexes.get("description");
                if (desc != null && lineArr[desc] != null) {
                    course.setDescription(lineArr[desc]);
                }

                //add course and prerequisite to appropriate data structures
                courseList.add(course);
                prerequisites.put(course.getName(), course.getPrerequisites());

                line = br.readLine();
            } else {
                throw new MissingFormatArgumentException("Missing a required column");
            }
        }
    }

    public ArrayList<Course> getCourses() {
        return courseList;
    }

    /**
     * get a map of a course and course prerequisites
     * key is a String representing a course
     * each String is mapped to an arraylist holding the codes of a prerequisite for the course
     *
     * @return a map showing a course and its corresponding prerequisites
     */
    public Map<String, String> getPrerequisites() {
        return prerequisites;
    }

    /**
     * Gets the indexes of the header using a given file
     *
     * @param firstLine The first line of the file
     * @return A map of the headers
     * @throws FileNotFoundException if file is not found
     * @author Trenton Bowser
     */
    private Map<String, Integer> getIndexes(String firstLine) throws FileNotFoundException {
        String[] header = firstLine.split(",");
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            for (String value : VALID_HEADERS) {
                if (header[i].toLowerCase().equals(value)) {
                    map.put(header[i].toLowerCase(), i);
                }
            }
        }
        return map;
    }

    private String[] split(String line) {
        String[] ret = line.split(",");
        if(line.substring(line.length()-1).equals(",")) {
            String[] newReturn = new String[ret.length + 1];
            for(int x=0; x < ret.length; x++) {
                newReturn[x] = ret[x];
            }
            newReturn[newReturn.length - 1] = "";
            ret = newReturn;
        }
        return ret;
    }
}
