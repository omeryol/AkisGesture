package io.github.omeryol.akisgesture.automation

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Manifest ile otomasyon kapısının ve komut çözümleyicisinin tutarlılığını
 * doğrular. Bileşen veya action adı uyuşmazlığı, sessizce çalışmayan bir
 * güvenlik kapısı ya da çözülemeyen intent üretirdi.
 */
class AutomationManifestTest {

    private val androidNamespace = "http://schemas.android.com/apk/res/android"
    private val appPackagePrefix = "io.github.omeryol.akisgesture."

    private fun manifest(): Document = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }
        .newDocumentBuilder()
        .parse(File("src/main/AndroidManifest.xml"))

    private fun elements(document: Document, tag: String): List<Element> {
        val nodes = document.getElementsByTagName(tag)
        return (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }
    }

    private fun name(element: Element): String = element.getAttributeNS(androidNamespace, "name")

    private fun actionsOf(element: Element): Set<String> {
        val filters = element.getElementsByTagName("intent-filter")
        val actions = mutableSetOf<String>()
        for (index in 0 until filters.length) {
            val filter = filters.item(index) as? Element ?: continue
            val actionNodes = filter.getElementsByTagName("action")
            for (actionIndex in 0 until actionNodes.length) {
                actions += name(actionNodes.item(actionIndex) as Element)
            }
        }
        return actions
    }

    private fun declaredComponent(document: Document, type: Class<*>): Element? {
        val relative = type.name.removePrefix(appPackagePrefix)
        return (elements(document, "activity") + elements(document, "receiver")).firstOrNull {
            val declared = name(it)
            declared == type.name || declared.trimStart('.') == relative
        }
    }

    @Test
    fun `every gated component is declared and exported in the manifest`() {
        val document = manifest()

        for (component in AutomationGate.exportedComponentClasses) {
            val element = declaredComponent(document, component)
            assertNotNull("Otomasyon kapısının hedeflediği bileşen manifestte yok: ${component.name}", element)
            assertEquals(
                "Bileşen dışa açık olmalı: ${component.name}",
                "true",
                element!!.getAttributeNS(androidNamespace, "exported"),
            )
        }
    }

    @Test
    fun `every command action resolves both as broadcast and as activity`() {
        val document = manifest()

        val receiverActions = elements(document, "receiver")
            .filter { name(it).endsWith("GestureCommandReceiver") }
            .flatMap { actionsOf(it) }
            .toSet()

        val activityActions = elements(document, "activity")
            .filter { name(it).endsWith("GestureActivity") }
            .flatMap { actionsOf(it) }
            .toSet()

        assertEquals(
            "Receiver'ın ilan ettiği action kümesi kodla birebir aynı olmalı",
            AutomationCommand.declaredActions,
            receiverActions,
        )
        for (action in AutomationCommand.declaredActions) {
            assertTrue(
                "Action activity olarak çözülemiyor: $action",
                action in activityActions,
            )
        }
    }

    @Test
    fun `locale plugin components use the protocol action names`() {
        val document = manifest()

        val pluginActivity = elements(document, "activity")
            .firstOrNull { name(it).endsWith("MacroDroidPluginActivity") }
        val pluginReceiver = elements(document, "receiver")
            .firstOrNull { name(it).endsWith("MacroDroidPluginReceiver") }

        assertNotNull("MacroDroidPluginActivity manifestte yok", pluginActivity)
        assertNotNull("MacroDroidPluginReceiver manifestte yok", pluginReceiver)
        assertEquals(setOf(LocalePluginProtocol.ACTION_EDIT_SETTING), actionsOf(pluginActivity!!))
        assertEquals(setOf(LocalePluginProtocol.ACTION_FIRE_SETTING), actionsOf(pluginReceiver!!))
    }
}
