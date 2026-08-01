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

    private fun resourceKeys(file: File): Set<String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        return buildSet {
            val children = document.documentElement.childNodes
            for (index in 0 until children.length) {
                val node = children.item(index)
                val name = node.attributes?.getNamedItem("name")?.nodeValue ?: continue
                add("${node.nodeName}:$name")
            }
        }
    }
}
