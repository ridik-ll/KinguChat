package lv.rtu.dip701.kinguchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DishAdapter(private var dishList: ArrayList<String>) :
    RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dish, parent, false)
        return DishViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val currentDish = dishList[position]
        holder.bindDish(currentDish)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val dishEditText = (context as ChatActivity).findViewById<EditText>(R.id.dish_edit_text)
            val currentText = dishEditText.text.toString()
            val newText = if (currentText.isNotEmpty()) "$currentText $currentDish" else currentDish
            dishEditText.setText(newText)
        }
    }

    override fun getItemCount(): Int {
        return dishList.size
    }

    fun getDishList(): ArrayList<String> {
        return dishList
    }

    fun updateDishList(newDishList: ArrayList<String>) {
        dishList = newDishList
        notifyDataSetChanged()
    }

    inner class DishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dishNameTextView: TextView = itemView.findViewById(R.id.dish_name_text_view)

        fun bindDish(dishName: String) {
            dishNameTextView.text = dishName
        }
    }
}
