package com.kenzglobe.debugmate;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import java.util.List;

public class CodingKeyboardService extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private CustomKeyboardView kv;
    private Keyboard keyboardQwerty;
    private Keyboard keyboardSymbols;
    
    private boolean isCaps = false;
    private boolean isCapsLock = false;
    private long lastShiftPressTime = 0;

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

    // Helper to change the Caps key icon dynamically
    private void updateShiftIcon(int resId) {
        if (keyboardQwerty == null) return;
        List<Keyboard.Key> keys = keyboardQwerty.getKeys();
        for (Keyboard.Key key : keys) {
            if (key.codes[0] == Keyboard.KEYCODE_SHIFT) {
                // THE FIX: Convert the integer ID into a Drawable object
                key.icon = getResources().getDrawable(resId);
                key.label = null;
                kv.invalidateAllKeys();
                break;
            }
        }
    }

    @Override
    public void onPress(int primaryCode) {}

    @Override
    public void onRelease(int primaryCode) {}

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
                
            case Keyboard.KEYCODE_SHIFT:
                long currentTime = System.currentTimeMillis();
                
                // Check for double tap (within 300ms)
                if (currentTime - lastShiftPressTime < 300) {
                    isCapsLock = !isCapsLock; // Toggle Caps Lock
                    isCaps = isCapsLock;
                } else {
                    // Single tap
                    isCaps = !isCaps;
                    if (!isCaps) {
                        isCapsLock = false; // Turn off caps lock if manually going back to lowercase
                    }
                }
                lastShiftPressTime = currentTime;

                // Update Icons based on state
                if (isCapsLock) {
                    updateShiftIcon(R.drawable.ic_caps_locked);
                } else if (isCaps) {
                    updateShiftIcon(R.drawable.ic_caps_solid);
                } else {
                    updateShiftIcon(R.drawable.ic_caps_regular);
                }

                keyboardQwerty.setShifted(isCaps);
                kv.invalidateAllKeys();
                break;
                
            case Keyboard.KEYCODE_DONE:
            case 10: // Enter
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
                
            case 9: // Tab
                ic.commitText("    ", 1);
                break;

            // --- SWITCHING KEYBOARDS ---
            case -102: // Switch to Symbols
                kv.setKeyboard(keyboardSymbols);
                break;
            case -103: // Switch to ABC
                kv.setKeyboard(keyboardQwerty);
                break;
                
            // --- ARROW KEYS ---
            case -100: ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT)); break;
            case -101: ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT)); break;
            case -104: ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP)); break;
            case -105: ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN)); break;
                
            // --- AUTO PAIRING ---
            case 40: // (
                ic.commitText("()", 1);
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                break;
            case 123: // {
                ic.commitText("{}", 1);
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                break;
            case 34: // "
                ic.commitText("\"\"", 1);
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                break;
            case 39: // '
                ic.commitText("''", 1);
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                break;
            case 91: // [
                ic.commitText("[]", 1);
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
                break;

            default:
                char code = (char) primaryCode;
                
                // If it's a letter and caps is on
                if (Character.isLetter(code) && isCaps) {
                    code = Character.toUpperCase(code);
                    
                    // If NOT caps lock, turn off shift after one letter
                    if (!isCapsLock) {
                        isCaps = false;
                        keyboardQwerty.setShifted(false);
                        updateShiftIcon(R.drawable.ic_caps_regular);
                    }
                }
                ic.commitText(String.valueOf(code), 1);
        }
    }

    @Override
    public void onText(CharSequence text) {}

    @Override
    public void swipeLeft() {}

    @Override
    public void swipeRight() {}

    @Override
    public void swipeDown() {}

    @Override
    public void swipeUp() {}
}