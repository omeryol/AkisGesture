package io.github.omeryol.akisgesture.rule

import io.github.omeryol.akisgesture.rule.RuleSerializer.toGestureRuleGraph
import io.github.omeryol.akisgesture.rule.RuleSerializer.toJson
import org.json.JSONObject

object AppRuleProfilesSerializer {

    fun toJson(profiles: Map<String, GestureRuleGraph>): String {
        val root = JSONObject()
        profiles.toSortedMap().forEach { (packageName, graph) ->
            root.put(packageName, JSONObject(graph.toJson()))
        }
        return root.toString(2)
    }

    fun fromJson(json: String): Map<String, GestureRuleGraph> {
        if (json.isBlank()) return emptyMap()
        val root = JSONObject(json)
        return buildMap {
            root.keys().forEach { packageName ->
                val graph = root.getJSONObject(packageName)
                    .toString()
                    .toGestureRuleGraph()
                put(packageName, graph)
            }
        }
    }
}
