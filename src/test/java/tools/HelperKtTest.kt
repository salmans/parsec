package tools

import org.junit.Test
import kotlin.test.assertEquals

class HelperKtTest {
    @Test
    fun identity() {
        run {
            assertEquals(42, identity(42) )
            assertEquals(10, identity(10) )
        }
    }

    @Test
    fun compose() {
        run {
            val f1: (Int) -> Int = { x -> x + 1 }
            val f2: (Int) -> Int = { x -> x * 2 }
            assertEquals(10, (f2 compose f1)(4) )
            assertEquals(9, (f1 compose f2)(4) )
        }
    }
}