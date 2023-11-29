package lv.rtu.dip701.kinguchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class DishActivity : AppCompatActivity() {
    private lateinit var dishNameEditText: EditText
    private lateinit var addButton: Button
    private lateinit var dishRecyclerView: RecyclerView
    private lateinit var dishAdapter: DishAdapter
    private lateinit var dishesRef: DatabaseReference
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dish)

        dishNameEditText = findViewById(R.id.dish_name_edit_text)
        addButton = findViewById(R.id.add_button)
        dishRecyclerView = findViewById(R.id.dish_recycler_view)
        deleteButton = findViewById(R.id.delete_button)
        deleteButton.setOnClickListener {
            showDeleteDialog()
        }

        // Получение ссылки на "dishes" в базе данных
        val database = FirebaseDatabase.getInstance()
        dishesRef = database.reference.child("dishes")

        addButton.setOnClickListener {
            val dishName = dishNameEditText.text.toString().trim()

            if (dishName.isNotEmpty()) {
                val dishRef = dishesRef.push()
                dishRef.setValue(dishName)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "Dish added to database", Toast.LENGTH_SHORT).show()
                        dishNameEditText.text.clear()
                    }
                    .addOnFailureListener {
                        Toast.makeText(applicationContext, "Failed to add dish to database", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(applicationContext, "Please enter a dish name", Toast.LENGTH_SHORT).show()
            }
        }

        // Настройка RecyclerView и адаптера
        dishRecyclerView.layoutManager = LinearLayoutManager(this)
        dishAdapter = DishAdapter(ArrayList())
        dishRecyclerView.adapter = dishAdapter

        // Отслеживание изменений в базе данных и обновление списка блюд
        val dishListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dishList = ArrayList<String>()
                for (dishSnapshot in dataSnapshot.children) {
                    val dish = dishSnapshot.getValue(String::class.java)
                    dish?.let { dishList.add(it) }
                }
                dishAdapter.updateDishList(dishList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, "Failed to fetch dishes", Toast.LENGTH_SHORT).show()
            }
        }
        dishesRef.addValueEventListener(dishListener)
    }

    private fun showDeleteDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Select a dish to delete")

        val dishArray = dishAdapter.getDishList().toTypedArray()

        dialogBuilder.setItems(dishArray) { _, which ->
            val dishNameToDelete = dishArray[which]
            deleteDishFromDatabase(dishNameToDelete)
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun deleteDishFromDatabase(dishName: String) {
        val dishRef = dishesRef.orderByValue().equalTo(dishName)
        dishRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    childSnapshot.ref.removeValue()
                }
                Toast.makeText(this@DishActivity, "Dish deleted: $dishName", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@DishActivity, "Failed to delete dish", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
