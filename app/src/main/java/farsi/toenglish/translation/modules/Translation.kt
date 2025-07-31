package farsi.toenglish.translation.modules

class Translation {
    var id = 0
    var from_lang: String? = null
    var from_text: String? = null
    var to_lang: String? = null
    var to_text: String? = null
    var date: String? = null

    constructor() {}
    constructor(
        id: Int,
        from_lang: String?,
        from_text: String?,
        to_lang: String?,
        to_text: String?,
        date: String?
    ) {
        this.id = id
        this.from_lang = from_lang
        this.from_text = from_text
        this.to_lang = to_lang
        this.to_text = to_text
        this.date = date
    }

    constructor(
        from_lang: String?,
        from_text: String?,
        to_lang: String?,
        to_text: String?,
        date: String?
    ) {
        this.from_lang = from_lang
        this.from_text = from_text
        this.to_lang = to_lang
        this.to_text = to_text
        this.date = date
    }
}