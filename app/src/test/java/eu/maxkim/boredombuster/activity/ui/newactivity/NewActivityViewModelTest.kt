package eu.maxkim.boredombuster.activity.ui.newactivity

import app.cash.turbine.test
import eu.maxkim.boredombuster.activity.fake.FakeDeleteActivity
import eu.maxkim.boredombuster.activity.fake.FakeGetRandomActivity
import eu.maxkim.boredombuster.activity.fake.FakeIsActivitySaved
import eu.maxkim.boredombuster.activity.fake.FakeSaveActivity
import eu.maxkim.boredombuster.activity.model.activity1
import eu.maxkim.boredombuster.activity.model.activity2
import eu.maxkim.boredombuster.util.CoroutineRule
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class NewActivityViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineRule()

    @Test
    fun `creating a ViewModel exposes loading ui state`() {
        // Arrange
        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(),
            FakeSaveActivity(),
            FakeDeleteActivity(),
            FakeIsActivitySaved()
        )

        // Assert
        assert(viewModel.uiState.value is NewActivityUiState.Loading)
    }

    @Test
    fun `creating a ViewModel updates ui state to success after loading`() {
        // Arrange
        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(),
            FakeSaveActivity(),
            FakeDeleteActivity(),
            FakeIsActivitySaved()
        )

        val expectedUiState = NewActivityUiState.Success(activity1, false)

        // Act
        coroutineRule.testDispatcher.scheduler.runCurrent()

        // Assert
        val actualState = viewModel.uiState.value
        assertEquals(actualState, expectedUiState)
    }

    @Test
    fun `creating a ViewModel updates ui state to error in case of failure`() {
        // Arrange
        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(isSuccessful = false),
            FakeSaveActivity(),
            FakeDeleteActivity(),
            FakeIsActivitySaved()
        )

        // Act
        coroutineRule.testDispatcher.scheduler.runCurrent()

        // Assert
        val currentUiState = viewModel.uiState.value
        assert(currentUiState is NewActivityUiState.Error)
    }

    @Test
    fun `if activity is already saved, ui state's isFavorite is set to true`() {
        // Arrange
        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(),
            FakeSaveActivity(),
            FakeDeleteActivity(),
            FakeIsActivitySaved(isActivitySaved = true)
        )

        val expectedUiState = NewActivityUiState.Success(activity1, true)

        // Act
        coroutineRule.testDispatcher.scheduler.runCurrent()

        // Assert
        val actualState = viewModel.uiState.value
        assertEquals(actualState, expectedUiState)
    }

    @Test
    fun `calling loadNewActivity() updates ui state with a new activity`() {
        // Arrange
        val fakeGetRandomActivity = FakeGetRandomActivity()

        val viewModel = NewActivityViewModel(
            fakeGetRandomActivity,
            FakeSaveActivity(),
            FakeDeleteActivity(),
            FakeIsActivitySaved()
        )

        // this can be omitted, but it is nice to not have any pending tasks
        coroutineRule.testDispatcher.scheduler.runCurrent()

        fakeGetRandomActivity.activity = activity2
        val expectedUiState = NewActivityUiState.Success(activity2, false)

        // Act
        viewModel.loadNewActivity()
        coroutineRule.testDispatcher.scheduler.runCurrent()

        // Assert
        val actualState = viewModel.uiState.value
        assertEquals(actualState, expectedUiState)
    }

    @Test
    fun `calling setIsFavorite(true) triggers SaveActivity use case`() {
        // Arrange
        val fakeSaveActivity = FakeSaveActivity()

        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(),
            fakeSaveActivity,
            FakeDeleteActivity(),
            FakeIsActivitySaved()
        )

        // Act
        viewModel.setIsFavorite(activity1, true)
        coroutineRule.testDispatcher.scheduler.runCurrent()

        // Assert
        assert(fakeSaveActivity.wasCalled)
    }

    @Test
    fun `calling setIsFavorite(false) triggers DeleteActivity use case`() {
        // Arrange
        val fakeDeleteActivity = FakeDeleteActivity()
        val viewModel = NewActivityViewModel(
            FakeGetRandomActivity(),
            FakeSaveActivity(),
            fakeDeleteActivity,
            FakeIsActivitySaved()
        )

        // Act
        viewModel.setIsFavorite(activity1, false)
        coroutineRule.testDispatcher.scheduler.runCurrent()

        // Assert
        assert(fakeDeleteActivity.wasCalled)
    }

    @Test
    fun `calling loadNewActivity() twice goes through expected ui states`() = runTest {
        // Note that we call loadNewActivity function
        // inside the init block of the ViewModel

        val fakeGetRandomActivity = FakeGetRandomActivity()
        val viewModel = NewActivityViewModel(
            fakeGetRandomActivity,
            FakeSaveActivity(),
            FakeDeleteActivity(),
            FakeIsActivitySaved()
        )

        // First of all we expect a Loading state
        assert(viewModel.uiState.value is NewActivityUiState.Loading)

        launch {

            // test function will collection emitted values from this flow for us
            viewModel.uiState.test {

                // Here is the first successful result
                with(awaitItem()) {
                    assert(this is NewActivityUiState.Success)
                    assertEquals((this as NewActivityUiState.Success).activity, activity1)
                }

                // The second request has been called, we expect
                // loading state to be displayed every time
                // we call loadNewActivity function
                assert(awaitItem() is NewActivityUiState.Loading)

                // Here is the second successful result
                with(awaitItem()) {
                    assert(this is NewActivityUiState.Success)
                    assertEquals((this as NewActivityUiState.Success).activity, activity2)
                }

                // This is not necessary for this specific test
                // but better safe than sorry
                // especially when dealing with hot flows
                cancelAndIgnoreRemainingEvents()
            }
        }

        // Runs the initial request
        runCurrent()

        // Prepares and runs the second request
        fakeGetRandomActivity.activity = activity2
        viewModel.loadNewActivity()
        runCurrent()
    }

}