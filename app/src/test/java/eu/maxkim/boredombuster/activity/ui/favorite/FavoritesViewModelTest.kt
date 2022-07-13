package eu.maxkim.boredombuster.activity.ui.favorite

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import eu.maxkim.boredombuster.activity.model.Activity
import eu.maxkim.boredombuster.activity.model.activity1
import eu.maxkim.boredombuster.activity.model.activity2
import eu.maxkim.boredombuster.activity.usecase.DeleteActivity
import eu.maxkim.boredombuster.activity.usecase.GetFavoriteActivities
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FavoritesViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mockGetFavoriteActivities: GetFavoriteActivities = mock()

    private val mockDeleteActivity: DeleteActivity = mock()

    @Test
    fun `the view model maps list of activities to list ui state`() {
        val liveDataToReturn = MutableLiveData<List<Activity>>()
            .apply {
                value = listOf(activity1, activity2)
            }
        val expectedList = listOf(activity1, activity2)

        whenever(mockGetFavoriteActivities.invoke()).doReturn(liveDataToReturn)

        val viewModel = FavoritesViewModel(
            mockGetFavoriteActivities,
            mockDeleteActivity
        )


    }

}