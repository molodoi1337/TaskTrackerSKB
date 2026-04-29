package com.skb.tasktracker.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val displayFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
private val displayFormatWithTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))

fun Long?.formatDate(): String = this?.let { displayFormat.format(Date(it)) } ?: "—"
fun Long?.formatDateTime(): String = this?.let { displayFormatWithTime.format(Date(it)) } ?: "—"
