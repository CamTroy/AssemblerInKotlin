import java.io.File

fun main() {
    val parser = InstructionParser()
    val labelManager = LabelManager()
    val filePath = "src/instructions.txt"
    val newFilePath = "src/kernel7.img"

    val instructions = readInstructions(filePath, mutableListOf())

    var instructionIndex = 0
    instructions.forEach { line ->
        val label = parser.extractLabel(line)
        if (label != null) {
            labelManager.addLabel(label, instructionIndex)
        }
        if (line.trim().isNotEmpty() && !line.trim().startsWith("//")) {
            instructionIndex++
        }
    }

    println("Found labels:")
    labelManager.getAllLabels().forEach { (name, addr) ->
        println("$name -> $addr")
    }

    val parsedInstructions = instructions.mapNotNull { line ->
        if (line.trim().isEmpty() || line.trim().startsWith("//")) {
            null
        } else {
            parser.parse(line)
        }
    }

    val binaryInstructions = mutableListOf<String>()

    for (i in parsedInstructions.indices) {
        val instruction = parsedInstructions[i]

        val binary = when (instruction) {
            is BranchWithLabelInstruction -> {
                val targetAddress = labelManager.getLabel(instruction.label)
                    ?: throw IllegalStateException("Unknown label: ${instruction.label}")

                val currentInstructionAddr = i
                val relativeOffset = targetAddress - currentInstructionAddr - 2

                encodeBranchWithOffset(instruction.opCode, relativeOffset)
            }
            else -> encodeToBinary(instruction)
        }

        binaryInstructions.add(binary)
    }

    val content = buildString {
        binaryInstructions.forEach { binary ->
            println(binary)
            println("${binaryToHex(binary)} -> ${flipBytes(binaryToHex(binary))}")
            println()
            append(flipBytes(binaryToHex(binary)))
        }
    }

    val byteArr: ByteArray = decodeHex(content)

    File(newFilePath).writeBytes(byteArr)
}

fun encodeBranchWithOffset(opCode: OpCode, offset: Int): String {
    val cond = when (opCode) {
        OpCode.BPL -> "0101"
        else -> "1110"
    }

    val op = "101"
    val link = when (opCode) {
        OpCode.BL -> "1"
        else -> "0"
    }

    val offsetBinary = Integer.toBinaryString(offset and 0xFFFFFF)
        .takeLast(24)
        .padStart(24, if (offset < 0) '1' else '0')

    return cond + op + link + offsetBinary
}

fun binaryToHex(binary: String): String {
    val paddedBinary = binary.padStart((binary.length + 3) / 4 * 4, '0')

    return paddedBinary.chunked(4).joinToString("") { chunk ->
        chunk.toInt(2).toString(16).uppercase()
    }
}

