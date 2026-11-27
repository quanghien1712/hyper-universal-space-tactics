package com.game.spaceshooter.infrastructure.sound;

import com.game.spaceshooter.infrastructure.utils.ResourceLoader;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;

public class Sound {
    private final List<Clip> clips = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(Sound.class.getName());
    private int index;

    public Sound(String path, int poolSize) {
        if (poolSize <= 0) throw new IllegalArgumentException("poolSize must be positive");
        for (int i = 0; i < poolSize; ++i) {
            try {
                Clip clip = loadClip(path);
                clips.add(clip);
            } catch (Exception e) {
                ResourceLoader.warn(path, e, "sound will be muted");
                break;
            }
        }
    }

    private Clip loadClip (String path) throws Exception {
        try (InputStream is = ResourceLoader.open(path)) {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is))) {
                Clip clip = AudioSystem.getClip();
                try {
                    clip.open(ais);
                    return clip;
                } catch (Exception e) {
                    clip.close();
                    throw e;
                }
            }
        }
    }

    public synchronized void play() {
        if (clips.isEmpty()) return;
        Clip clip = clips.get(index);
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
        index = (index + 1) % clips.size();
    }

    public synchronized void loop() {
        if (clips.isEmpty()) return;
        Clip clip = clips.get(index);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        index = (index + 1) % clips.size();
    }

    public synchronized void stop() {
        clips.forEach(Clip::stop);
    }

    public synchronized void setVolume(float percent) {
        float p = Math.max(0f, Math.min(1f, percent));
        for (Clip clip : clips) {
            try {
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB;
                if (p <= 0.0001f) {
                    dB = control.getMinimum();
                } else {
                    dB = (float) (20.0 * Math.log10(p));
                    dB = Math.max(control.getMinimum(), Math.min(control.getMaximum(), dB));
                }
                control.setValue(dB);
            } catch (IllegalArgumentException e) {
                logger.log(Level.FINE, "Clip does not support MASTER_GAIN", e);
            }
        }
    }

    public synchronized void reset() {
        for (Clip clip : clips) {
            clip.stop();
            clip.setFramePosition(0);
        }
    }

    public synchronized int getClipCount() {
        return clips.size();
    }
}
