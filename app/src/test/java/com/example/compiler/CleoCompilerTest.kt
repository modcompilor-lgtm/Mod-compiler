package com.example.compiler

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CleoCompilerTest {

  @Test
  fun appendsTheCLEOTerminatorInsteadOfMainScmTerminator() {
    val result = CleoCompiler.compile("0001: wait 0")

    assertTrue(result is CompilationResult.Success)
    result as CompilationResult.Success
    assertArrayEquals(
      byteArrayOf(0x01, 0x00, 0x04, 0x00, 0x93.toByte(), 0x0A),
      result.bytecode
    )
  }

  @Test
  fun translatesLegacy004EToSafeCLEOTerminator() {
    val result = CleoCompiler.compile("004E: end_thread")

    assertTrue(result is CompilationResult.Success)
    result as CompilationResult.Success
    assertArrayEquals(byteArrayOf(0x93.toByte(), 0x0A), result.bytecode)
  }

  @Test
  fun variableLengthStringsDoNotReceiveAnExtraNullByte() {
    val result = CleoCompiler.compile("0ACA: show_text_box \"HELLO WORLD\"")

    assertTrue(result is CompilationResult.Success)
    result as CompilationResult.Success
    assertEquals(
      "CA 0A 0E 0B 48 45 4C 4C 4F 20 57 4F 52 4C 44 93 0A",
      result.hexDump
    )
  }

  @Test
  fun resolvesForwardLabelsAgainstTheScriptBase() {
    val result = CleoCompiler.compile(
      """
      0002: jump @END
      0001: wait 0
      :END
      0A93: end_custom_thread
      """.trimIndent()
    )

    assertTrue(result is CompilationResult.Success)
    result as CompilationResult.Success
    assertEquals(
      "02 00 01 F5 FF FF FF 01 00 04 00 93 0A",
      result.hexDump
    )
  }

  @Test
  fun rejectsUnknownUnquotedParametersInsteadOfEncodingThemAsStrings() {
    val result = CleoCompiler.compile("0001: wait not_a_number")

    assertTrue(result is CompilationResult.Failure)
    result as CompilationResult.Failure
    assertEquals(CompilerErrorType.INVALID_PARAMETERS, result.error.type)
  }

  @Test
  fun keepsCommentMarkersInsideQuotedText() {
    val result = CleoCompiler.compile("0ACA: show_text_box \"A; B // C\"")

    assertTrue(result is CompilationResult.Success)
    result as CompilationResult.Success
    assertTrue(result.hexDump.contains("41 3B 20 42 20 2F 2F 20 43"))
  }

  @Test
  fun acceptsBinaryIntegerLiterals() {
    val result = CleoCompiler.compile("0001: wait 0b1010")

    assertTrue(result is CompilationResult.Success)
    result as CompilationResult.Success
    assertEquals("01 00 04 0A 93 0A", result.hexDump)
  }


  @Test
  fun supportsSannyIfThenElseBlocks() {
    val result = CleoCompiler.compile(
      """
      if
        0@ > 0
      then
        0001: wait 0
      else
        0001: wait 1
      end
      0A93: end_custom_thread
      """.trimIndent()
    )

    assertTrue(result is CompilationResult.Success)
    result as CompilationResult.Success
    assertTrue(result.hexDump.contains("4D 00"))
    assertTrue(result.hexDump.contains("02 00"))
  }

  @Test
  fun supportsAndAndOrConditionBlocks() {
    val andResult = CleoCompiler.compile(
      """
      if and
        0@ > 0
        1@ == 1
      then
        0001: wait 0
      end
      0A93: end_custom_thread
      """.trimIndent()
    )
    val orResult = CleoCompiler.compile(
      """
      if or
        0@ > 0
        1@ == 1
      then
        0001: wait 0
      end
      0A93: end_custom_thread
      """.trimIndent()
    )

    assertTrue(andResult is CompilationResult.Success)
    assertTrue(orResult is CompilationResult.Success)
  }


  @Test
  fun supportsNamedLocalDeclarationsInExpressionsAndCommands() {
    val result = CleoCompiler.compile(
      """
      int counter = 10
      float distance
      distance = 1.5
      if
        counter > 0
      then
        0001: wait counter
      end
      0A93: end_custom_thread
      """.trimIndent()
    )

    assertTrue(result is CompilationResult.Success)
    result as CompilationResult.Success
    assertTrue(result.bytecode.isNotEmpty())
  }

  @Test
  fun supportsTypedGlobalDeclarations() {
    val result = CleoCompiler.compile(
      """
      ${': int = 7
      0001: wait 4score
      0A93: end_custom_thread
      """.trimIndent()
    )

    assertTrue(result is CompilationResult.Success)
  }

}
}score: int = 7
      0001: wait ${'
      0A93: end_custom_thread
      """.trimIndent()
    )

    assertTrue(result is CompilationResult.Success)
  }

}
}score
      0A93: end_custom_thread
      """.trimIndent()
    )

    assertTrue(result is CompilationResult.Success)
  }

}
