package com.omer.akisgesture.rule

import com.omer.akisgesture.model.ActionNode
import com.omer.akisgesture.model.GestureRule
import com.omer.akisgesture.model.GestureType
import com.omer.akisgesture.model.SectionRange
import com.omer.akisgesture.model.TriggerMode
import com.omer.akisgesture.model.TriggerNode
import com.omer.akisgesture.overlay.Edge
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
            if (rule.action is ActionNode.LaunchApp) {
                actionObj.put("appName", rule.action.appName)
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
            val action = if (actionId.startsWith("launch_app:")) {
                val pkg = actionId.removePrefix("launch_app:")
                val appName = actionObj.optString("appName", pkg)
                ActionNode.LaunchApp(pkg, appName)
            } else {
                ActionNode.fromId(actionId) ?: ActionNode.NoAction
            }

            val enabled = ruleObj.optBoolean("enabled", true)
            val triggerMode = ruleObj.optString("triggerMode", TriggerMode.SWIPE.name)
                .let { runCatching { TriggerMode.valueOf(it) }.getOrDefault(TriggerMode.SWIPE) }

            rules.add(GestureRule(id, TriggerNode(edge, section, gestureType), action, enabled, triggerMode))
        }

        return GestureRuleGraph(rules)
    }
}
