package io.github.omeryol.akisgesture.rule

import io.github.omeryol.akisgesture.model.ActionNode
import io.github.omeryol.akisgesture.model.GestureRule
import io.github.omeryol.akisgesture.model.GestureType
import io.github.omeryol.akisgesture.model.SectionRange
import io.github.omeryol.akisgesture.model.TriggerMode
import io.github.omeryol.akisgesture.model.TriggerNode
import io.github.omeryol.akisgesture.overlay.Edge
import org.json.JSONArray
import org.json.JSONObject

object RuleSerializer {

    fun GestureRuleGraph.toJson(): String {
        val root = JSONObject()
        root.put("version", 2)
        val rulesArray = JSONArray()
        for (rule in rules) {
            val ruleObj = JSONObject()
            ruleObj.put("id", rule.id)

            val triggerObj = JSONObject()
            triggerObj.put("edge", rule.trigger.edge.name)
            triggerObj.put("section", JSONArray().apply {
                put(rule.trigger.section.start.toDouble())
                put(rule.trigger.section.end.toDouble())
            })
            triggerObj.put("gesture", rule.trigger.gestureType.name)
            ruleObj.put("trigger", triggerObj)

            val actionObj = JSONObject()
            actionObj.put("id", rule.action.id)
            when (val action = rule.action) {
                is ActionNode.LaunchApp -> {
                    actionObj.put("appName", action.appName)
                }
                is ActionNode.AppShortcut -> {
                    // shortcutLabel ve shortcutId JSON'da saklanmazsa yeniden
                    // başlatmada/yedek yüklemesinde UI etiketi bozuluyordu.
                    actionObj.put("shortcutLabel", action.shortcutLabel)
                    actionObj.put("shortcutId", action.shortcutId)
                }
                is ActionNode.SendKeyCode -> {
                    // keyLabel JSON'da saklanmazsa UI etiketi "Tuş: <kod>" yerine
                    // sadece sayı gösteriyordu.
                    actionObj.put("keyLabel", action.keyLabel)
                }
                else -> Unit
            }
            ruleObj.put("action", actionObj)
            ruleObj.put("enabled", rule.enabled)
            ruleObj.put("triggerMode", rule.triggerMode.name)

            rulesArray.put(ruleObj)
        }
        root.put("rules", rulesArray)
        return root.toString(2)
    }

    fun String.toGestureRuleGraph(): GestureRuleGraph {
        val root = JSONObject(this)
        val rulesArray = root.getJSONArray("rules")
        val rules = mutableListOf<GestureRule>()

        for (i in 0 until rulesArray.length()) {
            val ruleObj = rulesArray.getJSONObject(i)
            val id = ruleObj.optString("id", java.util.UUID.randomUUID().toString())

            val triggerObj = ruleObj.getJSONObject("trigger")
            val edge = Edge.valueOf(triggerObj.getString("edge"))
            val sectionArr = triggerObj.getJSONArray("section")
            val section = SectionRange(sectionArr.getDouble(0).toFloat(), sectionArr.getDouble(1).toFloat())
            val gestureRaw = triggerObj.getString("gesture")
            val gestureType = when (gestureRaw) {
                "SWIPE", "SHORT_SWIPE" -> GestureType.QUICK_SWIPE
                "LONG_SWIPE" -> GestureType.SWIPE_HOLD
                else -> GestureType.valueOf(gestureRaw)
            }

            val actionObj = ruleObj.getJSONObject("action")
            val actionId = actionObj.getString("id")
            val action = when {
                actionId.startsWith("launch_app:") -> {
                    val pkg = actionId.removePrefix("launch_app:")
                    val appName = actionObj.optString("appName", pkg)
                    ActionNode.LaunchApp(pkg, appName)
                }
                actionId.startsWith("app_shortcut:") -> {
                    val parts = actionId.removePrefix("app_shortcut:").split(":", limit = 2)
                    if (parts.size == 2) {
                        val pkg = parts[0]
                        val shortcutId = parts[1]
                        // shortcutLabel JSON'dan okunur; yoksa shortcutId kullanılır
                        val shortcutLabel = actionObj.optString("shortcutLabel", shortcutId)
                        ActionNode.AppShortcut(pkg, shortcutId, shortcutLabel)
                    } else {
                        ActionNode.NoAction
                    }
                }
                actionId.startsWith("keycode:") -> {
                    val code = actionId.removePrefix("keycode:").toIntOrNull()
                    if (code != null) {
                        // keyLabel JSON'dan okunur; yoksa kod numarası kullanılır
                        val keyLabel = actionObj.optString("keyLabel", code.toString())
                        ActionNode.SendKeyCode(code, keyLabel)
                    } else {
                        ActionNode.NoAction
                    }
                }
                else -> ActionNode.fromId(actionId) ?: ActionNode.NoAction
            }

            val enabled = ruleObj.optBoolean("enabled", true)
            val triggerMode = ruleObj.optString("triggerMode", TriggerMode.SWIPE.name)
                .let { runCatching { TriggerMode.valueOf(it) }.getOrDefault(TriggerMode.SWIPE) }

            rules.add(GestureRule(id, TriggerNode(edge, section, gestureType), action, enabled, triggerMode))
        }

        return GestureRuleGraph(rules)
    }
}
