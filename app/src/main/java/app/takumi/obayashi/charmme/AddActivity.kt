package app.takumi.obayashi.charmme

import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.save_dialog.view.*
import java.util.*

class AddActivity : AppCompatActivity() {

    private var gestureLibrary: GestureLibrary? = null
    private val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        // GestureLibrary取得
        setUpGestureLibrary()

        readGesture.addOnGesturePerformedListener { _, gesture ->

            if (effectEditText.text.isEmpty() && timeEditText.text.isEmpty()) {
                Toast.makeText(this, "効果名と持続時間を入力してください", Toast.LENGTH_SHORT).show()
            } else {
                val saveDialog = layoutInflater.inflate(R.layout.save_dialog, null)

                // ジェスチャーイメージ
                val bitmap = gesture.toBitmap(128, 128, 10, -0x10000)

                // ダイアログの要素に代入
                saveDialog.gestureBitMap.setImageBitmap(bitmap)
                saveDialog.gestureName.text = effectEditText.text
                saveDialog.effectDuration.text = timeEditText.text

                // ダイアログ表示
                AlertDialog.Builder(this)
                    .setView(saveDialog)
                    .setPositiveButton("保存") { _, _ ->
                        // ジェスチャーライブラリの保存
                        gestureLibrary!!.addGesture(effectEditText.text.toString(), gesture)
                        gestureLibrary!!.save()

                        realm.executeTransaction {
                            val charm =
                                it.createObject(Charm::class.java, UUID.randomUUID().toString())
                            charm.name = effectEditText.text.toString()
                            charm.duration = timeEditText.text.toString().toInt()
                        }

                        Toast.makeText(this, "登録完了", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }

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
}
