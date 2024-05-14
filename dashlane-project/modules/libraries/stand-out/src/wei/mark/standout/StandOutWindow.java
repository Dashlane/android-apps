package wei.mark.standout;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.dashlane.util.DeviceUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;


@SuppressWarnings({"squid:S1192", "squid:S1604"})
public abstract class StandOutWindow extends Service {
    static final String TAG = "StandOutWindow";

    public static final int DEFAULT_ID = 0;

    public static final int ONGOING_NOTIFICATION_ID = -1;

    public static final int DISREGARD_ID = -2;

    public static final String ACTION_SHOW = "SHOW";

    public static final String ACTION_RESTORE = "RESTORE";

    public static final String ACTION_CLOSE = "CLOSE";

    public static final String ACTION_CLOSE_ALL = "CLOSE_ALL";

    public static final String ACTION_SEND_DATA = "SEND_DATA";

    public static final String ACTION_HIDE = "HIDE";

    public static void show(Context context,
                            Class<? extends StandOutWindow> cls, int id) {
        startService(context, getShowIntent(context, cls, id));
    }

    public static void hide(Context context,
                            Class<? extends StandOutWindow> cls, int id) {
        startService(context, getHideIntent(context, cls, id));
    }

    public static void close(Context context,
                             Class<? extends StandOutWindow> cls, int id) {
        startService(context, getCloseIntent(context, cls, id));
    }

    public static void closeAll(Context context,
                                Class<? extends StandOutWindow> cls) {
        startService(context, getCloseAllIntent(context, cls));
    }

    public static void sendData(Context context,
                                Class<? extends StandOutWindow> toCls, int toId, int requestCode,
                                Bundle data, Class<? extends StandOutWindow> fromCls, int fromId) {
        startService(context, getSendDataIntent(context, toCls, toId, requestCode, data, fromCls, fromId));
    }

    public static Intent getShowIntent(Context context,
                                       Class<? extends StandOutWindow> cls, int id) {
        boolean cached = sWindowCache.isCached(id, cls);
        String action = cached ? ACTION_RESTORE : ACTION_SHOW;
        Uri uri = cached ? Uri.parse("standout://" + cls + '/' + id) : null;
        return new Intent(context, cls).putExtra("id", id).setAction(action)
                .setData(uri);
    }

    public static Intent getHideIntent(Context context,
                                       Class<? extends StandOutWindow> cls, int id) {
        return new Intent(context, cls).putExtra("id", id).setAction(
                ACTION_HIDE);
    }

    public static Intent getCloseIntent(Context context,
                                        Class<? extends StandOutWindow> cls, int id) {
        return new Intent(context, cls).putExtra("id", id).setAction(
                ACTION_CLOSE);
    }

    public static Intent getCloseAllIntent(Context context,
                                           Class<? extends StandOutWindow> cls) {
        return new Intent(context, cls).setAction(ACTION_CLOSE_ALL);
    }

    public static Intent getSendDataIntent(Context context,
                                           Class<? extends StandOutWindow> toCls, int toId, int requestCode,
                                           Bundle data, Class<? extends StandOutWindow> fromCls, int fromId) {
        return new Intent(context, toCls).putExtra("id", toId)
                .putExtra("requestCode", requestCode)
                .putExtra("wei.mark.standout.data", data)
                .putExtra("wei.mark.standout.fromCls", fromCls)
                .putExtra("fromId", fromId).setAction(ACTION_SEND_DATA);
    }

    private static void startService(Context context, Intent serviceIntent) {
        if (context == null) {
            return;
        }
        try {
            context.startService(serviceIntent);
        } catch (Exception ex) {
        }
    }

    
    static WindowCache sWindowCache;
    static Window sFocusedWindow;

    
    static {
        sWindowCache = new WindowCache();
        sFocusedWindow = null;
    }

    
    WindowManager mWindowManager;
    private NotificationManager mNotificationManager;
    LayoutInflater mLayoutInflater;

    
    private boolean startedForeground;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        startedForeground = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            String action = intent.getAction();
            int id = intent.getIntExtra("id", DEFAULT_ID);

            
            if (id == ONGOING_NOTIFICATION_ID) {
                throw new RuntimeException(
                        "ID cannot equals StandOutWindow.ONGOING_NOTIFICATION_ID");
            }

