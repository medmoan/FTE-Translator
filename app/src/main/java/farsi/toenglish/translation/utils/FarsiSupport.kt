@file:Suppress("NAME_SHADOWING")

package farsi.toenglish.translation.utils

import android.content.Context
import android.graphics.Typeface

object FarsiSupport {
    var isFarsiConversionNeeded = true
    private val szLamAndAlef = Character
        .toString(0xfedf.toChar()) + Character.toString(0xfe8e.toChar())
    private val szLamStickAndAlef = Character
        .toString(0xfee0.toChar()) + Character.toString(0xfe8e.toChar())
    private val szLa = Character.toString(0xfefb.toChar())
    private val szLaStick = Character.toString(0xfefc.toChar())
    private var arrStruc = arrayOf(
        struc(0x630.toChar(), 0xfeac.toChar(), 0xfeab.toChar(), 0xfeac.toChar(), 0xfeab.toChar()),
        struc(0x62f.toChar(), 0xfeaa.toChar(), 0xfea9.toChar(), 0xfeaa.toChar(), 0xfea9.toChar()),
        struc(0x62c.toChar(), 0xfe9e.toChar(), 0xfe9f.toChar(), 0xfea0.toChar(), 0xfe9d.toChar()),
        struc(0x62d.toChar(), 0xfea2.toChar(), 0xfea3.toChar(), 0xfea4.toChar(), 0xfea1.toChar()),
        struc(0x62e.toChar(), 0xfea6.toChar(), 0xfea7.toChar(), 0xfea8.toChar(), 0xfea5.toChar()),
        struc(0x647.toChar(), 0xfeea.toChar(), 0xfeeb.toChar(), 0xfeec.toChar(), 0xfee9.toChar()),
        struc(0x639.toChar(), 0xfeca.toChar(), 0xfecb.toChar(), 0xfecc.toChar(), 0xfec9.toChar()),
        struc(0x63a.toChar(), 0xfece.toChar(), 0xfecf.toChar(), 0xfed0.toChar(), 0xfecd.toChar()),
        struc(0x641.toChar(), 0xfed2.toChar(), 0xfed3.toChar(), 0xfed4.toChar(), 0xfed1.toChar()),
        struc(0x642.toChar(), 0xfed6.toChar(), 0xfed7.toChar(), 0xfed8.toChar(), 0xfed5.toChar()),
        struc(0x62b.toChar(), 0xfe9a.toChar(), 0xfe9b.toChar(), 0xfe9c.toChar(), 0xfe99.toChar()),
        struc(0x635.toChar(), 0xfeba.toChar(), 0xfebb.toChar(), 0xfebc.toChar(), 0xfeb9.toChar()),
        struc(0x636.toChar(), 0xfebe.toChar(), 0xfebf.toChar(), 0xfec0.toChar(), 0xfebd.toChar()),
        struc(0x637.toChar(), 0xfec2.toChar(), 0xfec3.toChar(), 0xfec4.toChar(), 0xfec1.toChar()),
        struc(0x643.toChar(), 0xfeda.toChar(), 0xfedb.toChar(), 0xfedc.toChar(), 0xfed9.toChar()),
        struc(0x645.toChar(), 0xfee2.toChar(), 0xfee3.toChar(), 0xfee4.toChar(), 0xfee1.toChar()),
        struc(0x646.toChar(), 0xfee6.toChar(), 0xfee7.toChar(), 0xfee8.toChar(), 0xfee5.toChar()),
        struc(0x62a.toChar(), 0xfe96.toChar(), 0xfe97.toChar(), 0xfe98.toChar(), 0xfe95.toChar()),
        struc(0x627.toChar(), 0xfe8e.toChar(), 0xfe8d.toChar(), 0xfe8e.toChar(), 0xfe8d.toChar()),
        struc(0x644.toChar(), 0xfede.toChar(), 0xfedf.toChar(), 0xfee0.toChar(), 0xfedd.toChar()),
        struc(0x628.toChar(), 0xfe90.toChar(), 0xfe91.toChar(), 0xfe92.toChar(), 0xfe8f.toChar()),
        struc(0x64a.toChar(), 0xfef2.toChar(), 0xfef3.toChar(), 0xfef4.toChar(), 0xfef1.toChar()),
        struc(0x633.toChar(), 0xfeb2.toChar(), 0xfeb3.toChar(), 0xfeb4.toChar(), 0xfeb1.toChar()),
        struc(0x634.toChar(), 0xfeb6.toChar(), 0xfeb7.toChar(), 0xfeb8.toChar(), 0xfeb5.toChar()),
        struc(0x638.toChar(), 0xfec6.toChar(), 0xfec7.toChar(), 0xfec8.toChar(), 0xfec5.toChar()),
        struc(0x632.toChar(), 0xfeb0.toChar(), 0xfeaf.toChar(), 0xfeb0.toChar(), 0xfeaf.toChar()),
        struc(0x648.toChar(), 0xfeee.toChar(), 0xfeed.toChar(), 0xfeee.toChar(), 0xfeed.toChar()),
        struc(0x629.toChar(), 0xfe94.toChar(), 0xfe93.toChar(), 0xfe93.toChar(), 0xfe93.toChar()),
        struc(0x649.toChar(), 0xfef0.toChar(), 0xfeef.toChar(), 0xfef0.toChar(), 0xfeef.toChar()),
        struc(0x631.toChar(), 0xfeae.toChar(), 0xfead.toChar(), 0xfeae.toChar(), 0xfead.toChar()),
        struc(0x624.toChar(), 0xfe86.toChar(), 0xfe85.toChar(), 0xfe86.toChar(), 0xfe85.toChar()),
        struc(0x621.toChar(), 0xfe80.toChar(), 0xfe80.toChar(), 0xfe80.toChar(), 0xfe80.toChar()),
        struc(0x626.toChar(), 0xfe8a.toChar(), 0xfe8b.toChar(), 0xfe8c.toChar(), 0xfe89.toChar()),
        struc(0x623.toChar(), 0xfe84.toChar(), 0xfe83.toChar(), 0xfe84.toChar(), 0xfe83.toChar()),
        struc(0x622.toChar(), 0xfe82.toChar(), 0xfe81.toChar(), 0xfe82.toChar(), 0xfe81.toChar()),
        struc(0x625.toChar(), 0xfe88.toChar(), 0xfe87.toChar(), 0xfe88.toChar(), 0xfe87.toChar()),
        struc(
            0x67e.toChar(),
            0xfb57.toChar(),
            0xfb58.toChar(),
            0xfb59.toChar(),
            0xfb56.toChar()
        ),  // peh
        struc(
            0x686.toChar(),
            0xfb7b.toChar(),
            0xfb7c.toChar(),
            0xfb7d.toChar(),
            0xfb7a.toChar()
        ),  // cheh
        struc(
            0x698.toChar(),
            0xfb8b.toChar(),
            0xfb8a.toChar(),
            0xfb8b.toChar(),
            0xfb8a.toChar()
        ),  // jeh
        struc(
            0x6a9.toChar(),
            0xfb8f.toChar(),
            0xfb90.toChar(),
            0xfb91.toChar(),
            0xfb8e.toChar()
        ),  // keheh
        struc(
            0x6af.toChar(),
            0xfb93.toChar(),
            0xfb94.toChar(),
            0xfb95.toChar(),
            0xfb92.toChar()
        ),  // gaf
        struc(
            0x6cc.toChar(),
            0xfbfd.toChar(),
            0xfbfe.toChar(),
            0xfbff.toChar(),
            0xfbfc.toChar()
        ),  // Farsi yeh
        struc(
            0x6cc.toChar(),
            0xfbfd.toChar(),
            0xfef3.toChar(),
            0xfef4.toChar(),
            0xfbfc.toChar()
        ),  // Arabic yeh
        struc(
            0x6c0.toChar(),
            0xfba5.toChar(),
            0xfba4.toChar(),
            0xfba5.toChar(),
            0xfba4.toChar()
        ) // heh with yeh
    )
    private const val N_DISTINCT_CHARACTERS = 43
    private fun ArabicReverse(s: String): String {
        var s = s
        try {
            var Out = ""
            var rev: String
            s = MakeReverse(s)
            var chs = CharArray(s.length)
            chs = s.toCharArray()
            var i = 0
            while (i < s.length) {
                if (chs[i] >= '0' && chs[i] <= '9') {
                    rev = ""
                    while (i < s.length
                        && (chs[i] >= '0' && chs[i] <= '9' || chs[i] == '/')
                    ) {
                        rev = rev + chs[i]
                        ++i
                    }
                    rev = MakeReverse(rev)
                    Out = Out + rev
                } else {
                    Out = Out + chs[i]
                    ++i
                }
            }
            s = Out
        } catch (ex: Exception) {
        }
        return s
    }

