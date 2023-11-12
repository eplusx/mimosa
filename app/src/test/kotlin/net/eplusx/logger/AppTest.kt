package net.eplusx.logger

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

 class AppTests : ShouldSpec({
     context("Basic") {
         should("Make a greeting") {
             App().greeting shouldBe "Hello World!"
         }
     }
 })