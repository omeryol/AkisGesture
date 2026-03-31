package com.openswipe.rule

import com.openswipe.model.*
import com.openswipe.overlay.Edge
import org.json.JSONArray
import org.json.JSONObject

object RuleSerializer {

    fun GestureRuleGraph.toJson(): String {
        val root = JSONObject()
        root.put("version", 1)
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
            val gestureType = GestureType.valueOf(triggerObj.getString("gesture"))

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

            rules.add(GestureRule(id, TriggerNode(edge, section, gestureType), action, enabled))
        }

        return GestureRuleGraph(rules)
    }
}
