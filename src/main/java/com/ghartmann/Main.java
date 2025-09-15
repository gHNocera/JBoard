package com.ghartmann;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.sampled.Mixer;
import java.io.File;

public class Main extends Application {

    private DeviceSelector deviceSelector;
    private AudioPlayer audioPlayer;
    private ComboBox<String> deviceComboBox;
    private ListView<File> soundList;
    private Slider volumeSlider;

    @Override
    public void start(Stage stage) {

        deviceSelector = new DeviceSelector();
        deviceComboBox = new ComboBox<>(FXCollections.observableArrayList(deviceSelector.getOutputDeviceNames()));
        if (!deviceComboBox.getItems().isEmpty()) deviceComboBox.getSelectionModel().selectFirst();

        soundList = new ListView<>();

        Button addSoundBtn = new Button("Adicionar Som");
        addSoundBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Áudio", "*.wav", "*.mp3"));
            File file = fc.showOpenDialog(stage);
            if (file != null) soundList.getItems().add(file);
        });

        Button playBtn = new Button("Tocar/Pausar");
        Button stopBtn = new Button("Parar");

        volumeSlider = new Slider(0, 1, 0.5); // 0 a 1

        playBtn.setOnAction(e -> {
            File selected = soundList.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            int index = deviceComboBox.getSelectionModel().getSelectedIndex();
            Mixer mixer = deviceSelector.getOutputMixers().get(index);

            if (audioPlayer == null) {
                audioPlayer = new AudioPlayer(mixer, selected);
                audioPlayer.setVolume(volumeSlider.getValue());
                audioPlayer.play();
            } else {
                if (audioPlayer.isPaused()) {
                    audioPlayer.resume();
                } else {
                    audioPlayer.pause();
                }
            }
        });

        stopBtn.setOnAction(e -> {
            if (audioPlayer != null) {
                audioPlayer.stop();
                audioPlayer = null;
            }
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (audioPlayer != null) audioPlayer.setVolume(newVal.doubleValue());
        });

        VBox root = new VBox(10,
                new Label("Dispositivo de saída:"), deviceComboBox,
                addSoundBtn, soundList,
                playBtn, stopBtn,
                new Label("Volume"), volumeSlider);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 400, 400));
        stage.setTitle("JBoard - Soundboard");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
