
sealed class Instruction()

data class RegisterInstruction(
    val opCode: OpCode,
    val rd: Int,
    val rn: Int,
) : Instruction()

data class ImmediateInstruction(
    val opCode: OpCode,
    val rd: Int,
    val rn: Int?,
    val immediate: Int,
) : Instruction()


data class MemoryInstruction(
    val opCode: OpCode,
    val rd: Int,
    val rn: Int,
) : Instruction()

data class BranchInstruction(
    val opCode: OpCode,
    val offset: Int,
) : Instruction()

enum class OpCode(val code: String, val binaryString: String) {
    ADD(code = "ADD", binaryString = "0010"),
    SUB(code = "SUB", binaryString = "0010"),
    SUBS(code = "SUBS", binaryString = "0010"),
    ORR(code = "ORR", binaryString = "0011"),
    MOVW(code = "MOVW", binaryString = "0011"),
    MOVT(code = "MOVT", binaryString = "0011"),
    LDR(code = "LDR", binaryString = "0100"),
    STR(code = "STR", binaryString = "0100"),
    B(code = "B", binaryString = "1010"),
    BPL(code = "BPL", binaryString = "1010");

    companion object {
        fun fromCode(code: String): OpCode? {
            return entries.find { it.code.equals(code, ignoreCase = true) }
        }
    }
}