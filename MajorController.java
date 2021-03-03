import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/*
 * Course: SE2811 - 011
 * Winter 2019 - 2020
 * File Header: Contains the class MajorController
 * Name: chapmann
 *Created: 3/29/2020
 */
public class MajorController {
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private ChoiceBox<String> termBox;
    @FXML
    private ChoiceBox<String> yearBox;
    @FXML
    private ScrollPane scrollPane;

    private Controller controller;
    private Stage stage;
    private Map<String, ArrayList<ArrayList<String>>> majorFlows;       //Map from major name to an arraylist of arraylists that contain the courses in the major. Organized so that there are 5 lists, each representing a possible class in a quarter
    private FlowChartHandler flowChartHandler;
    private Map<String, File> files;                        //List of all files that may be used
    private CSVParser parser;
    private CSVPrerequisite prerequisite;
    private boolean filesSet;
    private boolean validTranscript;
    private TranscriptParser transcriptParser;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @FXML
    public void initialize() {
        choiceBox.setItems(FXCollections.observableArrayList("Graduation Plan", "My Flowchart", "All Courses", "Computer Science",
                "Computer Engineering", "Electrical Engineering", "Software Engineering"));
        termBox.setItems(FXCollections.observableArrayList("-", "Fall", "Winter", "Spring")); //box for different terms
        yearBox.setItems(FXCollections.observableArrayList("-", "Frosh", "Soph", "Junior", "Senior")); //box for years

        //sets specification type boxes to disabled until choicebox's value is set
        termBox.setValue("Term");
        yearBox.setValue("Year");
        termBox.setDisable(true);
        yearBox.setDisable(true);

        //should we just initialize all the files at once?

        //initialize globals
        majorFlows = new HashMap<>();
        flowChartHandler = new FlowChartHandler(scrollPane);
        parser = new CSVParser();
        prerequisite = new CSVPrerequisite();
        filesSet = false;
    }

    public ArrayList<ArrayList<String>> parse(String[] lineArray) {
        ArrayList<ArrayList<String>> list = new ArrayList<>(); //list of the major
        ArrayList<String> quarterList = new ArrayList<>(); //list of courses per quarter
        int counter = 0;
        for (int i = 1; i < lineArray.length; i++) {
            if (!lineArray[i].equals(" ")) {
                quarterList.add(lineArray[i]);
            }
            if (counter == 5) {
                counter = 0;
                list.add(quarterList);
                quarterList = new ArrayList<>();
            } else {
                counter++;
            }
        }
        return list;
    }

