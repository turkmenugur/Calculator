package com.trkmn.calculator

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.trkmn.calculator.databinding.ActivityMainBinding
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentInput: StringBuilder = StringBuilder()
    private var operand1: Double? = null
    private var pendingOperation: String? = null
    private var isNewOperationAfterEquals: Boolean = true

    private val decimalFormat = DecimalFormat("#.##########")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        resetCalculator()
        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        // Sayı Butonları
        val numberButtons = listOf(
            binding.button0, binding.button1, binding.button2, binding.button3, binding.button4,
            binding.button5, binding.button6, binding.button7, binding.button8, binding.button9
        )
        numberButtons.forEach { button ->
            button.setOnClickListener { onNumberClicked(button) }
        }

        // Ondalık Butonu
        binding.buttonDecimal.setOnClickListener { onDecimalClicked() }

        // Operatör Butonları
        val operatorButtons = listOf(
            binding.buttonAdd, binding.buttonSubtract, binding.buttonMultiply, binding.buttonDivide
        )
        operatorButtons.forEach { button ->
            button.setOnClickListener { onOperatorClicked(button) }
        }

        // Fonksiyon Butonları
        binding.buttonEquals.setOnClickListener { onEqualsClicked() }
        binding.buttonClear.setOnClickListener { onClearClicked() }
        binding.buttonDelete.setOnClickListener { onDeleteClicked() }
        binding.buttonPercent.setOnClickListener { onPercentClicked() }
        binding.buttonPlusMinus.setOnClickListener { onPlusMinusClicked() }
    }

    private fun onNumberClicked(button: Button) {
        val numberText = button.text.toString()

        if (isNewOperationAfterEquals) {
            currentInput.clear()
            isNewOperationAfterEquals = false
        }

        if (currentInput.toString() == "0" && numberText != "0" && !currentInput.contains(".")) {
            currentInput.clear()
        }
        if (currentInput.toString() == "0" && numberText == "0" && !currentInput.contains(".")) {
            return
        }

        currentInput.append(numberText)
        updateDisplay()
    }

    private fun onDecimalClicked() {
        if (isNewOperationAfterEquals) {
            currentInput.clear()
            currentInput.append("0")
            isNewOperationAfterEquals = false
        }
        if (currentInput.isEmpty()) {
            currentInput.append("0")
        }
        if (!currentInput.contains(".")) {
            currentInput.append(".")
            updateDisplay()
        }
    }

    private fun onOperatorClicked(button: Button) {
        val operator = button.text.toString()

        if (currentInput.isNotEmpty()) {
            if (operand1 == null) {
                operand1 = currentInput.toString().replace(",", ".").toDoubleOrNull()
            } else {
                calculateResult()
                operand1 = binding.textViewResult.text.toString().replace(",", ".").toDoubleOrNull()
            }
            pendingOperation = operator
            currentInput.clear()
            isNewOperationAfterEquals = false
            updateDisplay()
        } else if (operand1 != null) {
            pendingOperation = operator
            isNewOperationAfterEquals = false
        }
    }

    private fun onEqualsClicked() {
        if (operand1 != null && pendingOperation != null && currentInput.isNotEmpty()) {
            calculateResult()
            operand1 = null
            pendingOperation = null
            if (binding.textViewResult.text.toString() != "Hata" && binding.textViewResult.text.toString() != "Sıfıra Bölme!") {
                currentInput.clear().append(binding.textViewResult.text.toString().replace(",", "."))
            } else {
                currentInput.clear()
            }
            isNewOperationAfterEquals = true
        }
    }

    private fun calculateResult() {
        val operand2String = currentInput.toString().replace(",", ".")
        val operand2 = operand2String.toDoubleOrNull()

        if (operand1 == null || operand2 == null || pendingOperation == null) {
            if (currentInput.isNotEmpty() && operand1 == null && pendingOperation == null) {
                try {
                    binding.textViewResult.text = decimalFormat.format(currentInput.toString().replace(",", ".").toDouble())
                } catch (e: NumberFormatException) {
                    showError("Hata")
                }
            }
            return
        }

        var resultValue: Double? = null
        try {
            when (pendingOperation) {
                "+" -> resultValue = operand1!! + operand2
                "−" -> resultValue = operand1!! - operand2
                "×" -> resultValue = operand1!! * operand2
                "÷" -> {
                    if (operand2 == 0.0) {
                        showError("Sıfıra Bölme!")
                        resetAfterError()
                        return
                    }
                    resultValue = operand1!! / operand2
                }
            }

            if (resultValue != null) {
                binding.textViewResult.text = decimalFormat.format(resultValue)
            }
        } catch (e: Exception) {
            showError("Hata")
            resetAfterError()
        }
    }

    private fun onClearClicked() {
        resetCalculator()
    }

    private fun resetCalculator() {
        currentInput.clear()
        operand1 = null
        pendingOperation = null
        binding.textViewResult.text = "0"
        isNewOperationAfterEquals = true
    }

    private fun resetAfterError() {
        currentInput.clear()
        operand1 = null
        pendingOperation = null
        isNewOperationAfterEquals = true
    }

    private fun onDeleteClicked() {
        if (currentInput.isNotEmpty()) {
            currentInput.deleteCharAt(currentInput.length - 1)
            if (currentInput.isEmpty()) {
                binding.textViewResult.text = "0"
            } else {
                updateDisplay()
            }
        }
    }

    private fun onPercentClicked() {
        if (currentInput.isEmpty()) {
            if (operand1 != null && pendingOperation == null && !isNewOperationAfterEquals) {
                try {
                    val valueFromResult = binding.textViewResult.text.toString().replace(",", ".").toDouble()
                    val result = valueFromResult / 100.0
                    currentInput.clear().append(decimalFormat.format(result))
                    updateDisplay()
                    operand1 = null
                    isNewOperationAfterEquals = true
                } catch (e: NumberFormatException) {
                    showError("Hata")
                }
            }
            return
        }

        try {
            val currentValue = currentInput.toString().replace(",", ".").toDouble()
            val result: Double

            if (operand1 != null && pendingOperation != null) {
                result = operand1!! * (currentValue / 100.0)
            } else {
                result = currentValue / 100.0
            }
            currentInput.clear().append(decimalFormat.format(result))
            updateDisplay()
            isNewOperationAfterEquals = false
        } catch (e: NumberFormatException) {
            showError("Hata")
        }
    }

    private fun onPlusMinusClicked() {
        if (currentInput.isNotEmpty() && currentInput.toString() != "0") {
            if (currentInput.startsWith("-")) {
                currentInput.deleteCharAt(0)
            } else {
                currentInput.insert(0, "-")
            }
            updateDisplay()
            isNewOperationAfterEquals = false
        } else if (currentInput.isEmpty() && binding.textViewResult.text.toString() != "0" && binding.textViewResult.text.toString() != "Hata") {
            try {
                var displayedValue = binding.textViewResult.text.toString().replace(",", ".").toDouble()
                displayedValue *= -1
                val formattedValue = decimalFormat.format(displayedValue)
                binding.textViewResult.text = formattedValue
                currentInput.clear().append(formattedValue) // Değişikliği currentInput'a da yansıt
                isNewOperationAfterEquals = false
            } catch (e: NumberFormatException) { /* Hata veya "0" ise bir şey yapma */ }
        }
    }

    private fun updateDisplay() {
        if (currentInput.isEmpty()) {
            binding.textViewResult.text = "0"
        } else {
            binding.textViewResult.text = currentInput.toString()
        }
    }

    private fun showError(message: String) {
        binding.textViewResult.text = message
    }
}