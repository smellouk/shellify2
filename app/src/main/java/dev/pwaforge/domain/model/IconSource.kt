package dev.pwaforge.domain.model

import org.json.JSONObject

sealed class IconSource {
    /** PNG/JPEG already on disk (fetch, favicon, gallery) */
    data class Path(val path: String) : IconSource()

    /** Simple Icons SVG — slug for CDN + background hex + already-rendered PNG path */
    data class SvgIcon(
        val slug: String,
        val background: String,
        val renderedPath: String? = null,
    ) : IconSource()

    fun toJson(): String = when (this) {
        is Path -> JSONObject().put("type", "path").put("path", path).toString()
        is SvgIcon -> JSONObject()
            .put("type", "svg")
            .put("slug", slug)
            .put("background", background)
            .apply { if (renderedPath != null) put("renderedPath", renderedPath) }
            .toString()
    }

    companion object {
        fun fromJson(json: String?): IconSource? {
            if (json == null) return null
            return runCatching {
                val obj = JSONObject(json)
                when (obj.getString("type")) {
                    "path" -> Path(obj.getString("path"))
                    "svg" -> SvgIcon(
                        slug = obj.getString("slug"),
                        background = obj.getString("background"),
                        renderedPath = obj.optString("renderedPath").takeIf { it.isNotBlank() },
                    )
                    else -> null
                }
            }.getOrNull()
        }

        /** Migrate a legacy plain file path string (no JSON wrapper) */
        fun fromLegacyPath(path: String?): IconSource? =
            path?.let { Path(it) }
    }
}
