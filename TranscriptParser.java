import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Meant to extract data from a unofficial transcript pdf file given by my.msoe.edu
 * in which the student's name, major, and courses taken are stored.
 *
 * @author albiterri
 * @version 1.0
 * Last Edited: 04/10/2020
 */
public class TranscriptParser {

    private String studentName = "";
    private String studentMajor = "";
    private ArrayList<String> coursesTaken = new ArrayList<>();
    private ArrayList<String> wipCourses = new ArrayList<>();

    private int quarterCount = 0;
    private String lastLine = "";

    public TranscriptParser(File transcriptFile) throws IOException {
        parse(transcriptFile);
    }

    /**
     * Parses the given unofficial transcript PDF and extracts studentName, major, and courses taken
     * Credit: satyendra singh yadav https://www.youtube.com/watch?v=rLsKLk-hPH8 (First 9 lines)
     *
     * @param transcriptFile PDF file that contains the unofficial transcript
     * @throws IOException if file is not found
     */
    private void parse(File transcriptFile) throws IOException {
        PDDocument document = PDDocument.load(transcriptFile);
        if (!document.isEncrypted()) {
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(false);
            PDFTextStripper Tstripper = new PDFTextStripper();
            String str = Tstripper.getText(document);
            Scanner scn;
            scn = new Scanner(str);
            String line;
            while (scn.hasNextLine()) {

                if (studentName.isBlank()) {
                    //Ignore Student ID
                    //Get Name
                    line = scn.nextLine();
                    String nameBlock = "NAME: ";
                    while (!line.contains("NAME: ")) {
                        line = scn.nextLine();
                        if (!scn.hasNextLine()) {
                            throw new NoSuchElementException("Cannot find name title. Please adjust transcript." +
                                    " Try again.");
                        }
                    }
                    int nameBlockIndex = line.indexOf(nameBlock);
                    studentName = line.substring(nameBlockIndex + nameBlock.length());
                } else if (studentMajor.isBlank()) {
                    //Ignore everything up until "BS in..."
                    line = scn.nextLine();
                    while (!line.contains("BS in")) {
                        line = scn.nextLine();
                        if (!scn.hasNextLine()) {
                            throw new NoSuchElementException("Cannot find degree title. Please adjust transcript." +
                                    " Try again.");
                        }
                    }
                    int incompleteStatus = line.indexOf("Incomplete");
                    int completeStatus = line.indexOf("Complete");
                    if (incompleteStatus != -1) {
                        studentMajor = line.substring(0, incompleteStatus);
                    } else if (completeStatus != -1) {
                        studentMajor = line.substring(0, completeStatus);
                    } else {
                        studentMajor = line;
                    }
                } else {
                    line = scn.nextLine();
                    quarterCounter(line);
                    while (!validCourse(line) & scn.hasNextLine()) {
                        line = scn.nextLine();
                        quarterCounter(line);
                    }
                    if (validCourse(line)) { //Gets courses matching "Ex: MA136 or CE1911" and removes spaces
                        if (line.contains("WIP")) {
                            //Gets courses matching "Ex: MA136 or CE1911" and removes spaces
                            int temp = quarterCount * -1;
                            String course = line.substring(0, findCourseSplit(line) + 1).trim();
                            coursesTaken.add(course + ":" + temp);
                            wipCourses.add(course);
                        } else {
                            //Gets courses matching "Ex: MA136 or CE1911" and removes spaces
                            coursesTaken.add(line.substring(0, findCourseSplit(line) + 1).trim() +
                                    ":" + quarterCount);
                        }
                    }
                }
            }
            document.close();
        }
    }

    public String getStudentName() {
        return this.studentName;
    }

    public String getStudentMajor() {
        return this.studentMajor;
    }

    public ArrayList<String> getCoursesTaken() {
        return this.coursesTaken;
    }

    public ArrayList<String> getWipCourses() {
        return this.wipCourses;
    }

    /**
     * Helper method used to find the proper format for a class
     *
     * @param course Line being read that possibly matches correct format (LLNNNN or LLNNN) where
     *               L == Letter and N == Number
     * @return Whether or not the line matches the format of a course at MSOE
     */
    private static boolean validCourse(String course) {
        String regexFourNum = "\\w\\w\\d\\d\\d\\d .*"; //Ex: CE1901
        String regexThreeNum = "\\w\\w\\d\\d\\d .*"; //Ex: MA136
        return course.matches(regexFourNum) | course.matches(regexThreeNum);
    }

    /**
     * Helper method used to find the proper format for a WIP class
     *
     * @param course Line being read that possibly matches correct format (WIP) indicating
     *               student has yet to finish class.
     * @return Whether or not the line matches the format of a WIP course
     */
    private static boolean wipCourse(String course) {
        String regexWIP = ".*WIP.*";
        return course.matches(regexWIP);
    }

    /**
     * Helper method for parse in order to split the course code from the name, credits earned, and grade.
     * Can be adjusted later if any of the other information is necessary besides course code.
     *
     * @param line given line that is in format "CourseCode CourseName CreditsAndGradeEarned"
     */
    private static int findCourseSplit(String line) {
        String regexThreeNum = "\\w\\w\\d\\d\\d"; //Ex: MA136 or CE1011
        Pattern threeNumPattern = Pattern.compile(regexThreeNum);
        Matcher matcher = threeNumPattern.matcher(line);

        if (matcher.find()) {
            return matcher.end();
        }
        return 0;
    }

    private void quarterCounter(String line) {
        if ((line.contains("Fall Quarter") || line.contains("Winter Quarter") ||
                line.contains("Spring Quarter")) && !line.equals(lastLine)) {
            lastLine = line;
            quarterCount++;
        }
    }

    public static void main(String[] args) {
        try {
            File transcriptAlbiter = new File(System.getProperty("user.dir") + "\\docs\\transcript.pdf");
            File transcriptChapman = new File(System.getProperty("user.dir") + "\\docs\\Chapmann_transcript.pdf");
            TranscriptParser tParse = new TranscriptParser(transcriptAlbiter);
            System.out.println(tParse.getStudentName());
            System.out.println(tParse.getStudentMajor());

            System.out.println("Classes Taken: ");
            for (String t : tParse.getCoursesTaken()) {
                System.out.println(t);
            }

            System.out.println("\n\nClasses WIP: ");
            for (String w : tParse.getWipCourses()) {
                System.out.println(w);
                for (String c : tParse.getCoursesTaken()) {
                    System.out.println(c);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