fun encodeToBinary(instruction: Instruction): String {
    return when (instruction) {
        is RegisterInstruction -> {
            if (instruction.opCode == OpCode.BX) {
                val cond = "1110"
                val fixed = "000100101111111111110001"
                val rn = instruction.rn.toString(2).padStart(4, '0')

                cond + fixed + rn

            } else {
                val cond = "1110"
                cond + "0010" + "1000" + instruction.rn.toString(2).padStart(4, '0') +
                        instruction.rd.toString(2).padStart(4, '0') + "000000000000"
            }
        }

        is ImmediateInstruction -> {
            when (instruction.opCode) {
                OpCode.MOVW -> {
                    val cond = "1110"
                    val op = "0011"
                    val imm4 = (instruction.imm shr 12).toString(2).padStart(4, '0')
                    val rd = instruction.rd.toString(2).padStart(4, '0')
                    val imm12 = (instruction.imm and 0xFFF).toString(2).padStart(12, '0')
                    cond + op + "0000" + imm4 + rd + imm12
                }

                OpCode.MOVT -> {
                    val cond = "1110"
                    val op = "0011"
                    val imm4 = (instruction.imm shr 12).toString(2).padStart(4, '0')
                    val rd = instruction.rd.toString(2).padStart(4, '0')
                    val imm12 = (instruction.imm and 0xFFF).toString(2).padStart(12, '0')
                    cond + op + "0100" + imm4 + rd + imm12
                }

                OpCode.ADD -> {
                    "1110" + "0010" + "1000" +
                            (instruction.rn?.toString(2)?.padStart(4, '0') ?: "0000") +
                            instruction.rd.toString(2).padStart(4, '0') +
                            instruction.imm.toString(2).padStart(12, '0')
                }

                OpCode.SUBS -> {
                    val cond = "1110"
                    val op = "00"
                    val i = "1"
                    val opcode = "0010"
                    val s = "1"
                    val rn = instruction.rn?.toString(2)?.padStart(4, '0') ?: "0000"
                    val rd = instruction.rd.toString(2).padStart(4, '0')
                    val imm = instruction.imm.toString(2).padStart(12, '0')
                    cond + op + i + opcode + s + rn + rd + imm
                }

                OpCode.ORR -> {
                    val cond = "1110"
                    val op = "00"
                    val i = "1"
                    val opcode = "1100"
                    val s = "0"
                    val rn = instruction.rn?.toString(2)?.padStart(4, '0') ?: "0000"
                    val rd = instruction.rd.toString(2).padStart(4, '0')
                    val imm = instruction.imm.toString(2).padStart(12, '0')
                    cond + op + i + opcode + s + rn + rd + imm
                }

                else -> ""
            }
        }

        is MemoryInstruction -> {
            val cond = "1110"
            val op = "01"
            val i = "0"
            val p = "0"
            val u = "0"
            val b = "0"
            val w = "0"
            val l = if (instruction.opCode == OpCode.LDR) "1" else "0"
            val rn = instruction.rn.toString(2).padStart(4, '0')
            val rd = instruction.rd.toString(2).padStart(4, '0')
            val offset = "000000000000"

            cond + op + i + p + u + b + w + l + rn + rd + offset
        }

        is BranchInstruction -> {
            when (instruction.opCode) {
                OpCode.BPL -> {
                    val cond = "0101"
                    val op = "101"
                    val link = "0"
                    val offsetBinary = Integer.toBinaryString(instruction.offset)
                        .takeLast(24)
                        .padStart(24, '1')
                    cond + op + link + offsetBinary
                }

                OpCode.B -> {
                    val cond = "1110"
                    val op = "101"
                    val link = "0"
                    val offsetBinary = Integer.toBinaryString(instruction.offset)
                        .takeLast(24)
                        .padStart(24, '1')
                    cond + op + link + offsetBinary
                }

                OpCode.BL -> {
                    val cond = "1110"
                    val op = "101"
                    val link = "1"
                    val offsetBinary = Integer.toBinaryString(instruction.offset)
                        .takeLast(24)
                        .padStart(24, '1')
                    cond + op + link + offsetBinary
                }

                else -> ""
            }
        }

        is StackMultipleInstruction -> {
            val cond = "1110"
            val op = "100"
            val p = "0"
            val u = "1"
            val s = "0"
            val w = if (instruction.writeBack) "1" else "0"
            val l = if (instruction.opCode == OpCode.LDMEA) "1" else "0"
            val rn = instruction.baseReg.toString(2).padStart(4, '0')

            val registerList = (0..15).joinToString("") { regNum ->
                if (instruction.registers.contains(regNum)) "1" else "0"
            }

            cond + op + p + u + s + w + l + rn + registerList
        }

        is StackSingleInstruction -> {
            val cond = "1110"
            val op = "01"
            val i = "0"
            val p = "1"
            val u = "1"
            val b = "0"
            val w = if (instruction.writeBack) "1" else "0"
            val l = if (instruction.opCode == OpCode.LDREA) "1" else "0"
            val rn = instruction.baseReg.toString(2).padStart(4, '0')
            val rd = instruction.rd.toString(2).padStart(4, '0')
            val offset = instruction.offset.toString(2).padStart(12, '0')

            cond + op + i + p + u + b + w + l + rn + rd + offset
        }

        else -> ""
    }
}

fun readInstructions(fileName: String, instructions: MutableList<String>): MutableList<String> {
    File(fileName).forEachLine { instructions.add(it) }
    return instructions
}

fun flipBytes(hex: String): String {
    return hex.chunked(2)
        .reversed()
        .joinToString("")
}

fun decodeHex(hex: String): ByteArray {
    return hex.chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}