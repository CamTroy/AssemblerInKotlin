
fun main() {
    val parser = InstructionParser()

    // Example usage
    val testInstructions = listOf(
        "ADD R1, R2",
        "MOVW R0, #0x1234",
        "SUB R3, R4"
    )

    testInstructions.forEach { line ->
        parser.parse(line)?.let { instruction ->
            val binary = encodeToBinary(instruction)
            val hex = binaryToHex(binary)
            println("Instruction: $line")
            println("Binary: $binary")
            println("Hex: $hex")
            println()
        }
    }
}

fun encodeToBinary(instruction: Instruction): String {
    return when (instruction) {
        is RegisterInstruction -> {
            val cond = "1110"
            val op = instruction.opCode.binaryString.padStart(4, '0')
            val rn = instruction.rn.toString(2).padStart(4, '0')
            val rd = instruction.rd.toString(2).padStart(4, '0')
            "$cond$op$rn$rd".replace(" ", "") // Remove spaces for consistent formatting
        }
        is ImmediateInstruction -> {
            val cond = "1110"
            val op = instruction.opCode.binaryString.padStart(9, '0')
            val rd = instruction.rd.toString(2).padStart(4, '0')
            val imm = instruction.immediate.toString(2).padStart(16, '0')
            val imm4 = imm.take(4)
            val imm12 = imm.drop(4)
            "$cond$op$imm4$rd$imm12".replace(" ", "") // Remove spaces for consistent formatting
        }
    }
}

fun binaryToHex(binary: String): String {
    val paddedBinary = binary.padStart((binary.length + 3) / 4 * 4, '0')

    return paddedBinary.chunked(4).joinToString("") { chunk ->
        chunk.toInt(2).toString(16).uppercase()
    }
}