# Save data in a local database using Room, Coroutines, Hilt, and Viewmodel

## 1. Room Library
The Room persistence library provides an abstraction layer over SQLite to allow fluent database access while harnessing the full power of SQLite. There are three major components in Room: **database class**, **data entity**, and **data access objects** (DAO). 

As the main access point, the **database class** provides to app with instances of the DAOs associated with that database. **DAOs** provide methods that the app can use to query, update, insert, and delete data in the database. Using DAO, the app retrieves data from the database as instances of the associated **data entity** objects, which represent tables in the app's database. 

<div style="text-align: center; background-color: white;">
    <img src="https://developer.android.com/static/images/training/data-storage/room_architecture.png" alt="Fig. 1 Diagram of Room library architecture.">
</div>

### Adding dependencies
```kotlin
// In build.gradle.kts, project level
plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}
```
```kotlin
// In build.gradle.kts, module level
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    val test_espresso_ver="3.6.1"
    val test_junit_ver="1.2.1"
    val room_version = "2.6.1"
    val kotlinx_coroutines_test_ver="1.10.1"

    implementation("androidx.room:room-runtime:$room_version")

    // Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:$room_version")

    // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")

    // Test helpers
    androidTestImplementation("androidx.room:room-testing:$room_version")
    androidTestImplementation("androidx.test.espresso:espresso-core:$test_espresso_ver")
    androidTestImplementation("androidx.test.ext:junit:$test_junit_ver")
    androidTestImplementation("androidx.test.ext:junit-ktx:$test_junit_ver")
}
```
### Data Entity
For example, we will use Orgs project to explain how to implement room and so on. In the `br.com.alura.orgs/model/entity`, we create `Item.kt`, the data entity. This entity is composed by five fields, with just one key field. When a field name does not match the corresponding a variable name, we need to use @ColumnInfo with name in the database.
```kotlin
// 
@Entity(tableName = "item")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name="name")
    val itemName: String,
    @ColumnInfo(name="description")
    val itemDescription: String,
    @ColumnInfo(name="value")
    val itemValue: Double,
    @ColumnInfo(name="quantity")
    val quantityInStock: Int
)
```

### Data access object (DAO)
In `br.com.alura.orgs/model/source/`, we create the `ItemDAO.kt`, the Data access object (DAO). Each DAO must have the **@DAO** annotation to be recognized as a DAO. The methods that interact with the database must also have an annotation. Some operations is predefined, such as `@update` and `@delete`, but others require additional options. For example, `@Insert` requires an insertion strategy for handling conflicts. In this project, we choose `ABORT` as part of a failure scenario test, which will be explained shortly. For better understanding, we can choose among `ABORT`, `IGNORE`, and `REPLACE`. Besides predefind operations, we can create a custom operation using `@Query` with some SQLight command.

```kotlin
@Dao
interface ItemDAO {
    @Insert (onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: Item)
    @Update
    suspend fun update(item: Item)
    @Delete
    suspend fun delete(item: Item)
    @Query("Select * from item where id = :id")
    suspend fun getItemById(id: Int): Item
    @Query("Select * from item Order by name ASC")
    fun getItems(): Flow<List<Item>>
}
```

### Database
In `br.com.alura.orgs/model/source/`, we also create the `ItemDatabase.kt`, the database class that defines the database configuration and serves as the app's main access point to the persisted data. This classe must be annotated with `@Database`, followed by entities, version, and exportSchema parameters. The entities define all the entity classes that the database operates on. The version defines the database version and needs to be changed when the database structure is altered. Finally, the exportSchema defines the room library will automatically export the database schema to a .json file whenever the database is created or upgraded. 

Since each RoomDatabase instance is fairly expensive, and we rarely need access to multiple instances within a single process, the singleton design pattern is a good way to create and expose the database. The database instance requires an application context, the database class, and a database name. For this example, we decided to apply `fallbackToDestructiveMigration()`, which, when upgrading the database, means that all existing data will be deleted and the database will be reset, starting from a fresh state with the new schema.

