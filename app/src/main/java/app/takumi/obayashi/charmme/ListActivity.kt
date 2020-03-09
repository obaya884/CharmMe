package app.takumi.obayashi.charmme

import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_list.*


class ListActivity : AppCompatActivity() {

    private val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }
    private var charmList: RealmResults<Charm>? = null
    private var gestureLibrary: GestureLibrary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        makeCharmList()
        setUpGestureLibrary()
        setUpRecyclerView()

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    private fun makeCharmList() {
        charmList = realm.where<Charm>().findAll().sort("createdAt", Sort.ASCENDING)
    }

    private fun setUpGestureLibrary() {
        gestureLibrary = GestureLibraries.fromFile("$filesDir/gestures")
        gestureLibrary?.load()
    }

    private fun setUpRecyclerView() {
        val adapter = CharmAdapter(
            this,
            charmList,
            gestureLibrary,
            object : CharmAdapter.OnItemClickListener {
                override fun onItemClick(item: Charm) {
                    // TODO タップ時に編集画面へ
                }
            },
            true
        )
        recyclerView.adapter = adapter
    }
}

