package com.example.interestcalculator.data.repository

import android.content.Context
import com.example.interestcalculator.data.model.DebtSourceItem
import com.example.interestcalculator.data.model.DebtSourceType
import com.example.interestcalculator.data.model.KpiProfile
import com.example.interestcalculator.data.model.LoanMethod
import com.example.interestcalculator.data.model.LoanProfile
import com.example.interestcalculator.data.model.LoanStatus
import com.example.interestcalculator.data.model.SavedDebtParams
import com.example.interestcalculator.data.model.SavedLoanParams
import com.example.interestcalculator.data.model.SavedSavingsParams
import com.example.interestcalculator.data.model.SavingsPaymentType
import com.example.interestcalculator.data.model.SavingsProfile
import com.example.interestcalculator.data.model.SavingsStatus
import com.example.interestcalculator.util.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class BankDataRepository(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val storageFile = File(context.filesDir, "bank_records.json")

    private val _loans = MutableStateFlow<List<LoanProfile>>(emptyList())
    val loans: StateFlow<List<LoanProfile>> = _loans.asStateFlow()

    private val _savings = MutableStateFlow<List<SavingsProfile>>(emptyList())
    val savings: StateFlow<List<SavingsProfile>> = _savings.asStateFlow()

    private val _kpi = MutableStateFlow(KpiProfile())
    val kpi: StateFlow<KpiProfile> = _kpi.asStateFlow()

    private val _lastLoanParams = MutableStateFlow(SavedLoanParams())
    val lastLoanParams: StateFlow<SavedLoanParams> = _lastLoanParams.asStateFlow()

    private val _lastSavingsParams = MutableStateFlow(SavedSavingsParams())
    val lastSavingsParams: StateFlow<SavedSavingsParams> = _lastSavingsParams.asStateFlow()

    private val _savedLoanPresets = MutableStateFlow<List<SavedLoanParams>>(emptyList())
    val savedLoanPresets: StateFlow<List<SavedLoanParams>> = _savedLoanPresets.asStateFlow()

    private val _savedSavingsPresets = MutableStateFlow<List<SavedSavingsParams>>(emptyList())
    val savedSavingsPresets: StateFlow<List<SavedSavingsParams>> = _savedSavingsPresets.asStateFlow()

    private val _lastDebtParams = MutableStateFlow(
        SavedDebtParams(
            startDateStr = DateTimeUtils.todayString(),
            bankRate = 10.5,
            tctdRate = 28.0,
            calculationMethod = LoanMethod.REDUCING_BALANCE,
            items = listOf(
                DebtSourceItem(
                    name = "Vietcombank",
                    sourceType = DebtSourceType.BANK,
                    currentDebt = 300_000_000.0,
                    maturityDate = DateTimeUtils.addMonths(DateTimeUtils.todayString(), 24)
                ),
                DebtSourceItem(
                    name = "FE Credit",
                    sourceType = DebtSourceType.CREDIT_INSTITUTION,
                    currentDebt = 50_000_000.0,
                    maturityDate = DateTimeUtils.addMonths(DateTimeUtils.todayString(), 12)
                )
            )
        )
    )
    val lastDebtParams: StateFlow<SavedDebtParams> = _lastDebtParams.asStateFlow()

    private val _savedDebtPresets = MutableStateFlow<List<SavedDebtParams>>(emptyList())
    val savedDebtPresets: StateFlow<List<SavedDebtParams>> = _savedDebtPresets.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        if (!storageFile.exists()) {
            initDefaultSampleData()
            saveDataToFile()
            return
        }

        try {
            val jsonText = storageFile.readText()
            val root = JSONObject(jsonText)

            // Parse Loans
            val loansArray = root.optJSONArray("loans") ?: JSONArray()
            val loanList = mutableListOf<LoanProfile>()
            for (i in 0 until loansArray.length()) {
                val obj = loansArray.getJSONObject(i)
                loanList.add(
                    LoanProfile(
                        id = obj.optString("id"),
                        customerName = obj.optString("customerName"),
                        customerPhone = obj.optString("customerPhone"),
                        loanPurpose = obj.optString("loanPurpose"),
                        principalAmount = obj.optDouble("principalAmount"),
                        termMonths = obj.optInt("termMonths"),
                        repaymentPeriodMonths = obj.optInt("repaymentPeriodMonths", 1),
                        interestRate = obj.optDouble("interestRate"),
                        hasPreferential = obj.optBoolean("hasPreferential"),
                        preferentialMonths = obj.optInt("preferentialMonths"),
                        preferentialRate = obj.optDouble("preferentialRate"),
                        gracePeriodMonths = obj.optInt("gracePeriodMonths"),
                        calculationMethod = try {
                            LoanMethod.valueOf(obj.optString("calculationMethod"))
                        } catch (e: Exception) {
                            LoanMethod.REDUCING_BALANCE
                        },
                        disbursementDate = obj.optString("disbursementDate"),
                        status = try {
                            LoanStatus.valueOf(obj.optString("status"))
                        } catch (e: Exception) {
                            LoanStatus.CONSULTING
                        },
                        note = obj.optString("note"),
                        createdAt = obj.optLong("createdAt")
                    )
                )
            }
            _loans.value = loanList

            // Parse Savings
            val savingsArray = root.optJSONArray("savings") ?: JSONArray()
            val savingsList = mutableListOf<SavingsProfile>()
            for (i in 0 until savingsArray.length()) {
                val obj = savingsArray.getJSONObject(i)
                val maturityDate = obj.optString("maturityDate")
                var status = try {
                    SavingsStatus.valueOf(obj.optString("status"))
                } catch (e: Exception) {
                    SavingsStatus.ACTIVE
                }
                // Tự động kiểm tra trạng thái sắp đáo hạn
                if (status == SavingsStatus.ACTIVE && DateTimeUtils.isExpiringSoon(maturityDate, 15)) {
                    status = SavingsStatus.EXPIRING_SOON
                }

                savingsList.add(
                    SavingsProfile(
                        id = obj.optString("id"),
                        customerName = obj.optString("customerName"),
                        customerPhone = obj.optString("customerPhone"),
                        depositAmount = obj.optDouble("depositAmount"),
                        termMonths = obj.optInt("termMonths"),
                        periodMonths = obj.optInt("periodMonths", obj.optInt("termMonths")),
                        interestRate = obj.optDouble("interestRate"),
                        interestPaymentType = try {
                            SavingsPaymentType.valueOf(obj.optString("interestPaymentType"))
                        } catch (e: Exception) {
                            SavingsPaymentType.END_OF_TERM
                        },
                        isAutoRenewCompounding = obj.optBoolean("isAutoRenewCompounding"),
                        depositDate = obj.optString("depositDate"),
                        maturityDate = maturityDate,
                        status = status,
                        note = obj.optString("note"),
                        createdAt = obj.optLong("createdAt")
                    )
                )
            }
            _savings.value = savingsList

            // Parse KPI
            val kpiObj = root.optJSONObject("kpi")
            if (kpiObj != null) {
                _kpi.value = KpiProfile(
                    staffName = kpiObj.optString("staffName", "Chuyên viên QHKH"),
                    bankName = kpiObj.optString("bankName", "Ngân hàng Thương mại"),
                    loanDisbursementTarget = kpiObj.optDouble("loanDisbursementTarget", 5_000_000_000.0),
                    savingsMobilizationTarget = kpiObj.optDouble("savingsMobilizationTarget", 10_000_000_000.0)
                )
            }

            // Parse Last & Saved Params
            root.optJSONObject("lastLoanParams")?.let {
                _lastLoanParams.value = jsonToLoanParams(it)
            }
            root.optJSONObject("lastSavingsParams")?.let {
                _lastSavingsParams.value = jsonToSavingsParams(it)
            }

            val loanPresetsArray = root.optJSONArray("savedLoanPresets") ?: JSONArray()
            val loanPresetList = mutableListOf<SavedLoanParams>()
            for (i in 0 until loanPresetsArray.length()) {
                loanPresetList.add(jsonToLoanParams(loanPresetsArray.getJSONObject(i)))
            }
            _savedLoanPresets.value = loanPresetList

            val savingsPresetsArray = root.optJSONArray("savedSavingsPresets") ?: JSONArray()
            val savingsPresetList = mutableListOf<SavedSavingsParams>()
            for (i in 0 until savingsPresetsArray.length()) {
                savingsPresetList.add(jsonToSavingsParams(savingsPresetsArray.getJSONObject(i)))
            }
            _savedSavingsPresets.value = savingsPresetList

            // Parse Last & Saved Debt Params
            root.optJSONObject("lastDebtParams")?.let {
                _lastDebtParams.value = jsonToDebtParams(it)
            }
            val debtPresetsArray = root.optJSONArray("savedDebtPresets") ?: JSONArray()
            val debtPresetList = mutableListOf<SavedDebtParams>()
            for (i in 0 until debtPresetsArray.length()) {
                debtPresetList.add(jsonToDebtParams(debtPresetsArray.getJSONObject(i)))
            }
            if (debtPresetList.isNotEmpty()) {
                _savedDebtPresets.value = debtPresetList
            }
        } catch (e: Exception) {
            e.printStackTrace()
            initDefaultSampleData()
        }
    }

    private fun saveDataToFile() {
        scope.launch {
            try {
                val root = JSONObject()

                // Loans
                val loansArray = JSONArray()
                for (loan in _loans.value) {
                    val obj = JSONObject().apply {
                        put("id", loan.id)
                        put("customerName", loan.customerName)
                        put("customerPhone", loan.customerPhone)
                        put("loanPurpose", loan.loanPurpose)
                        put("principalAmount", loan.principalAmount)
                        put("termMonths", loan.termMonths)
                        put("repaymentPeriodMonths", loan.repaymentPeriodMonths)
                        put("interestRate", loan.interestRate)
                        put("hasPreferential", loan.hasPreferential)
                        put("preferentialMonths", loan.preferentialMonths)
                        put("preferentialRate", loan.preferentialRate)
                        put("gracePeriodMonths", loan.gracePeriodMonths)
                        put("calculationMethod", loan.calculationMethod.name)
                        put("disbursementDate", loan.disbursementDate)
                        put("status", loan.status.name)
                        put("note", loan.note)
                        put("createdAt", loan.createdAt)
                    }
                    loansArray.put(obj)
                }
                root.put("loans", loansArray)

                // Savings
                val savingsArray = JSONArray()
                for (sav in _savings.value) {
                    val obj = JSONObject().apply {
                        put("id", sav.id)
                        put("customerName", sav.customerName)
                        put("customerPhone", sav.customerPhone)
                        put("depositAmount", sav.depositAmount)
                        put("termMonths", sav.termMonths)
                        put("periodMonths", sav.periodMonths)
                        put("interestRate", sav.interestRate)
                        put("interestPaymentType", sav.interestPaymentType.name)
                        put("isAutoRenewCompounding", sav.isAutoRenewCompounding)
                        put("depositDate", sav.depositDate)
                        put("maturityDate", sav.maturityDate)
                        put("status", sav.status.name)
                        put("note", sav.note)
                        put("createdAt", sav.createdAt)
                    }
                    savingsArray.put(obj)
                }
                root.put("savings", savingsArray)

                // KPI
                val kpiObj = JSONObject().apply {
                    put("staffName", _kpi.value.staffName)
                    put("bankName", _kpi.value.bankName)
                    put("loanDisbursementTarget", _kpi.value.loanDisbursementTarget)
                    put("savingsMobilizationTarget", _kpi.value.savingsMobilizationTarget)
                }
                root.put("kpi", kpiObj)

                // Last & Saved Presets
                root.put("lastLoanParams", loanParamsToJson(_lastLoanParams.value))
                root.put("lastSavingsParams", savingsParamsToJson(_lastSavingsParams.value))

                val loanPresetsArray = JSONArray()
                for (preset in _savedLoanPresets.value) {
                    loanPresetsArray.put(loanParamsToJson(preset))
                }
                root.put("savedLoanPresets", loanPresetsArray)

                val savingsPresetsArray = JSONArray()
                for (preset in _savedSavingsPresets.value) {
                    savingsPresetsArray.put(savingsParamsToJson(preset))
                }
                root.put("savedSavingsPresets", savingsPresetsArray)

                // Debt Params
                root.put("lastDebtParams", debtParamsToJson(_lastDebtParams.value))
                val debtPresetsArray = JSONArray()
                for (preset in _savedDebtPresets.value) {
                    debtPresetsArray.put(debtParamsToJson(preset))
                }
                root.put("savedDebtPresets", debtPresetsArray)

                storageFile.writeText(root.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initDefaultSampleData() {
        val today = DateTimeUtils.todayString()
        val sampleLoans = listOf(
            LoanProfile(
                customerName = "Nguyễn Văn Tuấn",
                customerPhone = "0912345678",
                loanPurpose = "Vay mua nhà Ecopark",
                principalAmount = 2_000_000_000.0, // 2 tỷ
                termMonths = 240, // 20 năm
                interestRate = 10.5,
                hasPreferential = true,
                preferentialMonths = 12,
                preferentialRate = 6.8,
                gracePeriodMonths = 6,
                calculationMethod = LoanMethod.REDUCING_BALANCE,
                disbursementDate = today,
                status = LoanStatus.DISBURSED,
                note = "Đã thế chấp sổ đỏ, giải ngân đợt 1"
            ),
            LoanProfile(
                customerName = "Trần Thị Bích Lan",
                customerPhone = "0987654321",
                loanPurpose = "Vay mua ô tô Honda CR-V",
                principalAmount = 600_000_000.0, // 600 tr
                termMonths = 60, // 5 năm
                interestRate = 9.2,
                hasPreferential = true,
                preferentialMonths = 6,
                preferentialRate = 7.5,
                gracePeriodMonths = 0,
                calculationMethod = LoanMethod.REDUCING_BALANCE,
                disbursementDate = today,
                status = LoanStatus.APPROVED,
                note = "Khách chọn nhận xe ngày 15"
            ),
            LoanProfile(
                customerName = "Công ty TNHH Hoàng Nam",
                customerPhone = "0903112233",
                loanPurpose = "Bổ sung vốn kinh doanh cuối năm",
                principalAmount = 1_500_000_000.0, // 1.5 tỷ
                termMonths = 12, // 1 năm
                interestRate = 8.5,
                hasPreferential = false,
                calculationMethod = LoanMethod.REDUCING_BALANCE,
                disbursementDate = today,
                status = LoanStatus.CONSULTING,
                note = "Đang thẩm định báo cáo tài chính quý 3"
            )
        )

        val sampleSavings = listOf(
            SavingsProfile(
                customerName = "Bác Lê Thị Mai",
                customerPhone = "0934567890",
                depositAmount = 500_000_000.0, // 500 tr
                termMonths = 6,
                interestRate = 5.8,
                interestPaymentType = SavingsPaymentType.END_OF_TERM,
                isAutoRenewCompounding = false,
                depositDate = DateTimeUtils.addDays(today, -170),
                maturityDate = DateTimeUtils.addDays(today, 10), // Còn 10 ngày nữa -> Sắp đáo hạn!
                status = SavingsStatus.EXPIRING_SOON,
                note = "Tiết kiệm dưỡng già, hẹn gọi điện tư vấn tái tục gói 12 tháng"
            ),
            SavingsProfile(
                customerName = "Phạm Quang Dũng",
                customerPhone = "0971239988",
                depositAmount = 2_000_000_000.0, // 2 tỷ
                termMonths = 12,
                interestRate = 6.2,
                interestPaymentType = SavingsPaymentType.MONTHLY,
                isAutoRenewCompounding = false,
                depositDate = today,
                maturityDate = DateTimeUtils.addMonths(today, 12),
                status = SavingsStatus.ACTIVE,
                note = "Khách VIP, nhận lãi hàng tháng về tài khoản thanh toán"
            ),
            SavingsProfile(
                customerName = "Đỗ Thu Hà",
                customerPhone = "0918889900",
                depositAmount = 300_000_000.0, // 300 tr
                termMonths = 3,
                interestRate = 4.5,
                interestPaymentType = SavingsPaymentType.END_OF_TERM,
                isAutoRenewCompounding = true,
                depositDate = today,
                maturityDate = DateTimeUtils.addMonths(today, 3),
                status = SavingsStatus.ACTIVE,
                note = "Mở sổ online, chế độ tự động quay vòng gốc và lãi"
            )
        )

        _loans.value = sampleLoans
        _savings.value = sampleSavings
    }

    fun addLoan(loan: LoanProfile) {
        _loans.value = listOf(loan) + _loans.value
        saveDataToFile()
    }

    fun updateLoan(loan: LoanProfile) {
        _loans.value = _loans.value.map { if (it.id == loan.id) loan else it }
        saveDataToFile()
    }

    fun deleteLoan(loanId: String) {
        _loans.value = _loans.value.filterNot { it.id == loanId }
        saveDataToFile()
    }

    fun addSavings(savings: SavingsProfile) {
        _savings.value = listOf(savings) + _savings.value
        saveDataToFile()
    }

    fun updateSavings(savings: SavingsProfile) {
        _savings.value = _savings.value.map { if (it.id == savings.id) savings else it }
        saveDataToFile()
    }

    fun deleteSavings(savingsId: String) {
        _savings.value = _savings.value.filterNot { it.id == savingsId }
        saveDataToFile()
    }

    fun updateKpi(kpi: KpiProfile) {
        _kpi.value = kpi
        saveDataToFile()
    }

    // --- Quản lý thông số tính toán (Saved & Last Params) ---
    fun saveLastLoanParams(params: SavedLoanParams) {
        if (_lastLoanParams.value == params) return
        _lastLoanParams.value = params
        saveDataToFile()
    }

    fun saveLastSavingsParams(params: SavedSavingsParams) {
        if (_lastSavingsParams.value == params) return
        _lastSavingsParams.value = params
        saveDataToFile()
    }

    fun addSavedLoanPreset(preset: SavedLoanParams) {
        _savedLoanPresets.value = listOf(preset) + _savedLoanPresets.value
        saveDataToFile()
    }

    fun deleteSavedLoanPreset(presetId: String) {
        _savedLoanPresets.value = _savedLoanPresets.value.filterNot { it.id == presetId }
        saveDataToFile()
    }

    fun addSavedSavingsPreset(preset: SavedSavingsParams) {
        _savedSavingsPresets.value = listOf(preset) + _savedSavingsPresets.value
        saveDataToFile()
    }

    fun deleteSavedSavingsPreset(presetId: String) {
        _savedSavingsPresets.value = _savedSavingsPresets.value.filterNot { it.id == presetId }
        saveDataToFile()
    }

    fun saveLastDebtParams(params: SavedDebtParams) {
        if (_lastDebtParams.value == params) return
        _lastDebtParams.value = params
        saveDataToFile()
    }

    fun addSavedDebtPreset(preset: SavedDebtParams) {
        _savedDebtPresets.value = listOf(preset) + _savedDebtPresets.value
        saveDataToFile()
    }

    fun deleteSavedDebtPreset(presetId: String) {
        _savedDebtPresets.value = _savedDebtPresets.value.filterNot { it.id == presetId }
        saveDataToFile()
    }

    private fun loanParamsToJson(params: SavedLoanParams): JSONObject = JSONObject().apply {
        put("id", params.id)
        put("name", params.name)
        put("customerName", params.customerName)
        put("principalAmount", params.principalAmount)
        put("termMonths", params.termMonths)
        put("repaymentPeriodMonths", params.repaymentPeriodMonths)
        put("baseRate", params.baseRate)
        put("isBaseRateMonthly", params.isBaseRateMonthly)
        put("startDateStr", params.startDateStr)
        put("hasPreferential", params.hasPreferential)
        put("preferentialMonths", params.preferentialMonths)
        put("preferentialRate", params.preferentialRate)
        put("hasGracePeriod", params.hasGracePeriod)
        put("gracePeriodMonths", params.gracePeriodMonths)
        put("selectedMethod", params.selectedMethod.name)
        put("savedAt", params.savedAt)
    }

    private fun jsonToLoanParams(obj: JSONObject): SavedLoanParams = SavedLoanParams(
        id = obj.optString("id", UUID.randomUUID().toString()),
        name = obj.optString("name", ""),
        customerName = obj.optString("customerName", ""),
        principalAmount = obj.optDouble("principalAmount", 1_000_000_000.0),
        termMonths = obj.optInt("termMonths", 120),
        repaymentPeriodMonths = obj.optInt("repaymentPeriodMonths", 1),
        baseRate = obj.optDouble("baseRate", 9.5),
        isBaseRateMonthly = obj.optBoolean("isBaseRateMonthly", false),
        startDateStr = obj.optString("startDateStr", DateTimeUtils.todayString()),
        hasPreferential = obj.optBoolean("hasPreferential", true),
        preferentialMonths = obj.optInt("preferentialMonths", 12),
        preferentialRate = obj.optDouble("preferentialRate", 6.8),
        hasGracePeriod = obj.optBoolean("hasGracePeriod", false),
        gracePeriodMonths = obj.optInt("gracePeriodMonths", 6),
        selectedMethod = try { LoanMethod.valueOf(obj.optString("selectedMethod")) } catch (e: Exception) { LoanMethod.REDUCING_BALANCE },
        savedAt = obj.optLong("savedAt", System.currentTimeMillis())
    )

    private fun savingsParamsToJson(params: SavedSavingsParams): JSONObject = JSONObject().apply {
        put("id", params.id)
        put("name", params.name)
        put("customerName", params.customerName)
        put("depositAmount", params.depositAmount)
        put("termMonths", params.termMonths)
        put("periodMonths", params.periodMonths)
        put("interestRate", params.interestRate)
        put("startDateStr", params.startDateStr)
        put("paymentType", params.paymentType.name)
        put("isCompounding", params.isCompounding)
        put("savedAt", params.savedAt)
    }

    private fun jsonToSavingsParams(obj: JSONObject): SavedSavingsParams = SavedSavingsParams(
        id = obj.optString("id", UUID.randomUUID().toString()),
        name = obj.optString("name", ""),
        customerName = obj.optString("customerName", ""),
        depositAmount = obj.optDouble("depositAmount", 500_000_000.0),
        termMonths = obj.optInt("termMonths", 12),
        periodMonths = obj.optInt("periodMonths", obj.optInt("termMonths", 6)),
        interestRate = obj.optDouble("interestRate", 5.8),
        startDateStr = obj.optString("startDateStr", DateTimeUtils.todayString()),
        paymentType = try { SavingsPaymentType.valueOf(obj.optString("paymentType")) } catch (e: Exception) { SavingsPaymentType.END_OF_TERM },
        isCompounding = obj.optBoolean("isCompounding", false),
        savedAt = obj.optLong("savedAt", System.currentTimeMillis())
    )

    private fun debtParamsToJson(params: SavedDebtParams): JSONObject = JSONObject().apply {
        put("id", params.id)
        put("name", params.name)
        put("customerName", params.customerName)
        put("startDateStr", params.startDateStr)
        put("bankRate", params.bankRate)
        put("isBankRateMonthly", params.isBankRateMonthly)
        put("tctdRate", params.tctdRate)
        put("isTctdRateMonthly", params.isTctdRateMonthly)
        put("calculationMethod", params.calculationMethod.name)
        put("savedAt", params.savedAt)
        val itemsArr = JSONArray()
        for (item in params.items) {
            val itemObj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("sourceType", item.sourceType.name)
                put("currentDebt", item.currentDebt)
                put("maturityDate", item.maturityDate)
                if (item.customRate != null) {
                    put("customRate", item.customRate)
                }
            }
            itemsArr.put(itemObj)
        }
        put("items", itemsArr)
    }

    private fun jsonToDebtParams(obj: JSONObject): SavedDebtParams {
        val itemsList = mutableListOf<DebtSourceItem>()
        val itemsArr = obj.optJSONArray("items") ?: JSONArray()
        for (i in 0 until itemsArr.length()) {
            val itemObj = itemsArr.getJSONObject(i)
            itemsList.add(
                DebtSourceItem(
                    id = itemObj.optString("id", UUID.randomUUID().toString()),
                    name = itemObj.optString("name", ""),
                    sourceType = try { DebtSourceType.valueOf(itemObj.optString("sourceType")) } catch (e: Exception) { DebtSourceType.BANK },
                    currentDebt = itemObj.optDouble("currentDebt", 0.0),
                    maturityDate = itemObj.optString("maturityDate", ""),
                    customRate = if (itemObj.has("customRate")) itemObj.optDouble("customRate") else null
                )
            )
        }
        return SavedDebtParams(
            id = obj.optString("id", UUID.randomUUID().toString()),
            name = obj.optString("name", ""),
            customerName = obj.optString("customerName", ""),
            startDateStr = obj.optString("startDateStr", DateTimeUtils.todayString()),
            bankRate = obj.optDouble("bankRate", 10.5),
            isBankRateMonthly = obj.optBoolean("isBankRateMonthly", false),
            tctdRate = obj.optDouble("tctdRate", 28.0),
            isTctdRateMonthly = obj.optBoolean("isTctdRateMonthly", false),
            calculationMethod = try { LoanMethod.valueOf(obj.optString("calculationMethod")) } catch (e: Exception) { LoanMethod.REDUCING_BALANCE },
            items = itemsList,
            savedAt = obj.optLong("savedAt", System.currentTimeMillis())
        )
    }
}
