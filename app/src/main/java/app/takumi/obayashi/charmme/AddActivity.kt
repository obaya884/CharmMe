package app.takumi.obayashi.charmme

import android.app.Activity
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
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_add.*
import java.util.*


class AddActivity : AppCompatActivity() {

    private var gestureLibrary: GestureLibrary? = null
    private val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    private var mPaint: Paint? = null
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private var drawnGesture: Gesture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        setUpGestureLibrary()
        setUpPaint()

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
    }

    private val addFloatingActionButtonClickListener = View.OnClickListener { view: View ->
        val isFillEffect = effectEditText.text.isNotBlank()
        val isFillTime = timeEditText.text.isNotBlank()
        val isDrawnGesture = (drawnGesture != null)

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

        if (isFillEffect && isFillTime && isDrawnGesture) {
            // ジェスチャーライブラリの保存
            gestureLibrary!!.addGesture(effectEditText.text.toString(), drawnGesture)
            gestureLibrary!!.save()

            realm.executeTransaction {
                val charm =
                    it.createObject(Charm::class.java, UUID.randomUUID().toString())
                charm.name = effectEditText.text.toString()
                charm.duration = timeEditText.text.toString().toInt()
            }

            val intent = Intent()
            intent.putExtra("message", "${effectEditText.text.toString()}が使えるようになった！")
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            Snackbar.make(view, "${errorMessage}を入力してください", LENGTH_SHORT).show()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
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

}
