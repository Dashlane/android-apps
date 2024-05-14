package wei.mark.standout.ui;

import java.util.Locale;

public class TouchInfo {
    public int firstX, firstY, lastX, lastY;
    public double dist, scale, firstWidth, firstHeight;
    public float ratio;

    public boolean moving;

    @Override
    public String toString() {
        return String
                .format(Locale.US,
                        "WindowTouchInfo { firstX=%d, firstY=%d,lastX=%d, lastY=%d, firstWidth=%d, firstHeight=%d }",
                        firstX, firstY, lastX, lastY, firstWidth, firstHeight);
    }
}
