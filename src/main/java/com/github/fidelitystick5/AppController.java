package com.github.fidelitystick5;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;

import javafx.event.ActionEvent;

public class AppController {
  private final static byte THREADS_NUMBER = 4;

  private Stage primaryStage;

  @FXML
  private TextField shiftValueField;

  @FXML
  private VBox progressContainer;

  private File selectedDirectory;

  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  @FXML
  private void chooseDirectory(ActionEvent event) {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("Open Resource File");

    selectedDirectory = directoryChooser.showDialog(primaryStage);
  }

  @FXML
  private void encryptFiles(ActionEvent event) {
    if (selectedDirectory == null)
      return;

    progressContainer.getChildren().clear();

    final File[] files = selectedDirectory.listFiles();
    final ArrayBlockingQueue<File> queue = new ArrayBlockingQueue<>(files.length);
    final Thread[] threads = new Thread[THREADS_NUMBER];
    final String header = "__ENCRYPTED__\n";
    int shift;

    try {
      shift = Integer.parseInt(shiftValueField.getText());
    } catch (NumberFormatException e) {
      shift = 1;
    }

    for (File file : files) {
      try {
        queue.put(file);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    for (int i = 0; i < THREADS_NUMBER; i++) {
      Label label = new Label(String.format("Thread %s label", i));
      ProgressBar progressBar = new ProgressBar();

      progressBar.setMaxWidth(Double.MAX_VALUE);

      progressContainer.getChildren().add(label);
      progressContainer.getChildren().add(progressBar);

      EncryptorTask task = new EncryptorTask(queue, header, shift);

      label.textProperty().bind(task.messageProperty());
      progressBar.progressProperty().bind(task.progressProperty());

      Thread thread = new Thread(task);
      thread.setDaemon(true);
      thread.start();

      threads[i] = thread;
    }
  }
}
