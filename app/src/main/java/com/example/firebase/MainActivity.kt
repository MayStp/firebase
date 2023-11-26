package com.example.firebase

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.example.firebase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private val budgetCollectionRef = firestore.collection("budgets")
    private lateinit var binding: ActivityMainBinding
    private val budgetListLiveData: MutableLiveData<List<Budget>> by lazy {
        MutableLiveData<List<Budget>>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeBudgets()
        getAllBudgets()

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, Second::class.java)
            startActivity(intent)
        }
    }

    private fun getAllBudgets() {
        observeBudgetChanges()
    }

    private fun observeBudgets() {
        budgetListLiveData.observe(this) { budgets ->
            val adapter = BudgetAdapter(this, budgets)
            binding.listView.adapter = adapter
        }
    }

    private fun observeBudgetChanges() {
        budgetCollectionRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("MainActivity", "Error listening for budget changes: ", error)
                return@addSnapshotListener
            }
            val budgets = snapshots?.toObjects(Budget::class.java)
            if (budgets != null) {
                budgetListLiveData.postValue(budgets)
            }
        }
    }

    inner class BudgetAdapter(
        context: Context,
        private val budgets: List<Budget>
    ) : ArrayAdapter<Budget>(context, R.layout.item_container, budgets) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val itemView = inflater.inflate(R.layout.item_container, parent, false)

            val tvNominal: TextView = itemView.findViewById(R.id.tvNominal)
            val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
            val tvDate: TextView = itemView.findViewById(R.id.tvDate)

            val budget = budgets[position]

            tvNominal.text = budget.judul_aduan
            tvDescription.text = budget.description
            tvDate.text = budget.pengadu

            itemView.setOnClickListener {
                // Saat item di ListView dipilih, kita akan memulai Second dengan mengirim ID budget yang dipilih
                val intent = Intent(this@MainActivity, Second::class.java)
                val selectedBudgetId = budgets[position].id
                intent.putExtra("UPDATE_ID", selectedBudgetId)
                intent.putExtra("NOMINAL", budget.judul_aduan)
                intent.putExtra("DESCRIPTION", budget.description)
                intent.putExtra("DATE", budget.pengadu)
                context.startActivity(intent)
            }

            itemView.setOnLongClickListener {
                val item = budgets[position]
                deleteBudget(item)
                true // Menandakan bahwa event sudah di-handle
            }

            return itemView
        }
    }

    fun deleteBudget(budget: Budget) {
        if (budget.id.isNotEmpty()) {
            budgetCollectionRef.document(budget.id)
                .delete()
                .addOnSuccessListener {
                    Log.d("MainActivity", "Budget successfully deleted!")
                }
                .addOnFailureListener { e ->
                    Log.d("MainActivity", "Error deleting budget: ", e)
                }
        }
    }
}


