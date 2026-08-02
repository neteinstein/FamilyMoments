package org.neteinstein.family.domain.model

sealed class QuestionCategory(val label: String) {
    data object Reflections : QuestionCategory("💭 Reflections")
    data object Dreams : QuestionCategory("✨ Dreams")
}
