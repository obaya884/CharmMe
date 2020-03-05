package app.takumi.obayashi.charmme

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
            setChildOnClickListener(com.bitvale.lavafab.Child.TOP) { showToast() }
            setChildOnClickListener(com.bitvale.lavafab.Child.LEFT) { showToast() }
        }

    }

    private fun showToast() = Toast.makeText(this, "Child clicked", Toast.LENGTH_SHORT).show()

}
