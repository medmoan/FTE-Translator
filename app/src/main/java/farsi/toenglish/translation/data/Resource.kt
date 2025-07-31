package farsi.toenglish.translation.data

import java.lang.Exception

sealed class Resource<out T>() {
    class Success<out T>(val result: T): Resource<T>()
    class Failed(val exception: Exception?): Resource<Nothing>()
}