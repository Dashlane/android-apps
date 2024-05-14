package wei.mark.standout.constants;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.ui.Window;

@SuppressWarnings("squid:S1118")
public class StandOutFlags {
	
	private static int flag_bit = 0;

	public static final int FLAG_DECORATION_SYSTEM = 1 << flag_bit++;

	public static final int FLAG_DECORATION_CLOSE_DISABLE = FLAG_DECORATION_SYSTEM
			| 1 << flag_bit++;

	public static final int FLAG_DECORATION_RESIZE_DISABLE = FLAG_DECORATION_SYSTEM
			| 1 << flag_bit++;

	public static final int FLAG_DECORATION_MAXIMIZE_DISABLE = FLAG_DECORATION_SYSTEM
			| 1 << flag_bit++;

	public static final int FLAG_DECORATION_MOVE_DISABLE = FLAG_DECORATION_SYSTEM
			| 1 << flag_bit++;

	public static final int FLAG_BODY_MOVE_ENABLE = 1 << flag_bit++;

    public static final int FLAG_BODY_MOVE_AXIS_X_DISABLED = 1 << flag_bit++;
    public static final int FLAG_BODY_MOVE_AXIS_Y_DISABLED = 1 << flag_bit++;

	public static final int FLAG_WINDOW_HIDE_ENABLE = 1 << flag_bit++;

	public static final int FLAG_WINDOW_BRING_TO_FRONT_ON_TOUCH = 1 << flag_bit++;

	public static final int FLAG_WINDOW_BRING_TO_FRONT_ON_TAP = 1 << flag_bit++;

	public static final int FLAG_WINDOW_EDGE_LIMITS_ENABLE = 1 << flag_bit++;

	public static final int FLAG_WINDOW_ASPECT_RATIO_ENABLE = 1 << flag_bit++;

	public static final int FLAG_WINDOW_PINCH_RESIZE_ENABLE = 1 << flag_bit++;

	public static final int FLAG_WINDOW_FOCUSABLE_DISABLE = 1 << flag_bit++;

	public static final int FLAG_WINDOW_FOCUS_INDICATOR_DISABLE = 1 << flag_bit++;

	public static final int FLAG_FIX_COMPATIBILITY_ALL_DISABLE = 1 << flag_bit++;

	public static final int FLAG_ADD_FUNCTIONALITY_ALL_DISABLE = 1 << flag_bit++;

	public static final int FLAG_ADD_FUNCTIONALITY_RESIZE_DISABLE = 1 << flag_bit++;

	public static final int FLAG_ADD_FUNCTIONALITY_DROP_DOWN_DISABLE = 1 << flag_bit++;

	public static final int FLAG_DISABLE_PERSISTENT_NOTIFICATION = 1 << flag_bit++;


}