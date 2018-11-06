package so.codeweaver.muzei.ponies.derpi

import java.util.Arrays

data class DerpibooruResult(
        val total: Int,
        val search: Array<DerpibooruImage>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DerpibooruResult

        if (total != other.total) return false
        if (!Arrays.equals(search, other.search)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = total
        result = 31 * result + Arrays.hashCode(search)
        return result
    }
}
