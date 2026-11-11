package com.example.mykeyboard

class Style(
    val name: String,
    private val sampleText: String,
    val local: Boolean,              // يصلح للكتابة المباشرة حرفاً بحرف
    val joiner: String = "",         // زخرفة تُضاف بعد الحرف (للعربي)
    private val fn: (String) -> String,
    private val post: (String) -> String = { it }
) {
    fun apply(t: String): String = post(fn(t))
    fun applyChar(c: String): String = fn(c)
    val sample: String get() = apply(sampleText)
}

object Styles {

    private fun perCp(t: String, f: (Int) -> String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < t.length) {
            val cp = t.codePointAt(i)
            i += Character.charCount(cp)
            sb.append(f(cp))
        }
        return sb.toString()
    }

    private fun cpStr(cp: Int) = String(Character.toChars(cp))

    private fun alpha(up: Int, low: Int, dig: Int = -1): (String) -> String = { t ->
        perCp(t) { cp ->
            cpStr(
                when {
                    cp in 65..90 -> up + cp - 65
                    cp in 97..122 -> low + cp - 97
                    dig >= 0 && cp in 48..57 -> dig + cp - 48
                    else -> cp
                }
            )
        }
    }

    private const val NONJOIN = "ادذرزوأإآؤةىء"
    private const val SMALL_CAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ"
    private const val DECO = "ـﮩ٨ـ"

    private fun isArLetter(c: Char) = c in '\u0621'..'\u064A' || "پچگڤژک".indexOf(c) >= 0
    private fun joins(c: Char) =
        (c in '\u0621'..'\u064A' && NONJOIN.indexOf(c) < 0) || "پچگڤک".indexOf(c) >= 0

    private fun arPerChar(t: String, f: (Char, String) -> String): String =
        perCp(t) { cp ->
            val s = cpStr(cp)
            if (cp < 0x10000) f(cp.toChar(), s) else s
        }

    private fun wrap(l: String, r: String): (String) -> String = { t -> "$l $t $r" }

    // ---------- مساعدات إضافية ----------
    private fun upperOnly(base: Int): (String) -> String = { t ->
        perCp(t) { cp ->
            when (cp) {
                in 65..90 -> cpStr(base + cp - 65)
                in 97..122 -> cpStr(base + cp - 97)
                else -> cpStr(cp)
            }
        }
    }

    private fun doubleStruck(): (String) -> String {
        val ex = mapOf(
            'C' to 0x2102, 'H' to 0x210D, 'N' to 0x2115, 'P' to 0x2119,
            'Q' to 0x211A, 'R' to 0x211D, 'Z' to 0x2124
        )
        return { t ->
            perCp(t) { cp ->
                val ch = if (cp < 0x10000) cp.toChar() else ' '
                cpStr(
                    when {
                        ex.containsKey(ch) -> ex.getValue(ch)
                        cp in 65..90 -> 0x1D538 + cp - 65
                        cp in 97..122 -> 0x1D552 + cp - 97
                        cp in 48..57 -> 0x1D7D8 + cp - 48
                        else -> cp
                    }
                )
            }
        }
    }

    private fun combining(mark: String): (String) -> String = { t ->
        perCp(t) { cp -> if (Character.isWhitespace(cp)) cpStr(cp) else cpStr(cp) + mark }
    }

    /** تطويل بحرف/رمز بين الحروف المتصلة */
    private fun joinStyle(name: String, sample: String, j: String): Style =
        Style(name, sample, true, joiner = j,
            fn = { t -> arPerChar(t) { c, s -> if (joins(c)) s + j else s } },
            post = { t -> t.replace(Regex(Regex.escape(j) + "(?=\\s|$)"), "") })

    /** زخرفة بين الحروف: ـ رمز ـ */
    private fun deco(sym: String) = joinStyle("ـ$sym" + "ـ", "كتب", "ـ$sym" + "ـ")

    /** تشكيل بحركة واحدة أو أكثر */
    private fun tashkeel(name: String, marks: String): Style =
        Style(name, "كتابة", true,
            fn = { t -> arPerChar(t) { c, s -> if (isArLetter(c)) s + marks else s } })

    /** تشكيل مختلط (فتحة/ضمة/كسرة بالتناوب حسب الحرف) */
    private val mixedMarks = arrayOf("\u064E", "\u064F", "\u0650")

    private fun frame(name: String, l: String, r: String = l) =
        Style(name, "نص", false, fn = wrap(l, r))

    val sections: List<Pair<String, List<Style>>> = listOf(
        "🔤 إنجليزي" to listOf(
            Style("Bold", "Abc 123", true, fn = alpha(0x1D400, 0x1D41A, 0x1D7CE)),
            Style("Italic", "Abc", true, fn = { t ->
                perCp(t) { cp ->
                    cpStr(
                        when {
                            cp == 104 -> 0x210E
                            cp in 65..90 -> 0x1D434 + cp - 65
                            cp in 97..122 -> 0x1D44E + cp - 97
                            else -> cp
                        }
                    )
                }
            }),
            Style("Bold Italic", "Abc", true, fn = alpha(0x1D468, 0x1D482)),
            Style("Script", "Abc", true, fn = alpha(0x1D4D0, 0x1D4EA)),
            Style("Fraktur", "Abc", true, fn = alpha(0x1D56C, 0x1D586)),
            Style("Double", "Abc 123", true, fn = doubleStruck()),
            Style("Sans", "Abc 123", true, fn = alpha(0x1D5A0, 0x1D5BA, 0x1D7E2)),
            Style("Sans Bold", "Abc 123", true, fn = alpha(0x1D5D4, 0x1D5EE, 0x1D7EC)),
            Style("Sans Italic", "Abc", true, fn = alpha(0x1D608, 0x1D622)),
            Style("Sans B-I", "Abc", true, fn = alpha(0x1D63C, 0x1D656)),
            Style("Mono", "Abc 123", true, fn = alpha(0x1D670, 0x1D68A, 0x1D7F6)),
            Style("Wide", "Abc 123", true, fn = alpha(0xFF21, 0xFF41, 0xFF10)),
            Style("Circled", "Abc 123", true, fn = { t ->
                perCp(t) { cp ->
                    cpStr(
                        when {
                            cp in 65..90 -> 0x24B6 + cp - 65
                            cp in 97..122 -> 0x24D0 + cp - 97
                            cp in 49..57 -> 0x2460 + cp - 49
                            cp == 48 -> 0x24EA
                            else -> cp
                        }
                    )
                }
            }),
            Style("Dark Circle", "Abc", true, fn = upperOnly(0x1F150)),
            Style("Squared", "Abc", true, fn = upperOnly(0x1F130)),
            Style("Dark Square", "Abc", true, fn = upperOnly(0x1F170)),
            Style("Small Caps", "Abc", true, fn = { t ->
                perCp(t) { cp ->
                    when (cp) {
                        in 97..122 -> SMALL_CAPS[cp - 97].toString()
                        in 65..90 -> SMALL_CAPS[cp - 65].toString()
                        else -> cpStr(cp)
                    }
                }
            }),
            Style("Strike", "Abc", true, fn = combining("\u0336")),
            Style("Underline", "Abc", true, fn = combining("\u0332")),
            Style("Overline", "Abc", true, fn = combining("\u0305")),
            Style("Slash", "Abc", true, fn = combining("\u0338")),
            Style("Dotted", "Abc", true, fn = combining("\u0323")),
            Style("Wavy", "Abc", true, fn = combining("\u0303"))
        ),

        "✒ تطويل" to listOf(
            joinStyle("تطويل", "كتابة", "ـ"),
            joinStyle("تطويل ٢", "كتابة", "ــ"),
            joinStyle("تطويل ٣", "كتابة", "ـــ")
        ),

        "🌸 زخرفة بين الحروف" to listOf(
            Style("مزخرف", "كتب", true, joiner = DECO,
                fn = { t -> arPerChar(t) { c, s -> if (joins(c)) s + DECO else s } },
                post = { t -> t.replace(Regex(Regex.escape(DECO) + "(?=\\s|$)"), "") }),
            deco("♥"), deco("✿"), deco("❀"), deco("★"), deco("☆"),
            deco("♡"), deco("•"), deco("°"), deco("✦"), deco("❁"),
            deco("☪"), deco("♪"), deco("✧"), deco("⚡"), deco("☘")
        ),

        "〰 تشكيل" to listOf(
            tashkeel("فتحة", "\u064E"),
            tashkeel("ضمة", "\u064F"),
            tashkeel("كسرة", "\u0650"),
            tashkeel("شدّة", "\u0651"),
            tashkeel("سكون", "\u0652"),
            tashkeel("تنوين", "\u064B"),
            tashkeel("شدّة+فتحة", "\u0651\u064E"),
            tashkeel("شدّة+ضمة", "\u0651\u064F"),
            tashkeel("شدّة+كسرة", "\u0651\u0650"),
            Style("مختلط", "كتابة", true,
                fn = { t ->
                    arPerChar(t) { c, s ->
                        if (isArLetter(c)) s + mixedMarks[c.code % 3] else s
                    }
                })
        ),

        "🎀 أطر (على النص الموجود)" to listOf(
            frame("نجوم", "★"),
            frame("نجمة", "✦"),
            frame("زهور", "❀"),
            frame("ورد", "✿"),
            frame("قلوب", "♥"),
            frame("قلب", "💗"),
            frame("هلال", "☪"),
            frame("أقواس", "『", "』"),
            frame("أقواس ٢", "【", "】"),
            frame("أقواس ٣", "《", "》"),
            frame("قرآني", "\uFD3F", "\uFD3E"),
            frame("زخرفة", "༺", "༻"),
            frame("زخرفة ٢", "꧁", "꧂"),
            frame("زخرفة ٣", "⚜", "⚜"),
            frame("زخرفة ٤", "❖", "❖"),
            frame("خطوط", "━━", "━━"),
            frame("نقاط", "•°•", "•°•"),
            frame("سهم", "➤", "◄")
        )
    )

    val all: List<Style> = sections.flatMap { it.second }
}
