package com.sivemore.mobile

import androidx.lifecycle.SavedStateHandle
import com.sivemore.mobile.feature.luces.LucesEvent
import com.sivemore.mobile.feature.luces.LucesUiAction
import com.sivemore.mobile.feature.luces.LucesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LucesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun buildViewModel(): LucesViewModel = LucesViewModel(
        savedStateHandle = SavedStateHandle(mapOf("vehicleId" to "1")),
    )

    @Test
    fun initialStateContainsExpectedQuestionsAndOptions() {
        val viewModel = buildViewModel()

        val section = viewModel.uiState.value.section

        assertEquals("Luces", section.title)
        assertEquals(10, section.questions.size)

        val generalOptions = section.questions.first { it.id == "luces_galibo" }.options.map { it.label }
        val indicatorsOptions = section.questions.first { it.id == "luces_indicadoras" }.options.map { it.label }
        val headlightOptions = section.questions.first { it.id == "faro_izquierdo" }.options.map { it.label }

        assertEquals(
            listOf("Aprobadas", "Izquierda fundida", "Derecha fundida", "Ambas fundidas"),
            generalOptions,
        )
        assertEquals(
            listOf("Aprobadas", "1 fundida", "2 fundidas", "3 fundidas"),
            indicatorsOptions,
        )
        assertEquals(
            listOf("Aprobado", "Flojo", "Roto"),
            headlightOptions,
        )
        assertFalse(viewModel.uiState.value.isNextEnabled)
    }

    @Test
    fun selectingOptionReplacesPreviousSelectionForSameQuestion() {
        val viewModel = buildViewModel()

        viewModel.onAction(LucesUiAction.OptionSelected("luces_altas", "LEFT_BURNT"))
        viewModel.onAction(LucesUiAction.OptionSelected("luces_altas", "RIGHT_BURNT"))

        val question = viewModel.uiState.value.section.questions.first { it.id == "luces_altas" }
        assertEquals("RIGHT_BURNT", question.selectedOptionId)
    }

    @Test
    fun nextRemainsDisabledUntilAllQuestionsAreAnswered() {
        val viewModel = buildViewModel()

        val allQuestions = viewModel.uiState.value.section.questions
        allQuestions.dropLast(1).forEach { question ->
            val firstOptionId = question.options.first().id
            viewModel.onAction(LucesUiAction.OptionSelected(question.id, firstOptionId))
        }

        assertFalse(viewModel.uiState.value.isNextEnabled)
        assertNull(viewModel.uiState.value.section.questions.last().selectedOptionId)
    }

    @Test
    fun nextDoesNotNavigateWhenAnyQuestionIsMissing() = runTest {
        val viewModel = buildViewModel()
        val event = async { viewModel.events.first() }

        viewModel.onAction(LucesUiAction.NextClicked)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isNextEnabled)
        assertFalse(event.isCompleted)
        event.cancel()
    }

    @Test
    fun nextNavigatesWhenAllQuestionsAreAnswered() = runTest {
        val viewModel = buildViewModel()
        val event = async { viewModel.events.first() }

        viewModel.uiState.value.section.questions.forEach { question ->
            viewModel.onAction(LucesUiAction.OptionSelected(question.id, question.options.first().id))
        }
        viewModel.onAction(LucesUiAction.NextClicked)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isNextEnabled)
        assertEquals(LucesEvent.NavigateToNextSection("1"), event.await())
    }
}
