package com.github.fidelitystick5;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class App extends Application {
    private void encrypt(File directory) {
        final File[] files = directory.listFiles();
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

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();

        grid.setPadding(new Insets(16));

        Scene scene = new Scene(grid, 300, 200);

        Button button = new Button("Choose directory to encrypt files");

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Resource File");

        button.setOnAction(e -> {
            File selectedDirectory = directoryChooser.showDialog(primaryStage);

            if (selectedDirectory != null) {
                encrypt(selectedDirectory);
                return;
            }

            System.out.println("No directory selected.");
        });

        grid.add(button, 0, 0);

        primaryStage.setTitle("JavaFX Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
