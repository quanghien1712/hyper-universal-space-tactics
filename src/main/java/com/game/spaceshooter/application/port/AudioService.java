package com.game.spaceshooter.application.port;

public interface AudioService {
    void playSfx(String name);
    void playBgm(String name);
    void stop(String name);
    void resetAll();
    void stopAllSFX();
    void setSfxVolume(float percent);
    void setBgmVolume(float percent);
    void updateAllVolumes();
}
