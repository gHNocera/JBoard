package com.ghartmann;

import javax.sound.sampled.*;
import java.io.File;

public class AudioPlayer {
    private final Mixer mixer;
    private final File audioFile;
    private SourceDataLine line;
    private FloatControl volumeControl;
    private Thread playThread;
    private boolean paused = false;

    public AudioPlayer(Mixer mixer, File audioFile) {
        this.mixer = mixer;
        this.audioFile = audioFile;
    }

    public void play() {
        if (playThread != null && playThread.isAlive()) return;
        playThread = new Thread(() -> {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile)) {

                // Formato original
                AudioFormat baseFormat = ais.getFormat();

                // Converter para PCM 16-bit
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                );

                try (AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, ais)) {
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
                    line = (SourceDataLine) mixer.getLine(info);
                    line.open(decodedFormat);
                    line.start();

                    // Controle de volume
                    if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                    }

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = din.read(buffer)) != -1) {
                        synchronized (this) {
                            while (paused) wait();
                        }
                        line.write(buffer, 0, bytesRead);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stop();
            }
        });
        playThread.start();
    }

    public void stop() {
        paused = false;
        if (line != null) {
            line.drain();
            line.stop();
            line.close();
        }
        if (playThread != null && playThread.isAlive()) playThread.interrupt();
        playThread = null;
    }

    public void pause() {
        paused = true;
    }

    public synchronized void resume() {
        paused = false;
        notifyAll();
    }

    public void setVolume(double value) {
        if (volumeControl != null) {
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float gain = (float) (min + (max - min) * value);
            volumeControl.setValue(gain);
        }
    }

    public boolean isPlaying() {
        return playThread != null && playThread.isAlive() && !paused;
    }

    public boolean isPaused() {
        return paused;
    }
}
