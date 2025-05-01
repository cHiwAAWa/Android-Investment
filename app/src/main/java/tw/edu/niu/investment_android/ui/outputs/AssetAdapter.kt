package tw.edu.niu.investment_android.ui.outputs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tw.edu.niu.investment_android.R
import java.io.File
import java.io.FileWriter

class AssetAdapter(
    private val assets: MutableList<Asset>,
    private val onEditClick: (Asset, Int) -> Unit
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

        // 修改按鈕點擊事件
        holder.btnEdit.setOnClickListener {
            onEditClick(asset, position)
        }

        // 刪除按鈕點擊事件
        holder.btnDelete.setOnClickListener {
            assets.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, assets.size)
            updatePortfolioToml(holder.itemView.context)
        }
    }

    override fun getItemCount(): Int = assets.size

    // 更新 portfolio.toml 檔案
    private fun updatePortfolioToml(context: android.content.Context) {
        val file = File(context.filesDir, "portfolio.toml")
        val groupedAssets = assets.groupBy { it.category }

        FileWriter(file).use { writer ->
            groupedAssets.forEach { (category, assetList) ->
                writer.write("[$category]\n")
                assetList.forEach { asset ->
                    val amount = asset.amount.toDoubleOrNull()
                    if (amount != null) {
                        writer.write("${asset.symbol} = $amount\n")
                    } else {
                        writer.write("${asset.symbol} = \"${asset.amount}\"\n")
                    }
                }
                writer.write("\n")
            }
        }
    }
}