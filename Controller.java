import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioMenuItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/*
 * Course: SE2811 - 011
 * Winter 2019 - 2020
 * File Header: Contains the class Controller
 * Name: chapmann
 *Created: 3/25/2020
 */
public class Controller {
    @FXML
    RadioMenuItem student;
    @FXML
    RadioMenuItem admin;
    @FXML
    RadioMenuItem advisor;
    @FXML
    ChoiceBox<String> choices;
    @FXML
    TableView<Course> table;
    @FXML
    TextField search;

    private Stage majorStage;
    private MajorController majorController;
    private List<String> studentList;
    private List<String> advisorList;
    private List<String> adminList;
    private Map<String, File> files;
    private TranscriptParser transcriptParser;
    private TableColumn<Course, String> course = new TableColumn<>("Course");
    private TableColumn<Course, String> description = new TableColumn<>("Description");
    private TableColumn<Course, String> prerequisites = new TableColumn<>("Prerequisites");
    private TableColumn<Course, String> credits = new TableColumn<>("Credits");
    private ObservableList<Course> data;
    private ObservableList<Course> filtered;

    public void setMajorStage(Stage stage) {
        this.majorStage = stage;
    }

    public void setMajorController(MajorController majorController) {
        this.majorController = majorController;
    }

    @FXML
    public void initialize() {
        files = new HashMap<>();
        studentList = new ArrayList<>();
        advisorList = new ArrayList<>();
        adminList = new ArrayList<>();

        table.getColumns().clear();
        table.getColumns().addAll(course, credits, prerequisites, description);
        course.setCellValueFactory(new PropertyValueFactory<>("name"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        prerequisites.setCellValueFactory(new PropertyValueFactory<>("prerequisites"));
        credits.setCellValueFactory(new PropertyValueFactory<>("credits"));

        description.setPrefWidth(277.0);
        prerequisites.setPrefWidth(181.0);
        course.setPrefWidth(65.0);
        credits.setPrefWidth(62.0);

        course.setStyle("-fx-alignment: CENTER;");
        description.setStyle("-fx-alignment: CENTER;");
        credits.setStyle("-fx-alignment: CENTER;");
        prerequisites.setStyle("-fx-alignment: CENTER;");

        
        //studentList.add("Course Offerings");
        //studentList.add("Course Prerequisites");
        studentList.add("View Flowchart");
        //studentList.add("Course Recommendations");
        //studentList.add("Graduation Plan");
        //studentList.add("View Prerequisites");
        //studentList.add("File output");

        //advisorList.add("Generate Graduation Plan");
        //advisorList.add("Edit Courses");
        //advisorList.add("File output");

        //adminList.add("Projected Enrollment");
        //adminList.add("View Offering Differences");
        //adminList.add("File output");
    }

    @FXML
    public void onMenu(ActionEvent e) {
        choices.setValue(null);
        if (student == e.getSource()) {
            choices.setItems(FXCollections.observableArrayList(studentList));
            admin.setSelected(false);
            advisor.setSelected(false);
            choices.setValue("");
        } else if (admin == e.getSource()) {
            choices.setItems(FXCollections.observableArrayList(adminList));
            student.setSelected(false);
            advisor.setSelected(false);
            choices.setValue("");

        } else {
            choices.setItems(FXCollections.observableArrayList(adminList));
            admin.setSelected(false);
            student.setSelected(false);
            choices.setValue("");
        }
    }

    @FXML
    public void importDataFiles() {
        List<String> fileNames = new LinkedList<>();
        fileNames.add("offerings");
        fileNames.add("prerequisites");
        fileNames.add("curriculum");
        try {
            DirectoryChooser directory = new DirectoryChooser();
            directory.setTitle("Select Data File Directory");
            String path = directory.showDialog(null) + "\\";
            if (path.equals("null\\")) {
                throw new FileSystemException("The Directory Chooser was Closed");
            }
            for (String name : fileNames) {
                getFiles(path + name + ".csv", name);
            }
            majorController.setFiles(files);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        } catch (FileSystemException e1) {
            //when the file chooser is closed by the user -- do nothing
        }
        load();
    }

    @FXML
    public void selectedChoice() {
        String selected = choices.getValue();
        if (selected != null) {
            switch (selected) {
                case "Course Offerings":
                    break;
                case "Course Prerequisites":
                    break;
                case "View Flowchart":
                    majorStage.show();
                    break;
                case "Course Recommendations":
                    break;
                case "Graduation Plan":
                    break;
                case "View Prerequisites":
                    break;
                case "File Output":
                    break;
                case "Generate Graduation Plan":
                    break;
                case "Edit Courses":
                    break;
                case "Projected Enrollment":
                    break;
                case "View Offering Differences":
                    break;
            }
        }
    }

    @FXML
    public void importTranscript() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Transcript");
        File transcript = null;
        transcript = chooser.showOpenDialog(null);
        if (transcript != null) {
            try {
                transcriptParser = new TranscriptParser(transcript);
                majorController.setTranscriptParser(transcriptParser);
            } catch (IOException ex) {
                System.out.println("Error reading the transcript file");
            }
        }
        System.out.println(description.getWidth());
        System.out.println(prerequisites.getWidth());
        System.out.println(course.getWidth());
        System.out.println(credits.getWidth());
    }

    /**
     * Helper method to open a file chooser and save the file data to the files Map
     *
     * @param filePath - the path of the file that the user must find and import
     * @param fileName - the name of the file that the user must find and import
     * @throws NullPointerException - if a file didn't exist in the folder
     * @author Trenton Bowser
     */
    private void getFiles(String filePath, String fileName) throws NullPointerException {
        Path path = Paths.get(filePath);
        File file = path.toFile();
        if(file.canRead()) {
            files.put(fileName, file);
        } else {
            throw new NullPointerException("The " + fileName + ".csv file cannot be found");
        }
    }

    private void load() {
        CSVPrerequisite loader = new CSVPrerequisite();
        try {
            loader.parse(files.get("prerequisites"));
            data = FXCollections.observableArrayList(loader.getCourses());
            table.setItems(data);
        } catch (IOException e) {
            System.out.println("Error reading prerequisite file");
        }
    }

    @FXML
    public void search(KeyEvent e) {
        String wanted = search.getText();
        List<Course> filter = new ArrayList<>();
        for (Course x : data) {
            try {
                if (x.getCredits() == Integer.parseInt(wanted)) {
                    filter.add(x);
                }
            } catch (NumberFormatException error) {
                //Do nothing
            }
            wanted = wanted.toLowerCase();
            if (x.getDescription().toLowerCase().contains(wanted) || x.getName().toLowerCase().contains(wanted) || x.getPrerequisites().toLowerCase().contains(wanted)) {
                filter.add(x);
            }
        }
        filtered = FXCollections.observableArrayList(filter);
        table.setItems(filtered);
    }
}