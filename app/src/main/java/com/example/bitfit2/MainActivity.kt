package com.example.bitfit2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codepath.bitfit2.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

const val AVERAGE: String = "AVERAGE"
const val MINIMUM: String = "MINIMUM"
const val MAXIMUM: String = "MAXIMUM"

class MainActivity : AppCompatActivity(), DashboardFragment.OnClearListener {

    lateinit var logsFragment: FragmentLog
    lateinit var dashboardFragment: DashboardFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logsFragment = FragmentLog()
        dashboardFragment = DashboardFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, logsFragment, "log_fragment").commit()

        val addNutrition = findViewById<Button>(R.id.addNewNutritionButton)
        addNutrition.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, NutritionActivity::class.java)
            startActivityForResult(intent, 1)
        })

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.logs_menu -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, logsFragment, "log_fragment").commit()
                    true
                }
                R.id.dashboard_menu -> {
                    var (average, min, max) = calculateDashboardValues()
                    val bundle = Bundle()
                    bundle.putString(AVERAGE, average.toString())
                    bundle.putString(MINIMUM, min.toString())
                    bundle.putString(MAXIMUM, max.toString())
                    dashboardFragment.arguments = bundle
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, dashboardFragment, "dashboard_fragment").commit()
                    true
                }
                else -> {
                    true
                }
            }
        }
    }

    private fun calculateDashboardValues(): Triple<Int, Int, Int> {
        var nutritionCalories: ArrayList<Int> = ArrayList()
        for (nutrition in logsFragment.nutrition) {
            nutrition.calorieCount?.toInt()?.let { nutritionCalories.add(it) }
        }

        if (nutritionCalories.size == 0) {
            return Triple(0, 0, 0)
        }

        return Triple(
            nutritionCalories.average().toInt(),
            nutritionCalories.min(),
            nutritionCalories.max()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val nutritionResult = data?.getSerializableExtra("result") as Nutrition
                logsFragment.nutrition.add(nutritionResult)
                logsFragment.nutritionAdapter.notifyDataSetChanged()

                if (supportFragmentManager.findFragmentByTag("dashboard_fragment")?.isVisible == true) {
                    var (average, min, max) = calculateDashboardValues()

                    dashboardFragment.updateDashboard(
                        average.toString(),
                        min.toString(),
                        max.toString()
                    )
                }
                //data added to the DB
                lifecycleScope.launch(IO) {
                    (application as NutritionApplication).db.NutritionDao().insert(
                        NutritionEntity(
                            nutritionName = nutritionResult.nutritionName,
                            calorieCount = nutritionResult.calorieCount
                        )
                    )
                }
            }
        }
    }


    override fun onClearData() {
        logsFragment.nutrition.clear()
        logsFragment.nutritionAdapter.notifyDataSetChanged()
    }
}