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
    val clientCompanyId: Long,
    val clientCompanyName: String,
    val regionName: String,
    val scheduledAt: String,
    val vehicleUnitId: Long,
    val vehiclePlate: String,
    val vehicleCategory: String,
    val draftInspectionId: Long?,
)

data class VehicleClientDto(
    val id: Long,
    val name: String,
    val regionId: Long?,
)

data class VehicleRegionDto(
    val id: Long,
    val name: String,
)

data class CreateVehicleRequestDto(
    val clientCompanyId: Long,
    val verificationOrderId: Long? = null,
    val plate: String,
    val vin: String,
    val category: String = "N2",
    val brand: String,
    val model: String = "Sin modelo",
)

typealias UpdateVehicleRequestDto = CreateVehicleRequestDto

data class VehicleDto(
    val id: Long,
    val clientCompanyId: Long,
    val clientCompanyName: String,
    val plate: String,
    val vin: String,
    val category: String,
    val brand: String,
    val model: String,
)

data class CreateInspectionRequestDto(
    val orderUnitId: Long? = null,
    val vehicleUnitId: Long? = null,
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

data class InspectionHistoryDto(
    val id: Long,
    val orderNumber: String,
    val vehiclePlate: String,
    val clientCompanyName: String,
    val submittedAt: String,
    val overallComment: String?,
    val verdict: String?,
    val sections: List<InspectionSectionDraftDto>,
)

data class MobileEvaluacionRequestDto(
    val inspectionId: Long,
    val lucesGalibo: String? = null,
    val lucesAltas: String? = null,
    val lucesBajas: String? = null,
    val lucesDemarcadorasDelanteras: String? = null,
    val lucesDemarcadorasTraseras: String? = null,
    val lucesIndicadoras: String? = null,
    val faroIzquierdo: String? = null,
    val faroDerecho: String? = null,
    val lucesDireccionalesDelanteras: String? = null,
    val lucesDireccionalesTraseras: String? = null,
    val llantasRinesDelanteros: String? = null,
    val llantasRinesTraseros: String? = null,
    val llantasMasasDelanteras: String? = null,
    val llantasMasasTraseras: String? = null,
    val llantasPresionDelanteraIzquierda: Double? = null,
    val llantasPresionDelanteraDerecha: Double? = null,
    val llantasPresionTraseraIzquierda1: Double? = null,
    val llantasPresionTraseraIzquierda2: Double? = null,
    val llantasPresionTraseraDerecha1: Double? = null,
    val llantasPresionTraseraDerecha2: Double? = null,
    val llantasProfundidadDelanteraIzquierda: Double? = null,
    val llantasProfundidadDelanteraDerecha: Double? = null,
    val llantasProfundidadTraseraIzquierda1: Double? = null,
    val llantasProfundidadTraseraIzquierda2: Double? = null,
    val llantasProfundidadTraseraDerecha1: Double? = null,
    val llantasProfundidadTraseraDerecha2: Double? = null,
    val llantasTuercasDelanteraIzquierda: String? = null,
    val llantasTuercasDelanteraIzquierdaFaltantes: Int? = null,
    val llantasTuercasDelanteraIzquierdaRotas: Int? = null,
    val llantasTuercasDelanteraDerecha: String? = null,
    val llantasTuercasDelanteraDerechaFaltantes: Int? = null,
    val llantasTuercasDelanteraDerechaRotas: Int? = null,
    val llantasTuercasTraseraIzquierda: String? = null,
    val llantasTuercasTraseraIzquierdaFaltantes: Int? = null,
    val llantasTuercasTraseraIzquierdaRotas: Int? = null,
    val llantasTuercasTraseraDerecha: String? = null,
    val llantasTuercasTraseraDerechaFaltantes: Int? = null,
    val llantasTuercasTraseraDerechaRotas: Int? = null,
    val llantasBirlosDelanteraIzquierdaCount: Int? = null,
    val llantasBirlosDelanteraIzquierdaSelected: String? = null,
    val llantasBirlosDelanteraDerechaCount: Int? = null,
    val llantasBirlosDelanteraDerechaSelected: String? = null,
    val llantasBirlosTraseraIzquierdaCount: Int? = null,
    val llantasBirlosTraseraIzquierdaSelected: String? = null,
    val llantasBirlosTraseraDerechaCount: Int? = null,
    val llantasBirlosTraseraDerechaSelected: String? = null,
    val llantasBirlosMediaIzquierdaCount: Int? = null,
    val llantasBirlosMediaIzquierdaSelected: String? = null,
    val llantasBirlosMediaDerechaCount: Int? = null,
    val llantasBirlosMediaDerechaSelected: String? = null,
    val direccionBrazoPitman: String? = null,
    val direccionManijasPuertas: String? = null,
    val direccionChavetas: String? = null,
    val direccionChavetasFaltantes: Int? = null,
    val aireFrenosCompresor: String? = null,
    val aireFrenosTanquesAire: String? = null,
    val aireFrenosTiempoCargaPsi: Double? = null,
    val aireFrenosTiempoCargaTiempo: Double? = null,
    val motorEmisionesHumo: String? = null,
    val motorEmisionesGobernado: String? = null,
    val otrosCajaDireccion: String? = null,
    val otrosDepositoAceite: String? = null,
    val otrosParabrisas: String? = null,
    val otrosLimpiaparabrisas: String? = null,
    val otrosJuego: String? = null,
    val otrosEscape: String? = null,
    val comentarioLuces: String? = null,
    val comentarioLlantas: String? = null,
    val comentarioDireccion: String? = null,
    val comentarioAireFrenos: String? = null,
    val comentarioMotorEmisiones: String? = null,
    val comentarioOtros: String? = null,
    val comentariosGenerales: String? = null,
)

