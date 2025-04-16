package com.github.fidelitystick5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class EncryptorThread extends Thread {
  private ProgressBar progressBar;
  private Label label;

  private BlockingQueue<File> queue;
  private int initialQueueSize;
  private String header;
  private int shift;

  public EncryptorThread(ProgressBar progressBar, Label label, BlockingQueue<File> queue, int initialQueueSize,
      String header, int shift) {
    this.progressBar = progressBar;
    this.label = label;
    this.initialQueueSize = initialQueueSize;
    this.queue = queue;
    this.header = header;
    this.shift = shift;
  }

  private String readFile(File file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    StringBuilder builder = new StringBuilder();

    try {
      while (reader.ready()) {
        char character = (char) reader.read();
        builder.append(character);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      reader.close();
    }

    return builder.toString();
  }

  private void encryptFile(File file, String data, String header, int shift) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));

    try {
      final boolean isEncrypted = data.startsWith(header);

      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          label.setText(String.format("%s - %s file: %s",
              getName(),
              isEncrypted ? "decrypting " : "encrypting",
              file.getName()));
        }
      });

      if (!isEncrypted)
        writer.write(header);

      char[] characters = data.substring(isEncrypted ? header.length() : 0).toCharArray();

      for (char character : characters) {
        char encryptedCharacter = (char) (character + (isEncrypted ? -shift : shift));
        writer.write(encryptedCharacter);
      }

      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          label.setText(String.format("%s - %s file: %s",
              getName(),
              isEncrypted ? "decrypted" : "encrypted",
              file.getName()));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      writer.close();
    }
  }

  @Override
  public void run() {
    try {
      while (true) {
        if (queue.isEmpty())
          break;

        File file = queue.take();
        String data = readFile(file);

        encryptFile(file, data, header, shift);

        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            progressBar.setProgress(progressBar.getProgress() + (1.0 / initialQueueSize));
          }
        });
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