```kotlin
@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class ItemRoomDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDAO

    companion object {
        @Volatile
        private var INSTANCE: ItemRoomDatabase? = null

        fun getDatabase(ctx: Context): ItemRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    ctx.applicationContext,
                    ItemRoomDatabase::class.java,
                    "item_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
```

### Test Database

As we need to use the device, we create `ItemDatabaseTest.kt` into androidTest section, more precisely into ./data. 

```kotlin
class ItemDatabaseTest {
    private lateinit var itemDAO: ItemDAO
    private lateinit var db: ItemRoomDatabase

    @Before
    fun createDb(){
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            ctx,
            ItemRoomDatabase::class.java
        ).build()
        itemDAO = db.itemDao()
    }

    @Before
    fun setupTestData() = runTest {
        db.clearAllTables()
    }

    @After
    @Throws(IOException::class)
    fun closeDb(){
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testInsertItemAndRetrieveSuccessfully() = runTest {
        assertEquals(emptyList<Item>(), itemDAO.getItems().first())
        val item = mockItems[0]
        itemDAO.insert(item)
        val firsItem = itemDAO.getItems().first().first()
        val expectedItem = item.copy(id = firsItem.id)
        assertEquals(expectedItem,firsItem)
    }

    @Test
    @Throws(Exception::class)
    fun testInsertDuplicateItemThrowsException() = runTest {
        itemDAO.insert(mockItems[0])
        val item = itemDAO.getItemById(1)
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking { itemDAO.insert(item) }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testGetItemByIdReturnsCorrectItem() = runTest {
        mockItems.forEach { itemDAO.insert(it) }

        val secondItem = itemDAO.getItemById(2)

        assert(secondItem.id == 2)
        assert(
            compareValuesBy(secondItem, mockItems[1],
                {it.itemName},
                {it.itemDescription},
                {it.itemValue},
                {it.quantityInStock}
            ) == 0
        )
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateItemCorrectly() = runTest {
        itemDAO.insert(mockItems[2])
        val itemBeforeUpdate = itemDAO.getItemById(1).copy(
            itemName = mockItems[1].itemName,
            itemDescription = mockItems[1].itemDescription,
            itemValue = mockItems[1].itemValue,
            quantityInStock = mockItems[1].quantityInStock
        )
        itemDAO.update(itemBeforeUpdate)
        val itemAfterUpdate = itemDAO.getItemById(1)

        assert(itemDAO.getItems().first().size == 1)
        assert(
            compareValuesBy(
                itemAfterUpdate, itemBeforeUpdate,
                {it.itemName},
                {it.itemDescription},
                {it.itemValue},
                {it.quantityInStock}
            ) == 0
        )
    }

    @Test
    @Throws(Exception::class)
    fun testDeleteItemRemovesCorrectly() = runTest {
        val item = mockItems[0]
        itemDAO.insert(item)
        val itemForDelete = itemDAO.getItemById(1)
        assertEquals(1, itemDAO.getItems().first().size)
        itemDAO.delete(itemForDelete)
        assert(itemDAO.getItems().first().isEmpty())
    }


    @Test
    @Throws(Exception::class)
    fun testGetItemsReturnsCorrectlyAllItems() = runTest {
        mockItems.forEach { itemDAO.insert(it) }
        val allItems = itemDAO.getItems().first().map {
            Item(
                itemName = it.itemName,
                itemDescription = it.itemDescription,
                itemValue = it.itemValue,
                quantityInStock = it.quantityInStock
            )
        }
        assert(allItems.size == mockItems.size)
        assert(allItems.containsAll(mockItems))
    }

}
```

## 2. Repository

Before building the repository, we need to create a Response class, which is responsible for handling possible outcomes and helps the app manage the UI.