            if (ACTION_SHOW.equals(action) || ACTION_RESTORE.equals(action)) {
                show(id);
            } else if (ACTION_HIDE.equals(action)) {
                hide(id);
            } else if (ACTION_CLOSE.equals(action)) {
                close(id);
            } else if (ACTION_CLOSE_ALL.equals(action)) {
                closeAll();
            } else if (ACTION_SEND_DATA.equals(action)) {
                if (!isExistingId(id) && id != DISREGARD_ID) {
                                    "non-existant window. If this is not intended, make sure toId is " +
                                    "either an existing window's id or DISREGARD_ID.",
                            "",
                            false);
                }
                Bundle data = intent.getBundleExtra("wei.mark.standout.data");
                int requestCode = intent.getIntExtra("requestCode", 0);
                @SuppressWarnings("unchecked")
                Class<? extends StandOutWindow> fromCls = (Class<? extends StandOutWindow>) intent
                        .getSerializableExtra("wei.mark.standout.fromCls");
                int fromId = intent.getIntExtra("fromId", DEFAULT_ID);
                onReceiveData(id, requestCode, data, fromCls, fromId);
            }
        } else {
        }

        
        
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        
        closeAll();
    }

    public abstract String getAppName();

    public abstract int getAppIcon();

    public abstract void createAndAttachView(int id, FrameLayout frame);

    public abstract StandOutLayoutParams getParams(int id, Window window);

    public int getFlags(int id) {
        return 0;
    }

    public String getTitle(int id) {
        return getAppName();
    }

    public int getIcon(int id) {
        return getAppIcon();
    }

    public String getPersistentNotificationTitle(int id) {
        return getAppName() + " Running";
    }

    public String getPersistentNotificationMessage(int id) {
        return "";
    }

    public Intent getPersistentNotificationIntent(int id) {
        return null;
    }

    public NotificationCompat.Action getPersistentNotificationAction(int id) {
        return null;
    }

    public int getHiddenIcon() {
        return getAppIcon();
    }

    public String getHiddenNotificationTitle(int id) {
        return getAppName() + " Hidden";
    }

    public String getHiddenNotificationMessage(int id) {
        return "";
    }

    public Intent getHiddenNotificationIntent(int id) {
        return null;
    }

    public Notification getPersistentNotification(int id) {
        
        
        int icon = getAppIcon();
        long when = System.currentTimeMillis();
        Context c = getApplicationContext();
        String contentTitle = getPersistentNotificationTitle(id);
        String contentText = getPersistentNotificationMessage(id);
        String tickerText = String.format(Locale.getDefault(), "%s: %s", contentTitle, contentText);

        
        
        
        Intent notificationIntent = getPersistentNotificationIntent(id);

        PendingIntent contentIntent = null;

        if (notificationIntent != null) {
            contentIntent = PendingIntent.getService(this, 0,
                    notificationIntent,
                    
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), getNotificationChannelId())
                .setSmallIcon(icon)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .setWhen(when).build();

        return notification;
    }

    public String getNotificationChannelId() {
        return "default";
    }

    public Notification getHiddenNotification(int id) {
        
        int icon = getHiddenIcon();
        long when = System.currentTimeMillis();
        Context c = getApplicationContext();
        String contentTitle = getHiddenNotificationTitle(id);
        String contentText = getHiddenNotificationMessage(id);
        String tickerText = String.format(Locale.US, "%s: %s", contentTitle, contentText);

        
        Intent notificationIntent = getHiddenNotificationIntent(id);

        PendingIntent contentIntent = null;

        if (notificationIntent != null) {
            contentIntent = PendingIntent.getService(this, 0,
                    notificationIntent,
                    
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }


        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(icon)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .setWhen(when).build();
        return notification;
    }

    public Animation getShowAnimation(int id) {
        return AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
    }

    public Animation getHideAnimation(int id) {
        return AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
    }

    public Animation getCloseAnimation(int id) {
        return AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
    }

    public int getThemeStyle() {
        return 0;
    }

    public PopupWindow getDropDown(final int id) {
        final List<DropDownListItem> items;

        List<DropDownListItem> dropDownListItems = getDropDownItems(id);
        if (dropDownListItems != null) {
            items = dropDownListItems;
        } else {
            items = new ArrayList<StandOutWindow.DropDownListItem>();
        }

        
        items.add(new DropDownListItem(
                android.R.drawable.ic_menu_close_clear_cancel, "Quit "
                + getAppName(), new Runnable() {

            @Override
            public void run() {
                closeAll();
            }
        }));

        
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);

        final PopupWindow dropDown = new PopupWindow(list,
                StandOutLayoutParams.WRAP_CONTENT,
                StandOutLayoutParams.WRAP_CONTENT, true);

        for (final DropDownListItem item : items) {
            ViewGroup listItem = (ViewGroup) mLayoutInflater.inflate(
                    R.layout.drop_down_list_item, null);
            list.addView(listItem);

            ImageView icon = listItem.findViewById(R.id.icon);
            icon.setImageResource(item.icon);

            TextView description = listItem
                    .findViewById(R.id.description);
            description.setText(item.description);

            listItem.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    item.action.run();
                    dropDown.dismiss();
                }
            });
        }

        Drawable background = getResources().getDrawable(
                android.R.drawable.editbox_dropdown_dark_frame);
        dropDown.setBackgroundDrawable(background);
        return dropDown;
    }

    public List<DropDownListItem> getDropDownItems(int id) {
        return null;
    }

    public boolean onTouchBody(int id, Window window, View view,
                               MotionEvent event) {
        return false;
    }

    public void onMove(int id, Window window, View view, MotionEvent event) {
    }

    public void onResize(int id, Window window, View view, MotionEvent event) {
    }

    public boolean onShow(int id, Window window) {
        return false;
    }

    public boolean onHide(int id, Window window) {
        return false;
    }

    public boolean onClose(int id, Window window) {
        return false;
    }

    public boolean onCloseAll() {
        return false;
    }

    public void onReceiveData(int id, int requestCode, Bundle data,
                              Class<? extends StandOutWindow> fromCls, int fromId) {
    }

    public boolean onUpdate(int id, Window window, StandOutLayoutParams params) {
        return false;
    }

    public boolean onBringToFront(int id, Window window) {
        return false;
    }

    public boolean onFocusChange(int id, Window window, boolean focus) {
        return false;
    }

    public boolean onKeyEvent(int id, Window window, KeyEvent event) {
        return false;
    }

    public final synchronized Window show(int id) {
        
        Window cachedWindow = getWindow(id);
        final Window window;

        
        if (cachedWindow != null) {
            window = cachedWindow;
        } else {
            window = new Window(this, id);
        }

        
        if (onShow(id, window)) {
            return null;
        }

        
        if (window.visibility == Window.VISIBILITY_VISIBLE) {
            focus(id);
            return window;
        }

        window.visibility = Window.VISIBILITY_VISIBLE;

        
        Animation animation = getShowAnimation(id);

        
        StandOutLayoutParams params = window.getLayoutParams();

        try {
            
            mWindowManager.addView(window, params);

            
            if (animation != null) {
                window.getChildAt(0).startAnimation(animation);
            }
        } catch (Exception ex) {
            
            close(id);
        }

        
        sWindowCache.putCache(id, getClass(), window);

        
        Notification notification = null;
        if (!Utils.isSet(window.flags, StandOutFlags.FLAG_DISABLE_PERSISTENT_NOTIFICATION)) {
            notification = getPersistentNotification(id);
        }

        
        if (notification != null) {
            
            if (!startedForeground) {
                
                startForeground(ONGOING_NOTIFICATION_ID, notification);
                startedForeground = true;
            } else {
                
                mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
            }
        } else {
            
            if (!startedForeground && !Utils.isSet(window.flags, StandOutFlags.FLAG_DISABLE_PERSISTENT_NOTIFICATION)) {
                throw new RuntimeException("Your StandOutWindow service must"
                        + "provide a persistent notification."
                        + "The notification prevents Android"
                        + "from killing your service in low"
                        + "memory situations.");
            }
        }

        focus(id);

        return window;
    }

    public final synchronized void hide(int id) {
        
        final Window window = getWindow(id);

        if (window == null) {
            
            close(id);
            return;
            
        }

        
        if (onHide(id, window)) {
            return;
        }

        
        if (Utils.isSet(window.flags, StandOutFlags.FLAG_WINDOW_HIDE_ENABLE)) {
            window.visibility = Window.VISIBILITY_TRANSITION;

            
            Notification notification = getHiddenNotification(id);

            
            Animation animation = getHideAnimation(id);

            try {
                
                if (animation != null) {
                    animation.setAnimationListener(new AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            
                            mWindowManager.removeView(window);
                            window.visibility = Window.VISIBILITY_GONE;
                        }
                    });
                    window.getChildAt(0).startAnimation(animation);
                } else {
                    
                    mWindowManager.removeView(window);
                }
            } catch (Exception ex) {
            }

            
            notification.flags = notification.flags
                    | Notification.FLAG_NO_CLEAR
                    | Notification.FLAG_AUTO_CANCEL;

            mNotificationManager.notify(getClass().hashCode() + id,
                    notification);

        } else {
            
            close(id);
        }
    }

    public final synchronized void close(final int id) {
        
        final Window window = getWindow(id);

        if (window == null) {
            
            return;
            
        }

        if (window.visibility == Window.VISIBILITY_TRANSITION) {
            return;
        }

        
        if (onClose(id, window)) {
            return;
        }

        
        mNotificationManager.cancel(getClass().hashCode() + id);

        
        mayStop();

        unfocus(window);

        window.visibility = Window.VISIBILITY_TRANSITION;

        
        Animation animation = getCloseAnimation(id);

        
        try {
            
            if (animation != null) {
                animation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        
                        mWindowManager.removeView(window);
                        window.visibility = Window.VISIBILITY_GONE;

                        
                        sWindowCache.removeCache(id,
                                StandOutWindow.this.getClass());
                        mayStop();
                    }
                });
                window.getChildAt(0).startAnimation(animation);
            } else {
                
                mWindowManager.removeView(window);

                
                sWindowCache.removeCache(id, getClass());

                mayStop();
            }
        } catch (Exception ex) {
        }
    }

    public final synchronized void closeAll() {
        
        if (onCloseAll()) {
            return;
        }
        mayStop();
        
        LinkedList<Integer> ids = new LinkedList<Integer>();
        for (int id : getExistingIds()) {
            ids.add(id);
        }
        
        for (int id : ids) {
            close(id);
        }
    }

    public final void sendData(int fromId,
                               Class<? extends StandOutWindow> toCls, int toId, int requestCode,
                               Bundle data) {
        StandOutWindow.sendData(this, toCls, toId, requestCode, data,
                getClass(), fromId);
    }

    public final synchronized void bringToFront(int id) {
        Window window = getWindow(id);
        if (window == null) {
            return;
        }

        if (window.visibility == Window.VISIBILITY_GONE) {
            return;
        }

        if (window.visibility == Window.VISIBILITY_TRANSITION) {
            return;
        }

        
        if (onBringToFront(id, window)) {
            return;
        }

        StandOutLayoutParams params = window.getLayoutParams();

        
        try {
            mWindowManager.removeView(window);
        } catch (Exception ex) {
        }
        try {
            mWindowManager.addView(window, params);
        } catch (Exception ex) {
        }
    }

    public final synchronized boolean focus(int id) {
        
        final Window window = getWindow(id);
        if (window == null) {
            return false;
        }

        if (!Utils.isSet(window.flags,
                StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE)) {
            
            if (sFocusedWindow != null) {
                unfocus(sFocusedWindow);
            }

            return window.onFocus(true);
        }

        return false;
    }

    public final synchronized boolean unfocus(int id) {
        Window window = getWindow(id);
        return unfocus(window);
    }

    public final int getUniqueId() {
        int unique = DEFAULT_ID;
        for (int id : getExistingIds()) {
            unique = Math.max(unique, id + 1);
        }
        return unique;
    }

    public final boolean isExistingId(int id) {
        return sWindowCache.isCached(id, getClass());
    }

    public final Set<Integer> getExistingIds() {
        return sWindowCache.getCacheIds(getClass());
    }

    public final Window getWindow(int id) {
        return sWindowCache.getCache(id, getClass());
    }

    public final Window getFocusedWindow() {
        return sFocusedWindow;
    }

    
    @SuppressWarnings("squid:S2696")
    public final void setFocusedWindow(Window window) {
        sFocusedWindow = window;
    }

    public final void setTitle(int id, String text) {
        Window window = getWindow(id);
        if (window != null) {
            View title = window.findViewById(R.id.title);
            if (title instanceof TextView) {
                ((TextView) title).setText(text);
            }
        }
    }

    public final void setIcon(int id, int drawableRes) {
        Window window = getWindow(id);
        if (window != null) {
            View icon = window.findViewById(R.id.window_icon);
            if (icon instanceof ImageView) {
                ((ImageView) icon).setImageResource(drawableRes);
            }
        }
    }

    public boolean onTouchHandleMove(int id, Window window, View view,
                                     MotionEvent event) {
        StandOutLayoutParams params = window.getLayoutParams();

        
        

        int totalDeltaX = window.touchInfo.lastX - window.touchInfo.firstX;
        int totalDeltaY = window.touchInfo.lastY - window.touchInfo.firstY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();

                window.touchInfo.firstX = window.touchInfo.lastX;
                window.touchInfo.firstY = window.touchInfo.lastY;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) event.getRawX() - window.touchInfo.lastX;
                int deltaY = (int) event.getRawY() - window.touchInfo.lastY;

                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();

                if (window.touchInfo.moving
                        || Math.abs(totalDeltaX) >= params.threshold
                        || Math.abs(totalDeltaY) >= params.threshold) {
                    window.touchInfo.moving = true;

                    
                    if (Utils.isSet(window.flags,
                            StandOutFlags.FLAG_BODY_MOVE_ENABLE)) {

                        
                        if (event.getPointerCount() == 1) {
                            if (!Utils.isSet(window.flags, StandOutFlags.FLAG_BODY_MOVE_AXIS_X_DISABLED)) {
                                params.x += deltaX;
                            }
                            if (!Utils.isSet(window.flags, StandOutFlags.FLAG_BODY_MOVE_AXIS_Y_DISABLED)) {
                                params.y += deltaY;
                            }
                        }

                        window.edit().setPosition(params.x, params.y).commit();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                window.touchInfo.moving = false;

                if (event.getPointerCount() == 1) {

                    
                    boolean tap = Math.abs(totalDeltaX) < params.threshold
                            && Math.abs(totalDeltaY) < params.threshold;
                    if (tap
                            && Utils.isSet(
                            window.flags,
                            StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP)) {
                        StandOutWindow.this.bringToFront(id);
                    }
                }

                
                else if (Utils.isSet(window.flags,
                        StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TOUCH)) {
                    StandOutWindow.this.bringToFront(id);
                }

                break;
        }

        onMove(id, window, view, event);

        return true;
    }

    public boolean onTouchHandleResize(int id, Window window, View view,
                                       MotionEvent event) {
        StandOutLayoutParams params = window
                .getLayoutParams();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();

                window.touchInfo.firstX = window.touchInfo.lastX;
                window.touchInfo.firstY = window.touchInfo.lastY;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) event.getRawX() - window.touchInfo.lastX;
                int deltaY = (int) event.getRawY() - window.touchInfo.lastY;

                
                params.width += deltaX;
                params.height += deltaY;

                
                if (params.width >= params.minWidth
                        && params.width <= params.maxWidth) {
                    window.touchInfo.lastX = (int) event.getRawX();
                }

                if (params.height >= params.minHeight
                        && params.height <= params.maxHeight) {
                    window.touchInfo.lastY = (int) event.getRawY();
                }

                window.edit().setSize(params.width, params.height).commit();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        onResize(id, window, view, event);

        return true;
    }

    public synchronized boolean unfocus(Window window) {
        if (window == null) {
            throw new IllegalArgumentException(
                    "Tried to unfocus a null window.");
        }
        return window.onFocus(false);
    }

    public void updateViewLayout(int id, StandOutLayoutParams params) {
        Window window = getWindow(id);

        if (window == null || params == null) {
            return;
        }

        if (window.visibility == Window.VISIBILITY_GONE) {
            return;
        }

        if (window.visibility == Window.VISIBILITY_TRANSITION) {
            return;
        }

        
        if (onUpdate(id, window, params)) {
            return;
        }

        try {
            window.setLayoutParams(params);
            mWindowManager.updateViewLayout(window, params);
        } catch (Exception ex) {
        }
    }


    private void mayStop() {
        
        if (sWindowCache.getCacheSize(getClass()) == 0 || getExistingIds().size() <= 1) {
            
            
            startedForeground = false;
            stopForeground(true);
        }
    }

    public class StandOutLayoutParams extends WindowManager.LayoutParams {
        public static final int LEFT = 0;
        public static final int TOP = 0;
        public static final int RIGHT = Integer.MAX_VALUE;
        public static final int BOTTOM = Integer.MAX_VALUE;
        public static final int CENTER = Integer.MIN_VALUE;
        public static final int AUTO_POSITION = Integer.MIN_VALUE + 1;

        public int threshold;

        public int minWidth, minHeight, maxWidth, maxHeight;

        public StandOutLayoutParams(int id) {

            super(200, 200, getLayoutParamsType(),
                    StandOutLayoutParams.FLAG_NOT_TOUCH_MODAL
                            | StandOutLayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);

            int windowFlags = getFlags(id);

            setFocusFlag(false);

            if (!Utils.isSet(windowFlags,
                    StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE)) {
                
                flags |= FLAG_LAYOUT_NO_LIMITS;
            }

            x = getX(id, width);
            y = getY(id, height);

            gravity = Gravity.TOP | Gravity.LEFT;

            threshold = 10;
            minWidth = minHeight = 0;
            maxWidth = maxHeight = Integer.MAX_VALUE;
        }

        public StandOutLayoutParams(int id, int w, int h) {
            this(id);
            width = w;
            height = h;
        }

        public StandOutLayoutParams(int id, int w, int h, int xpos, int ypos) {
            this(id, w, h);

            if (xpos != AUTO_POSITION) {
                x = xpos;
            }
            if (ypos != AUTO_POSITION) {
                y = ypos;
            }

            int[] screenSize = DeviceUtils.getScreenSize(getApplicationContext());
            int width = screenSize[0];
            int height = screenSize[1];

            if (x == RIGHT) {
                x = width - w;
            } else if (x == CENTER) {
                x = (width - w) / 2;
            }

            if (y == BOTTOM) {
                y = height - h;
            } else if (y == CENTER) {
                y = (height - h) / 2;
            }
        }

        public StandOutLayoutParams(int id, int w, int h, int xpos, int ypos,
                                    int minWidth, int minHeight) {
            this(id, w, h, xpos, ypos);

            this.minWidth = minWidth;
            this.minHeight = minHeight;
        }

        public StandOutLayoutParams(int id, int w, int h, int xpos, int ypos,
                                    int minWidth, int minHeight, int threshold) {
            this(id, w, h, xpos, ypos, minWidth, minHeight);

            this.threshold = threshold;
        }

        
        private int getX(int id, int width) {
            int displayWidth = DeviceUtils.getScreenSize(getApplicationContext())[0];

            int types = sWindowCache.size();

            int initialX = 100 * types;
            int variableX = 100 * id;
            int rawX = initialX + variableX;

            return rawX % (displayWidth - width);
        }

        
        private int getY(int id, int height) {
            int[] displaySize = DeviceUtils.getScreenSize(getApplicationContext());
            int displayWidth = displaySize[0];
            int displayHeight = displaySize[1];

            int types = sWindowCache.size();

            int initialY = 100 * types;
            int variableY = x + 200 * (100 * id) / (displayWidth - width);

            int rawY = initialY + variableY;

            return rawY % (displayHeight - height);
        }

        public void setFocusFlag(boolean focused) {
            if (focused) {
                flags = flags ^ StandOutLayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                flags = flags | StandOutLayoutParams.FLAG_NOT_FOCUSABLE;
            }
        }

    }

    private static int getLayoutParamsType() {
        return StandOutLayoutParams.TYPE_APPLICATION_OVERLAY;
    }

    protected class DropDownListItem {
        public int icon;
        public String description;
        public Runnable action;

        public DropDownListItem(int icon, String description, Runnable action) {
            super();
            this.icon = icon;
            this.description = description;
            this.action = action;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
