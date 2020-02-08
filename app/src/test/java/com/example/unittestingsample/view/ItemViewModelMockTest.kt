package org.koin.sampleapp.view

import android.text.TextUtils
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.unittestingsample.CoroutineTestRule
import com.example.unittestingsample.activities.main.data.Headers
import com.example.unittestingsample.activities.main.data.Item
import com.example.unittestingsample.activities.main.viewModel.ItemViewModel
import com.example.unittestingsample.backend.ServiceUtil
import com.example.unittestingsample.util.ItemDataState
import com.example.unittestingsample.util.LoginDataState
import com.example.unittestingsample.util.UtilityClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import retrofit2.Response

@RunWith(PowerMockRunner::class)
@PrepareForTest(UtilityClass::class, TextUtils::class)
class ItemViewModelMockTest {
    private val serviceUtil: ServiceUtil = mock()

    lateinit var itemViewModel: ItemViewModel

    @Mock
    private lateinit var items: ArrayList<Item>


    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @Rule
    @JvmField
    val coroutineTestRule = CoroutineTestRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val mockObserverForStates = mock<Observer<ItemDataState>>()

    @Mock
    private lateinit var headersMap: HashMap<String, String>

    @Mock
    private lateinit var headers: Headers

    @Before
    fun before() {
        mockStatic(UtilityClass::class.java)
        mockStatic(TextUtils::class.java)
        MockitoAnnotations.initMocks(this)
        items = ArrayList<Item>()
        headers = Headers()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testIfHeadersMissingAndReport() {
        initValues("ClientId", "AccessToken", "")

        runBlockingTest {
            `when`(serviceUtil.getList(headersMap)).thenReturn(items)

            itemViewModel.showList()

            verify(mockObserverForStates).onChanged(ItemDataState.Error(ArgumentMatchers.any()))
            verifyNoMoreInteractions(mockObserverForStates)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testFetchListFromServer() {
        initValues("abc@example.com", "12345678", "sdsds")

        runBlockingTest {
            `when`(serviceUtil.getList(headersMap)).thenReturn(items)

            itemViewModel.showList()

            verify(mockObserverForStates).onChanged(ItemDataState.ShowProgress(true))
            verify(mockObserverForStates, times(2)).onChanged(
                ItemDataState.Success(ArgumentMatchers.any())
            )
            verifyNoMoreInteractions(mockObserverForStates)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testThrowErrorOnListCallbackFailed() {
        initValues("abc@example.com", "12345678", "userId1245")

        runBlocking {
            val error = RuntimeException()

            `when`(serviceUtil.getList(headersMap)).thenThrow(error)

            itemViewModel.showList()

            verify(mockObserverForStates).onChanged(ItemDataState.ShowProgress(true))
            verify(
                mockObserverForStates,
                times(2)
            ).onChanged(ItemDataState.Error(ArgumentMatchers.any()))
            verifyNoMoreInteractions(mockObserverForStates)
        }
    }

    private fun initValues(clientId: String, accessToken: String, userId: String) {
        headers.clientId = clientId
        headers.accessToken = accessToken
        headers.userId = userId
        itemViewModel = ItemViewModel(serviceUtil, headers).apply {
            uiState.observeForever(mockObserverForStates)
        }
    }

    private inline fun <reified T> mock(): T = mock(T::class.java)

}