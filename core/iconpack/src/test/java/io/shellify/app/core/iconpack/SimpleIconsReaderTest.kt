package io.shellify.app.core.iconpack

import org.junit.Assert.assertEquals
import org.junit.Test

class SimpleIconsReaderTest {

    // Testing the extracted companion slugify function directly

    @Test
    fun `slugify converts to lowercase`() {
        assertEquals("github", SimpleIconsReader.slugify("GitHub"))
    }

    @Test
    fun `slugify replaces plus with plus word`() {
        assertEquals("cplus", SimpleIconsReader.slugify("C+"))
        assertEquals("gplus", SimpleIconsReader.slugify("G+"))
    }

    @Test
    fun `slugify replaces dot with dot word`() {
        assertEquals("netdot", SimpleIconsReader.slugify("net."))
        assertEquals("dotnet", SimpleIconsReader.slugify(".Net"))
    }

    @Test
    fun `slugify removes spaces`() {
        assertEquals("visualstudiocode", SimpleIconsReader.slugify("Visual Studio Code"))
    }

    @Test
    fun `slugify removes non-alphanumeric characters`() {
        assertEquals("amazon", SimpleIconsReader.slugify("Amazon!"))
        assertEquals("googlechrome", SimpleIconsReader.slugify("Google-Chrome"))
    }

    @Test
    fun `slugify handles combined transformations`() {
        // "C++" → lowercase "c++" → replace "+" → "cplusplus" → remove non-alphanumeric → "cplusplus"
        assertEquals("cplusplus", SimpleIconsReader.slugify("C++"))
    }

    @Test
    fun `slugify produces empty string for fully non-alphanumeric input`() {
        assertEquals("", SimpleIconsReader.slugify("!!!"))
    }

    @Test
    fun `slugify preserves numbers`() {
        assertEquals("windows11", SimpleIconsReader.slugify("Windows 11"))
    }

    @Test
    fun `slugify handles already lowercase ascii`() {
        assertEquals("node", SimpleIconsReader.slugify("node"))
    }
}
