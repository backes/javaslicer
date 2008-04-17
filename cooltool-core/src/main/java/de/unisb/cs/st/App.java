package de.unisb.cs.st;

/**
 * Hello world!
 *
 */
public class App {

    /**
     * Localized "hello world" string.
     */
    private String localized = "Hello World!";

    /**
     * Getter for localized "hello world" string.
     *
     * @return the localized "hello world" string.
     */
    public String getLocalized() {
        return this.localized;
    }

    /**
     * Setter for localized "hello world" string.
     *
     * @param localized
     *            the localized string to set
     */
    public void setLocalized(String localized) {
        this.localized = localized;
    }

    /**
     * Runs this tool.
     */
    public void run() {
        System.out.println(this.localized);
    }

}
