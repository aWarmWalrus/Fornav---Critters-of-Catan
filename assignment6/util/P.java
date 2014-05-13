package util;

public class P {

	public static boolean rototyping = true;
	
	public static void rint(Object a){
		if(!rototyping){
			return;
		}
		if(!a.getClass().equals("".getClass())){
			System.out.println(a.toString());
		} else {
			System.out.println(a);
		} //System.out.println();
	}
}
