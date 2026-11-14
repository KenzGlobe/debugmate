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
import java.util.HashMap;
import java.util.List;

public class CodingKeyboardService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private static final int KEY_MENU  = -113;
    private static final int KEY_UNDO  = -114;
    private static final int KEY_REDO  = -115;
    private static final int KEY_EMOJI = -116;
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
    private Keyboard keyboardMenu;
    private Keyboard keyboardEmoji;

    private boolean isCaps = false;
    private boolean isCapsLock = false;
    private boolean isCtrl = false;
    private long lastShiftPressTime = 0;

    // --- For Delete Repeat ---
    private Handler deleteHandler = new Handler(Looper.getMainLooper());
    private Runnable deleteRunnable;
    private boolean isDeleting = false;

    // --- For Long Press Mappings ---
    private HashMap<Character, Character> longPressMap = new HashMap<>();
    private Handler longPressHandler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable;
    private boolean isLongPressed = false;

    @Override
    public View onCreateInputView() {
        View view = getLayoutInflater().inflate(R.layout.keyboard, null);
        kv = view.findViewById(R.id.keyboard_view);
        keyboardQwerty = new Keyboard(this, R.xml.qwerty_coding);
        keyboardSymbols = new Keyboard(this, R.xml.symbols_coding);
        keyboardMenu  = new Keyboard(this, R.xml.menu_coding);
        keyboardEmoji = new Keyboard(this, R.xml.emoji_coding);

        // Populate the long press mappings
        longPressMap.put('q', '%');
        longPressMap.put('w', '\\');
        longPressMap.put('e', '|');
        longPressMap.put('r', '=');
        longPressMap.put('t', '[');
        longPressMap.put('y', ']');
        longPressMap.put('u', '<');
        longPressMap.put('i', '>');
        longPressMap.put('o', '{');
        longPressMap.put('p', '}');
        longPressMap.put('a', '@');
        longPressMap.put('s', '#');
        longPressMap.put('d', '$');
        longPressMap.put('f', '_');
        longPressMap.put('g', '&');
        longPressMap.put('h', '-');
        longPressMap.put('j', '+');
        longPressMap.put('k', '(');
        longPressMap.put('l', ')');
        longPressMap.put('z', '*');
        longPressMap.put('x', '"');
        longPressMap.put('c', '\'');
        longPressMap.put('v', ':');
        longPressMap.put('b', ';');
        longPressMap.put('n', '!');
        longPressMap.put('m', '?');

        // Pass the map to the view so it can draw the superscripts
        kv.setLongPressMap(longPressMap);

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
            isCaps = false;
            updateCtrlLabel();
            updateShiftIcon(R.drawable.ic_caps_regular);
        }
    }

    private void updateShiftIcon(int resId) {
        if (keyboardQwerty == null || kv == null) return;
        List<Keyboard.Key> keys = keyboardQwerty.getKeys();
        if (keys == null || keys.isEmpty()) return;
        for (Keyboard.Key key : keys) {
            if (key.codes != null && key.codes.length > 0 && key.codes[0] == Keyboard.KEYCODE_SHIFT) {
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
        if (keys == null || keys.isEmpty()) return;
        for (Keyboard.Key key : keys) {
            if (key.codes != null && key.codes.length > 0 && key.codes[0] == KEY_CTRL) {
                key.label = isCtrl ? "•" : "CTRL";
                kv.invalidateAllKeys();
                break;
            }
        }
    }

    private void sendArrowKey(InputConnection ic, int keyCode) {
        int metaState = isCaps ? KeyEvent.META_SHIFT_ON : 0;
        long now = android.os.SystemClock.uptimeMillis(); // FIX: Use real timestamp
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaState));
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, metaState));
    }

    private void sendCtrlLetter(InputConnection ic, char letter) {
        char lower = Character.toLowerCase(letter);
        String action = null;

        // Map custom letters to their specific DebugMate actions
        switch (lower) {
            case 'f': action = "com.kenzglobe.debugmate.ACTION_FIND"; break;
            case 'h': action = "com.kenzglobe.debugmate.ACTION_REPLACE"; break;
            case 's': action = "com.kenzglobe.debugmate.ACTION_SAVE"; break;
            case 'g': action = "com.kenzglobe.debugmate.ACTION_GOTO"; break;
            case 'i': action = "com.kenzglobe.debugmate.ACTION_FORMAT"; break;
            
            // New mappings
            case 'b': action = "com.kenzglobe.debugmate.ACTION_TOGGLE_SIDEBAR"; break;
            case 'e': action = "com.kenzglobe.debugmate.ACTION_RUN"; break;
            case 'j': action = "com.kenzglobe.debugmate.ACTION_JOIN_LINES"; break;
            case 'l': action = "com.kenzglobe.debugmate.ACTION_EXPAND_LINE"; break;
            case 'm': action = "com.kenzglobe.debugmate.ACTION_FOLD"; break;
            case 'n': action = "com.kenzglobe.debugmate.ACTION_NEW_FILE"; break;
            case 'o': action = "com.kenzglobe.debugmate.ACTION_OPEN_FILE"; break;
            case 'q': action = "com.kenzglobe.debugmate.ACTION_TOGGLE_COMMENT"; break;
            case 'r': action = "com.kenzglobe.debugmate.ACTION_RELOAD"; break;
            case 't': action = "com.kenzglobe.debugmate.ACTION_DUPLICATE"; break;
            case 'u': action = "com.kenzglobe.debugmate.ACTION_UPPERCASE"; break;
            case 'w': action = "com.kenzglobe.debugmate.ACTION_SELECT_WORD"; break;
        }

        // If we mapped it, send the broadcast and exit
        if (action != null) {
            sendBroadcast(new android.content.Intent(action));
            isCtrl = false;
            updateCtrlLabel();
            return;
        }
        
        if (lower < 'a' || lower > 'z') {
            isCtrl = false;
            updateCtrlLabel();
            return;
        }
        int keyCode = KeyEvent.KEYCODE_A + (lower - 'a');
        int metaState = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
        long now = android.os.SystemClock.uptimeMillis(); // FIX: Use real timestamp
        
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaState));
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, metaState));
        
        isCtrl = false;
        updateCtrlLabel();
    }
    
    private void sendCtrlKey(InputConnection ic, int keyCode) {
        int metaState = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
        long now = android.os.SystemClock.uptimeMillis(); // FIX: Use real timestamp
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaState));
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, metaState));
    }

    @Override
    public void onPress(int primaryCode) {
        // --- DELETE/ARROW REPEAT LOGIC ---
        if (primaryCode == Keyboard.KEYCODE_DELETE || 
            primaryCode == -100 || primaryCode == -101 || 
            primaryCode == -104 || primaryCode == -105) {
            
            isDeleting = true; 
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
                                if (codeToRepeat == -100) sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_LEFT);
                                else if (codeToRepeat == -101) sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_RIGHT);
                                else if (codeToRepeat == -104) sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_UP);
                                else if (codeToRepeat == -105) sendArrowKey(ic, KeyEvent.KEYCODE_DPAD_DOWN);
                            }
                        }
                        deleteHandler.postDelayed(this, 50);
                    }
                }
            };
            deleteHandler.postDelayed(deleteRunnable, 400);
        }

        // --- LONG PRESS SYMBOL LOGIC ---
        isLongPressed = false; // Reset flag on every new press
        if (primaryCode > 0 && Character.isLetter((char) primaryCode)) {
            final char primaryChar = Character.toLowerCase((char) primaryCode);
            
            if (longPressMap.containsKey(primaryChar)) {
                longPressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        isLongPressed = true; // Flag that we inserted a symbol
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            char symbol = longPressMap.get(primaryChar);
                            ic.commitText(String.valueOf(symbol), 1);
                        }
                    }
                };
                // Trigger long press after 200ms
                longPressHandler.postDelayed(longPressRunnable, 200);
            }
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
        
        // Cancel long press if the user releases before the timer
        if (longPressRunnable != null) {
            longPressHandler.removeCallbacks(longPressRunnable);
        }
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        // If a long press just occurred, ignore this key up event
        if (isLongPressed) {
            isLongPressed = false;
            return;
        }

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                // Send KEYCODE_DEL so it deletes selected text if there is any
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                return;
                
            case KEY_UNDO:
                sendCtrlKey(ic, KeyEvent.KEYCODE_Z);
                return;
            
            case KEY_REDO:
                sendCtrlKey(ic, KeyEvent.KEYCODE_Y);
                return;
            
            case KEY_MENU:
                isCtrl = false;
                isCaps = false;
                keyboardQwerty.setShifted(false);
                updateCtrlLabel();
                updateShiftIcon(R.drawable.ic_caps_regular);
                kv.setKeyboard(keyboardMenu);
                return;
            
            case KEY_EMOJI:
                isCtrl = false;
                isCaps = false;
                keyboardQwerty.setShifted(false);
                updateCtrlLabel();
                updateShiftIcon(R.drawable.ic_caps_regular);
                kv.setKeyboard(keyboardEmoji);
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
                    if (primaryCode > 0xFFFF) {
                        ic.commitText(new String(Character.toChars(primaryCode)), 1);
                        return;
                    }
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
