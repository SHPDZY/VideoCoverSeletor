package com.zy.videocoverselector.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Arrays;

/**
 * 屏幕工具类
 */
public class ScreenUtils {
    private static Context context;
    private static float density;
    private static float scaledDensity;

    public static void init(Context context) {
        ScreenUtils.context = context.getApplicationContext();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        density = displayMetrics.density;
        scaledDensity = displayMetrics.scaledDensity;
    }

    public static void setDensity(float density) {
        ScreenUtils.density = density;
    }

    public static void setScaledDensity(float scaledDensity) {
        ScreenUtils.scaledDensity = scaledDensity;
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     */
    public static int px2dip(float pxValue) {
        return (int) (pxValue / density + 0.5f);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */
    public static int dip2px(float dipValue) {
        return (int) (dipValue * density + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    public static int px2sp(float pxValue) {
        return (int) (pxValue / scaledDensity + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public static int sp2px(float spValue) {
        return (int) (spValue * scaledDensity + 0.5f);
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return display.getWidth();
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return display.getHeight();
    }

    /**
     * To hide the softkeyboard
     */
    public static void hideSoftKeyboard(Window window) {
        if (window == null) return;
        View view = window.getCurrentFocus();
        if (view != null) {
            IBinder iBinder = view.getWindowToken();
            if (iBinder != null) {
                ((InputMethodManager) window.getContext().getApplicationContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(iBinder, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * To show the softkeyboard
     */
    public static void showSoftKeyboard(final EditText editText) {
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                InputMethodManager inputManager = (InputMethodManager) editText.getContext().getApplicationContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText, 0);
            }
        }, 50);
    }

    /**
     * 添加键盘状态监听
     */
    public static void addKeyboardListener(Window window, KeyboardListener listener) {
        if (keyboardListeners == null) keyboardListeners = new ArrayMap<>(1);
        window = findActivityWindow(window);//如果是dialog的window就只有adjustResize模式才能正确获取键盘高度，所以尽量使用activity的window
        KeyboardLayoutListener layoutListener = keyboardListeners.get(window);
        if (layoutListener == null) {
            layoutListener = new KeyboardLayoutListener(window, new KeyboardListener[]{listener});
            keyboardListeners.put(window, layoutListener);
            window.getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        } else {
            layoutListener.listeners = Arrays.copyOf(layoutListener.listeners, layoutListener.listeners.length + 1);
            layoutListener.listeners[layoutListener.listeners.length - 1] = listener;
        }
        keyboardListeners.put(window, layoutListener);
    }

    public static void removeKeyboardListener(Window window) {
        if (keyboardListeners != null) {
            window = findActivityWindow(window);
            KeyboardLayoutListener layoutListener = keyboardListeners.remove(window);
            if (layoutListener != null) {
                window.getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(layoutListener);
                layoutListener.window = null;
                layoutListener.listeners = null;
            }
            if (keyboardListeners.isEmpty()) keyboardListeners = null;
        }
    }

    private static Window findActivityWindow(Window window) {
        Context context = window.getContext();
        while (!(context instanceof Activity)) {
            if (context instanceof ContextWrapper)
                context = ((ContextWrapper) context).getBaseContext();
            else return window;
        }
        return ((Activity) context).getWindow();
    }

    private static ArrayMap<Window, KeyboardLayoutListener> keyboardListeners;
    private static int lastKeyboardHeight;

    public static int getKeyboardHeight() {
        return lastKeyboardHeight;
    }

    private static class KeyboardLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        private Window window;
        private KeyboardListener[] listeners;

        private Rect rect = new Rect();
        private int largeBottom = 0;
        private boolean isKeyboardShow;
        private boolean isFirstCall = true;

        KeyboardLayoutListener(Window window, KeyboardListener[] listeners) {
            this.window = window;
            this.listeners = listeners;
            window.getDecorView().getWindowVisibleDisplayFrame(rect);
            largeBottom = rect.bottom;
        }

        @Override
        public void onGlobalLayout() {
            if (window == null || listeners == null) return;
            window.getDecorView().getWindowVisibleDisplayFrame(rect);
//            Log.e("123asd", rect.toString() + " " + window.getDecorView().getHeight() + " " + window.findViewById(Window.ID_ANDROID_CONTENT).getHeight() +" "+largeBottom);
            if (ScreenUtils.getScreenHeight() - rect.bottom < ScreenUtils.dip2px(100)) {
                largeBottom = rect.bottom;
                if (isFirstCall || isKeyboardShow) {
                    isKeyboardShow = false;
                    int length = listeners.length;
                    for (int i = 0; i < length; ++i) {
                        if (listeners == null) break;
                        if (listeners[i] != null) listeners[i].onKeyboardHide();
                    }
                }
            } else if (largeBottom > 0) {
                int keyboardHeight = Math.abs(rect.bottom - largeBottom);
                if (isFirstCall || !isKeyboardShow || lastKeyboardHeight != keyboardHeight) {
                    isKeyboardShow = true;
                    int length = listeners.length;
                    for (int i = 0; i < length; ++i) {
                        if (listeners == null) break;
                        if (listeners[i] != null) listeners[i].onKeyboardShow(keyboardHeight);
                    }
                }
                lastKeyboardHeight = keyboardHeight;
            }
            isFirstCall = false;
        }
    }

    public interface KeyboardListener {
        void onKeyboardShow(int keyboardHeight);

        void onKeyboardHide();
    }
}
