package com.github.fidelitystick5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import javafx.concurrent.Task;

public class EncryptorTask extends Task<Void> {
  private ArrayBlockingQueue<File> queue;
  private String header;
  private int shift;

  public EncryptorTask(ArrayBlockingQueue<File> queue, String header, int shift) {
    this.queue = queue;
    this.header = header;
    this.shift = shift;
  }

  private String readFile(File file) throws IOException {
    final BufferedReader reader = new BufferedReader(new FileReader(file));
    final StringBuilder builder = new StringBuilder();
    int character;

    while ((character = reader.read()) != -1) {
      builder.append((char) character);
    }

    reader.close();
    return builder.toString();
  }

  private void encryptFile(File file, String data, String header, int shift) throws IOException, InterruptedException {
    final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    final boolean isEncrypted = data.startsWith(header);

    if (!isEncrypted)
      writer.write(header);

    final char[] characters = data.substring(isEncrypted ? header.length() : 0).toCharArray();
    final long size = characters.length;

    for (int i = 0; i < characters.length; i++) {
      final int delay = (int) (Math.random() * 50);

      Thread.sleep(delay);

      final char character = characters[i];
      final char encryptedCharacter = (char) (character + (isEncrypted ? -shift : shift));
      writer.write(encryptedCharacter);

      updateProgress(i + 1, size);
      updateMessage(
          String.format("%s file: %s (%s/%s)", isEncrypted ? "Decrypting" : "Encrypting", file.getName(), i + 1, size));
    }

    writer.close();
  }

  @Override
  protected Void call() {
    try {
      while (!queue.isEmpty()) {
        final File file = queue.take();
        final String data = readFile(file);

        encryptFile(file, data, header, shift);

        System.out.printf("Processed file: %s\n", file.getName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
