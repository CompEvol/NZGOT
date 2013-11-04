package nzgot.ec;

public class Debugger {
	
	public static boolean isEnabled() {
		return true;
	}
	
	public static void log(Object o) {
		if (Debugger.isEnabled()) {
			System.out.println(o.toString());
		}
	}
}