```kotlin
sealed class Response <out T>{
    data class Success <out T>(val result: T): Response<T>()
    data class Failure (val exception: Exception): Response<Nothing>()
    data object Loading: Response<Nothing>()
}
```

After that, we create the repository with the corresponding ItemDAO. Since the Response class has three possible outcomes, we create an auxiliary method to handle the Response without repeating code. Each repository method first emits a Loading state, followed by another Response, which can either be Success or Failure. 
As we need to use the device, we creAs we need to use the device, we cre*ate `class ItemRepository.kt` into androidTest section, more precisely into ./data.*ate `class ItemRepository.kt` into androidTest section, more precisely into ./data.*

```kotlin


class ItemRepository (private val itemDao: ItemDAO) {
    private suspend fun <T> performDatabaseOperation(
        databaseOperation: suspend () -> T
    ): Response<T> =
        try { Response.Success(databaseOperation()) }
        catch (e: Exception) { Response.Failure(e) }

    fun getAllItems(): Flow<Response<List<Item>>> = flow {
        emit(Response.Loading)
        try {
            emitAll(
                itemDao.getItems().map {
                    Response.Success(it)
                }
            )
        }catch (e: Exception){
            Response.Failure(e)
        }
    }

    fun insertItem(item: Item): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { itemDao.insert(item) })
    }

    fun updateItem(item: Item): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { itemDao.update(item) })
    }

    fun deleteItem(item: Item): Flow<Response<Unit>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { itemDao.delete(item) })
    }

    fun getItemById(id: Int): Flow<Response<Item>> = flow {
        emit(Response.Loading)
        emit(performDatabaseOperation { itemDao.getItemById(id) })
    }

}
```

### Test Repository

```kotlin
class ItemRepositoryTest {

    private lateinit var itemRepository: ItemRepository
    private lateinit var itemDao: ItemDAO
    private lateinit var db: ItemRoomDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, ItemRoomDatabase::class.java)
            .build()
        itemDao = db.itemDao()
        itemRepository = ItemRepository(itemDao)
    }

    @Before
    fun setupTestData() = runTest {
        db.clearAllTables()
    }


    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertItemSucceeds() = runTest {
        itemRepository.insertItem(mockItems[0]).collect { result ->
            when (result) {
                is Response.Success -> {
                    val item = itemDao.getItemById(1)
                    assertEquals(item.itemName, mockItems[0].itemName)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun testInsertItemWhenAlreadyExistsThrowsError() = runTest {
        itemDao.insert(mockItems[0])
        val item = itemDao.getItemById(1)
        itemRepository.insertItem(item).collect { result ->
            when (result) {
                is Response.Success -> assert(false)
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(
                    result.exception is SQLiteConstraintException
                )
            }
        }
    }

    @Test
    fun testGetAllItemsReturnsCorrectItems() = runTest {
        mockItems.forEach { itemDao.insert(it) }
        itemRepository.getAllItems().take(2).collect { result ->
            when (result) {
                is Response.Success -> assert(
                    result.result.map {
                        Item(
                            itemName = it.itemName,
                            itemDescription = it.itemDescription,
                            itemValue = it.itemValue,
                            quantityInStock = it.quantityInStock
                        )
                    }.containsAll(mockItems)
                )
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun testDeleteItemRemovesItemCorrectly() = runTest {
        val item = mockItems[0]
        itemDao.insert(item)
        val itemForDelete = itemDao.getItemById(1)


        itemRepository.deleteItem(itemForDelete).collect {
            when (it) {
                is Response.Success -> assert(
                    itemDao.getItems().first().isEmpty()
                )

                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun testUpdateItemUpdatesItemCorrectly() = runTest {
        itemDao.insert(mockItems[0])
        val itemBeforeUpdate = itemDao.getItems().first().first().copy(
            itemName = mockItems[1].itemName,
            itemDescription = mockItems[1].itemDescription,
            itemValue = mockItems[1].itemValue,
            quantityInStock = mockItems[1].quantityInStock
        )
        itemRepository.updateItem(itemBeforeUpdate).collect {
            when (it) {
                is Response.Success -> {
                    val itemAfterUpdate = itemDao.getItemById(1)
                    assertEquals(itemBeforeUpdate, itemAfterUpdate)
                }

                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun testUpdateItemWithSameDataDoesNotChangeItem() = runTest {
        itemDao.insert(mockItems[2])
        val sameItem = itemDao.getItems().first().first()
        itemRepository.updateItem(sameItem).collect {
            when (it) {
                is Response.Success -> assert(true)
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }
}
```

