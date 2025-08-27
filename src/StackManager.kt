
class StackManager {
    private var stackPointer = 0

    private val stackValues = mutableMapOf<String, Int>()

    private var currentFrameSize = 0

    fun pushValue(name: String, value: Int) {
        stackValues[name] = value
        stackPointer -= 4
        currentFrameSize += 4
    }

    fun pushRegisters(registers: List<String>) {
        registers.forEach { reg ->
            pushValue(reg, 0)
        }
    }

    fun popValue(name: String): Int? {
        val value = stackValues[name]
        if (value != null) {
            stackValues.remove(name)
            stackPointer += 4
            currentFrameSize -= 4
        }
        return value
    }

    fun popRegisters(registers: List<String>) {
        registers.forEach { reg ->
            popValue(reg)
        }
    }

    fun storeAtOffset(name: String, value: Int, offset: Int) {
        stackValues["$name@$offset"] = value
    }

    fun loadFromOffset(name: String, offset: Int): Int? {
        return stackValues["$name@$offset"]
    }

    fun getStackPointer(): Int {
        return stackPointer
    }

    fun getCurrentFrameSize(): Int {
        return currentFrameSize
    }

    fun resetStack() {
        stackValues.clear()
        currentFrameSize = 0
    }
}