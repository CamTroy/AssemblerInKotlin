
sealed class Instruction()

data class RegisterInstruction(
    val opCode: OpCode,
    val rd: Int,
    val rn: Int,
) : Instruction()

data class ImmediateInstruction(
    val opCode: OpCode,
    val rd: Int,
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
    ADD(code = "ADD",  binaryString = "0100"),
    SUB(code = "SUB", binaryString = "0010"),
    SUBS(code = "SUBS", binaryString = "0010"), // Added SUBS
    ORR(code = "ORR", binaryString = "1100"),
    MOVW(code = "MOVW", binaryString = "00110000"),
    MOVT(code = "MOVT", binaryString = "00110100"),
    LDR(code = "LDR", binaryString = "01100001"), // Added LDR
    STR(code = "STR", binaryString = "01100000"), // Added STR
    B(code = "B", binaryString = "10100000"),
    BPL(code = "BPL", binaryString = "01010"); // Added BPL

    companion object {
        fun fromCode(code: String): OpCode? {
            return entries.find { it.code.equals(code, ignoreCase = true) }
        }
    }
}