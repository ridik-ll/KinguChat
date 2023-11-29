package lv.rtu.dip701.kinguchat

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity(), OrderAdapter.OnOrderActionListener {
    private lateinit var dishEditText: EditText
    private lateinit var quantityEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageAdapter: OrderAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var dishAdapter: DishAdapter

    private lateinit var dishesRef: DatabaseReference
    private lateinit var ordersRef: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        supportActionBar?.hide()

        dishEditText = findViewById(R.id.dish_edit_text)
        quantityEditText = findViewById(R.id.quantity_edit_text)
        sendButton = findViewById(R.id.send_button)
        messageRecyclerView = findViewById(R.id.message_list)
        recyclerView = findViewById(R.id.dishRecyclerView)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser
        database = FirebaseDatabase.getInstance()
        dishesRef = database.reference.child("dishes")
        ordersRef = database.reference.child("orders")

        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = OrderAdapter(ArrayList())
        messageAdapter.setOnOrderActionListener(this)
        messageRecyclerView.adapter = messageAdapter

        recyclerView.layoutManager = LinearLayoutManager(this)
        dishAdapter = DishAdapter(ArrayList())
        recyclerView.adapter = dishAdapter

        // Set the default value of amount field to 1
        quantityEditText.setText("1")

        sendButton.setOnClickListener {
            val dishName = dishEditText.text.toString().trim()
            val quantity = quantityEditText.text.toString().trim()

            if (dishName.isNotEmpty()) {
                val userId = currentUser?.uid ?: ""
                val order = Order("", dishName, quantity, userId, false)
                sendOrderToCook(order)
            } else {
                Toast.makeText(applicationContext, "Please enter a dish", Toast.LENGTH_SHORT).show()
            }
        }

        fetchDishes()

        val ordersListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val orderList = ArrayList<Order>()

                for (snapshot in dataSnapshot.children) {
                    val order = snapshot.getValue(Order::class.java)
                    if (order != null) {
                        orderList.add(order)
                    }
                }

                messageAdapter.updateOrderList(orderList)

                // Scroll RecyclerView to the last item
                messageRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, "Failed to fetch orders", Toast.LENGTH_SHORT).show()
            }
        }

        ordersRef.addValueEventListener(ordersListener)
    }

    private fun fetchDishes() {
        dishesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dishList = ArrayList<String>()

                for (snapshot in dataSnapshot.children) {
                    val dishName = snapshot.getValue(String::class.java)
                    dishName?.let {
                        dishList.add(dishName)
                    }
                }

                dishAdapter.updateDishList(dishList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, "Failed to fetch dishes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendOrderToCook(order: Order) {
        val orderId = ordersRef.push().key ?: ""
        order.orderId = orderId

        val orderRef = ordersRef.child(orderId)
        orderRef.setValue(order)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Заказ отправлен повару", Toast.LENGTH_SHORT).show()
                dishEditText.text.clear()

                val currentAmount = quantityEditText.text.toString().toInt()
                quantityEditText.setText((currentAmount + 1).toString())
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Не удалось отправить заказ", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onOrderMarkReady(order: Order) {
        if (order.userId == currentUser?.uid) {
            val orderId = order.orderId

            val orderRef = ordersRef.child(orderId)
            orderRef.child("ready").setValue(true)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Order marked as ready", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "Failed to mark order as ready", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onOrderDelete(order: Order) {
        if (order.userId == currentUser?.uid) {
            val orderId = order.orderId

            val orderRef = ordersRef.child(orderId)
            orderRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Order deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "Failed to delete order", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onItemClick(order: Order) {
        // Handle click on list item
        // You can add your own logic here
    }
}
