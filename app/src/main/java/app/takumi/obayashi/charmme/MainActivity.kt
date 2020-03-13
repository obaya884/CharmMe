package app.takumi.obayashi.charmme

import android.app.Activity
import android.content.Intent
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.gesture.Prediction
import android.graphics.*
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
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

    private var mPaint: Paint? = null
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null

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
        setContentView(R.layout.activity_main)

        with(lava_fab) {
            setParentOnClickListener { lava_fab.trigger() }
            setChildOnClickListener(com.bitvale.lavafab.Child.TOP) {
                startAddActivity { lava_fab.collapse() }
            }
            setChildOnClickListener(com.bitvale.lavafab.Child.LEFT) {
                startListActivity { lava_fab.collapse() }
            }
        }

        makeCharmList()
        setUpGestureLibrary()
        setUpPaint()
        timerViewLayout.visibility = INVISIBLE

        readGesture.addOnGesturePerformedListener { _, gesture ->
            val predictions: ArrayList<Prediction>? = gestureLibrary?.recognize(gesture)
            val mostLikelyPrediction: Prediction? = predictions?.maxBy { it.score }
            val charm = charmList?.find { it.name == mostLikelyPrediction?.name }
            startCharmTimer(charm!!)

            if (gesture.strokesCount > 0) {
                for (stroke in gesture.strokes) {
                    val path: Path = stroke.path
                    mCanvas!!.drawPath(path, mPaint!!)
                }
                gestureImage.setImageBitmap(mBitmap)
            }
        }

        readGesture.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    readGesture.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    mBitmap = Bitmap.createBitmap(
                        readGesture.width,
                        readGesture.height,
                        Bitmap.Config.ARGB_8888
                    )
                    mCanvas = Canvas(mBitmap!!)
                }
            })
    }

    override fun onResume() {
        super.onResume()
        setUpGestureLibrary()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            Snackbar.make(rootLayout, data?.extras?.getString("message").toString(), LENGTH_LONG)
                .show()
        }
    }

    private fun startCharmTimer(charm: Charm) {
        timerViewLayout.visibility = VISIBLE
        timerViewLayout.charmNameText.text = charm.name
        readGesture.isEnabled = false
        lava_fab.visibility = INVISIBLE
        lava_fab.isEnabled = false

        object : CountDownTimer((charm.duration * 1000).toLong(), 1000) {

            override fun onFinish() {
                timerViewLayout.visibility = INVISIBLE
                readGesture.isEnabled = true
                lava_fab.visibility = VISIBLE
                lava_fab.isEnabled = true
                gestureImage.setImageDrawable(null)
                mCanvas!!.drawColor(0, PorterDuff.Mode.CLEAR)
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
        startActivityForResult(activity, 1000)
    }

    private fun startListActivity(function: () -> Unit) {
        val activity = Intent(this, ListActivity::class.java)
        startActivity(activity)
    }

    private fun setUpGestureLibrary() {
        Log.d("log", "set up gesture library")
        gestureLibrary = GestureLibraries.fromFile("$filesDir/gestures")
        gestureLibrary?.load()
    }

    private fun makeCharmList() {
        charmList = realm.where<Charm>().findAll().sort("createdAt", Sort.ASCENDING)
    }

    private fun setUpPaint() {
        mPaint = Paint()
        mPaint?.style = Paint.Style.STROKE
        mPaint?.strokeWidth = 48F
        mPaint?.strokeCap = Paint.Cap.ROUND
        mPaint?.strokeJoin = Paint.Join.ROUND
        mPaint?.color = Color.parseColor("#488FAF")
        mPaint?.setShadowLayer(60F, 0F, 0F, Color.parseColor("#5DCEFF"));
    }
}
