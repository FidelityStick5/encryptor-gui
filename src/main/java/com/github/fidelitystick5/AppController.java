package com.github.fidelitystick5;

import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javafx.event.ActionEvent;

public class AppController {
  private Stage primaryStage;

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
    final File[] files = selectedDirectory.listFiles();
    final BlockingQueue<File> queue = new ArrayBlockingQueue<>(files.length + 1);
    final EncryptorThread[] threads = new EncryptorThread[4];

    final int shift = 1;
    final String header = "__ENCRYPTED__\n";

    for (File file : files) {
      try {
        queue.put(file);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    for (EncryptorThread thread : threads) {
      thread = new EncryptorThread(queue, header, shift);
      thread.start();
    }
  }
}
