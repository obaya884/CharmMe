package app.takumi.obayashi.charmme

import android.content.Context
import android.gesture.GestureLibrary
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.list_item.view.*


class CharmAdapter(
    private val context: Context,
    private var charmList: OrderedRealmCollection<Charm>?,
    private var gestureLibrary: GestureLibrary?,
    private var listener: OnItemClickListener,
    private val autoUpdate: Boolean
) : RealmRecyclerViewAdapter<Charm, CharmAdapter.CharmViewHolder>(charmList, autoUpdate) {

    override fun getItemCount(): Int {
        return charmList?.size ?: 0
    }

    override fun onBindViewHolder(holder: CharmViewHolder, position: Int) {
        val charm: Charm = charmList?.get(position) ?: return

        // ジェスチャーイメージ
        val gestures = gestureLibrary?.getGestures(charm.name)
        val gestureBitMap = gestures?.get(0)?.toBitmap(80, 80, 10, -0x10000)

        holder.gestureImage.setImageBitmap(gestureBitMap)
        holder.effectName.text = charm.name
        holder.effectDuration.text = charm.duration.toString()

        holder.container.setOnClickListener {
            listener.onItemClick(charm)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharmViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return CharmViewHolder(v)
    }

    class CharmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.container
        val gestureImage: ImageView = view.gestureImage
        val effectName: TextView = view.effectName
        val effectDuration: TextView = view.effectDuration
    }

    interface OnItemClickListener {
        fun onItemClick(item: Charm)
    }
}