
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
    val imm: Int,
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

data class BranchWithLabelInstruction(
    val opCode: OpCode,
    val label: String,
) : Instruction()

data class StackMultipleInstruction(
    val opCode: OpCode,
    val baseReg: Int,
    val registers: List<Int>,
    val writeBack: Boolean,
) : Instruction()

data class StackSingleInstruction(
    val opCode: OpCode,
    val baseReg: Int,
    val rd: Int,
    val offset: Int,
    val writeBack: Boolean,
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
    BPL(code = "BPL", binaryString = "1010"),
    BL(code = "BL", binaryString = "1011"),
    BX(code = "BX", binaryString = "0001"),
    STMEA(code = "STMEA", binaryString = "0100"),
    LDMEA(code = "LDMEA", binaryString = "0100"),
    STREA(code = "STREA", binaryString = "0100"),
    LDREA(code = "LDREA", binaryString = "0100");


    companion object {
        fun fromCode(code: String): OpCode? {
            return entries.find { it.code.equals(code, ignoreCase = true) }
        }
    }
}