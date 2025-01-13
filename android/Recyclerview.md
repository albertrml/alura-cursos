# Create dynamic lists with RecyclerView
Link: https://developer.android.com/develop/ui/views/layout/recyclerview
Date: 2025-01-12

In the view system, the RecyclerView library efficiently displays large sets of data, how they look, and dynamically creates the elements only when needed. As the name implies, the library doesn't destroy the its view when items scrolls off the screen but instead recycles them for news ones. To do so, several classes work together to build the dynamic list: **RecyclerView**, **RecyclerView.ViewHolder**, **RecyclerView.Adapter**, and **LayoutManager**

The **RecyclerView** is a ViewGroup that contains the views corresponding to the data. For each individual element in the list, the **ViewHolder** defines how it is displayed. Once the ViewHolder is created, the RecyclerView binds it to its data by invoking methods in the **Adapter**. Finally, the **LayoutManager** arranges the individual elements within the XML fragments or activity.

## How to implement
1. **Import the dependencies**: considering the project added the Google Maven repository, the following dependencies must be added in th build.gradle file for app or module.
```kotlin
dependencies{
    val recyclerview_ver="1.3.2"
    val recyclerview_selection_ver="1.1.0"
    
    implementation("androidx.recyclerview:recyclerview:$recyclerview_ver")
    // For control over item selection of both touch and mouse driven selection
    implementation("androidx.recyclerview:recyclerview-selection:$recyclerview_selection_ver")
}
```

2. **Plan layout**: The items in the RecyclerView are arranged by a LayoutManager class. The RecyclerView library provides three layout managers to arrange the items: **LinearLayoutManager** and **GridLayoutManager**, which arrange them in a one-dimensional and a two-dimensional grid, respectively. Bothe can be oriented vertically or horizontally; The third layout manager is **StaggeredGridLayoutManager**, which is similar to GridLayoutManager, but it does not require that items in a row have the same height (for vertial grid) or items in the same column have the same width (for horizontal grids).

3. **Create Layout for items**: The layout for the each view item is defined in an XML layout file, as usual. In this case, the app has a text_row_item.xml file like this:
```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="@dimen/list_item_height"
    android:layout_marginLeft="@dimen/margin_medium"
    android:layout_marginRight="@dimen/margin_medium"
    android:gravity="center_vertical">

    <TextView
        android:id="@+id/textView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/element_text"/>
</FrameLayout>
```

4. **Adding RecyclerView into Layout**
```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:listitem="@layout/item_layout" />

</LinearLayout>
```

5. **Implement ViewHolder**: As explained before, the ViewHolder works together with the Adapter to display the list. The ViewHolder is a wrapper around a View that contains the layout for an individual item in the list. The Adapter creates ViewHolder objects as needed and also sets the data for those views. The process of associating views to their data is called binding. The Adapter needs to override three methods: **onCreateViewHolder()**, which creates a new ViewHolder without content; **onBindViewHolder()**, which attach the data to ViewHolder; and **getItemCount()**, which is responsible to get the size of the dataset.

```kotlin
class CustomAdapter(private val dataSet: Array<String>) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView by lazy { view.findViewById(R.id.textView) }

        fun bind(data: String){
            textView.text = data
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.text_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.bind(dataSet[position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
```

6. Initialization into Activity or Fragment

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dataset = arrayOf("January", "February", "March")
        val customAdapter = CustomAdapter(dataset)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = customAdapter

    }

}
```
