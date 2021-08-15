package cz.skup5.e2shows.utils

import android.content.Context
import android.widget.Toast

fun toast(context: Context, resourceId: Int, duration: Int) {
    Toast.makeText(context, resourceId, duration).show()
}

fun toast(context: Context, msg: String?, duration: Int) {
    Toast.makeText(context, msg, duration).show()
}