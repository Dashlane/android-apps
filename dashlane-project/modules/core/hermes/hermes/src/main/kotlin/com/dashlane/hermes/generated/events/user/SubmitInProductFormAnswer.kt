package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.FormName
import com.dashlane.hermes.generated.definitions.PossibleFormAnswers
import javax.annotation.processing.Generated

@Generated
data class SubmitInProductFormAnswer(
    val chosenAnswerList: List<PossibleFormAnswers>? = null,
    val answerList: List<PossibleFormAnswers>? = null,
    val formName: FormName,
) : TrackingLog {
    override val name
        get() = "submit_in_product_form_answer"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["chosen_answer_list"] = chosenAnswerList
        collector["answer_list"] = answerList
        collector["form_name"] = formName
    }
}