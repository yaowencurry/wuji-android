package com.personal.biji.android.editor

import com.google.gson.Gson
import com.personal.biji.android.domain.RichTextDocument

interface RichTextCodec {
    fun encode(document: RichTextDocument): String
    fun decode(json: String): RichTextDocument
}

class GsonRichTextCodec(private val gson: Gson = Gson()) : RichTextCodec {
    override fun encode(document: RichTextDocument): String = gson.toJson(document)
    override fun decode(json: String): RichTextDocument = gson.fromJson(json, RichTextDocument::class.java)
}
