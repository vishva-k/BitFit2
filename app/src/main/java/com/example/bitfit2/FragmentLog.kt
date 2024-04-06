package com.example.bitfit2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.bitfit2.R
import kotlinx.coroutines.launch


class FragmentLog : Fragment() {
    private lateinit var nutritionRV: RecyclerView
    lateinit var nutritionAdapter: NutritionAdapter
    val nutrition = mutableListOf<Nutrition>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_logs, container, false)

        // Setup the recycler view and it's data
        nutritionRV = view.findViewById(R.id.nutritionRV)
        nutritionAdapter = NutritionAdapter(view.context, nutrition)
        nutritionRV.adapter = nutritionAdapter
        nutritionRV.layoutManager = LinearLayoutManager(view.context).also {
            val dividerItemDecorator = DividerItemDecoration(view.context, it.orientation)
            nutritionRV.addItemDecoration(dividerItemDecorator)
        }

        lifecycleScope.launch{
            (activity?.application as NutritionApplication).db.NutritionDao().getAll().collect{
                    databaseList -> databaseList.map { entity ->
                Nutrition(
                    entity.nutritionName,
                    entity.calorieCount
                )
            }.also { mappedList ->
                nutrition.clear()
                nutrition.addAll(mappedList)
                nutritionAdapter.notifyDataSetChanged() }
            }
        }

        return view
    }
}