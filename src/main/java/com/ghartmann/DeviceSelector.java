package com.ghartmann;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class DeviceSelector {
    private final List<Mixer> outputMixers = new ArrayList<>();

    public DeviceSelector() {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : infos) {
            Mixer mixer = AudioSystem.getMixer(info);
            // filtramos mixers que suportam SourceDataLine (saída)
            if (mixer.isLineSupported(new DataLine.Info(SourceDataLine.class, getDefaultFormat()))) {
                outputMixers.add(mixer);
            }
        }
    }

    public List<String> getOutputDeviceNames() {
        List<String> names = new ArrayList<>();
        for (Mixer m : outputMixers) names.add(m.getMixerInfo().getName());
        return names;
    }

    public List<Mixer> getOutputMixers() {
        return outputMixers;
    }

    private AudioFormat getDefaultFormat() {
        return new AudioFormat(44100f, 16, 2, true, false);
    }
}
