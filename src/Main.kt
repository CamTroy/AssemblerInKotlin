import java.io.File

fun main() {
    val parser = InstructionParser()
    val filePath = "src/instructions.txt"
    val newFilePath = "src/kernel7.img"

    val instructions = readInstructions(filePath, mutableListOf())

//    val instructions = listOf( // Test instructions
//        "MOVW R4, 0",
//        "MOVT R4, 0x3F20",
//        "ADD R2, R4, 0x08",
//        "LDR R3, (R2)",
//        "ORR R3, R3, 0x00000008",
//        "STR R3, (R2)",
//        "ADD R3, R4, 0x1C",
//        "MOVW R2, 0x0000",
//        "MOVT R2, 0x0020",
//        "STR R2, (R3)",
//        "MOVW R5, 0x4240",
//        "MOVT R5, 0x000F",
//        "SUBS R5, R5, 1",
//        "BPL 0xFFFD",
//        "B 0xFFEE"
//    )

    val binaryInstructions = instructions.mapNotNull { line ->
        parser.parse(line)?.let { instruction ->
            encodeToBinary(instruction)
        }
    }

    val content = buildString {
        binaryInstructions.forEach { binary ->
            println(binary)
            println("${binaryToHex(binary)} -> ${flipBytes(binaryToHex(binary))}")
            println()
            append(flipBytes(binaryToHex(binary)))
        }
    }
    File(newFilePath).writeText(content)
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
            val cond = "1110"
            cond + "0010" + "1000" + instruction.rn.toString(2).padStart(4, '0') +
                    instruction.rd.toString(2).padStart(4, '0') + "000000000000"
        }

        is ImmediateInstruction -> {
            when (instruction.opCode) {
                OpCode.MOVW -> {
                    val cond = "1110"
                    val op = "0011"
                    val imm4 = (instruction.immediate shr 12).toString(2).padStart(4, '0')
                    val rd = instruction.rd.toString(2).padStart(4, '0')
                    val imm12 = (instruction.immediate and 0xFFF).toString(2).padStart(12, '0')
                    cond + op + "0000" + imm4 + rd + imm12
                }
                OpCode.MOVT -> {
                    val cond = "1110"
                    val op = "0011"
                    val imm4 = (instruction.immediate shr 12).toString(2).padStart(4, '0')
                    val rd = instruction.rd.toString(2).padStart(4, '0')
                    val imm12 = (instruction.immediate and 0xFFF).toString(2).padStart(12, '0')
                    cond + op + "0100" + imm4 + rd + imm12
                }
                OpCode.ADD -> {
                    "1110" + "0010" + "1000" +
                            (instruction.rn?.toString(2)?.padStart(4, '0') ?: "0000") +
                            instruction.rd.toString(2).padStart(4, '0') +
                            instruction.immediate.toString(2).padStart(12, '0')
                }
                OpCode.SUBS -> {
                    val cond = "1110"
                    val op = "00"
                    val i = "1"
                    val opcode = "0010"
                    val s = "1"
                    val rn = instruction.rn?.toString(2)?.padStart(4, '0') ?: "0000"
                    val rd = instruction.rd.toString(2).padStart(4, '0')
                    val imm = instruction.immediate.toString(2).padStart(12, '0')
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
                    val imm = instruction.immediate.toString(2).padStart(12, '0')
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
                else -> ""
            }
        }
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