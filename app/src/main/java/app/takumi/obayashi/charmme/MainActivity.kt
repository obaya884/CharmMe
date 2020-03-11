package app.takumi.obayashi.charmme

import android.content.Intent
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.gesture.Prediction
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
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

        timerViewLayout.visibility = INVISIBLE
        makeCharmList()
        setUpGestureLibrary()

        with(lava_fab) {
            setParentOnClickListener { lava_fab.trigger() }
            setChildOnClickListener(com.bitvale.lavafab.Child.TOP) {
                startAddActivity { lava_fab.collapse() }
            }
            setChildOnClickListener(com.bitvale.lavafab.Child.LEFT) {
                startListActivity { lava_fab.collapse() }
            }
        }

        readGesture.addOnGesturePerformedListener { _, gesture ->
            val predictions: ArrayList<Prediction>? = gestureLibrary?.recognize(gesture)
            val mostLikelyPrediction: Prediction? = predictions?.maxBy { it.score }

            val charm = charmList?.find { it.name == mostLikelyPrediction?.name }

            startCharmTimer(charm!!)
        }
    }

    private fun startCharmTimer(charm: Charm) {
        timerViewLayout.visibility = VISIBLE
        timerViewLayout.charmNameText.text = charm.name
        readGesture.isEnabled = false

        object : CountDownTimer((charm.duration * 1000).toLong(), 1000) {

            override fun onFinish() {
                timerViewLayout.visibility = INVISIBLE
                readGesture.isEnabled = true
            }

            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60

                val remainTime = String.format(Locale.JAPAN, "%02d:%02d", minutes, seconds)

                timerViewLayout.timerText.text = remainTime
            }
        }.start()
    }

    private fun startAddActivity(function: () -> Unit) {
        val activity = Intent(this, AddActivity::class.java)
        startActivity(activity)
    }

    private fun startListActivity(function: () -> Unit) {
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