    /**
     * Actionevent that controls what happens when the user selects a major.
     * Displays a flowchart on the pane.
     *
     * @throws IOException                  - if the fileset cannot be read
     * @throws MalformedParametersException - if the parse function cannot correctly read a line
     */
    @FXML
    public void choose() throws IOException, MalformedParametersException {
        String choosen = choiceBox.getValue();
        // If the fileset is not complete
        if (!filesSet) {
            return;
        }
        prerequisite.parse(files.get("prerequisites"));
        flowChartHandler.setPrereqs(prerequisite.getPrerequisites());
        Scanner scanner = new Scanner(files.get("curriculum"));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] lineArray = line.split(",");
            String title = "";
            title = getMajor(lineArray[0]);
            ArrayList<ArrayList<String>> list = parse(lineArray);
            majorFlows.put(title, list);
        }
        if (choosen.equals("Graduation Plan") && validTranscript) {
            flowChartHandler.setTaken(transcriptParser.getCoursesTaken());
            String major = transcriptParser.getStudentMajor();
            major = major.substring(6, major.length() - 1);
            flowChartHandler.displayGraduation(majorFlows.get(major),
                    parser.parse(files.get("offerings")), major, prerequisite.getCourses());
            //flowChartHandler.displayGraduation(majorFlows.get("Computer Science"));
        } else if (choosen.equals("All Courses")) {
            flowChartHandler.displayAllCourses(parser.parse(files.get("offerings")));
            yearBox.setDisable(true);
            yearBox.setValue("Years");
            termBox.setValue("-");
            termBox.setDisable(false);
        } else if (choosen.equals("My Flowchart") && validTranscript) {
            termBox.setValue("-");
            yearBox.setValue("-");
            yearBox.setDisable(true);
            termBox.setDisable(true);
            flowChartHandler.setTaken(transcriptParser.getCoursesTaken());
            String major = transcriptParser.getStudentMajor();
            flowChartHandler.displayMajor(majorFlows.get(major.substring(6, major.length() - 1)));
        } else {
            yearBox.setDisable(false);
            termBox.setDisable(false);
            termBox.setValue("-");
            yearBox.setValue("-");
            ArrayList<ArrayList<String>> list = majorFlows.get(choosen);
            flowChartHandler.displayMajor(list);
        }
    }

    /**
     * decision method based on if the user has chosen a term and/or year
     *
     * @author bunalesj
     */
    @FXML
    public void terms() throws IOException {
        String choice = choiceBox.getValue();
        if (!filesSet) {
            return;
        }
        if(!termBox.isDisabled()) {
            //ensure all files are set
            prerequisite.parse(files.get("prerequisites"));
            flowChartHandler.setPrereqs(prerequisite.getPrerequisites());

            if (choice.equals("All Courses")) { //All Courses only option. No need to choose a year
                justTerms(termBox.getValue());

            }else if(choice.equals("My Flowchart")){
                flowChartHandler.setTaken(transcriptParser.getCoursesTaken());
                String major = transcriptParser.getStudentMajor();
                flowChartHandler.displayMajor(majorFlows.get(major.substring(6, major.length()-1)));

            } else if(!yearBox.isDisabled()) {
                //should only need to check for term and year choice box values from here
                if (!termBox.getValue().equals("-") && yearBox.getValue().equals("-")) { //term only case
                    //major curriculum term only case
                    majorTerms(majorFlows.get(choice), termBox.getValue());
                } else if (!yearBox.getValue().equals("-") && termBox.getValue().equals("-")) { //year only case
                    findYear(majorFlows.get(choice), yearBox.getValue());

                } else if (!termBox.getValue().equals("-") && !yearBox.getValue().equals("-")) {//if the user chose a term and year
                    yearAndTerm(majorFlows.get(choice), termBox.getValue(), yearBox.getValue());

                } else if (yearBox.getValue().equals("-") && termBox.getValue().equals("-")) { //no year or term picked
                    flowChartHandler.displayMajor(majorFlows.get(choice));
                }
            }
        }
    }

    /**
     * prints out all the courses offered for a specified term
     *
     * @param term term to display courses for
     */
    private void justTerms(String term) throws IOException {
        try {
            switch (term) {
                case "Fall":  //fall quarter option

                    flowChartHandler.displayFallCourses(parser.parse(files.get("offerings")));
                    break;
                case "Winter":  //winter quarter option
                    flowChartHandler.displayWinterCourses(parser.parse(files.get("offerings")));
                    break;
                case "Spring":  //spring quarter options
                    flowChartHandler.displaySpringCourses(parser.parse(files.get("offerings")));
                    break;
                case "-":  //displays all quarters by default
                    flowChartHandler.displayAllCourses(parser.parse(files.get("offerings")));
                    break;
            }
        } catch (IOException e) {
            //intentionally left blank
        }

    }

    /**
     * finds the courses in a major correlating to a term to see what is being offered
     *
     * @param list curriculum of a major
     * @param term term the user wishes to see
     */
    private void majorTerms(ArrayList<ArrayList<String>> list, String term){
        ArrayList<ArrayList<String>> majorTerm = new ArrayList<>(); //term of a major

        //get all courses of a major per term?
        switch (term) {
            case "-": //basic, show all terms
                flowChartHandler.displayMajor(list); //display the entire major
                break;
            case "Fall":
                for (ArrayList<String> quarter : list) { //show fall terms of a major
                    if (quarter.get(0).contains("Q1")) {
                        majorTerm.add(quarter);
                    }
                }
                break;
            case "Winter":
                for (ArrayList<String> quarter : list) { //show winter terms of a major
                    if (quarter.get(0).contains("Q2")) {
                        majorTerm.add(quarter);
                    }
                }
                break;
            case "Spring":
                for (ArrayList<String> quarter : list) { //show spring terms of a major
                    if (quarter.get(0).contains("Q3")) {
                        majorTerm.add(quarter);
                    }
                }
                break;
        }
        if (!majorTerm.isEmpty()) { //if not "-" majorTerm should not be empty
            flowChartHandler.displayMajor(majorTerm);
        }

    }

    /**
     * prints the courses of a major for a year and term chosen by the student user
     *
     * @param list list of the major
     * @param term user wants to see
     * @param year user wants to see
     */
    private void yearAndTerm(ArrayList<ArrayList<String>> list, String term, String year) {
        ArrayList<ArrayList<String>> majorTerm = new ArrayList<>(); //term of a major

        //get all courses of a major per term?
        switch (term) {
            case "-": //basic, show all terms
                findYear(list, year); //display the year
                break;
            case "Fall":
                for (ArrayList<String> quarter : list) { //display the fall quarter of a year
                    if (quarter.get(0).contains("Q1") && quarter.get(0).contains(year)) {
                        majorTerm.add(quarter);
                    }
                }
                break;
            case "Winter":
                for (ArrayList<String> quarter : list) { //display the winter quarter of a year
                    if (quarter.get(0).contains("Q2") && quarter.get(0).contains(year)) {
                        majorTerm.add(quarter);
                    }
                }
                break;
            case "Spring":
                for (ArrayList<String> quarter : list) { //display the spring quarter of a year
                    if (quarter.get(0).contains("Q3") && quarter.get(0).contains(year)) {
                        majorTerm.add(quarter);
                    }
                }
                break;
        }
        if (!majorTerm.isEmpty()) {  //if not "-" majorTerm should not be empty
            flowChartHandler.displayMajor(majorTerm);
        }
    }

    /**
     * prints the courses of a major out according to the year the user wants to see
     *
     * @param list list of a major's courses
     * @param year year user wants to see courses of
     */
    private void findYear(ArrayList<ArrayList<String>> list, String year) {
        ArrayList<ArrayList<String>> majorYear = new ArrayList<>(); //term of a major

        //get all courses of a major per term?
        if(year.equals("-")){//basic, show all terms
            flowChartHandler.displayMajor(list); //display the entire major
        } else { //since the yearBox options match what's in the curriculum file, I will assume that we can just grab the year
            for (ArrayList<String> quarter : list) {
                if (quarter.get(0).contains(year)) { //check if it has the right year
                    majorYear.add(quarter); //
                }
            }
        }

        flowChartHandler.displayMajor(majorYear);
    }

    /**
     * Print function for the pane. Prints everything on the pane to a pdf
     */
    @FXML
    public void print() {
        Printer printer = Printer.getDefaultPrinter();
        ObservableSet<Printer> printers = Printer.getAllPrinters();
        for (Printer p : printers) {
            if (p.getName().equals("Microsoft Print to PDF")) {
                printer = p;
            }
        }
        PageLayout layout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.LANDSCAPE,
                .05, .05, .05, .05);
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            job.setPrinter(printer);
            ScrollPane pane = new ScrollPane();
            pane.setContent(scrollPane.getContent());
            job.printPage(layout, pane.getContent());
            job.endJob();
        }
    }

    /**
     * Sets the files to be used throughout the program
     *
     * @param files - pulls in the data files from the main window import
     */
    public void setFiles(Map<String, File> files) {
        this.files = files;
        filesSet = true;
    }

    public void setTranscriptParser(TranscriptParser transcriptParser) {
        this.transcriptParser = transcriptParser;
        if (this.transcriptParser != null) {
            validTranscript = true;
        }
    }

    private String getMajor(String in) {
        switch (in) {
            case "CS":
                return "Computer Science";
            case "SE":
                return "Software Engineering";
            case "CE":
                return "Computer Engineering";
            case "EE":
                return "Electrical Engineering";
        }
        return null;
    }
}
