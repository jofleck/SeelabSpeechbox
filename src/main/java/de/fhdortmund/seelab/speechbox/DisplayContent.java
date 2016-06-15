package de.fhdortmund.seelab.speechbox;

/**
 * Created by jonas on 17.04.16.
 */
public class DisplayContent {
    private String firstLine;
    private String secondLine;
    private int duration;
    private boolean blink;

    public DisplayContent(String firstLine, String secondLine, int duration, boolean blink) {
        this.firstLine = firstLine;
        this.secondLine = secondLine;
        this.duration = duration;
        this.blink = blink;

    }

    public String getFirstLine() {
        return firstLine;
    }

    public void setFirstLine(String firstLine) {
        this.firstLine = firstLine;
    }

    public String getSecondLine() {
        return secondLine;
    }

    public void setSecondLine(String secondLine) {
        this.secondLine = secondLine;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isBlink() {
        return blink;
    }

    public void setBlink(boolean blink) {
        this.blink = blink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisplayContent that = (DisplayContent) o;

        if (duration != that.duration) return false;
        if (blink != that.blink) return false;
        if (!firstLine.equals(that.firstLine)) return false;
        return secondLine.equals(that.secondLine);

    }

    @Override
    public int hashCode() {
        int result = firstLine.hashCode();
        result = 31 * result + secondLine.hashCode();
        result = 31 * result + duration;
        result = 31 * result + (blink ? 1 : 0);
        return result;
    }
}
