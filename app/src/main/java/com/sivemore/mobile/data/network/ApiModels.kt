package com.sivemore.mobile.data.network

data class LoginRequestDto(
    val username: String,
    val password: String,
)

data class RefreshRequestDto(
    val refreshToken: String,
)

data class LogoutRequestDto(
    val refreshToken: String,
)

data class SessionUserDto(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String,
)

data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: Long,
    val user: SessionUserDto,
)

data class AssignedOrderDto(
    val orderUnitId: Long,
    val orderId: Long,
    val orderNumber: String,
    val clientCompanyName: String,
    val regionName: String,
    val scheduledAt: String,
    val vehicleUnitId: Long,
    val vehiclePlate: String,
    val vehicleCategory: String,
    val draftInspectionId: Long?,
)

data class CreateInspectionRequestDto(
    val orderUnitId: Long,
)

data class ChecklistQuestionDto(
    val id: Long,
    val code: String,
    val prompt: String,
    val required: Boolean,
    val displayOrder: Int,
)

data class InspectionQuestionAnswerDto(
    val questionId: Long,
    val answer: String,
    val comment: String?,
)

data class EvidenceDto(
    val id: Long,
    val sectionId: Long?,
    val filename: String,
    val mimeType: String,
    val capturedAt: String,
    val comment: String?,
)

data class InspectionSectionDraftDto(
    val sectionId: Long,
    val title: String,
    val description: String?,
    val displayOrder: Int,
    val note: String?,
    val questions: List<ChecklistQuestionDto>,
    val answers: List<InspectionQuestionAnswerDto>,
    val evidences: List<EvidenceDto>,
)

data class InspectionDraftDto(
    val id: Long,
    val orderId: Long,
    val orderUnitId: Long,
    val orderNumber: String,
    val vehiclePlate: String,
    val clientCompanyName: String,
    val status: String,
    val lastSectionIndex: Int,
    val overallComment: String?,
    val startedAt: String,
    val updatedAt: String,
    val evidenceCount: Int,
    val sections: List<InspectionSectionDraftDto>,
)

data class QuestionUpdateDto(
    val questionId: Long,
    val answer: String,
    val comment: String? = null,
)

data class SectionUpdateDto(
    val sectionId: Long,
    val note: String? = null,
    val questions: List<QuestionUpdateDto> = emptyList(),
)

data class UpdateInspectionRequestDto(
    val lastSectionIndex: Int = 0,
    val overallComment: String? = null,
    val sections: List<SectionUpdateDto> = emptyList(),
)
