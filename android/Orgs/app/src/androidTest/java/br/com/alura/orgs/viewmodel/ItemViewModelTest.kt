package br.com.alura.orgs.viewmodel

import br.com.alura.orgs.model.entity.Item
import br.com.alura.orgs.model.mock.mockItems
import br.com.alura.orgs.model.repository.ItemRepository
import br.com.alura.orgs.utils.Response
import br.com.alura.orgs.utils.rules.MainDispatcherRule
import br.com.alura.orgs.view.viemodel.ItemUiEvent
import br.com.alura.orgs.view.viemodel.ItemViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ItemViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ItemViewModel
    private lateinit var repository: ItemRepository

    @BeforeEach
    fun setup() {
        repository = mockk()
        viewModel = ItemViewModel(repository)
    }

    @Test
    fun onIncreaseQuantityUpdatesitemquantity() = runTest {
        val initialItem = Item(
            itemName = "Test Item",
            itemDescription = "Description",
            itemValue = 10.0,
            quantityInStock = 5
        )
        val updatedItem = initialItem.copy(quantityInStock = 6)

        coEvery { repository.updateItem(updatedItem) } returns flowOf(Response.Success(Unit))

        viewModel.onEvent(ItemUiEvent.OnIncreaseQuantity(initialItem))

        val state = viewModel.uiState.first()
        assertEquals(Response.Success(Unit), state.updateState)
    }

    @Test
    fun onInsertItemtriggersinsertItem() = runTest {
        val newItem = Item(
            itemName = "New Item",
            itemDescription = "New Description",
            itemValue = 20.0,
            quantityInStock = 10
        )

        coEvery { repository.insertItem(newItem) } returns flowOf(Response.Success(Unit))

        viewModel.onEvent(
            ItemUiEvent.OnInsertItem(
                itemName = "New Item",
                itemDescription = "New Description",
                itemValue = "20",
                quantityInStock = "10"
            )
        )

        val state = viewModel.uiState.first()
        assertEquals(Response.Success(Unit), state.insertState)
    }

    @Test
    fun onDeleteItemdeletesitem() = runTest {
        val itemToDelete = Item(
            itemName = "To Delete",
            itemDescription = "Description",
            itemValue = 15.0,
            quantityInStock = 2
        )

        coEvery { repository.deleteItem(itemToDelete) } returns flowOf(Response.Success(Unit))

        viewModel.onEvent(ItemUiEvent.OnDeleteItem(itemToDelete))

        val state = viewModel.uiState.first()
        assertEquals(Response.Success(Unit), state.deleteState)
    }

    @Test
    fun fetchAllItemsupdatesitemsinstate() = runTest {

        coEvery { repository.getAllItems() } returns flowOf(Response.Success(mockItems))

        viewModel.onEvent(ItemUiEvent.OnFetchAllItems)

        val state = viewModel.uiState.first()
        assertEquals(mockItems, state.items)
        assertEquals(Response.Success(Unit), state.fetchState)
    }

    @Test
    fun fetchItemByIdupdatesfetchByIdStateinstate() = runTest {
        val mockItem = mockItems[0]

        coEvery { repository.getItemById(1) } returns flowOf(Response.Success(mockItem))

        viewModel.onEvent(ItemUiEvent.OnFetchItemById(1))

        val state = viewModel.uiState.first()
        assertEquals(Response.Success(mockItem), state.fetchByIdState)
    }
}