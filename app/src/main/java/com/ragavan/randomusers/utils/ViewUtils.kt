package com.ragavan.randomusers.utils

import android.view.View
import android.widget.ProgressBar
import com.google.android.material.snackbar.Snackbar

fun View.snackBar(message:String){
    Snackbar.make(this,message,Snackbar.LENGTH_LONG).show()
}

fun ProgressBar.show(){
    visibility = View.VISIBLE
}
fun ProgressBar.hide(){
    visibility = View.GONE
}