    private fun isFromTheSet1(ch: Char): Boolean {
        val theSet1 = charArrayOf(
            0x62c.toChar(),
            0x62d.toChar(),
            0x62e.toChar(),
            0x647.toChar(),
            0x639.toChar(),
            0x63a.toChar(),
            0x641.toChar(),
            0x642.toChar(),
            0x62b.toChar(),
            0x635.toChar(),
            0x636.toChar(),
            0x637.toChar(),
            0x643.toChar(),
            0x645.toChar(),
            0x646.toChar(),
            0x62a.toChar(),
            0x644.toChar(),
            0x628.toChar(),
            0x64a.toChar(),
            0x633.toChar(),
            0x634.toChar(),
            0x638.toChar(),
            0x67e.toChar(),
            0x686.toChar(),
            0x6a9.toChar(),
            0x6af.toChar(),
            0x6cc.toChar(),
            0x626.toChar()
        )
        var i = 0
        while (i < 28) {
            if (ch == theSet1[i]) return true
            ++i
        }
        return false
    }

    private fun isFromTheSet2(ch: Char): Boolean {
        val theSet2 = charArrayOf(
            0x627.toChar(),
            0x623.toChar(),
            0x625.toChar(),
            0x622.toChar(),
            0x62f.toChar(),
            0x630.toChar(),
            0x631.toChar(),
            0x632.toChar(),
            0x648.toChar(),
            0x624.toChar(),
            0x629.toChar(),
            0x649.toChar(),
            0x698.toChar(),
            0x6c0.toChar()
        )
        var i = 0
        while (i < 14) {
            if (ch == theSet2[i]) return true
            ++i
        }
        return false
    }

