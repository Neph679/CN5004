package org.example.cryptostockportfoliotracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/org/example/cryptostockportfoliotracker/portfolio-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 800, 600);

        stage.setTitle("Portfolio Tracker");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
