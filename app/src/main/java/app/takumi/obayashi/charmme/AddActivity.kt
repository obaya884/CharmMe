package app.takumi.obayashi.charmme

import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.save_dialog.view.*

class AddActivity : AppCompatActivity() {

    private var gestureLibrary: GestureLibrary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        // GestureLibrary取得
        gestureLibrary = getMyGestureLibrary()

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
                saveDialog.effectTime.text = timeEditText.text

                // ダイアログ表示
                AlertDialog.Builder(this)
                    .setView(saveDialog)
                    .setPositiveButton("保存") { _, _ ->
                        // ジェスチャー保存
                        gestureLibrary!!.addGesture(effectEditText.text.toString(), gesture)
                        // 保存
                        gestureLibrary!!.save()
                        Toast.makeText(this, "登録完了", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }

        }
    }

    // GestureLibrary取得
    private fun getMyGestureLibrary(): GestureLibrary? {
        return GestureLibraries.fromPrivateFile(this, "xxxx.gesture")
    }

}
