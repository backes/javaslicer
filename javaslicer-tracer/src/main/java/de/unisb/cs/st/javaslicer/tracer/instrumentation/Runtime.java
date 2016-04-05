package de.unisb.cs.st.javaslicer.tracer.instrumentation;

public class Runtime {

	private static boolean isRunning = false;
	
	public static boolean isRunning(){
		return isRunning;
	}
	
	public static void startTracing(){
		isRunning = true;
	}
	
	public static void stopTracing(){
		isRunning = false;
	}
	
	public static void finishTracing(){
		stopTracing();
	}
}
