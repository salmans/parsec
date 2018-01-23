package tools

import org.junit.Test
import kotlin.test.assertEquals

class HelperKtTest {
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