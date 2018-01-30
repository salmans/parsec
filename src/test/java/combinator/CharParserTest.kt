package combinator

import org.junit.Assert.assertEquals
import org.junit.Test

class CharParserTest {
    @Test
    fun char() {
        assertEquals('A', charA("A".asSequence()).first.right()!!)
        assertEquals('A', charA("ABC".asSequence()).first.right()!!)
        assertEquals("Expecting 'A' but '' was found.", charA("".asSequence()).first.left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", charA("BC".asSequence()).first.left()!!.message)
    }
}