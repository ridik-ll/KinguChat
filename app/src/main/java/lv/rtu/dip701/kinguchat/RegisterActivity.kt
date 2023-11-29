package lv.rtu.dip701.kinguchat

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var registerButton: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        roleSpinner = findViewById(R.id.role_spinner)
        registerButton = findViewById(R.id.register_button)

        // Создание ArrayAdapter для ролей
        val roles = arrayOf("Cook", "Cashier")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val role = roleSpinner.selectedItem.toString()

            register(email, password, role)
        }
    }

    private fun register(email: String, password: String, role: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Регистрация выполнена успешно
                    addUserToDb(email, role, mAuth.currentUser?.uid!!)
                    Toast.makeText(this, "Регистрация выполнена успешно!", Toast.LENGTH_SHORT).show()

                    // Обновление профиля пользователя с выбранной ролью
                    val user = mAuth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(role)
                        .build()
                    user?.updateProfile(profileUpdates)


                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Ошибка во время регистрации
                    Toast.makeText(this, "Ошибка регистрации. Проверьте данные", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToDb(email: String, role: String, uid: String) {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("users").child(uid).setValue(User(email, role, uid))
            .addOnSuccessListener {
                // Пользователь успешно добавлен в базу данных
                Toast.makeText(this, "Пользователь успешно добавлен в базу данных", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Ошибка при добавлении пользователя в базу данных
                Toast.makeText(this, "Ошибка при добавлении пользователя в базу данных: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
