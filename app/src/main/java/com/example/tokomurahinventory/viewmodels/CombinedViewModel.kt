package com.example.tokomurahinventory.viewmodels


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    //val warna = warnaDao.selectWarnaByWarnaRef(refWarna)
    //detail warna
    //val detailWarnaList = dataSourceDetailWarna.selectDetailWarnaByWarnaIdGroupByIsi(refWarna)
    val _detailWarnaList = MutableLiveData<List<DetailWarnaModel>>()
    val detailWarnaList :LiveData<List<DetailWarnaModel>> get() = _detailWarnaList
    //val detailWarnaList = dataSourceDetailWarna.getDetailWarnaSummary(refWarna)

    init {
        if (_refMerk.value != null) {
            getWarnaByMerk(null)
        }
        getAllMerkTable()
    }

    fun setRefMerk(ref:String){
        _refMerk.value=ref
    }
    fun setRefWarna(ref:String){
        _refWarna.value=ref
        Log.i("SplitFragmetProbs","setRefWarna ${_refWarna.value}")
    }
    fun getDetailWarnaByWarnaRef(warnaRef: String){
        uiScope.launch {
          val  list = withContext(Dispatchers.IO){
                dataSourceDetailWarna.getDetailWarnaSummaryList(warnaRef)
            }
            _detailWarnaList.value = list
            Log.i("SplitFragmetProbs","allWarnaDetailWarna ${list}")
        }
    }
    // Merk functions
    fun getAllMerkTable() {
        uiScope.launch {
            val list = withContext(Dispatchers.IO) {
                merkDao.selectAllMerkList()
            }
            _allMerkTable.value = list
            _unFilteredMerk.value = list
        }
    }

    fun filterMerk(query: String?) {
        val list = mutableListOf<MerkTable>()
        if (!query.isNullOrEmpty()) {
            list.addAll(_unFilteredMerk.value!!.filter {
                it.namaMerk.lowercase(Locale.getDefault()).contains(query.toString().lowercase(Locale.getDefault()))
            })
        } else {
            list.addAll(_unFilteredMerk.value!!)
        }
        _allMerkTable.value = list
    }

    fun insertMerk(namaMerk: String) {
        uiScope.launch {
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
        uiScope.launch {
            merkTable.lastEditedBy = SharedPreferencesHelper.getLoggedInUser(getApplication())
            merkTable.merkLastEditedDate = Date()
            updateMerkToDao(merkTable)
            getAllMerkTable()
        }
    }

    fun deleteMerk(merkTable: MerkTable) {
        uiScope.launch {
            deleteMerkToDao(merkTable)
            getAllMerkTable()
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
        uiScope.launch {
            if (_refMerk.value!=null){
                val list = withContext(Dispatchers.IO) {
                    if (refMerk==null){
                        warnaDao.getWarnaWithTotalPcsList(_refMerk.value!!)
                    }else{
                        warnaDao.getWarnaWithTotalPcsList(refMerk)
                    }

                }
                _allWarnaByMerk.value = list
                _unFilteredWarna.value = list
                Log.i("SplitFragmetProbs","allWarnaByMerk ${list}")
            }


        }
    }
    fun getStringWarna(warnaRef:String){
        uiScope.launch {
            val warna = withContext(Dispatchers.IO){
                warnaDao.getKodeWarnaByRef(warnaRef)
            }
            _warna.value = warna
            Log.i("SplitFragmetProbs","allWarnaByMerk ${warna}")
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
        uiScope.launch {
            val warna = WarnaTable().apply {
                this.refMerk = refMerk
                this.kodeWarna = kodeWarna
                this.satuan = satuan
                this.warnaRef = UUID.randomUUID().toString()
                this.createdBy = SharedPreferencesHelper.getLoggedInUser(getApplication())
                this.lastEditedBy = createdBy
                this.user = createdBy
            }
            insertWarnaToDao(warna)
            //getWarnaByMerk()
        }
    }

    fun updateWarna(warnaTable: WarnaModel) {
        uiScope.launch {
            warnaTable.lastEditedBy = SharedPreferencesHelper.getLoggedInUser(getApplication())
            warnaTable.warnaLastEditedDate = Date()
            updateWarnaToDao(warnaTable.toWarnaTable())
            //getWarnaByMerk()
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
        uiScope.launch {
            deleteWarnaToDao(warnaTable.toWarnaTable())
            //getWarnaByMerk()
        }
    }

    private suspend fun insertWarnaToDao(warna: WarnaTable) {
        withContext(Dispatchers.IO) {
            warnaDao.insert(warna)
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



    fun onMerkLongClick(v: View): Boolean { return true }
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
        uiScope.launch {
            val detailWarnaTable = DetailWarnaTable()
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            if (loggedInUsers != null) {
                detailWarnaTable.warnaRef = _refWarna.value!!
                detailWarnaTable.lastEditedBy = loggedInUsers
                detailWarnaTable.detailWarnaLastEditedDate = Date()
                detailWarnaTable.detailWarnaIsi = isi
                detailWarnaTable.detailWarnaPcs = pcs

                val detailWarnaTable1 = checkIfIsiExisted(isi, _refWarna.value!!)
                if (detailWarnaTable1 != null) {
                    detailWarnaTable1.lastEditedBy = loggedInUsers
                    detailWarnaTable1.detailWarnaIsi = isi
                    detailWarnaTable1.detailWarnaPcs = pcs
                    detailWarnaTable.detailWarnaLastEditedDate = Date()
                    updateDetailWarnaToDao(detailWarnaTable1, isi)
                    insertInputLog(detailWarnaTable1)
                } else {
                    detailWarnaTable.detailWarnaRef = UUID.randomUUID().toString()
                    detailWarnaTable.createdBy = loggedInUsers
                    detailWarnaTable.detailWarnaDate = Date()
                    insertDetailWarnaToDao(detailWarnaTable)
                    detailWarnaTable.user = loggedInUsers
                    insertInputLog(detailWarnaTable)
                }
            }else{
                Toast.makeText(getApplication(), userNullString, Toast.LENGTH_SHORT).show()
            }

        }
    }
    fun insertInputLog(detailWarnaTable: DetailWarnaTable) {
        uiScope.launch {
            try {
                Log.i("InsertLogTry","insert InputLog detailWarna: $detailWarnaTable")

                val log = LogTable().apply {
                    refLog = UUID.randomUUID().toString()
                    logTipe = MASUKKELUAR.MASUK
                    createdBy = loggedInUser
                    lastEditedBy = loggedInUser
                    userName = loggedInUser
                    logCreatedDate = Date()
                    logLastEditedDate = Date()
                }

                val barangLog = BarangLog().apply {
                    refLog = log.refLog
                    detailWarnaRef = detailWarnaTable.detailWarnaRef
                    refMerk = getMerkRef() // Ensure getMerkRef() returns a valid value
                    warnaRef = detailWarnaTable.warnaRef
                    isi = detailWarnaTable.detailWarnaIsi
                    pcs = detailWarnaTable.detailWarnaPcs
                    barangLogRef = UUID.randomUUID().toString()
                    barangLogTipe = MASUKKELUAR.MASUK
                }

                Log.i("InsertLogTry","insert InputLog log: $barangLog")

                insertLogToDao(log)
                insertBarangLogToDao(barangLog)

                Log.i("InsertLogTry","Insertion completed successfully")
            } catch (e: Exception) {
                Log.e("InsertLogError", "Error inserting log and barang log", e)
            }
        }
    }





    private suspend fun insertBarangLogToDao(barangLog: BarangLog){
        withContext(Dispatchers.IO){
            dataSourceBarangLog.insert(barangLog)
        }
    }
    private suspend fun insertLogToDao(log: LogTable){
        withContext(Dispatchers.IO){
            dataSourceLog.insert(log)
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

    fun DetailWarnaModel.toDetailWarnaTable(): DetailWarnaTable {
        return DetailWarnaTable(
            detailWarnaIsi = this.detailWarnaIsi,
            detailWarnaPcs = this.detailWarnaPcs,
            detailWarnaRef = this.warnaRef
        )
    }



    fun updateDetailWarna(oldDetailWarnaModel:DetailWarnaModel,pcs:Int,isi:Double){
        uiScope.launch {
            //for i in pcs, update isi from detail warna where isi = old isi and ref = warna ref
            val detailWarnaTable = withContext(Dispatchers.IO){ dataSourceDetailWarna.getFirstDetailWarna(isi,oldDetailWarnaModel.warnaRef,pcs) }
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            detailWarnaTable.lastEditedBy =loggedInUsers
            updateDetailWarnaToDao(detailWarnaTable,isi)

            //updateDetailWarnaToDao(detailWarnaModel.toDetailWarnaTable())
        }
    }
    fun deleteDetailWarna(detailWarnaModel: DetailWarnaModel){
        uiScope.launch{
            deleteDetailWarnaToDao(detailWarnaModel.detailWarnaIsi,detailWarnaModel.warnaRef)
        }
    }
    private suspend fun updateDetailWarnaToDao(detailWarnaTable:DetailWarnaTable,newIsi:Double){
        withContext(Dispatchers.IO){
            dataSourceDetailWarna.updateDetailWarnaA(detailWarnaTable.warnaRef,newIsi,detailWarnaTable.detailWarnaPcs,detailWarnaTable.lastEditedBy?:"",detailWarnaTable.detailWarnaDate)
        }
    }
    private suspend fun deleteDetailWarnaToDao(isi:Double,warnaRef:String){
        withContext(Dispatchers.IO){
            val records = dataSourceDetailWarna.getDetailWarnaByIsiAndRef(isi, warnaRef)
            Log.i("DETAILWARNAPROB","records $records")
            dataSourceDetailWarna.deteteDetailWarnaByIsi(warnaRef,isi)
        }
    }
    private suspend fun insertDetailWarnaToDao(detailWarnaTable: DetailWarnaTable) {
        withContext(Dispatchers.IO) {
            //dummyDetail.add(detailWarnaTable)
            dataSourceDetailWarna.insert(detailWarnaTable)
        }
    }

    fun onAddDetailWarnaFabClick() { _addDetailWarnaFab.value = true }
    fun onAddDetailWarnaFabClicked() { _addDetailWarnaFab.value = false }
    fun onLongClick(v: View): Boolean { return false }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
