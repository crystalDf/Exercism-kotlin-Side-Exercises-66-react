class Reactor<T>() {

    // Your compute cell's addCallback method must return an object
    // that implements the Subscription interface.
    interface Subscription {
        fun cancel()
    }

    open inner class InputCell(i: T) {

        val listeners = mutableListOf<ComputeCell>()

        open var value = i
            set(newValue) {
                if (field != newValue) {
                    field = newValue
                    listeners.forEach { it.value }
                }
            }

        fun addListener(computeCell: ComputeCell) = listeners.add(computeCell)
    }

    inner class ComputeCell(private vararg val inputCells: InputCell,
                            private val compute: (List<T>) -> T) :
            InputCell(compute(inputCells.map { it.value })) {

        private val callbackList = mutableListOf<(T) -> Unit>()

        override var value = super.value
                get() {
                    val newValue = compute(inputCells.map { it.value })
                    if (field != newValue) {
                        field = newValue
                        listeners.forEach { it.value }
                        callbackList.forEach { it(field) }
                    }
                    return field
                }

        init {
            inputCells.forEach { it.addListener(this) }
        }

        fun addCallback(callback: (T) -> Unit): Subscription =
                callback.let {
                    callbackList.add(it)
                    object : Subscription {
                        override fun cancel() {
                            callbackList.remove(it)
                        }
                    }
                }
    }

}
