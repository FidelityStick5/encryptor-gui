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

  @FXML
  private ProgressBar threadProgressBar;

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

    Scene scene = primaryStage.getScene();

    final File[] files = selectedDirectory.listFiles();
    final BlockingQueue<File> queue = new ArrayBlockingQueue<>(files.length + 1);
    final EncryptorThread[] threads = new EncryptorThread[4];

    int shift = 1;
    String header = "__ENCRYPTED__\n";

    try {
      shift = Integer.parseInt(shiftValueField.getText());
    } catch (NumberFormatException e) {
      shift = 1;
    }

    threadProgressBar.setProgress(0);

    for (File file : files) {
      try {
        queue.put(file);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    for (int i = 0; i < threads.length; i++) {
      EncryptorThread thread = threads[i];

      Label label = (Label) scene.lookup("#threadStatusLabel" + i);

      thread = new EncryptorThread(threadProgressBar, label, queue, files.length, header, shift);
      thread.start();
    }
  }
}