## 3. Viewmodel

 As user interact with viewmodel from the UI, we need implement Uniderectional Data Flow (UDF), which is a design pattern, where state flows down and events flows up. By following UDF, we can decouple state that display in the UI or change its behavior from the parts the app that store and change state. The UI update loop for an app using UDF looks like this:

 * **Event**: Part of the UI generates an event an passes it upward (a button click passed to the viewmodel to handler) or an event is passed from other layers of the app (user session has expired)
 * **Update state**: An event handler might change the state
 * **Display state**: The state holder passes down the state, and the UI display it.

<div style="text-align: center; background-color: white;">
    <img src="https://developer.android.com/static/develop/ui/compose/images/state-unidirectional-flow.png?hl=pt-br" alt="Fig. 2 Fluxo de dados unidirecional.">
</div>

In this project, we implement `ItemUIEvent.kt` and `ItemUIState.kt`, both stored in the `br.com.alura.orgs/view/viewmodel`

```kotlin
sealed class ItemUiEvent {
    data object OnFetchAllItems : ItemUiEvent()
    data class OnFetchItemById(val itemId: Int) : ItemUiEvent()
    data class OnInsertItem(
        val itemName: String,
        val itemDescription: String,
        val itemValue: String,
        val quantityInStock: String) : ItemUiEvent()
    data class OnDeleteItem(val item: Item) : ItemUiEvent()
    data class OnUpdateItem(val item: Item) : ItemUiEvent()
    data class OnIncreaseQuantity(val item: Item) : ItemUiEvent()
    data class OnDecreaseQuantity(val item: Item) : ItemUiEvent()
}
```

```kotlin
data class ItemUiState(
    val items: List<Item> = emptyList(),
    val fetchAllItemsState: Response<Unit> = Response.Loading,
    val fetchItemByIdState: Response<Item> = Response.Loading,
    val deleteState: Response<Unit> = Response.Loading,
    val insertState: Response<Unit> = Response.Loading,
    val updateState: Response<Unit> = Response.Loading,
)
```


