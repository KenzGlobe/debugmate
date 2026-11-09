package com.kenzglobe.debugmate;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import java.util.List;

public class CodingKeyboardService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private static final int KEY_COPY = -106;
    private static final int KEY_CUT = -107;
    private static final int KEY_PASTE = -108;
    private static final int KEY_CTRL = -109;
    private static final int KEY_SELECT_ALL = -110;
    private static final int KEY_SETTINGS = -111;
    private static final int KEY_CLIPBOARD = -112;

    private CustomKeyboardView kv;
    private Keyboard keyboardQwerty;
    private Keyboard keyboardSymbols;

    private boolean isCaps = false;
    private boolean isCapsLock = false;
    private boolean isCtrl = false;
    private long lastShiftPressTime = 0;

    // --- For Delete Repeat ---
    private Handler deleteHandler = new Handler(Looper.getMainLooper());
    private Runnable deleteRunnable;
    private boolean isDeleting = false;

    @Override
    public View onCreateInputView() {
        View view = getLayoutInflater().inflate(R.layout.keyboard, null);
        kv = view.findViewById(R.id.keyboard_view);
        keyboardQwerty = new Keyboard(this, R.xml.qwerty_coding);
        keyboardSymbols = new Keyboard(this, R.xml.symbols_coding);

        kv.setKeyboard(keyboardQwerty);
        kv.setOnKeyboardActionListener(this);
        return view;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (kv != null) {
            kv.setKeyboard(keyboardQwerty);
            isCtrl = false;
            isCaps = false; // <--- FIX: Changed isShift to isCaps
            updateCtrlLabel();
            updateShiftIcon(R.drawable.ic_caps_regular);
        }
    }

    private void updateShiftIcon(int resId) {
        if (keyboardQwerty == null || kv == null) return;
        List<Keyboard.Key> keys = keyboardQwerty.getKeys();
        for (Keyboard.Key key : keys) {
            if (key.codes[0] == Keyboard.KEYCODE_SHIFT) {
                key.icon = getResources().getDrawable(resId);
                key.label = null;
                kv.invalidateAllKeys();
                break;
            }
        }
    }

    private void updateCtrlLabel() {
        if (keyboardQwerty == null || kv == null) return;
        List<Keyboard.Key> keys = keyboardQwerty.getKeys();
        for (Keyboard.Key key : keys) {
            if (key.codes[0] == KEY_CTRL) {
                key.label = isCtrl ? "•" : "CTRL";
                kv.invalidateAllKeys();
                break;
            }
        }
    }

    // Arrow keys now respect Shift for selection
    private void sendArrowKey(InputConnection ic, int keyCode) {
        int metaState = isCaps ? KeyEvent.META_SHIFT_ON : 0;
        ic.sendKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyCode, 0, metaState));
        ic.sendKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP, keyCode, 0, metaState));
    }

    private void sendCtrlLetter(InputConnection ic, char letter) {
        char lower = Character.toLowerCase(letter);
        if (lower < 'a' || lower > 'z') {
            isCtrl = false;
            updateCtrlLabel();
            return;
        }
        int keyCode = KeyEvent.KEYCODE_A + (lower - 'a');
        int metaState = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
        ic.sendKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyCode, 0, metaState));
        ic.sendKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP, keyCode, 0, metaState));
        
        isCtrl = false;
        updateCtrlLabel();
    }

    // --- Handle Key Presses (Repeat Logic) ---
    @Override
    public void onPress(int primaryCode) {
        // Check if it's Delete or an Arrow key
        if (primaryCode == Keyboard.KEYCODE_DELETE || 
            primaryCode == -100 || primaryCode == -101 || // Left / Right
            primaryCode == -104 || primaryCode == -105) {  // Up / Down
            
            isDeleting = true; // We'll reuse this variable for any repeating key
            final int codeToRepeat = primaryCode;
            
            deleteRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isDeleting) {
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            if (codeToRepeat == Keyboard.KEYCODE_DELETE) {
                                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                            } else {
                                // It's an arrow key
                                if (codeToRepeat == -100) sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_LEFT);
                                else if (codeToRepeat == -101) sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_RIGHT);
                                else if (codeToRepeat == -104) sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_UP);
                                else if (codeToRepeat == -105) sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_DOWN);
                            }
                        }
                        // Repeat every 50ms
                        deleteHandler.postDelayed(this, 50);
                    }
                }
            };
            // Start repeating after a 400ms delay
            deleteHandler.postDelayed(deleteRunnable, 400);
        }
    }

    @Override
    public void onRelease(int primaryCode) {
        if (primaryCode == Keyboard.KEYCODE_DELETE || 
            primaryCode == -100 || primaryCode == -101 || 
            primaryCode == -104 || primaryCode == -105) {
            
            isDeleting = false;
            if (deleteRunnable != null) {
                deleteHandler.removeCallbacks(deleteRunnable);
            }
        }
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                // Send KEYCODE_DEL so it deletes selected text if there is any
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                return;

            case Keyboard.KEYCODE_SHIFT:
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShiftPressTime < 300) {
                    isCapsLock = !isCapsLock;
                    isCaps = isCapsLock;
                } else {
                    isCaps = !isCaps;
                    if (!isCaps) isCapsLock = false;
                }
                lastShiftPressTime = currentTime;
                if (isCapsLock) {
                    updateShiftIcon(R.drawable.ic_caps_locked);
                } else if (isCaps) {
                    updateShiftIcon(R.drawable.ic_caps_solid);
                } else {
                    updateShiftIcon(R.drawable.ic_caps_regular);
                }
                keyboardQwerty.setShifted(isCaps);
                kv.invalidateAllKeys();
                return;

            case Keyboard.KEYCODE_DONE:
            case 10:
                sendArrowKey(ic, KeyEvent.KEYCODE_ENTER);
                return;

            case 9:
                ic.commitText("    ", 1);
                return;

            case KEY_COPY:
                ic.performContextMenuAction(android.R.id.copy);
                return;

            case KEY_CUT:
                ic.performContextMenuAction(android.R.id.cut);
                return;

            case KEY_PASTE:
            case KEY_CLIPBOARD:
                ic.performContextMenuAction(android.R.id.paste);
                return;

            case KEY_CTRL:
                isCtrl = !isCtrl;
                updateCtrlLabel();
                return;

            case KEY_SELECT_ALL:
                ic.performContextMenuAction(android.R.id.selectAll);
                return;

            case KEY_SETTINGS:
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showInputMethodPicker();
                return;

            case -102: // SYMBOLS
                isCtrl = false;
                isCaps = false;
                updateCtrlLabel();
                updateShiftIcon(R.drawable.ic_caps_regular);
                kv.setKeyboard(keyboardSymbols);
                return;

            case -103: // ABC
                isCtrl = false;
                isCaps = false;
                updateCtrlLabel();
                updateShiftIcon(R.drawable.ic_caps_regular);
                kv.setKeyboard(keyboardQwerty);
                return;

            case -100: sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_LEFT); return;
            case -101: sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_RIGHT); return;
            case -104: sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_UP); return;
            case -105: sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_DOWN); return;

            case 40: ic.commitText("()", 1); sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_LEFT); return;
            case 123: ic.commitText("{}", 1); sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_LEFT); return;
            case 34: ic.commitText("\"\"", 1); sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_LEFT); return;
            case 39: ic.commitText("''", 1); sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_LEFT); return;
            case 91: ic.commitText("[]", 1); sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_LEFT); return;

            default:
                if (primaryCode > 0) {
                    char code = (char) primaryCode;
                    if (Character.isLetter(code) && isCaps) {
                        code = Character.toUpperCase(code);
                    }
                    if (isCtrl && Character.isLetter(code)) {
                        sendCtrlLetter(ic, code);
                        return;
                    }
                    ic.commitText(String.valueOf(code), 1);
                    if (Character.isLetter(code) && isCaps && !isCapsLock) {
                        isCaps = false;
                        keyboardQwerty.setShifted(false);
                        updateShiftIcon(R.drawable.ic_caps_regular);
                    }
                }
                break;
        }
    }

    @Override public void onText(CharSequence text) {}
    @Override public void swipeLeft() {}
    @Override public void swipeRight() {}
    @Override public void swipeDown() {}
    @Override public void swipeUp() {}
}
