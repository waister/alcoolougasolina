package br.com.gazoza.alcoolougasolina.utils

import org.junit.After
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
abstract class BaseRobolectricTest {

    @After
    fun tearDownKoin() {
        stopKoin()
    }
}
