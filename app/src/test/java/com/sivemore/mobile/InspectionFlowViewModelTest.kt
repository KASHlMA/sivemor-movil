package com.sivemore.mobile

import com.sivemore.mobile.feature.inspection.sanitizeDecimalInput
import org.junit.Assert.assertEquals
import org.junit.Test

class InspectionFlowViewModelTest {
    @Test
    fun sanitizeDecimalInput_allowsSingleDecimalPoint() {
        assertEquals("12.5", sanitizeDecimalInput("12.5"))
    }

    @Test
    fun sanitizeDecimalInput_normalizesLeadingDecimalPoint() {
        assertEquals("0.8", sanitizeDecimalInput(".8"))
    }

    @Test
    fun sanitizeDecimalInput_removesInvalidCharactersAndExtraPoints() {
        assertEquals("12.34", sanitizeDecimalInput("1a2..3.4"))
    }
}
