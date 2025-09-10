package com.dashlane.udf

import com.dashlane.udf.StateMachine.Graph.State

class StateMachine<STATE : Any, EVENT : Any, SIDE_EFFECT : Any> private constructor(
    private val graph: Graph<STATE, EVENT, SIDE_EFFECT>,
) {
    val initialState: STATE = graph.initialState

    fun transition(previousState: STATE, event: EVENT): Transition.Valid<STATE, EVENT, SIDE_EFFECT>? {
        return synchronized(this) {
            when (val transition = previousState.getTransition(event)) {
                is Transition.Valid -> transition
                is Transition.Invalid -> null
            }
        }
    }

    private fun STATE.getTransition(event: EVENT): Transition<STATE, EVENT, SIDE_EFFECT> {
        for ((eventMatcher, createTransitionTo) in getDefinition().transitions) {
            if (eventMatcher.matches(event)) {
                val (toState, sideEffect) = createTransitionTo(this, event)
                return Transition.Valid(this, event, toState, sideEffect)
            }
        }
        return Transition.Invalid(this, event)
    }

    private fun STATE.getDefinition() = graph.stateDefinitions
        .filter { it.key.matches(this) }
        .map { it.value }
        .firstOrNull() ?: error("Missing definition for state ${this.javaClass.simpleName}!")

    sealed class Transition<out STATE : Any, out EVENT : Any, out SIDE_EFFECT : Any> {
        abstract val fromState: STATE
        abstract val event: EVENT

        @ConsistentCopyVisibility
        data class Valid<out STATE : Any, out EVENT : Any, out SIDE_EFFECT : Any> internal constructor(
            override val fromState: STATE,
            override val event: EVENT,
            val toState: STATE,
            val sideEffect: SIDE_EFFECT?,
        ) : Transition<STATE, EVENT, SIDE_EFFECT>()

        @ConsistentCopyVisibility
        data class Invalid<out STATE : Any, out EVENT : Any, out SIDE_EFFECT : Any> internal constructor(
            override val fromState: STATE,
            override val event: EVENT,
        ) : Transition<STATE, EVENT, SIDE_EFFECT>()
    }

    data class Graph<STATE : Any, EVENT : Any, SIDE_EFFECT : Any>(
        val initialState: STATE,
        val stateDefinitions: Map<Matcher<STATE, STATE>, State<STATE, EVENT, SIDE_EFFECT>>,
    ) {
        @ConsistentCopyVisibility
        data class State<STATE : Any, EVENT : Any, SIDE_EFFECT : Any> internal constructor(
            val transitions: LinkedHashMap<Matcher<EVENT, EVENT>, (STATE, EVENT) -> TransitionTo<STATE, SIDE_EFFECT>> = linkedMapOf(),
        ) {
            @ConsistentCopyVisibility
            data class TransitionTo<out STATE : Any, out SIDE_EFFECT : Any> internal constructor(
                val toState: STATE,
                val sideEffect: SIDE_EFFECT?,
            )
        }
    }

    class Matcher<T : Any, out R : T> private constructor(private val clazz: Class<R>) {
        private val predicates = mutableListOf<(T) -> Boolean>({ clazz.isInstance(it) })

        fun matches(value: T) = predicates.all { it(value) }

        companion object {
            fun <T : Any, R : T> any(clazz: Class<R>): Matcher<T, R> = Matcher(clazz)

            inline fun <T : Any, reified R : T> any(): Matcher<T, R> = any(R::class.java)
        }
    }

    class GraphBuilder<STATE : Any, EVENT : Any, SIDE_EFFECT : Any> {
        private lateinit var initialState: STATE
        private val stateDefinitions = LinkedHashMap<Matcher<STATE, STATE>, State<STATE, EVENT, SIDE_EFFECT>>(emptyMap())

        fun initialState(initialState: STATE) {
            this.initialState = initialState
        }

        fun <S : STATE> state(
            stateMatcher: Matcher<STATE, S>,
            init: StateDefinitionBuilder<S>.() -> Unit,
        ) {
            stateDefinitions[stateMatcher] = StateDefinitionBuilder<S>().apply(init).build()
        }

        inline fun <reified S : STATE> state(noinline init: StateDefinitionBuilder<S>.() -> Unit) {
            state(Matcher.any(), init)
        }

        fun build(): Graph<STATE, EVENT, SIDE_EFFECT> {
            return Graph(initialState, stateDefinitions.toMap())
        }

        inner class StateDefinitionBuilder<S : STATE> {
            private val stateDefinition = State<STATE, EVENT, SIDE_EFFECT>()

            inline fun <reified E : EVENT> any(): Matcher<EVENT, E> = Matcher.any()

            fun <E : EVENT> on(
                eventMatcher: Matcher<EVENT, E>,
                createTransitionTo: S.(E) -> State.TransitionTo<STATE, SIDE_EFFECT>,
            ) {
                stateDefinition.transitions[eventMatcher] = { state, event ->
                    @Suppress("UNCHECKED_CAST")
                    createTransitionTo((state as S), event as E)
                }
            }

            inline fun <reified E : EVENT> on(
                noinline createTransitionTo: S.(E) -> State.TransitionTo<STATE, SIDE_EFFECT>,
            ) {
                return on(any(), createTransitionTo)
            }

            fun build() = stateDefinition

            fun S.transitionTo(state: STATE, sideEffect: SIDE_EFFECT? = null) = State.TransitionTo(state, sideEffect)

            fun S.noTransition() = State.TransitionTo(this, null)

            fun S.sideEffect(sideEffect: SIDE_EFFECT? = null) = transitionTo(this, sideEffect)
        }
    }

    companion object {
        fun <STATE : Any, EVENT : Any, SIDE_EFFECT : Any> create(
            init: GraphBuilder<STATE, EVENT, SIDE_EFFECT>.() -> Unit,
        ): StateMachine<STATE, EVENT, SIDE_EFFECT> {
            return StateMachine(GraphBuilder<STATE, EVENT, SIDE_EFFECT>().apply(init).build())
        }
    }
}