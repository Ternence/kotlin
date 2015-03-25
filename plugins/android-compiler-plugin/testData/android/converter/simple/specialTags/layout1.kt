package kotlinx.android.synthetic.layout.view

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

val View.includeTag: ft<View, View?>
    get() = findViewById(0) : View

val View.fragmentTag: ft<View, View?>
    get() = findViewById(0) : View

val View.`fun`: ft<TextView, TextView?>
    get() = findViewById(0) as TextView

