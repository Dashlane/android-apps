package com.dashlane.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TableLayout;
import android.widget.TextView;

import com.dashlane.R;



public class PinCodeKeyboardView extends TableLayout implements View.OnClickListener {

    private PinCodeKeyboardListener mListener;
    private static final int[] sPincodeButtonsViewIds =
            {R.id.pincode_keyboard_0, R.id.pincode_keyboard_1, R.id.pincode_keyboard_2,
             R.id.pincode_keyboard_3, R.id.pincode_keyboard_4, R.id.pincode_keyboard_5,
             R.id.pincode_keyboard_6, R.id.pincode_keyboard_7, R.id.pincode_keyboard_8,
             R.id.pincode_keyboard_9};

    public PinCodeKeyboardView(Context context) {
        super(context);
        init();
    }

    public PinCodeKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void onClick(View view) {
        if (mListener == null) {
            return;
        }
        int id = view.getId();
        if (id == R.id.pincode_keyboard_0) {
            mListener.onClickNumber(0);
        } else if (id == R.id.pincode_keyboard_1) {
            mListener.onClickNumber(1);
        } else if (id == R.id.pincode_keyboard_2) {
            mListener.onClickNumber(2);
        } else if (id == R.id.pincode_keyboard_3) {
            mListener.onClickNumber(3);
        } else if (id == R.id.pincode_keyboard_4) {
            mListener.onClickNumber(4);
        } else if (id == R.id.pincode_keyboard_5) {
            mListener.onClickNumber(5);
        } else if (id == R.id.pincode_keyboard_6) {
            mListener.onClickNumber(6);
        } else if (id == R.id.pincode_keyboard_7) {
            mListener.onClickNumber(7);
        } else if (id == R.id.pincode_keyboard_8) {
            mListener.onClickNumber(8);
        } else if (id == R.id.pincode_keyboard_9) {
            mListener.onClickNumber(9);
        } else if (id == R.id.pincode_keyboard_clear) {
            mListener.onClickEraseLastNumber();
        }
    }

    public void setListener(PinCodeKeyboardListener listener) {
        mListener = listener;
    }

    public void setEnableButtons(boolean enabled) {
        setEnableViewAndSubViews(this, enabled);
    }

    private void init() {
        inflate(getContext(), R.layout.include_fragment_lock_pincode_keyboard, this);

        for (int i = 0; i < sPincodeButtonsViewIds.length; i++) {
            setPinCodeButton(sPincodeButtonsViewIds[i], Integer.toString(i));
        }
        findViewById(R.id.pincode_keyboard_clear).setOnClickListener(this);
    }

    private void setEnableViewAndSubViews(View view, boolean enabled) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setEnableViewAndSubViews(viewGroup.getChildAt(i), enabled);
            }
        } else {
            view.setEnabled(enabled);
        }
    }

    private TextView setPinCodeButton(int viewId, String text) {
        TextView btn = findViewById(viewId);
        btn.setOnClickListener(this);
        btn.setText(text);
        return btn;
    }

    public interface PinCodeKeyboardListener {
        void onClickNumber(int value);

        void onClickEraseLastNumber();
    }

    public static void setupSoftKeyboard(final View view, PinCodeKeyboardListener listener) {
        view.setOnClickListener(new ShowKeyboardOnClick());
        view.setOnKeyListener(new KeyForward(listener));
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
    }

    public static void showSoftKeyboardIfNeeded(final View view) {
        if (!view.isClickable()) {
            return;
        }
        view.postDelayed(() -> {
            
            view.performClick();
        }, 500);
    }

    private static class ShowKeyboardOnClick implements OnClickListener {

        @Override
        public void onClick(View view) {
            InputMethodManager im =
                    (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            im.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }

    private static class KeyForward implements OnKeyListener {
        private final PinCodeKeyboardListener mListener;

        public KeyForward(PinCodeKeyboardListener listener) {
            mListener = listener;
        }

        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (mListener == null || keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                mListener.onClickEraseLastNumber();
                return true;
            }
            int unicodeChar = keyEvent.getUnicodeChar();
            if (unicodeChar < '0' || unicodeChar > '9') {
                return false;
            }
            mListener.onClickNumber(unicodeChar - '0');
            return true;
        }
    }
}
