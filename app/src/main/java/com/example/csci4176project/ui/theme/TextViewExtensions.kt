package com.example.csci4176project.ui.theme

import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView

fun TextView.setClickableSpan(fullText: String, clickableText: String, onClickAction: () -> Unit) {
    val spannableString = SpannableString(fullText)
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClickAction()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = true
            ds.color = ds.linkColor // Or set a custom color
        }
    }

    val clickableStartIndex = fullText.indexOf(clickableText)
    val clickableEndIndex = clickableStartIndex + clickableText.length
    spannableString.setSpan(clickableSpan, clickableStartIndex, clickableEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

    this.text = spannableString
    this.movementMethod = LinkMovementMethod.getInstance()
}
