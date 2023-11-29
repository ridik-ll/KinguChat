package lv.rtu.dip701.kinguchat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button

    private lateinit var database: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        searchButton = findViewById(R.id.search_button)
        mAuth = FirebaseAuth.getInstance()
        val chatsButton = findViewById<Button>(R.id.chats_button)

        database = FirebaseDatabase.getInstance().reference.child("users")

        searchButton.setOnClickListener {
            val intent = Intent(this, DishActivity::class.java)
            startActivity(intent)
        }

        chatsButton.setOnClickListener {
            val currentUser = mAuth.currentUser
            val currentUserRole = currentUser?.displayName

            if (currentUserRole == "Cashier") {
                val intent = Intent(this, ChatActivity::class.java)
                startActivity(intent)
            } else if (currentUserRole == "Cook") {
                val intent = Intent(this, CookActivity::class.java)
                startActivity(intent)
            }
        }
    }


}
