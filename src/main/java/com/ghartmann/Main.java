package com.ghartmann;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FilenameFilter;

import javax.sound.sampled.Mixer;

public class Main extends Application {

    private DeviceSelector deviceSelector;
    private AudioPlayer audioPlayer;
    private ComboBox<String> deviceComboBox;
    private ListView<File> soundList;
    private Slider volumeSlider;
    private KeyCode playPauseKey = null; // tecla configurada pelo usuário

    @Override
    public void start(Stage stage) {

        deviceSelector = new DeviceSelector();
        deviceComboBox = new ComboBox<>(FXCollections.observableArrayList(deviceSelector.getOutputDeviceNames()));
        if (!deviceComboBox.getItems().isEmpty()) deviceComboBox.getSelectionModel().selectFirst();

        soundList = new ListView<>();

        // Adiciona arquivos da pasta padrão
        File defaultFolder = new File("audios"); // pasta padrão "audios" na raiz do projeto
        if (defaultFolder.exists() && defaultFolder.isDirectory()) {
            File[] audioFiles = defaultFolder.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".wav") || name.toLowerCase().endsWith(".mp3"));
            if (audioFiles != null) {
                soundList.getItems().addAll(audioFiles);
            }
        }

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

            if (audioPlayer == null) {
                Mixer mixer = deviceSelector.getMixer(index); // <-- use o mixer selecionado
                audioPlayer = new AudioPlayer(selected, mixer);
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

        Button configKeyBtn = new Button("Configurar tecla Play/Pause");
        Label keyLabel = new Label("Nenhuma tecla configurada");

        VBox root = new VBox(10,
                new Label("Dispositivo de saída:"), deviceComboBox,
                addSoundBtn, soundList,
                playBtn, stopBtn,
                configKeyBtn, keyLabel,
                new Label("Volume"), volumeSlider);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 400, 450);
        stage.setScene(scene);
        stage.setTitle("JBoard - Soundboard");

        // Adicione os listeners de teclado AQUI, depois de setar a cena!
        configKeyBtn.setOnAction(e -> {
            keyLabel.setText("Pressione a tecla desejada...");
            scene.setOnKeyPressed(event -> {
                playPauseKey = event.getCode();
                keyLabel.setText("Tecla configurada: " + playPauseKey.getName());
                // Remove o listener após configurar
                scene.setOnKeyPressed(null);
            });
        });

        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (playPauseKey != null && event.getCode() == playPauseKey) {
                File selected = soundList.getSelectionModel().getSelectedItem();
                if (selected == null) return;

                int index = deviceComboBox.getSelectionModel().getSelectedIndex();

                if (audioPlayer == null) {
                    Mixer mixer = deviceSelector.getMixer(index);
                    audioPlayer = new AudioPlayer(selected, mixer);
                    audioPlayer.setVolume(volumeSlider.getValue());
                    audioPlayer.play();
                } else {
                    if (audioPlayer.isPaused()) {
                        audioPlayer.resume();
                    } else {
                        audioPlayer.pause();
                    }
                }
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
