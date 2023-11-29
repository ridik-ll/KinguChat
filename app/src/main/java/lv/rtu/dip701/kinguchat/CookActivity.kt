package lv.rtu.dip701.kinguchat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class CookActivity : AppCompatActivity(), OrderAdapter.OnOrderActionListener {
    private lateinit var orderList: ArrayList<Order>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private lateinit var database: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var ordersListener: ChildEventListener
    private lateinit var ordersRef: DatabaseReference
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cook)
        supportActionBar?.hide()

        orderList = ArrayList()
        recyclerView = findViewById(R.id.order_list)
        adapter = OrderAdapter(orderList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser
        database = FirebaseDatabase.getInstance().reference.child("orders")

        adapter.setUserRole("cook")
        adapter.setOnOrderActionListener(this)

        fetchOrders()
    }


    private fun fetchOrders() {
        ordersRef = database
        ordersListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val order = dataSnapshot.getValue(Order::class.java)
                if (order != null) {
                    orderList.add(order)
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(orderList.size - 1) // Scroll to the latest order
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val order = dataSnapshot.getValue(Order::class.java)
                if (order != null) {
                    val index = orderList.indexOfFirst { it.orderId == order.orderId }
                    if (index != -1) {
                        orderList[index] = order
                        adapter.notifyItemChanged(index)
                    }
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val order = dataSnapshot.getValue(Order::class.java)
                if (order != null) {
                    val index = orderList.indexOfFirst { it.orderId == order.orderId }
                    if (index != -1) {
                        orderList.removeAt(index)
                        adapter.notifyItemRemoved(index)
                    }
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // Not used in this scenario
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, "Failed to fetch orders", Toast.LENGTH_SHORT).show()
            }
        }

        database.addChildEventListener(ordersListener)
    }

    override fun onOrderMarkReady(order: Order) {
        val orderId = order.orderId
        val orderIndex = orderList.indexOfFirst { it.orderId == orderId }

        if (orderIndex != -1) {
            val orderRef = ordersRef.child(orderId)
            orderRef.child("ready").setValue(true)
                .addOnSuccessListener {
                    adapter.updateOrderReadyStatus(orderId, true)
                    Toast.makeText(applicationContext, "Order marked as ready", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "Failed to mark order as ready", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(applicationContext, "Order not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOrderDelete(order: Order) {
        val orderRef = database.child(order.orderId)
        orderRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Order deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Failed to delete order", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onItemClick(order: Order) {
        // Implementation of actions when an order item is clicked
        // For example, opening detailed information about the order
    }
}
