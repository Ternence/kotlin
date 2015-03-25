package kotlinx.android.synthetic.layout

import android.app.*
import android.view.*
import android.widget.*
import android.webkit.*
import android.inputmethodservice.*
import android.opengl.*
import android.appwidget.*
import android.support.v4.app.*
import android.support.v4.view.*
import android.support.v4.widget.*
import kotlin.internal.flexible.ft

val Activity.includeTag: ft<View, View?>
    get() = findViewById(0) : View

val Fragment.includeTag: ft<View, View?>
    get() = getView().findViewById(0) : View

val Activity.fragmentTag: ft<View, View?>
    get() = findViewById(0) : View

val Fragment.fragmentTag: ft<View, View?>
    get() = getView().findViewById(0) : View