```kotlin
class ItemViewModel @Inject constructor(private val repository: ItemRepository): ViewModel() {
    private val _uiState = MutableStateFlow(ItemUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: ItemUiEvent){
        when(event){
            is ItemUiEvent.OnDecreaseQuantity -> decreaseQuantity(event.item)
            is ItemUiEvent.OnDeleteItem -> deleteItem(event.item)
            is ItemUiEvent.OnFetchAllItems -> fetchAllItems()
            is ItemUiEvent.OnFetchItemById -> fetchItemById(event.itemId)
            is ItemUiEvent.OnIncreaseQuantity -> increaseQuantity(event.item)
            is ItemUiEvent.OnInsertItem -> {
                val item  = event.run {
                    Item(
                        itemName = itemName,
                        itemDescription = itemDescription,
                        itemValue = itemValue.toDouble(),
                        quantityInStock = quantityInStock.toInt()
                    )
                }
                insertItem(item)
            }
            is ItemUiEvent.OnUpdateItem -> updateItem(event.item)
        }
    }

    private fun decreaseQuantity(item: Item){
        val newItem = item.copy(quantityInStock = item.quantityInStock - 1)
        updateItem(newItem)
    }

    private fun increaseQuantity(item: Item){
        val newItem = item.copy(quantityInStock = item.quantityInStock + 1)
        updateItem(newItem)
    }

    private fun deleteItem(item: Item){
        viewModelScope.launch {
            repository.deleteItem(item).collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(deleteState = res)
                }
            }
        }
    }

    private fun fetchAllItems() {
        viewModelScope.launch {
            repository.getAllItems().collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(
                        items = if (res is Response.Success) res.result.sortedBy(Item::id) else state.items,
                        fetchAllItemsState = res.mapTo(Unit)
                    )
                }
            }
        }
    }


    private fun fetchItemById(id: Int) {
        viewModelScope.launch {
            repository.getItemById(id).collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(fetchItemByIdState = res)
                }
            }
        }
    }

    private fun insertItem(item: Item) {
        viewModelScope.launch {
            repository.insertItem(item).collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(insertState = res)
                }
            }
        }
    }

    private fun updateItem(item: Item) {
        viewModelScope.launch {
            repository.updateItem(item).collect { response ->
                response.handleResponse(_uiState) { state, res ->
                    state.copy(updateState = res)
                }
            }
        }
    }
}
```


