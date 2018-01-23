package combinator

import org.junit.Assert.assertEquals
import org.junit.Test

class CharParserTest {
    @Test
    fun parse() {
        assertEquals('A', charA("A".asSequence()).right()!!.first)
        assertEquals('A', charA("ABC".asSequence()).right()!!.first)
        assertEquals("Expecting 'A' but '' was found.", charA("".asSequence()).left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", charA("BC".asSequence()).left()!!.message)
    }
}