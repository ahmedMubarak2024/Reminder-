package com.udacity.project4.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher

/*
NOTE this file should go into the main sources. Inject AppDispatchers wherever you need to reference dispatchers instead of
referencing them statically. Then they will be substitutable in your tests.
*/

@Suppress("PropertyName") // Made to match Dispatchers in platform for ease of change
interface AppDispatchers {
    val IO: CoroutineDispatcher
    val Main: MainCoroutineDispatcher
}

class ProductionDispatchers : AppDispatchers {
    override val IO: CoroutineDispatcher = Dispatchers.IO
    override val Main: MainCoroutineDispatcher = Dispatchers.Main
}