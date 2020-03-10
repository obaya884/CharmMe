package app.takumi.obayashi.charmme

import android.content.Intent
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.gesture.Prediction
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }
    private var charmList: RealmResults<Charm>? = null
    private var gestureLibrary: GestureLibrary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        makeCharmList()
        setUpGestureLibrary()

        with(lava_fab) {
            setParentOnClickListener { lava_fab.trigger() }
            setChildOnClickListener(com.bitvale.lavafab.Child.TOP) { startAddActivity() }
            setChildOnClickListener(com.bitvale.lavafab.Child.LEFT) { startListActivity() }
        }

        readGesture.addOnGesturePerformedListener { _, gesture ->
            val predictions: ArrayList<Prediction>? = gestureLibrary?.recognize(gesture)
            val mostLikelyPrediction: Prediction? = predictions?.maxBy { it.score }

            val charm = charmList?.find { it.name == mostLikelyPrediction?.name }

            startCharmTimer(charm!!)
        }
    }

    private fun startCharmTimer(charm: Charm) {

        Toast.makeText(
            this,
            "ジェスチャー名 :" + charm.name +
                    "\nジェスチャータイム：" + charm.duration,
            Toast.LENGTH_SHORT
        ).show()

        var timer: CountDownTimer =
            object : CountDownTimer((charm.duration * 1000).toLong(), 1000) {

                override fun onFinish() {

                }

                override fun onTick(millisUntilFinished: Long) {

                }
            }
    }

    private fun startAddActivity() {
        val activity = Intent(this, AddActivity::class.java)
        startActivity(activity)
    }

    private fun startListActivity() {
        val activity = Intent(this, ListActivity::class.java)
        startActivity(activity)
    }

    private fun setUpGestureLibrary() {
        gestureLibrary = GestureLibraries.fromFile("$filesDir/gestures")
        gestureLibrary?.load()
    }

    private fun makeCharmList() {
        charmList = realm.where<Charm>().findAll().sort("createdAt", Sort.ASCENDING)
    }
}
