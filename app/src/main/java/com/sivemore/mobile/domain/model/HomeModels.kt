package com.sivemore.mobile.domain.model

data class HighlightMetric(
    val label: String,
    val value: String,
    val footnote: String,
)

data class QuickAction(
    val id: String,
    val title: String,
    val description: String,
    val buttonLabel: String,
)

data class InsightCard(
    val id: String,
    val eyebrow: String,
    val title: String,
    val description: String,
)

data class HomeOverview(
    val greeting: String,
    val headline: String,
    val summary: String,
    val highlights: List<HighlightMetric>,
    val quickActions: List<QuickAction>,
    val insights: List<InsightCard>,
)