    private fun MakeReverse(text: String): String {
        var Result = ""
        var Ctext = CharArray(text.length)
        Ctext = text.toCharArray()
        for (i in text.length - 1 downTo 0) {
            Result += Ctext[i]
        }
        return Result
    }

    fun ConvertToRealFarsi(In: String): String {
        var strOut = ""
        val strBuilder = StringBuilder("")
        var i = 0
        var j = 0
        var chIn = CharArray(In.length)
        chIn = In.toCharArray()
        i = 0
        while (i < In.length) {
            var found = false
            j = 0
            while (j < arrStruc.size) {
                if (chIn[i] == arrStruc[j].midGlyph || chIn[i] == arrStruc[j].iniGlyph || chIn[i] == arrStruc[j].endGlyph || chIn[i] == arrStruc[j].isoGlyph) {
                    strBuilder.append(arrStruc[j].character)
                    found = true
                    break
                }
                j++
            }
            if (!found) strBuilder.append(chIn[i])
            i++
        }
        strOut = strBuilder.toString()
        strOut = strOut.replace(szLa, "لا")
        strOut = strOut.replace(szLaStick, "لا")
        return strOut
    }

    fun ConvertForDrawOnBitmap(In: String): String {
        return ArabicReverse(Convert(In))
    }

    fun Convert(In: String): String {
        return if (!isFarsiConversionNeeded) {
            In
        } else try {
            var linkBefore: Boolean
            var linkAfter: Boolean
            var Out = In
            var chOut = CharArray(Out.length)
            chOut = Out.toCharArray()
            var chIn = CharArray(In.length)
            chIn = In.toCharArray()
            for (i in 0 until In.length) {
                /* WCHAR */
                val ch = chIn[i]
                if (ch >= 0x0621.toChar() && ch <= 0x064a.toChar() || ch == 0x067e.toChar() || ch == 0x0686.toChar() || ch == 0x0698.toChar() || ch == 0x06a9.toChar() || ch == 0x06af.toChar() || ch == 0x06cc.toChar() || ch == 0x06c0.toChar()) // is an Arabic character?
                {
                    var idx = 0
                    while (idx < N_DISTINCT_CHARACTERS) {
                        if (arrStruc[idx].character == chIn[i]) break
                        ++idx
                    }
                    if (i == In.length - 1) linkAfter = false else linkAfter = isFromTheSet1(
                        chIn[i + 1]
                    ) || isFromTheSet2(chIn[i + 1])
                    linkBefore = if (i == 0) false else isFromTheSet1(chIn[i - 1])
                    if (idx < N_DISTINCT_CHARACTERS) {
                        if (linkBefore && linkAfter) chOut[i] = arrStruc[idx].midGlyph
                        if (linkBefore && !linkAfter) chOut[i] = arrStruc[idx].endGlyph
                        if (!linkBefore && linkAfter) chOut[i] = arrStruc[idx].iniGlyph
                        if (!linkBefore && !linkAfter) chOut[i] = arrStruc[idx].isoGlyph
                    } else {
                        chOut[i] = chIn[i]
                    }
                } else {
                    chOut[i] = chIn[i]
                }
            }
            Out = ""
            for (j in chOut.indices) Out += chOut[j]
            ArabicReverse(Out)
            Out = Out.replace(0x200c.toChar(), ' ')
            Out = Out.replace(szLamAndAlef, szLa)
            Out = Out.replace(szLamStickAndAlef, szLaStick)
            Out
        } catch (ex: Exception) {
            ""
        }
    }

    private var typeface: Typeface? = null
    fun GetFarsiFont(act: Context): Typeface? {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(act.assets, "Nazanin.ttf")
        }
        return typeface
    }

    private class struc(
        var character: Char, var endGlyph: Char, var iniGlyph: Char,
        var midGlyph: Char, var isoGlyph: Char
    )
}