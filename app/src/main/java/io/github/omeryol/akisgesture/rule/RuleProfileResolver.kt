package io.github.omeryol.akisgesture.rule

object RuleProfileResolver {
    fun resolve(
        foregroundPackage: String?,
        defaultRules: CompiledRuleSet,
        appProfiles: Map<String, CompiledRuleSet>,
    ): CompiledRuleSet {
        return foregroundPackage?.let(appProfiles::get) ?: defaultRules
    }
}
