package com.sivemore.mobile.data.fixtures

import com.sivemore.mobile.domain.model.AuthenticatedUser
import com.sivemore.mobile.domain.model.HighlightMetric
import com.sivemore.mobile.domain.model.HomeOverview
import com.sivemore.mobile.domain.model.InsightCard
import com.sivemore.mobile.domain.model.ProfilePill
import com.sivemore.mobile.domain.model.ProfileSetting
import com.sivemore.mobile.domain.model.ProfileSummary
import com.sivemore.mobile.domain.model.QuickAction

object FakeCatalog {
    val user = AuthenticatedUser(
        id = "usr-01",
        displayName = "Sofia Benitez",
        email = "sofia@sivemore.app",
    )

    val overview = HomeOverview(
        greeting = "Good morning, Sofia",
        headline = "Protect the next 90 minutes for deep work.",
        summary = "You are tracking well this week. Lock in one focus block and one renewal block before lunch.",
        highlights = listOf(
            HighlightMetric(label = "Focus score", value = "87", footnote = "+9 from yesterday"),
            HighlightMetric(label = "Habits closed", value = "05", footnote = "2 still open"),
            HighlightMetric(label = "Recovery", value = "High", footnote = "Sleep and movement aligned"),
        ),
        quickActions = listOf(
            QuickAction(
                id = "ritual",
                title = "Morning ritual",
                description = "Queue your first routine and block notifications.",
                buttonLabel = "Start ritual",
            ),
            QuickAction(
                id = "plan",
                title = "Daily plan",
                description = "Turn the top priorities into a concrete sequence.",
                buttonLabel = "Review plan",
            ),
        ),
        insights = listOf(
            InsightCard(
                id = "insight-01",
                eyebrow = "Momentum",
                title = "You have closed 12 focus sessions in the last 5 days.",
                description = "Consistency is stronger when your first session starts before 09:30.",
            ),
            InsightCard(
                id = "insight-02",
                eyebrow = "Energy",
                title = "Light movement is the strongest predictor of your afternoon score.",
                description = "A 10-minute walk after lunch raised your completion rate by 18%.",
            ),
        ),
    )

    val profile = ProfileSummary(
        displayName = user.displayName,
        role = "Product Operations Lead",
        email = user.email,
        city = "Mexico City",
        completion = 82,
        focusAreas = listOf(
            ProfilePill("Deep Work"),
            ProfilePill("Recovery"),
            ProfilePill("Planning"),
        ),
        settings = listOf(
            ProfileSetting(title = "Daily summary", value = "08:00"),
            ProfileSetting(title = "Focus notifications", value = "Enabled"),
            ProfileSetting(title = "Weekly review", value = "Friday"),
        ),
    )
}

