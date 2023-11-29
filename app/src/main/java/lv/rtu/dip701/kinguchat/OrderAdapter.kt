package lv.rtu.dip701.kinguchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderAdapter(private var orderList: ArrayList<Order>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private var listener: OnOrderActionListener? = null
    private var userRole: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val currentOrder = orderList[position]
        holder.bindOrder(currentOrder)

        // Показывать или скрывать кнопки в зависимости от роли пользователя
        if (userRole == "cook") {
            holder.showButtons()
        } else {
            holder.hideButtons()
        }

        holder.readyPackageTextView.visibility = if (currentOrder.ready) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    fun updateOrderReadyStatus(orderId: String, ready: Boolean) {
        val order = orderList.find { it.orderId == orderId }
        order?.ready = ready
        notifyDataSetChanged()
    }

    fun setOnOrderActionListener(listener: OnOrderActionListener) {
        this.listener = listener
    }

    fun setUserRole(role: String) {
        userRole = role
        notifyDataSetChanged()
    }

    fun updateOrderList(newOrderList: ArrayList<Order>) {
        orderList = newOrderList
        notifyDataSetChanged()
    }

    interface OnOrderActionListener {
        fun onItemClick(order: Order)
        fun onOrderMarkReady(order: Order)
        fun onOrderDelete(order: Order)
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dishTextView: TextView = itemView.findViewById(R.id.dish_text_view)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantity_text_view)
        val markReadyButton: Button = itemView.findViewById(R.id.mark_ready_button)
        private val deleteButton: Button = itemView.findViewById(R.id.delete_button)
        val readyPackageTextView: TextView = itemView.findViewById(R.id.ready_package_text_view)

        init {
            markReadyButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val order = orderList[position]
                    listener?.onOrderMarkReady(order)
                }
            }

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val order = orderList[position]
                    listener?.onOrderDelete(order)
                }
            }
        }

        fun bindOrder(order: Order) {
            dishTextView.text = order.dishName
            quantityTextView.text = order.quantity
        }

        fun showButtons() {
            markReadyButton.visibility = View.VISIBLE

        }

        fun hideButtons() {
            markReadyButton.visibility = View.GONE

        }
    }
}
