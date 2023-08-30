package demo.scsc

data class TestScenario<E: Any, R>(val name: String, val events: List<E>, val result: R?)

inline fun <reified P: Any, T: Any> TestScenario<T, *>.appliedTo(projection: P) {
    events.forEach { event ->
        projection::class.java.methods
            .find { it.name == "on" && it.parameterTypes.firstOrNull() == event::class.java }
            ?.invoke(projection, event)
    }
}

