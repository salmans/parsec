package combinator

import org.junit.Assert.*
import org.junit.Test
import tools.Either

class OrCombinatorTest {
    @Test
    fun or() {
        assertEquals(Either.left('A'), (charA or charB)("AB".asSequence()).right()!!.first)
        assertEquals(Either.right('B'), (charA or charB)("BC".asSequence()).right()!!.first)
        assertEquals(Either.left('A'), (charA or charB)("A".asSequence()).right()!!.first)
        assertEquals(Either.right('A'), (charB or charA)("A".asSequence()).right()!!.first)
        assertEquals(Either.right('C'), (charA or charB or charC)("C".asSequence()).right()!!.first)
        assertEquals("Expecting one of 'A','B' but '' was found.", (charA or charB)("".asSequence()).left()!!.message)
        assertEquals("Expecting one of 'A','B' but 'C' was found.", (charA or charB)("C".asSequence()).left()!!.message)
    }
}
