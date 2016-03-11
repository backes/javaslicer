package de.unisb.cs.st.javaslicer.integration.src;

import java.util.LinkedList;


public class MohammedsCode {

    public static void main(String[] args) {
        LinkedList<Integer> s = new LinkedList<>();
        foo(s);
        System.out.println("DONE: " + s.get(0));
    }

    public static void foo(LinkedList<Integer> s) {
        int x = 19;
        s.add(x);
    }
}
