package de.unisb.cs.st.javaslicer.tracedCode;

public class String1 {

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        String s = "hello ";
        String t = "world!";
        String m = s + t;
        char c2 = m.charAt(2);
        char c8 = m.charAt(8);
        int sLen = m.length();
        int tLen = m.length();
        int mLen = m.length();
    }

}
