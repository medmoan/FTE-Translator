package farsi.toenglish.translation.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmark")
data class Bookmark(
    @PrimaryKey var uid: Int?,
    @ColumnInfo(name = "from_lang") val fromLang: String,
    @ColumnInfo(name = "to_Lang") val toLang: String,
    @ColumnInfo(name= "from_lang_position") val fromLangPosition: Int,
    @ColumnInfo(name= "to_lang_position") val toLangPosition: Int,
    @ColumnInfo(name = "from_text") val fromText: String,
    @ColumnInfo(name = "to_text") val toText: String,
    @ColumnInfo(name = "date") val date: String
){
    constructor(
        fromLang: String,
        toLang: String,
        fromLangPosition: Int,
        toLangPosition: Int,
        fromText: String,
        toText: String,
        date: String
    ) : this(
        uid = null,
        fromLang = fromLang,
        toLang = toLang,
        fromLangPosition = fromLangPosition,
        toLangPosition = toLangPosition,
        fromText = fromText,
        toText = toText,
        date = date
    )
}
