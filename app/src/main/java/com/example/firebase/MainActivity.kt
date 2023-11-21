package com.example.firebase

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private val budgetCollectionRef = firestore.collection("budgets")
    private lateinit var binding: ActivityMainBinding
    private var updateId = ""
    private val budgetListLiveData: MutableLiveData<List<Budget>> by lazy {
        MutableLiveData<List<Budget>>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            btnAdd.setOnClickListener {
                val nominal = edtNominal.text.toString()
                val description = edtDesc.text.toString()
                val date = edtDate.text.toString()
                val newBudget = Budget(nominal = nominal, description = description,
                    date = date)
                addBudget(newBudget)
            }
            btnUpdate.setOnClickListener {
                val nominal = edtNominal.text.toString()
                val description = edtDesc.text.toString()
                val date = edtDate.text.toString()
                val budgetToUpdate = Budget(nominal = nominal, description =
                description, date = date)
                updateBudget(budgetToUpdate)
                updateId = ""
                setEmptyField()
            }
//            btnClear.setOnClickListener {
//                setEmptyField()
//            }
            listView.setOnItemClickListener { adapterView, _, i, _->
                val item = adapterView.adapter.getItem(i) as Budget
                updateId = item.id
                edtNominal.setText(item.nominal)
                edtDesc.setText(item.description)
                edtDate.setText(item.date)
            }
            listView.onItemLongClickListener = AdapterView.OnItemLongClickListener {
                    adapterView, _, i, _->
                val item = adapterView.adapter.getItem(i) as Budget
                deleteBudget(item)
                true
            }
        }
        observeBudgets()
        getAllBudgets()
    }


    private fun getAllBudgets() {
        observeBudgetChanges()
    }
    private fun observeBudgets() {
        budgetListLiveData.observe(this) { budgets ->
            val adapter = BudgetAdapter(this, budgets)
            binding.listView.adapter = adapter
        }

        // Old code
//        budgetListLiveData.observe(this) { budgets->
//            val adapter = ArrayAdapter(
//                this,
//                android.R.layout.simple_list_item_1,
//                budgets.toMutableList()
//            )
//            binding.listView.adapter = adapter
//        }
    }
    private fun observeBudgetChanges() {
        budgetCollectionRef.addSnapshotListener { snapshots, error->
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
    private fun addBudget(budget: Budget) {
        budgetCollectionRef.add(budget)
            .addOnSuccessListener { documentReference->
                val createdBudgetId = documentReference.id
                budget.id = createdBudgetId
                documentReference.set(budget)
                    .addOnFailureListener {
                        Log.d("MainActivity", "Error updating budget ID: ", it)
                    }
            }
            .addOnFailureListener {
                Log.d("MainActivity", "Error adding budget: ", it)
            }
    }
    private fun updateBudget(budget: Budget) {
        budget.id = updateId
        budgetCollectionRef.document(updateId).set(budget)
            .addOnFailureListener {
                Log.d("MainActivity", "Error updating budget: ", it)
            }
    }
    private fun deleteBudget(budget: Budget) {
        if (budget.id.isEmpty()) {
            Log.d("MainActivity", "Error deleting: budget ID is empty!")
            return
        }
        budgetCollectionRef.document(budget.id).delete()
            .addOnFailureListener {
                Log.d("MainActivity", "Error deleting budget: ", it)
            }
    }
    private fun setEmptyField() {
        with(binding) {
            edtNominal.setText("")
            edtDesc.setText("")
            edtDate.setText("")
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

            tvNominal.text = budget.nominal
            tvDescription.text = budget.description
            tvDate.text = budget.date

            return itemView
        }
    }
}

