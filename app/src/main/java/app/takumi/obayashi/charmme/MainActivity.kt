package app.takumi.obayashi.charmme

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with(lava_fab) {
            setParentOnClickListener { lava_fab.trigger() }
            setChildOnClickListener(com.bitvale.lavafab.Child.TOP) { startAddActivity() }
            setChildOnClickListener(com.bitvale.lavafab.Child.LEFT) { showToast() }
        }

    }

    private fun startAddActivity() {
        val addActivity = Intent(this, AddActivity::class.java)
        startActivityForResult(addActivity, 0)
    }

    private fun showToast() = Toast.makeText(this, "Child clicked", Toast.LENGTH_SHORT).show()

}
