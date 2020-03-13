package app.takumi.obayashi.charmme

import android.content.Intent
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
import kotlinx.android.synthetic.main.activity_main.view.*


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

        with(lava_fab) {
            setParentOnClickListener { lava_fab.trigger() }
            setChildOnClickListener(com.bitvale.lavafab.Child.TOP) {
                startAddActivity()
                lava_fab.collapse()
            }
            setChildOnClickListener(com.bitvale.lavafab.Child.LEFT) {
                backMainActivity()
                lava_fab.collapse()
            }
        }
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

    private fun startAddActivity() {
        finish()
        val activity = Intent(this, AddActivity::class.java)
        startActivityForResult(activity, 1000)
    }

    private fun backMainActivity() {
        finish()
    }
}

