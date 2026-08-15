package io.github.omeryol.akisgesture.localization

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizationResourcesTest {
    @Test
    fun `english and turkish expose identical resource keys`() {
        val english = resourceKeys(File("src/main/res/values/strings.xml"))
        val turkish = resourceKeys(File("src/main/res/values-tr/strings.xml"))

        assertEquals(english, turkish)
    }

    @Test
    fun `every main locale exposes the complete english key set`() {
        val root = File("src/main/res")
        val english = resourceKeys(File(root, "values/strings.xml"))
        root.listFiles { file -> file.isDirectory && file.name.startsWith("values-") }
            ?.sortedBy { it.name }
            ?.forEach { localeDir ->
                assertEquals(
                    "Resource keys differ for ${localeDir.name}",
                    english,
                    resourceKeys(File(localeDir, "strings.xml")),
                )
            }
    }

    @Test
    fun `every diagnostic locale exposes the complete diagnostic key set`() {
        val root = File("src/diagnostic/res")
        val english = resourceKeys(File(root, "values/diagnostics.xml"))
        root.listFiles { file -> file.isDirectory && file.name.startsWith("values-") }
            ?.sortedBy { it.name }
            ?.forEach { localeDir ->
                assertEquals(
                    "Diagnostic resource keys differ for ${localeDir.name}",
                    english,
                    resourceKeys(File(localeDir, "diagnostics.xml")),
                )
            }
    }

    private fun resourceKeys(file: File): Set<String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        return buildSet {
            val children = document.documentElement.childNodes
            for (index in 0 until children.length) {
                val node = children.item(index)
                if (node.nodeName != "string") continue
                val name = node.attributes?.getNamedItem("name")?.nodeValue ?: continue
                add("${node.nodeName}:$name")
            }
        }
    }
}
