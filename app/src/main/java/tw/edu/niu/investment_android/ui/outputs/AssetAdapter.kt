package tw.edu.niu.investment_android.ui.outputs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tw.edu.niu.investment_android.R

class AssetAdapter(private val assets: List<Asset>) :
    RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

    class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tv_category)
        val tvSymbol: TextView = itemView.findViewById(R.id.tv_symbol)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)
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
    }

    override fun getItemCount(): Int = assets.size
}