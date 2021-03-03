/*
 * Course: SE2800 A
 * Winter 2019 - 2020
 * Name: Trenton Bowser
 * Created: 5/3/2020
 */

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Handle the creation and logic behind the graduation plan
 */
public class GraduationHandler {
    private ScrollPane pane;
    private HBox hBox;
    private final BooleanProperty dragModeActiveProperty =
            new SimpleBooleanProperty(this, "dragModeActive", true);

    public GraduationHandler(ScrollPane pane) {
        this.pane = pane;
        hBox = new HBox();
        hBox.setMinWidth(870);
        hBox.setMinHeight(735);
        hBox.setSpacing(10);
        pane.setContent(hBox);
    }

    /**
     * Displays the graduation plan based on the student's transcript
     *
     * @param takenCourses - the students previously taken courses
     * @param majorNorm    - list of major courses
     * @param courses      - map of all offerings
     * @param major        - student's major
     * @param prereqData   - prerequisite and other data
     */
    public void setCourses(Map<String, Integer> takenCourses,
                           ArrayList<ArrayList<String>> majorNorm,
                           Map<String, String> courses, String major,
                           ArrayList<Course> prereqData) {
        String[] titles = {"Previous", "Frosh Q1", "Frosh Q2", "Frosh Q3", "Soph Q1",
                "Soph Q2", "Soph Q3", "Junior Q1", "Junior Q2", "Junior Q3", "Senior Q1",
                "Senior Q2", "Senior Q3"};
        int count = 0;
        ArrayList<ArrayList<String>> futureCourses = generateTrack(
                takenCourses, majorNorm, courses, major, prereqData);
        for (String title : titles) {
            VBox temp = new VBox();
            temp.setSpacing(10);
            temp.setMinWidth(100);
            temp.setAlignment(Pos.TOP_CENTER);
            Text tempTitle = new Text(title);
            tempTitle.setFont(Font.font(20));
            List<Node> list = new ArrayList<>();
            list.add(tempTitle);
            for (String course : takenCourses.keySet()) {
                if (Math.abs(takenCourses.get(course)) == count) {
                    Canvas canvas = new Canvas(80, 50);
                    if (takenCourses.get(course) >= 0) {
                        canvas.getGraphicsContext2D().setFill(Color.YELLOWGREEN);
                    } else {
                        canvas.getGraphicsContext2D().setFill(Color.DARKORANGE);
                    }
                    canvas.getGraphicsContext2D().fillRect(0, 0, 80, 50);
                    canvas.getGraphicsContext2D().setFill(Color.BLACK);
                    canvas.getGraphicsContext2D().fillText(course, 20, 30, 40);
                    list.add(canvas);
                }
            }
            for (String course : futureCourses.get(count)) {
                Canvas canvas = new Canvas(80, 50);
                canvas.getGraphicsContext2D().setFill(Color.LIGHTGRAY);
                canvas.getGraphicsContext2D().fillRect(0, 0, 80, 50);
                canvas.getGraphicsContext2D().setFill(Color.BLACK);
                canvas.getGraphicsContext2D().fillText(course, 20, 30, 40);
                enableDrag(canvas);
                list.add(canvas);
            }
            temp.getChildren().addAll(list);
            enableDragOver(temp);
            hBox.getChildren().add(temp);
            count++;
        }
    }

    /**
     * Draggable function concept from Stack Overflow:
     * https://stackoverflow.com/questions/17312734/how-to-make-a-draggable-node-in-javafx-2-0
     */
    //todo fix bugs
    static class Delta {
        double x, y;
    }

