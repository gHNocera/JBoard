package com.ghartmann;

import javax.sound.sampled.*;
import java.io.File;

public class AudioPlayer {
    private final File audioFile;
    private final Mixer mixer;
    private SourceDataLine line;
    private FloatControl volumeControl;
    private Thread playThread;
    private volatile boolean paused = false;
    private volatile boolean stopped = false;
    private double pendingVolume = 0.5; // valor padrão

    public AudioPlayer(File audioFile, Mixer mixer) {
        this.audioFile = audioFile;
        this.mixer = mixer;
    }

    public void play() {
        if (playThread != null && playThread.isAlive()) return;
        stopped = false;
        playThread = new Thread(() -> {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile)) {
                AudioFormat baseFormat = ais.getFormat();
                AudioFormat decodedFormat = baseFormat;

                // Só converte se não for PCM_SIGNED 16 bits
                if (baseFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || baseFormat.getSampleSizeInBits() != 16) {
                    decodedFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(),
                            16,
                            baseFormat.getChannels(),
                            baseFormat.getChannels() * 2,
                            baseFormat.getSampleRate(),
                            false // little endian
                    );
                }

                try (AudioInputStream din = 
                    decodedFormat == baseFormat ? ais : AudioSystem.getAudioInputStream(decodedFormat, ais)) {

                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
                    line = (SourceDataLine) mixer.getLine(info);
                    line.open(decodedFormat);

                    if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                        setVolume(pendingVolume); // aplica o volume desejado imediatamente
                    }

                    line.start();

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while (!stopped && (bytesRead = din.read(buffer)) != -1) {
                        synchronized (this) {
                            while (paused && !stopped) wait();
                        }
                        if (stopped) break;
                        line.write(buffer, 0, bytesRead);
                    }
                    line.drain();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (line != null) {
                    line.stop();
                    line.close();
                }
            }
        });
        playThread.start();
    }

    public synchronized void stop() {
        stopped = true;
        paused = false;
        notifyAll();
        if (playThread != null && playThread.isAlive()) playThread.interrupt();
        playThread = null;
    }

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void resume() {
        paused = false;
        notifyAll();
    }

    public void setVolume(double value) {
        pendingVolume = value;
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