```kotlin
class ItemViewModelTest {

    private lateinit var viewModel: ItemViewModel
    private lateinit var repository: ItemRepository
    private lateinit var itemDao: ItemDAO
    private lateinit var db: ItemRoomDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(context, ItemRoomDatabase::class.java)
            .build()
        itemDao = db.itemDao()
        repository = ItemRepository(itemDao)
        viewModel = ItemViewModel(repository)
    }

    @Before
    fun setupTestData() = runTest {
        db.clearAllTables()
    }


    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun onDecreaseQuantityUpdatesItemQuantity() = runTest {
        val item = mockItems[0]
        itemDao.insert(item)

        val itemBeforeDecrease = itemDao.getItemById(1)
        val itemQuantityBeforeDecrease = itemBeforeDecrease.quantityInStock

        viewModel.onEvent(ItemUiEvent.OnDecreaseQuantity(itemBeforeDecrease))
        viewModel.uiState.take(2).collect{ uiState ->
            when(uiState.updateState){
                is Response.Success -> {
                    val itemQuantityAfterDecrease = itemDao
                        .getItemById(1)
                        .quantityInStock

                    assertEquals(
                        itemQuantityBeforeDecrease - 1,
                        itemQuantityAfterDecrease,
                    )
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun onIncreaseQuantityUpdatesItemQuantity() = runTest {
        val item = mockItems[0]
        itemDao.insert(item)

        val itemBeforeIncrease = itemDao.getItemById(1)
        val itemQuantityBeforeIncrease = itemBeforeIncrease.quantityInStock

        viewModel.onEvent(ItemUiEvent.OnIncreaseQuantity(itemBeforeIncrease))
        viewModel.uiState.take(2).collect{ uiState ->
            when(uiState.updateState){
                is Response.Success -> {
                    val itemQuantityAfterIncrease = itemDao
                        .getItemById(1)
                        .quantityInStock

                    assertEquals(
                        itemQuantityBeforeIncrease + 1,
                        itemQuantityAfterIncrease,
                    )
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun onInsertItemTriggersSuccess() = runTest {
        mockItems[0].apply {
            viewModel.onEvent(
                ItemUiEvent.OnInsertItem(
                    itemName = itemName,
                    itemDescription = itemDescription,
                    itemValue = itemValue.toString(),
                    quantityInStock = quantityInStock.toString()
                )
            )
        }

        viewModel.uiState.take(2).collect { uiState ->
            when (uiState.insertState) {
                is Response.Success -> {
                    val itemFromViewModel = itemDao.getItemById(1).copy(
                        id = mockItems[0].id
                    )
                    assertEquals(mockItems[0], itemFromViewModel)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }

        }
    }

    @Test
    fun onInsertDuplicateItemButDifferentID() = runTest {
        mockItems[0].apply {
            viewModel.onEvent(
                ItemUiEvent.OnInsertItem(
                    itemName = itemName,
                    itemDescription = itemDescription,
                    itemValue = itemValue.toString(),
                    quantityInStock = quantityInStock.toString()
                )
            )
        }
        val item = itemDao.getItemById(1)

        item.apply {
            viewModel.onEvent(
                ItemUiEvent.OnInsertItem(
                    itemName = itemName,
                    itemDescription = itemDescription,
                    itemValue = itemValue.toString(),
                    quantityInStock = quantityInStock.toString()
                )
            )
        }

        viewModel.uiState.take(2).collect { uiState ->
            when (uiState.insertState) {
                is Response.Success -> {
                    val itemFromViewModel = itemDao.getItems().first()
                    assertEquals(itemFromViewModel.size,2)
                    assertEquals(item, itemFromViewModel[0])
                    assertNotEquals(item, itemFromViewModel[1])
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun onDeleteItemDeletesItem() = runTest {
        itemDao.insert(mockItems[0])
        val item = itemDao.getItemById(1)
        viewModel.onEvent(ItemUiEvent.OnDeleteItem(item))

        viewModel.uiState.take(2).collect{ uiState ->
            when(uiState.deleteState){
                is Response.Success -> {
                    val itemFromViewModel = itemDao.getItems().first()
                    assertEquals(emptyList<Item>(), itemFromViewModel)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun fetchAllItemsUpdatesItemsInState() = runTest {
        mockItems.forEach { itemDao.insert(it) }
        assertEquals(viewModel.uiState.value.items, emptyList<Item>())
        assertEquals(viewModel.uiState.value.fetchAllItemsState, Response.Loading)

        viewModel.onEvent(ItemUiEvent.OnFetchAllItems)
        viewModel.uiState.take(2).collect { uiState ->
            when (uiState.fetchAllItemsState) {
                is Response.Success -> {
                    val itemsFromViewModel = uiState.items.map {
                        Item(
                            itemName = it.itemName,
                            itemDescription = it.itemDescription,
                            itemValue = it.itemValue,
                            quantityInStock = it.quantityInStock
                        )
                    }
                    assertEquals(mockItems, itemsFromViewModel)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun onFetchItemByIdUpdatesFetchByIdStateInState() = runTest {
        val item = mockItems[0]
        itemDao.insert(item)
        viewModel.onEvent(ItemUiEvent.OnFetchItemById(1))
        viewModel.uiState.take(2).collect{ uiState ->
            when(uiState.fetchItemByIdState) {
                is Response.Success -> {
                    val itemFromViewModel = (
                            uiState.fetchItemByIdState as Response.Success<Item>
                        )
                        .result
                        .copy( id = item.id)
                    assertEquals(item, itemFromViewModel)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }

    @Test
    fun onUpdateItemUpdatesItem() = runTest {
        val item = mockItems[0]
        itemDao.insert(item)
        val itemBeforeUpdate = itemDao.getItems().first().first().copy(
            itemName = mockItems[1].itemName,
            itemDescription = mockItems[1].itemDescription,
            itemValue = mockItems[1].itemValue,
            quantityInStock = mockItems[1].quantityInStock
        )
        viewModel.onEvent(ItemUiEvent.OnUpdateItem(itemBeforeUpdate))
        viewModel.uiState.take(2).collect { uiState ->
            when (uiState.updateState) {
                is Response.Success -> {
                    val itemAfterUpdate = itemDao.getItemById(1)
                    assertEquals(itemBeforeUpdate, itemAfterUpdate)
                }
                is Response.Loading -> assert(true)
                is Response.Failure -> assert(false)
            }
        }
    }
}
```
## 4. Hilt - Dependency Injection