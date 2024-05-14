# Navigable Bottom Sheet Dialog Fragment

A bottom sheet dialog fragment that can load fragments inside it. In order to work one must
 provide a `navigationGraphId`.

The main class is the `NavigableBottomSheetDialogFragment`

One can simple use it as a regular dialog
````
    val dialog = NavigableBottomSheetDialogFragment()
    dialog.arguments = NavigableBottomSheetDialogFragmentArgs(
        R.navigation.colors_navigation
    ).toBundle()
    dialog.show(requireActivity().supportFragmentManager, "dialogTag")
````

And its navigation `navigable_bottom_sheet_dialog_navigation.xml` 

to be used in a navigation graph
````
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/main_navigation"
    app:startDestination="@id/go_main">

    <include app:graph="@navigation/navigable_bottom_sheet_dialog_navigation" />

    <fragment
        android:id="@+id/go_main"
        android:name="com.dashlane.bottomnavigationdemo.main.MainFragment">
        <action android:id="@+id/to_dialog"
            app:destination="@id/navigable_bottom_sheet_dialog_navigation">
            <argument
                app:argType="reference"
                android:name="navigationGraphId"
                app:nullable="false" />
        </action>
    </fragment>
</navigation>
````

The on cancel of the `NavigableBottomSheetDialogFragment` can be propagated to the current fragment
or to the activity that implements the `NavigableBottomSheetDialogFragmentCanceledListener`

In order to access the `NavHostFragment` from the fragments being loaded inside the bottom sheet
 dilaog, one can make those fragments implement the interface `NavigableBottomSheetFragment`
 
In order to delegate the navigation to the activity one can make the fragments loaded in the
 bottom sheet to implement the `DelegateNavigationBottomSheetFragment` and call the
  `delegateNavigation` function when its time to execute a navigation
  
In order to show the bottom sheet on inflation time one can make the activity implement the
 `NavigableBottomSheetActivity` and call the `configureBottomSheetDialogNavigation` after setting
  the content view.
  
```
class SimpleNavigableBottomSheetActivity :
    NavigableBottomSheetActivity,
    AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_with_unknown_nav_host_fragment)
        configureBottomSheetDialogNavigation(R.id.nav_host_fragment_id)
    }

    override fun getBottomSheetNavigationGraphId() = R.navigation.render_inside_bottom_sheet
}
```

There are also other options that can be passed to the `NavigableBottomSheetDialogFragment` such as:
- `startDestinationId` - change the start destination of the navigation being rendered inside
- `startDestinationArgs` - the start destination arguments
- `consumeBackPress` - a boolean declaring if by pressing back we apply it inside the bottom
 sheet or not