    // make a canvas movable by dragging it around with the mouse.
    private void enableDrag(final Canvas node) {
        final Delta dragDelta = new Delta();
        node.setOnMousePressed(mouseEvent -> {
            dragDelta.x = node.getTranslateX() - mouseEvent.getScreenX();
            dragDelta.y = node.getTranslateY() - mouseEvent.getScreenY();
            node.getScene().setCursor(Cursor.MOVE);
        });
        node.setOnMouseReleased(mouseEvent -> node.getScene().setCursor(Cursor.HAND));
        node.setOnMouseDragged(mouseEvent -> {
            node.setTranslateX(mouseEvent.getScreenX() + dragDelta.x);
            node.setTranslateY(mouseEvent.getScreenY() + dragDelta.y);
        });
        node.setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                node.getScene().setCursor(Cursor.HAND);
            }
        });
        node.setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                node.getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }

    // Allow the VBoxes to accept the canvas objects when dragged into them
    private void enableDragOver(VBox target) {
        target.setOnDragOver(event -> {
            // data is dragged over the target
            // accept it only if it is not dragged from the same node
            if (event.getGestureSource() != target) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            event.consume();
        });

        target.setOnDragEntered(event -> {
            // the drag-and-drop gesture entered the target
            // show to the user that it is an actual gesture target
            if (event.getGestureSource() != target) {
                // Create the Effect for the Rectangle
                InnerShadow rectangleShadow = new InnerShadow();
                rectangleShadow.offsetXProperty().bind(target.layoutXProperty());
                rectangleShadow.offsetYProperty().bind(target.layoutYProperty());
                rectangleShadow.setColor(Color.GRAY);
                target.setEffect(rectangleShadow);
            }

            event.consume();
        });

        target.setOnDragDropped(event -> {
            // data dropped
            // if there is a string data on dragboard, read it and use it
            Dragboard db = event.getDragboard();
            target.getChildren().add((Node) event.getSource());
            ((VBox) ((Node) event.getSource()).getParent())
                    .getChildren().remove((Node) event.getSource());
            event.setDropCompleted(true);

            event.consume();
        });
    }

    private ArrayList<ArrayList<String>> generateTrack(Map<String, Integer> takenCourses,
                                                       ArrayList<ArrayList<String>> majorNorm,
                                                       Map<String, String> courses, String major,
                                                       ArrayList<Course> prereqData) {
        ArrayList<ArrayList<String>> track = new ArrayList<>();
        track.add(new ArrayList<>());
        // Add already taken lists as empty lists
        int temp = 0;
        ArrayList<String> notTaken = new ArrayList<>();
        for (String x : takenCourses.keySet()) {
            if (Math.abs(takenCourses.get(x)) > temp) {
                temp = Math.abs(takenCourses.get(x));
            }
        }
        // Determine courses in track not yet taken
        for (int x = 0; x < temp; x++) {
            ArrayList<String> tempQuarter = majorNorm.get(x);
            tempQuarter.remove(0);
            for (String course : tempQuarter) {
                if (!takenCourses.containsKey(course)) {
                    notTaken.add(course);
                }
            }
            track.add(new ArrayList<>());
        }
        ArrayList<String> tempArr = new ArrayList<>();
        for (String z : notTaken) {
            for (int i = 0; i < prereqData.size(); i++) {
                Course course = prereqData.get(i);
                if (course.getName().equalsIgnoreCase(z)) {
                    tempArr.add(z);
                    i = prereqData.size();
                }
            }
            //todo determine what to do with electives showing up in list besides remove
            //todo parse electives file and bring data in
        }
        notTaken = tempArr;
        // Testing notTaken list
        for (String z : notTaken) {
            System.out.println(z);
        }
        // Generate courses for next terms
        for (int x = temp; x < majorNorm.size(); x++) {
            //todo add in not yet taken courses and check prereqs and quarter offered
            // also fill electives maybe?
            ArrayList<String> tempQuarter = majorNorm.get(x);
            String tempName = tempQuarter.get(0).toLowerCase();
            int quarterCode = 0;
            if (tempName.contains("q1")) {
                quarterCode = 1;
            } else if (tempName.contains("q2")) {
                quarterCode = 2;
            } else if (tempName.contains("q3")) {
                quarterCode = 3;
            }
            tempQuarter.remove(0);
            track.add(tempQuarter);
        }
        return track;
    }
}
