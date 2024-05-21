package com.example.java_project;

import javafx.application.Application;
import javafx.application.Platform;
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
import java.util.Objects;
import java.util.Optional;

public class TaskApp extends Application {

    private final ObservableList<Task> taskList = FXCollections.observableArrayList();
    private final ListView<Task> listView = new ListView<>();

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("To-Do List App");


        listView.setItems(taskList);
        listView.setCellFactory(param -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private final Button deleteButton = new Button("Delete");


            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item.isCompleted());
                    checkBox.setOnAction(event -> toggleTaskCompletion(item));
                    checkBox.getStyleClass().add("checkbox");

                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14pt;");

                    Label dateLabel = new Label(item.getDate().toString());
                    dateLabel.setStyle("-fx-font-size: 14pt;");

                    Label descriptionLabel = new Label(item.getDescription());
                    descriptionLabel.setStyle("-fx-font-size: 10pt;");

                    HBox titleDateBox = new HBox(5, titleLabel, dateLabel);
                    VBox taskDetailsVBox = new VBox(5, titleDateBox, descriptionLabel);

                    if (item.isCompleted()) {
                        titleLabel.setStyle("-fx-text-fill: grey;");
                        dateLabel.setStyle("-fx-text-fill: grey;");
                        descriptionLabel.setStyle("-fx-text-fill: grey;");
                    } else if (item.getDate().isBefore(LocalDate.now())) {
                        titleLabel.setStyle("-fx-text-fill: red;");
                        dateLabel.setStyle("-fx-text-fill: red;");
                        descriptionLabel.setStyle("-fx-text-fill: red;");
                    } else {
                        titleLabel.setStyle("-fx-text-fill: black;");
                        dateLabel.setStyle("-fx-text-fill: black;");
                        descriptionLabel.setStyle("-fx-text-fill: black;");
                    }

                    HBox.setHgrow(taskDetailsVBox, Priority.ALWAYS);
                    HBox.setHgrow(deleteButton, Priority.NEVER);

                    ImageView trashIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/java_project/trash-can-icon.png"))));
                    trashIcon.setFitWidth(16);
                    trashIcon.setFitHeight(16);

                    deleteButton.setGraphic(trashIcon);
                    deleteButton.getStyleClass().add("button-delete");
                    deleteButton.setOnAction(event -> promptDeleteConfirmation(item));

                    Button editButton = new Button("Edit");
                    editButton.setOnAction(event -> openEditTaskWindow(item));
                    HBox hbox = new HBox(10, checkBox, taskDetailsVBox, editButton, deleteButton);
                    setGraphic(hbox);
                }
            }


        });

        Button addButton = new Button("+");
        addButton.getStyleClass().add("add-button");
        VBox.setMargin(addButton, new Insets(10)); // Set margin for the button
        addButton.setOnAction(e -> openAddTaskWindow());

        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10, 10, 10, 10));
        vBox.getChildren().addAll(listView, addButton);

        Scene scene = new Scene(vBox, 800, 500);

        // Add the CSS file to the scene
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/java_project/styles.css")).toExternalForm());

        primaryStage.setScene(scene);


        primaryStage.setOnCloseRequest(event -> {
            // Ensure database connection is closed when the application is closed
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }


    private void openAddTaskWindow() {
        Stage addTaskStage = new Stage();
        addTaskStage.initModality(Modality.APPLICATION_MODAL);
        addTaskStage.setTitle("Add New Task");

        VBox addTaskLayout = new VBox(10);
        addTaskLayout.setPadding(new Insets(10, 10, 10, 10));

        TextField titleField = new TextField();
        TextArea descriptionArea = new TextArea();
        DatePicker datePicker = new DatePicker();

        Button addButton = new Button("Add Task");
        addButton.setOnAction(e -> {
            addTask(titleField.getText(), descriptionArea.getText(), datePicker.getValue());
            addTaskStage.close();
        });

        addTaskLayout.getChildren().addAll(
                new Label("Title:"),
                titleField,
                new Label("Description:"),
                descriptionArea,
                new Label("Due Date:"),
                datePicker,
                addButton
        );

        Scene addTaskScene = new Scene(addTaskLayout, 300, 250);
        addTaskStage.setScene(addTaskScene);
        addTaskStage.showAndWait();
    }


    private void openEditTaskWindow(Task task) {
        Stage editTaskStage = new Stage();
        editTaskStage.initModality(Modality.APPLICATION_MODAL);
        editTaskStage.setTitle("Edit Task");

        VBox editTaskLayout = new VBox(10);
        editTaskLayout.setPadding(new Insets(10, 10, 10, 10));

        TextField titleField = new TextField(task.getTitle());
        TextArea descriptionArea = new TextArea(task.getDescription());
        DatePicker datePicker = new DatePicker(task.getDate());

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            editTask(task, titleField.getText(), descriptionArea.getText(), datePicker.getValue());
            editTaskStage.close();
        });

        editTaskLayout.getChildren().addAll(
                new Label("Title:"),
                titleField,
                new Label("Description:"),
                descriptionArea,
                new Label("Due Date:"),
                datePicker,
                saveButton
        );

        Scene editTaskScene = new Scene(editTaskLayout, 300, 250);
        editTaskStage.setScene(editTaskScene);
        editTaskStage.showAndWait();
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void toggleTaskCompletion(Task task) {
        task.setCompleted(!task.isCompleted());
    }

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

    private void addTask(String title, String description, LocalDate date) {
        if (title.trim().isEmpty()) {
            showAlert("Title cannot be empty");
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            showAlert("Please select a date that is not behind the current date");
            return;
        }

        Task task = new Task(title, description, date, false);
        taskList.add(task);
    }

    private void editTask(Task task, String title, String description, LocalDate date) {
        if (title.trim().isEmpty()) {
            showAlert("Title cannot be empty");
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            showAlert("Please select a date that is not behind the current date");
            return;
        }

        task.setTitle(title);
        task.setDescription(description);
        task.setDate(date);

        listView.refresh();
    }

    private static class Task {
        private String title;
        private String description;
        private LocalDate date;
        private boolean completed;

        public Task(String title, String description, LocalDate date, boolean completed) {
            this.title = title;
            this.description = description;
            this.date = date;
            this.completed = completed;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        @Override
        public String toString() {
            return title;
        }
    }

}




