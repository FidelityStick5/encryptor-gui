package com.github.fidelitystick5;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javafx.event.ActionEvent;

public class AppController {
  private Stage primaryStage;

  @FXML
  private TextField shiftValueField;

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

    final Scene scene = primaryStage.getScene();

    final File[] files = selectedDirectory.listFiles();
    final BlockingQueue<File> queue = new ArrayBlockingQueue<>(files.length + 1);
    final Thread[] threads = new Thread[4];
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

    for (int i = 0; i < threads.length; i++) {
      Label label = (Label) scene.lookup("#threadStatusLabel" + i);
      ProgressBar threadProgressBar = (ProgressBar) scene.lookup("#threadProgressBar" + i);
      EncryptorTask task = new EncryptorTask(queue, header, shift);

      label.textProperty().bind(task.messageProperty());
      threadProgressBar.progressProperty().bind(task.progressProperty());

      Thread thread = new Thread(task);
      thread.setDaemon(true);
      thread.start();

      threads[i] = thread;
    }
  }
}
