package net.eplusx.logger

import kotlin.test.Test
import kotlin.test.assertNotNull

class AppTest {
    @Test fun appHasAGreeting(): Unit {
        val classUnderTest = App()
        assertNotNull(classUnderTest.greeting, "app should have a greeting")
    }
}

// class AppTests : ShouldSpec({
//     context("Basic") {
//         should("Make a greeting") {
//             App().greeting shouldBe "hello?"
//         }
//     }
// })