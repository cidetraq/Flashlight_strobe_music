package com.cidetraq.flashlight_strobe_music;

public class NoValidNoiseLevelException extends Exception {
    public NoValidNoiseLevelException(double errorMessage) {
        super(String.valueOf(errorMessage));
    }
}
