class InstructionParser {
    fun parse(line: String): Instruction? {
        val tokens = line.trim().split(Regex("[ ,#()]+")).filter { it.isNotEmpty() }
        val opCode = OpCode.fromCode(tokens[0]) ?: return null

        return when (opCode) {
            OpCode.ADD -> {
                if (tokens.size != 4) return null
                val rd = parseRegister(tokens[1]) ?: return null
                val rn = parseRegister(tokens[2]) ?: return null
                val imm = tokens[3].removePrefix("0x").toIntOrNull(16) ?: tokens[3].toIntOrNull() ?: return null
                ImmediateInstruction(opCode, rd, rn, imm)
            }

            OpCode.SUBS -> {
                if (tokens.size != 4) return null
                val rd = parseRegister(tokens[1]) ?: return null
                val rn = parseRegister(tokens[2]) ?: return null
                val imm = tokens[3].removePrefix("0x").toIntOrNull(16) ?: tokens[3].toIntOrNull() ?: return null
                ImmediateInstruction(opCode, rd, rn, imm)
            }

            OpCode.ORR -> {
                if (tokens.size != 4) return null
                val rd = parseRegister(tokens[1]) ?: return null
                val rn = parseRegister(tokens[2]) ?: return null
                val imm = tokens[3].removePrefix("0x").toIntOrNull(16) ?: tokens[3].toIntOrNull() ?: return null
                ImmediateInstruction(opCode, rd, rn, imm)
            }

            OpCode.MOVT, OpCode.MOVW -> {
                val rd = parseRegister(tokens[1]) ?: return null
                val imm = tokens[2].removePrefix("0x").toIntOrNull(16) ?: tokens[2].toIntOrNull() ?: return null
                ImmediateInstruction(opCode, rd, null, imm)
            }

            OpCode.LDR, OpCode.STR -> {
                val rd = parseRegister(tokens[1]) ?: return null
                val rn = parseRegister(tokens[2]) ?: return null
                MemoryInstruction(opCode, rd, rn)
            }

            OpCode.BPL, OpCode.B -> {
                val offset = tokens[1].removePrefix("0x").toInt(16)
                BranchInstruction(opCode, offset)
            }

            else -> {
                val rd = parseRegister(tokens[1]) ?: return null
                val secondOp = tokens[2]
                if (secondOp.startsWith("R", ignoreCase = true)) {
                    val rn = parseRegister(secondOp) ?: return null
                    RegisterInstruction(opCode, rd, rn)
                } else {
                    val imm = secondOp.removePrefix("0x").toIntOrNull(16) ?: secondOp.toIntOrNull() ?: return null
                    ImmediateInstruction(opCode, rd, rd, imm)
                }
            }
        }
    }

    private fun parseRegister(token: String): Int? {
        return if (token.startsWith("R", ignoreCase = true)) {
            token.drop(1).toIntOrNull()?.takeIf { it in 0..15 }
        } else null
    }
}