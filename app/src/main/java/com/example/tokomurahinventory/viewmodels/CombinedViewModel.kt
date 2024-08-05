package com.example.tokomurahinventory.viewmodels


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.BarangLogDao
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.database.LogDao
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.model.WarnaModel
import com.example.tokomurahinventory.models.WarnaTable
import com.example.tokomurahinventory.models.model.DetailWarnaModel
import com.example.tokomurahinventory.utils.MASUKKELUAR
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.userNullString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class CombinedViewModel(
    val merkDao: MerkDao,
    val warnaDao: WarnaDao,
    val refMerk: String?,
    val loggedInUser: String,
    val dataSourceDetailWarna:DetailWarnaDao,
    val dataSourceBarangLog:BarangLogDao,
    val dataSourceLog: LogDao,
    application: Application
) : BaseAndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Merk related
    private var _allMerkTable = MutableLiveData<List<MerkTable>>()
    val allMerkTable: LiveData<List<MerkTable>> get() = _allMerkTable

    private val addMerkFabM = MutableLiveData<Boolean>()
    val addMerkFab: LiveData<Boolean> get() = addMerkFabM

    private val _unFilteredMerk = MutableLiveData<List<MerkTable>>()
    private val navigateToWarnaM = MutableLiveData<String>()
    val navigateToWarna: LiveData<String> get() = navigateToWarnaM

    // Warna related
    private var _allWarnaByMerk = MutableLiveData<List<WarnaModel>>()
    val allWarnaByMerk: LiveData<List<WarnaModel>> get() = _allWarnaByMerk

    private val addWarnaFabM = MutableLiveData<Boolean>()
    val addWarnaFab: LiveData<Boolean> get() = addWarnaFabM

    private val navigateToDetailWarnaM = MutableLiveData<String>()
    val navigateToDetailWarna: LiveData<String> get() = navigateToDetailWarnaM

    private val _unFilteredWarna = MutableLiveData<List<WarnaModel>>()


    //Add detail warna fab
    private val _addDetailWarnaFab = MutableLiveData<Boolean>()
    val addDetailWarnaFab: LiveData<Boolean> get() = _addDetailWarnaFab
    var dummyDetail = mutableListOf<DetailWarnaTable>()

    val _refMerk = MutableLiveData<String>()
    val refMerkk :LiveData<String> get() = _refMerk
    val _refWarna = MutableLiveData<String>()
    val refWarna :LiveData<String> get() = _refWarna
    //delete?
    val _warna = MutableLiveData<String>()
    val warna :LiveData<String>get() = _warna

    val _merk = MutableLiveData<String>()
    val merk :LiveData<String>get() = _merk
    //val warna = warnaDao.selectWarnaByWarnaRef(refWarna)
    //detail warna
    //val detailWarnaList = dataSourceDetailWarna.selectDetailWarnaByWarnaIdGroupByIsi(refWarna)
    val _detailWarnaList = MutableLiveData<List<DetailWarnaModel>>()
    val detailWarnaList :LiveData<List<DetailWarnaModel>> get() = _detailWarnaList
    //val detailWarnaList = dataSourceDetailWarna.getDetailWarnaSummary(refWarna)

    val _orientationMode = MutableLiveData<Int>()
    val orientationMode:LiveData<Int> get() =  _orientationMode

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private val _isWarnaLoading = MutableLiveData<Boolean>(false)
    val isWarnaLoading: LiveData<Boolean> get() = _isWarnaLoading

    private val _isDetailWarnaLoading = MutableLiveData<Boolean>(false)
    val isDetailWarnaLoading: LiveData<Boolean> get() = _isWarnaLoading


    init {
        if (_refMerk.value != null) {
            getWarnaByMerk(_refMerk.value)
        }
        getAllMerkTable()
    }

    fun setOrientationMode(orientationMode:Int){
        _orientationMode.value = orientationMode
    }

    fun setRefMerk(ref:String){
        _refMerk.value=ref
    }
    fun setRefWarna(ref:String){
        _refWarna.value=ref
        Log.i("SplitFragmetProbs","setRefWarna ${_refWarna.value}")
    }
    fun getDetailWarnaByWarnaRef(warnaRef: String){
        viewModelScope.launch {
            _isDetailWarnaLoading.value = true
          val  list = withContext(Dispatchers.IO){
                dataSourceDetailWarna.getDetailWarnaSummaryList(warnaRef)
            }
            _detailWarnaList.value = list
            _isDetailWarnaLoading.value = false
           // Log.i("SplitFragmetProbs","allWarnaDetailWarna ${list}")
        }
    }
    // Merk functions
    fun getAllMerkTable() {
        viewModelScope.launch {
            _isLoading.value = true
            val list = withContext(Dispatchers.IO) {
                merkDao.selectAllMerkList()
            }
            _allMerkTable.value = list
            _unFilteredMerk.value = list
            _isLoading.value = false
        }
    }

    fun filterMerk(query: String?) {
        val list = mutableListOf<MerkTable>()
        if (!query.isNullOrEmpty()) {
            list.addAll(_unFilteredMerk.value!!.filter {
                it.namaMerk.lowercase(Locale.getDefault()).contains(query.toString().lowercase(Locale.getDefault()))
            })
        } else {
            list.addAll(_unFilteredMerk.value?: listOf())
        }
        _allMerkTable.value = list
    }

    fun insertMerk(namaMerk: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val merk = MerkTable().apply {
                this.namaMerk = namaMerk
                val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
                if (loggedInUsers != null) {
                    this.lastEditedBy = loggedInUsers
                    this.createdBy = loggedInUsers
                    this.merkCreatedDate = Date()
                    this.merkLastEditedDate = Date()
                    this.refMerk = UUID.randomUUID().toString()
                    this.user = loggedInUsers
                    insertMerkToDao(this)
                    getAllMerkTable()
                } else {
                    Toast.makeText(getApplication(), userNullString, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun updateMerk(merkTable: MerkTable) {
        viewModelScope.launch {
            _isLoading.value=true
            merkTable.lastEditedBy = SharedPreferencesHelper.getLoggedInUser(getApplication())
            merkTable.merkLastEditedDate = Date()
            updateMerkToDao(merkTable)
            getAllMerkTable()
        }
    }

    fun deleteMerk(merkTable: MerkTable) {
        viewModelScope.launch {
            _isLoading.value = true
            deleteMerkToDao(merkTable)
            getAllMerkTable()
            getWarnaByMerk(refMerkk.value)
        }
    }

    private suspend fun insertMerkToDao(merkTable: MerkTable) {
        withContext(Dispatchers.IO) {
            merkDao.insert(merkTable)
        }
    }

    private suspend fun updateMerkToDao(merkTable: MerkTable) {
        withContext(Dispatchers.IO) {
            merkDao.update(merkTable)
        }
    }

    private suspend fun deleteMerkToDao(merkTable: MerkTable) {
        withContext(Dispatchers.IO) {
            merkDao.deleteAnItemMerk(merkTable.id)
        }
    }

    fun onAddMerkFabClick(context: Context) {
        Log.i("SplitFragmetProbs","addWarnaFabClick ${addMerkFabM.value}")
        addMerkFabM.value = true
    }

    fun onAddMerkFabClicked() {
        addMerkFabM.value = false
        Log.i("SplitFragmetProbs","addWarnaFabClick ${addMerkFabM.value}")
    }
    fun onNavigateToWarna(refMerk: String) { navigateToWarnaM.value = refMerk }
    @SuppressLint("NullSafeMutableLiveData")
    fun onNavigatedToWarna() { navigateToWarnaM.value = null }

    // Warna functions
    fun getWarnaByMerk(refMerk: String?) {
        viewModelScope.launch {
            _isWarnaLoading.value = true
            if (_refMerk.value!=null){
                Log.i("WarnaProbs","")
                val list = withContext(Dispatchers.IO) {
                    if (refMerk==null){
                        warnaDao.getWarnaWithTotalPcsList(_refMerk.value!!)
                    }else{
                        warnaDao.getWarnaWithTotalPcsList(refMerk)
                    }
                }
                _allWarnaByMerk.value = list
                _unFilteredWarna.value = list
                Log.i("WarnaProbs","allWarnaByMerk ${list}")

            }
            _isWarnaLoading.value = false
        }
    }
    fun getStringWarna(warnaRef:String){
        viewModelScope.launch {
            val warna = withContext(Dispatchers.IO){
                warnaDao.getKodeWarnaByRef(warnaRef)
            }
            _warna.value = warna
            Log.i("SplitFragmetProbs","warna ${warna}")
        }
    }
    fun getStringMerk(refMerk:String){
        viewModelScope.launch {
            val merk = withContext(Dispatchers.IO){
                merkDao.getMerkNameByRef(refMerk)
            }
            _merk.value = merk
            Log.i("SplitFragmetProbs","merk ${merk}")
        }
    }

    fun filterWarna(query: String?) {
        val list = mutableListOf<WarnaModel>()
        if (!query.isNullOrEmpty()) {
            list.addAll(_unFilteredWarna.value!!.filter {
                it.kodeWarna.lowercase(Locale.getDefault()).contains(query.toString().lowercase(Locale.getDefault()))
            })
        } else {
            list.addAll(_unFilteredWarna.value!!)
        }
        _allWarnaByMerk.value = list
    }

    fun insertWarna(kodeWarna: String, satuan: String) {
        viewModelScope.launch {
            _isWarnaLoading.value = true
            Log.i("WarnaProbs","InserWarna called")
            Log.i("WarnaProbs","refMerkk = ${refMerkk.value}")
            if (refMerkk.value!=null){
                val warna = WarnaTable().apply {
                    this.refMerk = refMerkk.value!!
                    this.kodeWarna = kodeWarna
                    this.satuan = satuan
                    this.warnaRef = UUID.randomUUID().toString()
                    this.createdBy = SharedPreferencesHelper.getLoggedInUser(getApplication())
                    this.lastEditedBy = createdBy
                    this.user = createdBy
                }
                setRefWarna(warna.warnaRef)
                Log.i("UpdateWarnaProbs","warna = ${warna}")
                insertWarnaToDao(warna)
                getWarnaByMerk(refMerkk.value)
                //insertDetailWarna(0, 0.0)
            }else Toast.makeText(getApplication(), "Pilih merk", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateWarna(warnaTable: WarnaModel) {
        viewModelScope.launch {
            _isWarnaLoading.value = true
            try {
                warnaTable.lastEditedBy = SharedPreferencesHelper.getLoggedInUser(getApplication()) ?:""
                Log.i("UpdateWarnaProbs"," update warna ${warnaTable}")
                warnaTable.warnaLastEditedDate = Date()
                //getDetailWarnaByWarnaRef(warnaTable.warnaRef)
                updateWarnaToDao(warnaTable.toWarnaTable())
                setRefWarna(warnaTable.warnaRef)
                getWarnaByMerk(refMerkk.value)
            }catch (e:Exception){
                Toast.makeText(getApplication(),"Gagal Mengubah data, coba lagi",Toast.LENGTH_SHORT).show()
                Log.i("UpdateWarnaProbs"," error${e}}")
            }
            _isWarnaLoading.value = false
        }
    }
    fun WarnaModel.toWarnaTable(): WarnaTable {
        return WarnaTable(
            idWarna = this.idWarna,
            refMerk = this.refMerk,
            kodeWarna = this.kodeWarna,
            totalPcs = this.totalPcs,
            satuanTotal = this.satuanTotal,
            satuan = this.satuan,
            warnaRef = this.warnaRef,
            lastEditedBy = loggedInUser,
            createdBy = this.createdBy,
            warnaCreatedDate = this.warnaCreatedDate,
            warnaLastEditedDate = Date()
        )
    }
    fun deleteWarna(warnaTable: WarnaModel) {
        viewModelScope.launch {
            _isWarnaLoading.value = true
            deleteWarnaToDao(warnaTable.toWarnaTable())
            getWarnaByMerk(refMerkk.value)
            getDetailWarnaByWarnaRef(refWarna.value!!)
        }
    }

    private suspend fun insertWarnaToDao(warna: WarnaTable) {
        withContext(Dispatchers.IO) {
            try {
                warnaDao.insertNew(warna)
            } catch (e: Exception) {
                Log.e("WarnaProbs", "Error inserting warna", e)
            }
        }
    }

    private suspend fun deleteWarnaToDao(warna: WarnaTable) {
        withContext(Dispatchers.IO) {
            warnaDao.deleteAnItemWarna(warna.idWarna)
        }
    }

    private suspend fun updateWarnaToDao(warna: WarnaTable) {
        withContext(Dispatchers.IO) {
            warnaDao.update(warna)
        }
    }

    fun onAddWarnaFabClick() {
        addWarnaFabM.value = true
        Log.i("SplitFragmetProbs","addWarnaFabClick ${addWarnaFabM.value}")
    }
    fun onAddWarnaFabClicked() { addWarnaFabM.value = false }
    fun onNavigateToDetailWarna(refMerk: String) { navigateToDetailWarnaM.value = refMerk }
    @SuppressLint("NullSafeMutableLiveData")
    fun onNavigatedToDetailWarna() { navigateToDetailWarnaM.value = null }

    fun onMerkLongClick(v: View): Boolean {
        Log.i("SplitFragmetProbs","addWarnaFabLongClick ")
        return true
    }
    fun onWarnaLongClick(v: View): Boolean { return true }

    @SuppressLint("NullSafeMutableLiveData")
    fun onNavigatetedToWarna(){ navigateToWarnaM.value = null }

    private fun constructYesterdayDate(month: Int): Date? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -2)
        val date = calendar.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return date
    }

    fun insertDetailWarna(pcs: Int, isi: Double) {
        viewModelScope.launch {
            _isDetailWarnaLoading.value=true
            val detailWarnaTable = DetailWarnaTable()
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            val refMerk_ = getMerkRef()
            if (loggedInUsers != null) {
                if (_refWarna.value!=null&&refMerk_!=null){
                    detailWarnaTable.warnaRef = _refWarna.value!!
                    detailWarnaTable.lastEditedBy = loggedInUsers
                    detailWarnaTable.detailWarnaLastEditedDate = Date()
                    detailWarnaTable.detailWarnaIsi = isi
                    detailWarnaTable.detailWarnaPcs = pcs
                    detailWarnaTable.user = loggedInUsers
                    val detailWarnaTable1 = checkIfIsiExisted(isi, _refWarna.value!!)
                    if (detailWarnaTable1 != null) {
                        detailWarnaTable.detailWarnaRef = detailWarnaTable1.detailWarnaRef
                        detailWarnaTable.createdBy = detailWarnaTable1.createdBy
                        detailWarnaTable.detailWarnaDate = detailWarnaTable1.detailWarnaDate
                        val log = createLog(detailWarnaTable)
                        val barangLog = createBarangLog(detailWarnaTable,log,refMerk_,detailWarnaTable.detailWarnaRef)
                        updateDetailWarnaAndInsertBarangLogAndLog(detailWarnaTable.warnaRef,detailWarnaTable.detailWarnaIsi,detailWarnaTable.detailWarnaPcs,detailWarnaTable.lastEditedBy,detailWarnaTable.detailWarnaLastEditedDate,log,barangLog)

                    } else {
                        detailWarnaTable.detailWarnaRef = UUID.randomUUID().toString()
                        detailWarnaTable.createdBy = loggedInUsers
                        detailWarnaTable.detailWarnaDate = Date()
                        val log = createLog(detailWarnaTable)
                        val barangLog = createBarangLog(detailWarnaTable,log,refMerk_,detailWarnaTable.detailWarnaRef)
                        insertDetailWarnaAndBarangLogAndLog(detailWarnaTable,log,barangLog)
                    }
                } else Toast.makeText(getApplication(), "Pilih kode warna", Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(getApplication(), userNullString, Toast.LENGTH_SHORT).show()
            }
            getDetailWarnaByWarnaRef(refWarna.value!!)
            getWarnaByMerk(refMerkk.value)
        }
    }

    fun createLog(detailWarnaTable: DetailWarnaTable):LogTable{
        val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
        return  LogTable().apply {
            refLog = UUID.randomUUID().toString()
            logTipe = MASUKKELUAR.MASUK
            createdBy = loggedInUsers
            lastEditedBy = loggedInUsers
            userName = loggedInUsers
            logCreatedDate = Date()
            logLastEditedDate = Date()
        }
    }
    fun createBarangLog(detailWarnaTable: DetailWarnaTable,log:LogTable,refMerk_: String,detailWarmaRef: String):BarangLog{
        return BarangLog().apply {
            refLog = log.refLog
            detailWarnaRef = detailWarnaTable.detailWarnaRef
            refMerk = refMerk_ // Ensure getMerkRef() returns a valid value
            warnaRef = detailWarnaTable.warnaRef
            isi = detailWarnaTable.detailWarnaIsi
            pcs = detailWarnaTable.detailWarnaPcs
            barangLogRef = UUID.randomUUID().toString()
            barangLogTipe = MASUKKELUAR.MASUK
        }
    }


    private suspend fun updateDetailWarnaAndInsertBarangLogAndLog(
        refWarna: String,
        detailWarnaIsi: Double,
        detailWarnaPcs: Int,
        lastEditedBy: String?,
        lastEditedDate: Date,
        logTable: LogTable,
        barangLog: BarangLog
    ) {
        withContext(Dispatchers.IO){
            dataSourceBarangLog.performUpdateDetailWarnaAndInsertLogAndBarangLogFromDetailWarna(refWarna, detailWarnaIsi, detailWarnaPcs, lastEditedBy, lastEditedDate, logTable, barangLog)
        }

    }
    private suspend fun insertDetailWarnaAndBarangLogAndLog(
        detailWarnaTable: DetailWarnaTable,
        logTable: LogTable,
        barangLog: BarangLog
    ) {
        withContext(Dispatchers.IO){
            dataSourceBarangLog.insertDetailWarnaAndLogAndBarangLogFromDetailWarna(detailWarnaTable,logTable,barangLog)
        }

    }

    private suspend fun getMerkRef():String{
        return withContext(Dispatchers.IO){
            warnaDao.getMerkRefByWarnaRef(_refWarna.value!!)
        }
    }
    private suspend fun checkIfIsiExisted(isi:Double,refWarna: String):DetailWarnaTable?{
        return withContext(Dispatchers.IO){
            dataSourceDetailWarna.checkIfIsiExisted(isi,refWarna)
        }
    }
    fun onAddDetailWarnaFabClick() { _addDetailWarnaFab.value = true }
    fun onAddDetailWarnaFabClicked() { _addDetailWarnaFab.value = false }
    fun onLongClick(v: View): Boolean { return false }

}
