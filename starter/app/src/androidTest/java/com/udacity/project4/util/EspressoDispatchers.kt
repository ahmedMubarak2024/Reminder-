package com.udacity.project4.util

import androidx.test.espresso.idling.CountingIdlingResource
import com.udacity.project4.utils.AppDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlin.coroutines.CoroutineContext

/*
NOTE this file should go into the androidTest sources, not main. In tests you can substitute the dispatchers that
you inject -- you do inject your dispatchers don't you? ;) -- with these ones
*/

class EspressoDispatchers : AppDispatchers {
    override val IO = EspressoTrackedDispatcher(Dispatchers.IO)
    override val Main = EspressoTrackedMainDispatcher(Dispatchers.Main)
}

fun delegateDispatchWithCounting(
    delegateDispatcher: CoroutineDispatcher,
    context: CoroutineContext,
    block: Runnable,
    idlingResource: CountingIdlingResource
) {
    idlingResource.increment()
    delegateDispatcher.dispatch(context, Runnable {
        try {
            block.run()
        } finally {
            idlingResource.decrement()
        }
    })
}

/**
 * Decorates [CoroutineDispatcher] adding a [CountingIdlingResource]. Based on [https://github.com/Kotlin/kotlinx.coroutines/issues/242].
 */
class EspressoTrackedDispatcher(private val delegateDispatcher: CoroutineDispatcher) :
    CoroutineDispatcher(), DispatcherWithIdlingResource {
    override val idlingResource: CountingIdlingResource =
        CountingIdlingResource("EspressoTrackedDispatcher for $delegateDispatcher")

    override fun dispatch(context: CoroutineContext, block: Runnable) =
        delegateDispatchWithCounting(delegateDispatcher, context, block, idlingResource)
}

/**
 * Decorates [MainCoroutineDispatcher] adding a [CountingIdlingResource]. Based on [https://github.com/Kotlin/kotlinx.coroutines/issues/242].
 * The main dispatcher is a totally different class so we have to duplicate EspressoTrackedDispatcher to provide a dispatcher of that type too.
 */
class EspressoTrackedMainDispatcher(private val delegateDispatcher: MainCoroutineDispatcher) :
    MainCoroutineDispatcher(), DispatcherWithIdlingResource {
    override val idlingResource: CountingIdlingResource =
        CountingIdlingResource("EspressoTrackedMainDispatcher for $delegateDispatcher")

    override val immediate: MainCoroutineDispatcher =
        if (delegateDispatcher.immediate === delegateDispatcher) this else EspressoTrackedMainDispatcher(
            delegateDispatcher.immediate
        )

    override fun dispatch(context: CoroutineContext, block: Runnable) =
        delegateDispatchWithCounting(delegateDispatcher, context, block, idlingResource)
}