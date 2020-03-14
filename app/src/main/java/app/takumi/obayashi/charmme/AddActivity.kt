package app.takumi.obayashi.charmme

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.gesture.Gesture
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.gesture.GestureOverlayView
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.TimePicker
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_add.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow


class AddActivity : FragmentActivity(), TimePickerDialog.OnTimeSetListener {

    private var inputMethodManager: InputMethodManager? = null

    private var gestureLibrary: GestureLibrary? = null
    private val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }
    private var charmList: RealmResults<Charm>? = null

    private var mPaint: Paint? = null
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private var drawnGesture: Gesture? = null

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
        setContentView(R.layout.activity_add)

        makeCharmList()
        setUpGestureLibrary()
        setUpPaint()

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

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

        readGesture.addOnGesturePerformedListener { _, gesture ->
            drawnGesture = gesture
            if (gesture.strokesCount > 0) {
                for (stroke in gesture.strokes) {
                    val path: Path = stroke.path
                    mCanvas!!.drawPath(path, mPaint!!)
                }
                gestureImage.setImageBitmap(mBitmap)
            }
        }

        readGesture.addOnGestureListener(object : GestureOverlayView.OnGestureListener {
            override fun onGestureStarted(overlay: GestureOverlayView, event: MotionEvent) {
                gestureImage.setImageDrawable(null)
                drawnGesture = null
                mCanvas!!.drawColor(0, PorterDuff.Mode.CLEAR)
            }

            override fun onGestureEnded(overlay: GestureOverlayView, event: MotionEvent) {}
            override fun onGestureCancelled(overlay: GestureOverlayView, event: MotionEvent) {}
            override fun onGesture(overlay: GestureOverlayView, event: MotionEvent) {}
        })

        backFloatingActionButton.setOnClickListener { finish() }
        addFloatingActionButton.setOnClickListener(addFloatingActionButtonClickListener)
        timeEditText.setOnClickListener(timeEditTextClickListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // キーボードを隠す
        inputMethodManager?.hideSoftInputFromWindow(
            rootLayout.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
        // 背景にフォーカスを移す
        rootLayout.requestFocus();

        return super.dispatchTouchEvent(ev)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val calender = Calendar.getInstance()
        calender.set(0, 0, 0, hourOfDay, minute)
        val setTime = SimpleDateFormat("HH:mm", Locale.JAPAN).format(calender.time) + ":00"
        timeEditText.setText(setTime)
    }

    private val addFloatingActionButtonClickListener = View.OnClickListener { view: View ->
        val isFillEffect = effectEditText.text.isNotBlank()
        val isFillTime = timeEditText.text.isNotBlank()
        val isDrawnGesture = (drawnGesture != null)

        val newCharmName = effectEditText.text.toString()
        val newCharmDuration = timeEditText.text.toString()

        var errorMessage = ""
        if (!isFillEffect) {
            errorMessage += if (errorMessage.isEmpty()) {
                "効果名"
            } else {
                "と効果名"
            }
        }
        if (!isFillTime) {
            errorMessage += if (errorMessage.isEmpty()) {
                "持続時間"
            } else {
                "と持続時間"
            }
        }
        if (!isDrawnGesture) {
            errorMessage += if (errorMessage.isEmpty()) {
                "ジェスチャー"
            } else {
                "とジェスチャー"
            }
        }

        // 同一名登録されないかのチェック
        charmList?.forEach {
            if (it.name == newCharmName) {
                Snackbar.make(view, "${newCharmName}は既に登録されています", LENGTH_LONG).show()
                return@OnClickListener
            }
        }

        if (isFillEffect && isFillTime && isDrawnGesture) {
            // ジェスチャーライブラリの保存
            gestureLibrary!!.addGesture(newCharmName, drawnGesture)
            gestureLibrary!!.save()

            realm.executeTransaction {
                val charm =
                    it.createObject(Charm::class.java, UUID.randomUUID().toString())
                charm.name = newCharmName
                charm.duration = string2TimeInt(newCharmDuration)
            }

            val intent = Intent()
            intent.putExtra("message", "${newCharmName}が使えるようになった！")
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            Snackbar.make(view, "${errorMessage}を入力してください", LENGTH_LONG).show()

        }
    }

    private val timeEditTextClickListener = View.OnClickListener {
        TimePickerFragment().show(supportFragmentManager, "timePicker")
    }

    private fun setUpGestureLibrary() {
        gestureLibrary = GestureLibraries.fromFile("$filesDir/gestures")
        gestureLibrary?.load()
    }

    private fun setUpPaint() {
        mPaint = Paint()
        mPaint?.style = Paint.Style.STROKE
        mPaint?.strokeWidth = 48F
        mPaint?.strokeCap = Paint.Cap.ROUND
        mPaint?.strokeJoin = Paint.Join.ROUND
    }

    private fun makeCharmList() {
        charmList = realm.where<Charm>().findAll().sort("createdAt", Sort.ASCENDING)
    }

    private fun string2TimeInt(timeString: String): Int {
        val timeArray = timeString.split(":")
        var timeDuration = 0.0

        for ((index, value) in timeArray.reversed().withIndex()) {
            timeDuration += value.toDouble() * 60.0.pow(index)
        }
        return timeDuration.toInt()
    }
}
