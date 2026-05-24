package game.engine.gui;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Centralized sound manager so audio stays consistent across all screens.
 */
public class SoundManager {

    private static AudioClip buttonSound;
    private static AudioClip monsterSound;
    private static MediaPlayer musicPlayer;
    private static boolean musicMuted = false;
    private static boolean initialized = false;

    /** Load all audio clips ONCE at app startup. */
    public static void init() {
        if (initialized) return;
        initialized = true;

        try {
            String url = SoundManager.class
                .getResource("/audio/otherbuttons.mp3").toExternalForm();
            buttonSound = new AudioClip(url);
            buttonSound.setVolume(0.6);
        } catch (Exception e) {
            System.err.println("Could not load button sound: " + e.getMessage()); // e3mel try and catch better 
        }																		// better for not to cra												

        try {
            String url = SoundManager.class
                .getResource("/audio/monsterbutton_soundpress.mp3").toExternalForm();
            monsterSound = new AudioClip(url);
            monsterSound.setVolume(0.7);
        } catch (Exception e) {
            System.err.println("Could not load monster sound: " + e.getMessage());
        }

        try {
            String url = SoundManager.class
                .getResource("/audio/Je Sto Vicino A Te - Pino Daniele - Pino Daniele [Instrumental].mp3").toExternalForm();
            Media music = new Media(url);
            musicPlayer = new MediaPlayer(music);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.setVolume(0.3);
        } catch (Exception e) {
            System.err.println("Could not load music: " + e.getMessage());
        }
    }

    public static void playButtonClick() {
        if (buttonSound != null) buttonSound.play();
    }

    public static void playMonsterClick() {
        if (monsterSound != null) monsterSound.play();
    }

    public static void toggleMusic() {
        if (musicPlayer == null) return;
        if (musicMuted) {
            musicPlayer.play();
        } else {
            musicPlayer.pause();
        }
        musicMuted = !musicMuted;
    }

    public static boolean isMusicMuted() {
        return musicMuted;
    }

    public static void playMusicIfNeeded() {
        if (musicPlayer != null && !musicMuted) {
            musicPlayer.play();
        }
    }
}