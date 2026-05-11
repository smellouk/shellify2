package dev.pwaforge.domain.model

data class PwaManifest(
    val name: String? = null,
    val shortName: String? = null,
    val description: String? = null,
    val startUrl: String? = null,
    val themeColor: String? = null,
    val backgroundColor: String? = null,
    val display: String? = null,
    val icons: List<PwaIcon> = emptyList(),
) {
    /** Best icon URL, preferring 192px+ sizes. */
    fun bestIconUrl(baseUrl: String): String? {
        val preferred = icons
            .filter { it.sizes != null }
            .maxByOrNull { sizeScore(it.sizes!!) }
            ?: icons.firstOrNull()
        val src = preferred?.src ?: return null
        return if (src.startsWith("http")) src else "$baseUrl/$src".replace("//", "/").let {
            if (it.startsWith("http:/") && !it.startsWith("http://")) it.replace("http:/", "http://") else it
        }
    }

    private fun sizeScore(sizes: String): Int {
        val w = sizes.split("x").firstOrNull()?.trim()?.toIntOrNull() ?: 0
        return w
    }
}

data class PwaIcon(
    val src: String,
    val sizes: String? = null,
    val type: String? = null,
)
