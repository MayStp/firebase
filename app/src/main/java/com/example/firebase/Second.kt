package com.example.firebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.firebase.databinding.ActivitySecondBinding
import com.google.firebase.firestore.FirebaseFirestore

class Second : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private val budgetCollectionRef = firestore.collection("budgets")
    private lateinit var binding: ActivitySecondBinding
    private var updateId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)


        updateId = intent.getStringExtra("UPDATE_ID") ?: ""
        val receivedNominal = intent.getStringExtra("NOMINAL")
        val receivedDescription = intent.getStringExtra("DESCRIPTION")
        val receivedDate = intent.getStringExtra("DATE")

        // Kemudian, gunakan data yang diterima untuk mengisi EditText atau di tempat lainnya
        binding.edtNominal.setText(receivedNominal)
        binding.edtDesc.setText(receivedDescription)
        binding.edtDate.setText(receivedDate)

        binding.btnUpdate.setOnClickListener {
            onUpdateClicked() // Panggil fungsi onUpdateClicked saat tombol diklik
        }

        binding.btnAdd.setOnClickListener {
            val nominal = binding.edtNominal.text.toString()
            val description = binding.edtDesc.text.toString()
            val date = binding.edtDate.text.toString()
            val newBudget = Budget(judul_aduan = nominal, description = description,
                pengadu = date)
            if (updateId.isNotEmpty()) {
                newBudget.id = updateId
                updateBudget(newBudget)
            } else {
                addBudget(newBudget)
            }
        }
    }

    private fun addBudget(budget: Budget) {
        budgetCollectionRef.add(budget)
            .addOnSuccessListener { documentReference ->
                val createdBudgetId = documentReference.id
                budget.id = createdBudgetId
                documentReference.set(budget)
                    .addOnSuccessListener {
                        Log.d("SecondActivity", "Budget successfully added!")
                        navigateToMainActivity()
                    }
                    .addOnFailureListener { e ->
                        Log.d("SecondActivity", "Error adding budget: ", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.d("SecondActivity", "Error adding budget: ", e)
            }
    }

    private fun updateBudget(budget: Budget) {
        budgetCollectionRef.document(budget.id)
            .set(budget)
            .addOnSuccessListener {
                Log.d("SecondActivity", "Budget successfully updated!")
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                Log.d("SecondActivity", "Error updating budget: ", e)
            }
    }

    private fun onUpdateClicked() {
        val nominal = binding.edtNominal.text.toString()
        val description = binding.edtDesc.text.toString()
        val date = binding.edtDate.text.toString()
        val updateBudget = Budget(judul_aduan = nominal, description = description,
            pengadu = date)

        if (updateId.isNotEmpty()) {
            updateBudget.id = updateId
            updateBudget(updateBudget)
        } else {
            addBudget(updateBudget)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@Second, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
