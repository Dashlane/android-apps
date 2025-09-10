package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ProfilingFamiliarityPossibleAnswers
import com.dashlane.hermes.generated.definitions.ProfilingFeaturesPossibleAnswers
import com.dashlane.hermes.generated.definitions.ProfilingStep
import com.dashlane.hermes.generated.definitions.ProfilingTeamSizePossibleAnswers
import com.dashlane.hermes.generated.definitions.ProfilingUseCasePossibleAnswers
import javax.annotation.processing.Generated

@Generated
data class SubmitAdminProfilingQuestions(
    val featuresChoiceList: List<ProfilingFeaturesPossibleAnswers>? = null,
    val teamSizeChoiceList: List<ProfilingTeamSizePossibleAnswers>? = null,
    val isProfilingComplete: Boolean,
    val profilingStep: ProfilingStep,
    val useCaseAnswerList: List<ProfilingUseCasePossibleAnswers>? = null,
    val useCaseChoiceList: List<ProfilingUseCasePossibleAnswers>? = null,
    val featuresAnswerList: List<ProfilingFeaturesPossibleAnswers>? = null,
    val adminFamiliarityAnswerList: List<ProfilingFamiliarityPossibleAnswers>? = null,
    val teamSizeAnswerList: List<ProfilingTeamSizePossibleAnswers>? = null,
    val adminFamiliarityChoiceList: List<ProfilingFamiliarityPossibleAnswers>? = null,
) : TrackingLog {
    override val name
        get() = "submit_admin_profiling_questions"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["features_choice_list"] = featuresChoiceList
        collector["team_size_choice_list"] = teamSizeChoiceList
        collector["is_profiling_complete"] = isProfilingComplete
        collector["profiling_step"] = profilingStep
        collector["use_case_answer_list"] = useCaseAnswerList
        collector["use_case_choice_list"] = useCaseChoiceList
        collector["features_answer_list"] = featuresAnswerList
        collector["admin_familiarity_answer_list"] = adminFamiliarityAnswerList
        collector["team_size_answer_list"] = teamSizeAnswerList
        collector["admin_familiarity_choice_list"] = adminFamiliarityChoiceList
    }
}