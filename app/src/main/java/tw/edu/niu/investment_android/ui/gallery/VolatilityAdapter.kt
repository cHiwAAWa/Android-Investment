package tw.edu.niu.investment_android.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tw.edu.niu.investment_android.R

class VolatilityAdapter(private val volatilities: List<Volatility>) :
    RecyclerView.Adapter<VolatilityAdapter.VolatilityViewHolder>() {

    class VolatilityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSymbol: TextView = itemView.findViewById(R.id.tv_symbol)
        val tvVolatility: TextView = itemView.findViewById(R.id.tv_volatility)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VolatilityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_volatility, parent, false)
        return VolatilityViewHolder(view)
    }

    override fun onBindViewHolder(holder: VolatilityViewHolder, position: Int) {
        val volatility = volatilities[position]
        holder.tvSymbol.text = volatility.symbol
        holder.tvVolatility.text = String.format("%.2f%%", volatility.volatility)
    }

    override fun getItemCount(): Int = volatilities.size
}