package com.example.java_project;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class TaskApp extends Application {

    private static final ObservableList<Task> taskList = FXCollections.observableArrayList();
    private final ListView<Task> listView = new ListView<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("To-Do List App");

        HBox searchBox = getSearchBox();

        listView.setItems(taskList);
        listView.setCellFactory(param -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private final Button deleteButton = new Button("Delete");

            // update item with task details
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);

                // set graphic to null if item is empty
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item.isCompleted());
                    checkBox.setOnAction(event -> toggleTaskCompletion(item));
                    checkBox.getStyleClass().add("checkbox");

                    // set task details
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14pt;");

                    // set date label
                    Label dateLabel = new Label(item.getDate() != null ? item.getDate().toString() : "No date"); // Check if the date is null
                    dateLabel.setStyle("-fx-font-size: 14pt;");

                    // set description label
                    Label descriptionLabel = new Label(item.getDescription());
                    descriptionLabel.setStyle("-fx-font-size: 10pt;");

                    // set tag label
                    Label tagLabel = new Label(item.getTag());
                    tagLabel.setStyle("-fx-font-size: 14pt;");
                    tagLabel.setPadding(new Insets(0, 0, 0, 50));

                    // set task details in HBox
                    HBox titleDateTagBox = new HBox(5, titleLabel, dateLabel, tagLabel); // Add tagLabel here
                    VBox taskDetailsVBox = new VBox(5, titleDateTagBox, descriptionLabel);

                    // set text color based on task status
                    if (item.isCompleted()) {
                        titleLabel.setStyle("-fx-text-fill: grey;");
                        dateLabel.setStyle("-fx-text-fill: grey;");
                        descriptionLabel.setStyle("-fx-text-fill: grey;");
                    } else if (item.getDate() != null && item.getDate().isBefore(LocalDate.now())) { // Check if the date is null
                        titleLabel.setStyle("-fx-text-fill: red;");
                        dateLabel.setStyle("-fx-text-fill: red;");
                        descriptionLabel.setStyle("-fx-text-fill: red;");
                    } else {
                        titleLabel.setStyle("-fx-text-fill: white;");
                        dateLabel.setStyle("-fx-text-fill: white;");
                        descriptionLabel.setStyle("-fx-text-fill: white;");
                    }

                    // set HBox layout
                    HBox.setHgrow(taskDetailsVBox, Priority.ALWAYS);
                    HBox.setHgrow(deleteButton, Priority.NEVER);

                    // set delete button
                    ImageView trashIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/java_project/trash-can-icon.png"))));
                    trashIcon.setFitWidth(16);
                    trashIcon.setFitHeight(16);

                    // set delete button style
                    deleteButton.setGraphic(trashIcon);
                    deleteButton.getStyleClass().add("button-delete");
                    deleteButton.setOnAction(event -> promptDeleteConfirmation(item));

                    // set edit button
                    Button editButton = new Button("Edit");
                    editButton.setOnAction(event -> openEditTaskWindow(item));
                    HBox hbox = new HBox(10, checkBox, taskDetailsVBox, editButton, deleteButton);
                    setGraphic(hbox);
                }
            }
        });

        // add button to add new task
        Button addButton = new Button("+");
        addButton.getStyleClass().add("add-button");
        VBox.setMargin(addButton, new Insets(10)); // Set margin for the button
        addButton.setOnAction(e -> openAddTaskWindow());

        // set VBox layout
        VBox vBox = new VBox(10, searchBox);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().addAll(listView, addButton);

        // set scene
        Scene scene = new Scene(vBox, 800, 500);

        // add styles to scene
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/java_project/styles.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //    search box for filtering tasks by tag
    private HBox getSearchBox() {
        TextField searchField = new TextField();
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> {
            String tag = searchField.getText();
            if (tag == null || tag.trim().isEmpty()) {
                listView.setItems(taskList);
            } else {
                ArrayList<Task> filteredTasks = filterByTag(tag);
                listView.setItems(FXCollections.observableArrayList(filteredTasks));
            }
        });

        return new HBox(10, searchField, searchButton);
    }

    // open add task window
    private void openAddTaskWindow() {
        // set stage
        Stage addTaskStage = new Stage();
        addTaskStage.initModality(Modality.APPLICATION_MODAL);
        addTaskStage.setTitle("Add New Task");

        // set VBox layout
        VBox addTaskLayout = new VBox(10);
        addTaskLayout.setPadding(new Insets(10, 10, 10, 10));

        // set text fields
        TextField titleField = new TextField();
        TextArea descriptionArea = new TextArea();
        DatePicker datePicker = new DatePicker();
        TextField tagField = new TextField();

        // set add button
        Button addButton = new Button("Add Task");
        addButton.setOnAction(e -> {
            addTask(titleField.getText(), descriptionArea.getText(), datePicker.getValue(), tagField.getText());
            addTaskStage.close();
        });

        // add elements to VBox layout
        addTaskLayout.getChildren().addAll(
                new Label("Title:"),
                titleField,
                new Label("Description:"),
                descriptionArea,
                new Label("Due Date:"),
                datePicker,
                new Label("Tag:"),
                tagField,
                addButton
        );

        // set scene
        Scene addTaskScene = new Scene(addTaskLayout, 500, 350);
        addTaskStage.setScene(addTaskScene);
        addTaskStage.showAndWait();
    }

    //    open edit task window
    private void openEditTaskWindow(Task task) {
        // set stage
        Stage editTaskStage = new Stage();
        editTaskStage.initModality(Modality.APPLICATION_MODAL);
        editTaskStage.setTitle("Edit Task");

        // set VBox layout
        VBox editTaskLayout = new VBox(10);
        editTaskLayout.setPadding(new Insets(10, 10, 10, 10));

        // set text fields
        TextField titleField = new TextField(task.getTitle());
        TextArea descriptionArea = new TextArea(task.getDescription());
        DatePicker datePicker = new DatePicker(task.getDate());
        TextField tagField = new TextField(task.getTag());

        // set save button
        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            editTask(task, titleField.getText(), descriptionArea.getText(), datePicker.getValue(), tagField.getText());
            editTaskStage.close();
        });

        // add elements to VBox layout
        editTaskLayout.getChildren().addAll(
                new Label("Title:"),
                titleField,
                new Label("Description:"),
                descriptionArea,
                new Label("Due Date:"),
                datePicker,
                new Label("Tag:"),
                tagField,
                saveButton
        );

        // set scene
        Scene editTaskScene = new Scene(editTaskLayout, 500, 350);
        editTaskStage.setScene(editTaskScene);
        editTaskStage.showAndWait();
    }

    // show alert if date is behind current date
    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Please select a date that is not behind the current date");
        alert.showAndWait();
    }


    // toggle task completion status
    private void toggleTaskCompletion(Task task) {
        task.setCompleted(!task.isCompleted());
    }

    // prompt user to confirm task deletion
    private void promptDeleteConfirmation(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Task Deletion");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this task?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            taskList.remove(task);
        }
    }


    // add task with values
    private void addTask(String title, String description, LocalDate date, String tag) {
        if (title.trim().isEmpty()) {
            title = "Task " + (taskList.size() + 1); // Set default title if title is empty
        }

        if (date != null && date.isBefore(LocalDate.now())) {
            showAlert();
            return;
        }

        Task task = new Task(title, description, date, false, tag);
        taskList.add(task);
    }

    // edit task with new values
    private void editTask(Task task, String title, String description, LocalDate date, String tag) {
        if (title.trim().isEmpty()) {
            title = "Task " + (taskList.size() + 1); // Set default title if title is empty
        }
        if (date != null && date.isBefore(LocalDate.now())) {
            showAlert();
            return;
        }


        task.setTitle(title);
        task.setDescription(description);
        task.setDate(date);
        task.setTag(tag);

        listView.refresh();
    }

    // iterate over all tasks, find and return those with matching tag value
    private ArrayList<Task> filterByTag(String tag) {
        ArrayList<Task> tasks = new ArrayList<>();
        for (Task task : taskList) {
            if (task.getTag().equals(tag)) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    // Task class
    public static class Task {
        private String title;
        private String description;
        private LocalDate date;
        private boolean completed;
        private String tag;

        // constructor
        public Task(String title, String description, LocalDate date, boolean completed, String tag) {
            this.title = title.isEmpty() ? "Task " + (taskList.size() + 1) : title; // Set default title if title is empty
            this.description = description;
            this.date = date;
            this.completed = completed;
            this.tag = tag;
        }

        // getters and setters
        public String getTitle() {
            return title;
        }

        // getters and setters
        public void setTitle(String title) {
            this.title = title;
        }

        // getters and setters
        public String getDescription() {
            return description;
        }

        // getters and setters
        public void setDescription(String description) {
            this.description = description;
        }

        // getters and setters
        public LocalDate getDate() {
            return date;
        }

        // getters and setters
        public void setDate(LocalDate date) {
            this.date = date;
        }

        // getters and setters
        public boolean isCompleted() {
            return completed;
        }

        // getters and setters
        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        // getters and setters
        public String getTag() {
            return (tag == null || tag.trim().isEmpty()) ? "No tags" : tag;
        }

        // getters and setters
        public void setTag(String tag) {
            this.tag = tag;
        }

        // override toString method
        @Override
        public String toString() {
            return title;
        }
    }
}




