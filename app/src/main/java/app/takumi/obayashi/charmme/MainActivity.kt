package app.takumi.obayashi.charmme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with(lava_fab) {
            setParentOnClickListener { lava_fab.trigger() }
            setChildOnClickListener(com.bitvale.lavafab.Child.TOP) { startAddActivity() }
            setChildOnClickListener(com.bitvale.lavafab.Child.LEFT) { startListActivity() }
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


}
