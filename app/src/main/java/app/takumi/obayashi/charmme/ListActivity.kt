package app.takumi.obayashi.charmme

import android.content.Intent
import android.gesture.Gesture
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

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
                override fun onItemClick(realmItem: Charm, gesture: Gesture) {
                    AlertDialog.Builder(this@ListActivity)
                        .setTitle("削除")
                        .setMessage("この魔法を忘れますか？")
                        .setPositiveButton("OK") { _, _ ->
                            gestureLibrary?.removeGesture(realmItem.name, gesture)
                            realm.executeTransaction {
                                realmItem.deleteFromRealm()
                            }
                        }
                        .setNegativeButton("Cancel") { _, _ -> }
                        .show()
                }
            },
            true
        )
        recyclerView.adapter = adapter
    }

    private fun startAddActivity() {
        finish()
        //TODO 見直し
        val activity = Intent(this, AddActivity::class.java)
        startActivityForResult(activity, 1000)
    }

    private fun backMainActivity() {
        finish()
    }
}

