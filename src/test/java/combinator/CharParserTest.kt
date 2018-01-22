package combinator

import org.junit.Assert.*

import org.junit.Test

class CharParserTest {
    @Test
    fun parse() {
        assertEquals('A', charA.parse("A".asSequence()).right()!!.first)
        assertEquals('A', charA.parse("ABC".asSequence()).right()!!.first)
        assertEquals("Expecting 'A' but '' was found.", charA.parse("".asSequence()).left()!!.message)
        assertEquals("Expecting 'A' but 'B' was found.", charA.parse("BC".asSequence()).left()!!.message)
    }
}