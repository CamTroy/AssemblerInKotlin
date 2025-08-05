class InstructionParser {
    fun parse(line: String): Instruction? {
        val tokens = line.split(Regex("[ ,#]+"))
        val opCode = OpCode.fromCode(tokens[0]) ?: return null

        return when (opCode) {

            OpCode.MOVT, OpCode.MOVW -> {
                val rd = parseRegister(tokens[1]) ?: return null
                val imm = tokens[2].removePrefix("0x").toIntOrNull(16) ?: return null
                ImmediateInstruction(opCode, rd, imm)
            }

            else -> {
                val rd = parseRegister(tokens[1]) ?: return null
                val rn = parseRegister(tokens[2]) ?: return null
                RegisterInstruction(opCode, rd, rn)
            }
        }
    }

    private fun parseRegister(token: String): Int? {

        return if (token.startsWith("R", ignoreCase = true)) {
            token.drop(1).toIntOrNull()?.takeIf { it in 0..15 }
        } else null
    }
}