package com.crazylegend.kotlinextensions.views

import android.content.Context
import android.graphics.PorterDuff
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.crazylegend.kotlinextensions.context.getCompatDrawable
import com.google.android.material.textfield.TextInputEditText


/**
 * Created by hristijan on 3/4/19 to long live and prosper !
 */

val TextInputEditText.getString: String get() = this.text.toString()
val TextInputEditText.getStringTrimmed: String get() = this.text.toString().trim()

fun TextInputEditText.setTheText(text: String) {
    this.setText(text, TextView.BufferType.EDITABLE)
}


val EditText.getString: String get() = this.text.toString()
val EditText.getStringTrimmed: String get() = this.text.toString().trim()


fun EditText.setTheText(text: String) {
    this.setText(text, TextView.BufferType.EDITABLE)
}

/**
 * Accepts 3 text watcher methods with a default empty implementation.
 *
 * @return The `TextWatcher` being added to EditText
 */
fun EditText.addTextWatcher(afterTextChanged: (text: Editable?) -> Unit = { _ -> },
                            beforeTextChanged: (text: CharSequence?, start: Int, count: Int, after: Int) -> Unit = { _, _, _, _ -> },
                            onTextChanged: (text: CharSequence?, start: Int, before: Int, count: Int) -> Unit = { _, _, _, _ -> }): TextWatcher {

    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged(s)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            beforeTextChanged(s, start, count, after)
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged(s, start, before, count)
        }
    }

    addTextChangedListener(textWatcher)
    return textWatcher
}


fun EditText.focus() {
    if (hasFocus()) {
        setSelection(text.length)
    }
}

fun EditText.afterTextChanged(afterTextChanged: (chars: Editable?) -> Unit = {_->}) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged(s)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    })
}

fun EditText.beforeTextChanged(beforeTextChanged: (chars: CharSequence?, start: Int, count: Int, after: Int) -> Unit  = {_,_,_,_->}) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            beforeTextChanged(s, start, count, after)
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    })
}

fun EditText.onTextChanged(onTextChanged: (chars: CharSequence?, start: Int, count: Int, after: Int) -> Unit  = {_,_,_,_->}) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged(s, start, before, count)
        }

    })
}


fun EditText.requestFocusAndKeyboard() {
    requestFocus()
    val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE)
            as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun EditText.clearFocusAndKeyboard() {
    clearFocus()
    val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE)
            as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Resets the cursor drawable to the default.
 */
fun EditText.resetCursorColor() {
    try {
        val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
        f.isAccessible = true
        f.set(this, 0)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

/**
 * Listens for either the enter key to be pressed or the soft keyboard's editor action to activate.
 */
inline fun EditText.onImeAction(crossinline action: (text: String) -> Unit) {
    setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
        if ((event?.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            action(text.toString())
            return@OnKeyListener true
        }
        false
    })
    setOnEditorActionListener { v, actionId, event ->
        action(text.toString())
        true
    }
}

/**
 * Both sets the [EditText.setImeOptions] to "Done" and listens for the IME action.
 */
inline fun EditText.onDone(crossinline action: (text: String) -> Unit) {
    imeOptions = EditorInfo.IME_ACTION_DONE
    onImeAction {
        hideSoftInput()
        action(text.toString())
    }
}

/**
 * Both sets the [EditText.setImeOptions] to "Send" and listens for the IME action.
 */
inline fun EditText.onSend(crossinline action: (text: String) -> Unit) {
    imeOptions = EditorInfo.IME_ACTION_SEND
    onImeAction {
        hideSoftInput()
        action(text.toString())
    }
}

/**
 * Sets the cursor color of the edit text.
 */
fun EditText.setCursorColor(color: Int) {
    try {
        val fCursorDrawableRes = TextView::class.java.getDeclaredField("mCursorDrawableRes")
        fCursorDrawableRes.setAccessible(true)
        val mCursorDrawableRes = fCursorDrawableRes.getInt(this)
        val fEditor = TextView::class.java.getDeclaredField("mEditor")
        fEditor.isAccessible = true
        val editor = fEditor.get(this)
        val clazz = editor.javaClass
        val fCursorDrawable = clazz.getDeclaredField("mCursorDrawable")
        fCursorDrawable.isAccessible = true
        val drawables = arrayOf(
                context.getCompatDrawable(mCursorDrawableRes)?.mutate().apply { this?.setColorFilter(color, PorterDuff.Mode.SRC_IN) },
                context.getCompatDrawable(mCursorDrawableRes)?.mutate().apply { this?.setColorFilter(color, PorterDuff.Mode.SRC_IN) }
        )
        fCursorDrawable.set(editor, drawables)
    } catch (ignored: Throwable) {
    }
}