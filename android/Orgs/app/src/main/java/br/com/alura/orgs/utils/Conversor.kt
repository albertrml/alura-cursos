package br.com.alura.orgs.utils

import java.text.NumberFormat
import java.util.Locale

fun currencyFormat(value: Double, locale: Locale = Locale.US): String {
    val currencyFormatter = NumberFormat.getCurrencyInstance(locale)
    return currencyFormatter.format(value)
}