package wei.mark.standout;

@SuppressWarnings("squid:S1118")
public class Utils {
	public static boolean isSet(int flags, int flag) {
		return (flags & flag) == flag;
	}
}
