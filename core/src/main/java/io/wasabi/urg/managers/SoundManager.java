package io.wasabi.urg.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

public final class SoundManager implements Disposable {
    private static final SoundManager INSTANCE = new SoundManager();

    private final ObjectMap<String, Sound> sounds = new ObjectMap<>();
    private final ObjectMap<String, Music> musicTracks = new ObjectMap<>();

    private Music currentMusic;
    private float sfxVolume = 0.6f;
    private float musicVolume = 0.6f;
    private boolean muted = false;

    private SoundManager() {}

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        loadSound("spin1", "sfx/spin1.wav");
        loadSound("bounce1", "sfx/bounce1.wav");
        loadSound("bounce2", "sfx/bounce2.wav");
        loadSound("bounce3", "sfx/bounce3.wav");
        loadSound("bounce4", "sfx/bounce4.wav");
        loadSound("bounce5", "sfx/bounce5.wav");
        loadSound("charmConsume", "sfx/charmConsume.wav");
        loadSound("error", "sfx/error.wav");
        loadSound("tileSelect", "sfx/tileSelect.wav");
        loadSound("tileDeselect", "sfx/tileDeselect.wav");
    }

    // ---- Loading ----

    private void loadSound(String key, String internalPath) {
        Sound sound = Gdx.audio.newSound(Gdx.files.internal(internalPath));
        sounds.put(key, sound);
    }

    private void loadMusic(String key, String internalPath) {
        Music music = Gdx.audio.newMusic(Gdx.files.internal(internalPath));
        musicTracks.put(key, music);
    }

    // ---- SFX playback ----

    public void playSound(String key) {
        playSound(key, sfxVolume);
    }

    public void playSound(String key, float volumeScale) {
        if (muted) return;

        Sound sound = sounds.get(key);
        if (sound == null) {
            Gdx.app.error("SoundManager", "No sound loaded for key: " + key);
            return;
        }
        sound.play(sfxVolume * volumeScale);
    }

    // ---- Music playback ----

    public void playMusic(String key, boolean loop) {
        Music music = musicTracks.get(key);
        if (music == null) {
            Gdx.app.error("SoundManager", "No music loaded for key: " + key);
            return;
        }

        if (currentMusic != null && currentMusic != music) {
            currentMusic.stop();
        }

        currentMusic = music;
        currentMusic.setLooping(loop);
        currentMusic.setVolume(muted ? 0f : musicVolume);
        currentMusic.play();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    public void pauseMusic() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }

    public void resumeMusic() {
        if (currentMusic != null) {
            currentMusic.play();
        }
    }

    // ---- Volume controls ----

    public void setSfxVolume(float volume) {
        this.sfxVolume = clampVolume(volume);
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = clampVolume(volume);
        if (currentMusic != null && !muted) {
            currentMusic.setVolume(musicVolume);
        }
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (currentMusic != null) {
            currentMusic.setVolume(muted ? 0f : musicVolume);
        }
    }

    public boolean isMuted() {
        return muted;
    }

    private float clampVolume(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    @Override
    public void dispose() {
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();

        for (Music music : musicTracks.values()) {
            music.dispose();
        }
        musicTracks.clear();
    }
}
