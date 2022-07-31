package com.udacity.project4.util

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit test rule that registers an idling resource for each [DispatcherWithIdlingResource] given
 *
 */
class DispatchersIdlingResourceRule(private vararg val dispatchers: DispatcherWithIdlingResource) :
    TestWatcher() {
    override fun starting(description: Description?) {
        dispatchers.forEach {
            IdlingRegistry.getInstance().register(it.idlingResource)
        }
    }

    override fun finished(description: Description?) {
        dispatchers.forEach {
            IdlingRegistry.getInstance().unregister(it.idlingResource)
        }
    }
}

interface DispatcherWithIdlingResource {
    val idlingResource: IdlingResource
}