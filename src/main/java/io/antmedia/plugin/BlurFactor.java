package io.antmedia.plugin;

public enum BlurFactor {
    LOW(13),MEDIUM(19),HIGH(25);

    public final int value;

    private BlurFactor(int value) {
        this.value = value;
    }
}
