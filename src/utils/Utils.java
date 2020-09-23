package utils;

public class Utils {
	public static void invokeProcedure(Procedure procedure) {
		try {
			procedure.invoke();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
