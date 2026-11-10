package com.example.interestcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.interestcalculator.data.repository.BankDataRepository
import com.example.interestcalculator.ui.MainBankScreen
import com.example.interestcalculator.ui.theme.InterestCalculatorTheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: BankDataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = BankDataRepository(applicationContext)

        setContent {
            InterestCalculatorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainBankScreen(repository = repository)
                }
            }
        }
    }
}