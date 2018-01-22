package parsec

import org.junit.Assert.*
import org.junit.Test
import tools.Either

class OrCombinatorTest {
    @Test
    fun parse() {
        assertEquals(Either.left('A'), (charA or charB).parse("AB".asSequence()).right()!!.first)
        assertEquals(Either.right('B'), (charA or charB).parse("BC".asSequence()).right()!!.first)
        assertEquals(Either.left('A'), (charA or charB).parse("A".asSequence()).right()!!.first)
        assertEquals(Either.right('A'), (charB or charA).parse("A".asSequence()).right()!!.first)
        assertEquals(Either.right('C'), (charA or charB or charC).parse("C".asSequence()).right()!!.first)
        assertEquals("Expecting one of 'A','B' but '' was found.", (charA or charB).parse("".asSequence()).left()!!.message)
        assertEquals("Expecting one of 'A','B' but 'C' was found.", (charA or charB).parse("C".asSequence()).left()!!.message)
    }
}
