package com.github.fidelitystick5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import javafx.concurrent.Task;

public class EncryptorThread extends Task<Void> {
  private BlockingQueue<File> queue;
  private String header;
  private int shift;

  public EncryptorThread(BlockingQueue<File> queue, String header, int shift) {
    this.queue = queue;
    this.header = header;
    this.shift = shift;
  }

  private String readFile(File file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    StringBuilder builder = new StringBuilder();
    int ch;

    while ((ch = reader.read()) != -1) {
      builder.append((char) ch);
    }

    reader.close();
    return builder.toString();
  }

  private void encryptFile(File file, String data, String header, int shift) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    boolean isEncrypted = data.startsWith(header);
    updateMessage(String.format("%s file: %s", isEncrypted ? "Decrypting" : "Encrypting", file.getName()));

    if (!isEncrypted)
      writer.write(header);

    char[] characters = data.substring(isEncrypted ? header.length() : 0).toCharArray();
    long size = characters.length;

    for (int i = 0; i < characters.length; i++) {
      char character = characters[i];
      char encryptedCharacter = (char) (character + (isEncrypted ? -shift : shift));
      writer.write(encryptedCharacter);

      updateProgress(i + 1, size);
    }

    updateMessage(String.format("%s file: %s", isEncrypted ? "Decrypted" : "Encrypted", file.getName()));
    writer.close();
  }

  @Override
  protected Void call() {
    try {
      while (!queue.isEmpty()) {
        File file = queue.take();
        String data = readFile(file);

        encryptFile(file, data, header, shift);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
