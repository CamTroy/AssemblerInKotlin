class InstructionParser {
    private val stackManager = StackManager()
    private val labelPattern = Regex("<([A-Za-z0-9_]+)")

    fun extractLabel(line: String): String? {
        val matcher = labelPattern.find(line)
        return matcher?.groupValues?.getOrNull(1)
    }

    fun parse(line: String): Instruction? {
        val cleanLine = line.substringBefore("<")
        val tokens = cleanLine.trim().split(Regex("[ ,#(){}]+")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return null

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

            OpCode.STMEA, OpCode.LDMEA -> {
                if (tokens.size < 3) return null

                val baseRegToken = tokens[1]
                val writeBack = baseRegToken.endsWith("!")
                val baseReg = parseRegister(baseRegToken.removeSuffix("!")) ?: return null

                val registersList = mutableListOf<Int>()
                for (i in 2 until tokens.size) {
                    val regToken = tokens[i]
                    if (regToken.contains("-")) {
                        val range = regToken.split("-")
                        if (range.size != 2) return null

                        val startReg = parseRegister(range[0]) ?: return null
                        val endReg = parseRegister(range[1]) ?: return null

                        for (regNum in startReg..endReg) {
                            registersList.add(regNum)
                        }
                    } else {
                        val reg = parseRegister(regToken) ?: return null
                        registersList.add(reg)
                    }
                }

                StackMultipleInstruction(opCode, baseReg, registersList, writeBack)
            }

            OpCode.STREA, OpCode.LDREA -> {
                if (tokens.size < 4) return null

                val rd = parseRegister(tokens[1]) ?: return null
                val baseRegToken = tokens[2]
                val writeBack = baseRegToken.endsWith("!")
                val baseReg = parseRegister(baseRegToken.removeSuffix("!")) ?: return null
                val offset = tokens[3].toIntOrNull() ?: return null

                StackSingleInstruction(opCode, baseReg, rd, offset, writeBack)
            }


            OpCode.BPL, OpCode.B, OpCode.BL -> {
                val target = tokens[1]
                if (target.startsWith(">")) {
                    val labelName = target.substring(1)
                    BranchWithLabelInstruction(opCode, labelName)
                } else {
                    val offset = target.removePrefix("0x").toInt(16)
                    BranchInstruction(opCode, offset)
                }
            }

            OpCode.BX -> {
                val rn = parseRegister(tokens[1]) ?: return null
                RegisterInstruction(opCode, 0, rn)
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