package tw.edu.niu.investment_android.ui.outputs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tw.edu.niu.investment_android.R

class AssetAdapter(
    private val assets: MutableList<Asset>,
    private val onEditClick: (Asset, Int) -> Unit,
    private val onDeleteClick: (Asset, Int) -> Unit
) : RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

    class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tv_category)
        val tvSymbol: TextView = itemView.findViewById(R.id.tv_symbol)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asset, parent, false)
        return AssetViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        val asset = assets[position]
        holder.tvCategory.text = asset.category
        holder.tvSymbol.text = asset.symbol
        holder.tvAmount.text = asset.amount

        holder.btnEdit.setOnClickListener {
            onEditClick(asset, position)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(asset, position)
        }
    }

    override fun getItemCount(): Int = assets.size

    /** 外部呼叫：刪除一筆，並更新畫面 */
    fun deleteAt(position: Int) {
        assets.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, assets.size)
    }
}
