package com.example.tokomurahinventory.viewmodels


import android.annotation.SuppressLint
import android.app.Application
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
import com.example.tokomurahinventory.utils.merkAlredyExisted
import com.example.tokomurahinventory.utils.userNullString
import com.example.tokomurahinventory.utils.warnaAlredyExisted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
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

    val _refMerk = MutableLiveData<String>()
    val refMerkk :LiveData<String> get() = _refMerk
    val _refWarna = MutableLiveData<String>()
    val refWarna :LiveData<String> get() = _refWarna
    //delete?
    val _warna = MutableLiveData<String>()
    val warna :LiveData<String>get() = _warna

    val _merk = MutableLiveData<String>("")
    val merk :LiveData<String>get() = _merk

    val isMerkClick=MutableLiveData<Boolean>(false)
    val _isWarnaClick=MutableLiveData<Boolean>(false)

    //detail warna
    //val detailWarnaList = dataSourceDetailWarna.selectDetailWarnaByWarnaIdGroupByIsi(refWarna)
    val _detailWarnaList = MutableLiveData<List<DetailWarnaModel>>()
    val detailWarnaList :LiveData<List<DetailWarnaModel>> get() = _detailWarnaList

    val _orientationMode = MutableLiveData<Int>()
    val orientationMode:LiveData<Int> get() =  _orientationMode

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _isLoadMerkCrashed = MutableLiveData<Boolean>(false)
    val isLoadMerkCrashed: LiveData<Boolean> get() = _isLoadMerkCrashed

    private val _isWarnaLoading = MutableLiveData<Boolean>(false)
    val isWarnaLoading: LiveData<Boolean> get() = _isWarnaLoading
    private val _isLoadWarnaCrashed = MutableLiveData<Boolean>(false)
    val isLoadWarnaCrashed: LiveData<Boolean> get() = _isLoadWarnaCrashed

    private val _isDetailWarnaLoading = MutableLiveData<Boolean>(false)
    val isDetailWarnaLoading: LiveData<Boolean> get() = _isWarnaLoading
    private val _isLoadDetailWarnaCrashed = MutableLiveData<Boolean>(false)
    val isLoadDetailWarnaCrashed: LiveData<Boolean> get() = _isLoadDetailWarnaCrashed


    init {
        getAllMerkTable()
    }


    fun setOrientationMode(orientationMode:Int){
        _orientationMode.value = orientationMode
    }

    fun setRefMerk(ref:String?){
        _refMerk.value=ref
    }
    fun setRefWarna(ref:String?){
        _refWarna.value=ref
        Log.i("ShowHideItem","set ref warna ${_refWarna.value}")
    }
    fun getDetailWarnaByWarnaRef(warnaRef: String?){
        viewModelScope.launch {
            _isDetailWarnaLoading.value = true
            _isLoadDetailWarnaCrashed.value = false
            try {
                val list = withContext(Dispatchers.IO){
                    if (warnaRef!=null)dataSourceDetailWarna.getDetailWarnaSummaryList0(warnaRef)
                    else listOf<DetailWarnaModel>()
                }
                _detailWarnaList.value = list
                _isDetailWarnaLoading.value = false
            }catch (e:Exception){
                _isDetailWarnaLoading.value = false
                _isLoadDetailWarnaCrashed.value = true
            }
        }
    }

    fun showOneMerkOld(bool:Boolean,ref:String){
        viewModelScope.launch {
            _isLoading.value=true
            val list = mutableListOf<MerkTable>()
            withContext(Dispatchers.IO){
                if (_unFilteredMerk.value!=null) {
                    Log.i("ShowHideItem","is  unfiltered not null")
                    if (bool==true){
                        list.addAll(_unFilteredMerk.value!!.filter {
                            it.refMerk.lowercase(Locale.getDefault()).contains(ref)
                        })
                    }else list.addAll(_unFilteredMerk.value?: listOf())
                }
            }
            _isLoading.value=false
            _allMerkTable.value = list
        }
    }

    fun showOneWarna(ref:String){
        val list = mutableListOf<WarnaModel>()
        if (_unFilteredWarna.value!=null) {
            Log.i("showwarnaprob","showonewarna")
            if (_isWarnaClick.value==true){
                Log.i("showwarnaprob","showonewarna iswarnaclick true")
                list.addAll(_unFilteredWarna.value!!.filter {
                    it.warnaRef.lowercase(Locale.getDefault()).contains(ref)
                })
                _allWarnaByMerk.value = list
            }else {
                Log.i("showwarnaprob","showonewarna iswarnaclick false")
                getWarnaByMerk(refMerkk.value)
            } //list.addAll(_unFilteredWarna.value?: listOf())

        }
    }

    fun toggleIsMerkClick(){
        isMerkClick.value = !(isMerkClick.value!!)
    }
    fun toggleIsWarnaClick(){
        _isWarnaClick.value = !(_isWarnaClick.value!!)
    }
    fun setIsWarnaClickFalse(){
        _isWarnaClick.value = false
    }
    fun isShowOneMerk(){
        if (isMerkClick.value==true &&refMerkk.value!=null){
            showOneMerkOld(isMerkClick.value!!, refMerkk.value!!)
        }else
            getAllMerkTable()
    }
    fun isShowOneWarna(){
        viewModelScope.launch {
            Log.i("ShowWarnaProb", "isShowOneWarna called")
            if (refMerkk.value!=null){
                if (_isWarnaClick.value==true &&refWarna.value!=null){
                    getOneWarna(refWarna.value!!)
                    Log.i("ShowWarnaProb", "isShowOneWarna is warna click true and ref warna not null")
                //showOneWarna( refWarna.value!!)
                }else{
                    Log.i("showwarnaprob","showonewarna iswarnaclick ${_isWarnaClick.value} or refwarna ${refWarna.value} ")
                    getWarnaByMerk(refMerkk.value)
                }
            }
            }
    }
    private suspend fun getOneWarna(refWarna: String){
        val list =withContext(Dispatchers.IO){
            warnaDao.getOneWarnaWithTotalPcsList(refWarna)
        }
        _allWarnaByMerk.value = list
    }
    private suspend fun getSatuan(refWarna: String):String{
        return withContext(Dispatchers.IO){
            warnaDao.getSatuanByRefWarna(refWarna)
        }
    }
    // Merk functions
    fun getAllMerkTable() {
        viewModelScope.launch {
            _isLoading.value = true
            _isLoadMerkCrashed.value=false
            try {
                val list = withContext(Dispatchers.IO) {
                    merkDao.selectAllMerkList()
                }
                _allMerkTable.value = list
                _unFilteredMerk.value = list
                _isLoading.value = false
            }catch (e:Exception){
                _isLoading.value = false
                _isLoadMerkCrashed.value = true
            }
        }
    }

    fun filterMerk(query: String?) {
        if (isMerkClick.value!=true){
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
    }

    private suspend fun checkIfMerkAlreadyExist(namaMerk: String):Boolean{
      return  withContext(Dispatchers.IO){
            val a = merkDao.checkIfMerkExist(namaMerk)
          if (a!=null) false else true
        }
    }

    fun insertMerk(namaMerk: String) {
        viewModelScope.launch {
            _isLoading.value = true
            //check if namamerk ada di database
            val ismerkNotDuplicate=checkIfMerkAlreadyExist(namaMerk)
            //kalau ada toas
            if (ismerkNotDuplicate){
                MerkTable().apply {
                    this.namaMerk = namaMerk
                    val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
                    if (loggedInUsers != null) {
                        this.lastEditedBy = loggedInUsers
                        this.createdBy = loggedInUsers
                        this.merkCreatedDate = Date()
                        this.merkLastEditedDate = Date()
                        this.refMerk = UUID.randomUUID().toString()
                        this.user = loggedInUsers
                        isMerkClick.value=false
                        _isWarnaClick.value=false
                        insertMerkToDao(this)
                        setRefMerk(null)
                        getStringMerk(null)
                        setRefWarna(null)
                        getStringWarna(null)

                        //isShowOneWarna()
                        //_isLoading.value = false
                        getAllMerkTable()
                    } else {
                        _isLoading.value=false
                        Toast.makeText(getApplication(), userNullString, Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                _isLoading.value=false
                Toast.makeText(getApplication(), merkAlredyExisted, Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun updateMerk(merkTable: MerkTable) {
        viewModelScope.launch {
            _isLoading.value=true
            val ismerkNotDuplicate=checkIfMerkAlreadyExist(merkTable.namaMerk)
            //kalau ada toas
            if (ismerkNotDuplicate) {
                merkTable.lastEditedBy = SharedPreferencesHelper.getLoggedInUser(getApplication())
                merkTable.merkLastEditedDate = Date()
                updateMerkToDao(merkTable)
                isMerkClick.value=false
                _isWarnaClick.value=false
                getAllMerkTable()
                setRefMerk(null)
                getStringMerk(null)
                setRefWarna(null)
                getStringWarna(null)
                getDetailWarnaByWarnaRef(null)
            }else{
                _isLoading.value=false
                Toast.makeText(getApplication(), merkAlredyExisted, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteMerk(merkTable: MerkTable) {
        viewModelScope.launch {
            _isLoading.value = true
            deleteMerkToDao(merkTable)
            getAllMerkTable()
            isMerkClick.value=false
            _isWarnaClick.value=false
            setRefMerk(null)
            getStringMerk(null)
            setRefWarna(null)
            getStringWarna(null)
            getDetailWarnaByWarnaRef(null)

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

    fun onAddMerkFabClick() {
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
            _isLoadWarnaCrashed.value = false
            try {
                if (_isWarnaClick.value==false){
                    if (_refMerk.value!=null ){
                        Log.i("showwarnaprob","getwarnaByMerkCalled")
                        val list = withContext(Dispatchers.IO) {
                            warnaDao.getWarnaWithTotalPcsList(_refMerk.value!!)
                        }
                        _allWarnaByMerk.value = list
                        _unFilteredWarna.value = list
                        //Log.i("WarnaProbs","allWarnaByMerk ${list}")
                    }else {
                        _allWarnaByMerk.value = listOf<WarnaModel>()

                    }
                }

                _isWarnaLoading.value = false
            }catch (e:Exception){
                Log.i("WarnaProbs","exception:$e")
                _isWarnaLoading.value = false
                _isLoadWarnaCrashed.value = true
            }
        }
    }

    fun getStringWarna(warnaRef:String?){
        viewModelScope.launch {
            val warna = withContext(Dispatchers.IO){
                if (warnaRef!=null) warnaDao.getKodeWarnaByRef(warnaRef)
                else ""
            }
            _warna.value = warna
        }
    }
    fun getStringMerk(refMerk:String?){
        viewModelScope.launch {
            val merk = withContext(Dispatchers.IO){
                if (refMerk!=null){
                    merkDao.getMerkNameByRef(refMerk)
            }else ""
            }
            _merk.value = merk
        }
    }

    fun filterWarna(query: String?) {
        if (_isWarnaClick.value!=true &&isMerkClick.value==true){
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
    }
    private suspend fun checkIfWarnaAlredyExist(kodeWarna: String,refMerk: String):Boolean{
        return withContext(Dispatchers.IO){
            val w = warnaDao.getWarnaRefByName(kodeWarna,refMerk)
            if (w==null) true else false
        }
    }

    fun insertWarna(kodeWarna: String, satuan: String) {
        viewModelScope.launch {
            _isWarnaLoading.value = true

            if (refMerkk.value!=null){
                val isNotWarnaDuplicate = checkIfWarnaAlredyExist(kodeWarna,refMerkk.value!!)
                if (isNotWarnaDuplicate){
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
                    Log.i("showwarnaprob","insert warna = ${warna}")
                    try {
                        insertWarnaToDao(warna)
                        _isWarnaClick.value=false
                        getWarnaByMerk(refMerkk.value)
                        setRefWarna(null)
                        getStringWarna(null)
                    }
                    catch (e:Exception){
                        _isWarnaLoading.value = false
                    }

                    //insertDetailWarna(0, 0.0)
                } else{
                    Toast.makeText(getApplication(), warnaAlredyExisted, Toast.LENGTH_SHORT).show()
                    _isWarnaLoading.value=false
                }
            }
                else Toast.makeText(getApplication(), "Pilih merk", Toast.LENGTH_SHORT).show()

        }
    }

    fun updateWarna(warnaTable: WarnaModel) {
        viewModelScope.launch {
            _isWarnaLoading.value = true
            val isNotWarnaDuplicate = checkIfWarnaAlredyExist(warnaTable.kodeWarna,refMerkk.value!!)
            if (isNotWarnaDuplicate){
            try {
                val users = SharedPreferencesHelper.getLoggedInUser(getApplication()) ?:""
                warnaTable.lastEditedBy = users
                Log.i("showwarnaprob"," update warna ${warnaTable}")
                warnaTable.warnaLastEditedDate = Date()
                updateWarnaToDao(warnaTable.kodeWarna,warnaTable.satuan,warnaTable.lastEditedBy,warnaTable.warnaLastEditedDate,warnaTable.idWarna)
                //setRefWarna(null)
                if (_isWarnaClick.value==true)
                    getStringWarna(warnaTable.warnaRef)
                else{
                    setRefWarna(null)
                    getStringWarna(null)
                }
                isShowOneWarna()
            //getWarnaByMerk(refMerkk.value)
            }catch (e:Exception){
                Toast.makeText(getApplication(),"Gagal Mengubah data, coba lagi",Toast.LENGTH_SHORT).show()
                Log.i("UpdateWarnaProbs"," error${e}}")
            }
            }else{
                Toast.makeText(getApplication(), warnaAlredyExisted, Toast.LENGTH_SHORT).show()
                _isWarnaLoading.value=false
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
            user= this.lastEditedBy,
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
            _isWarnaClick.value=false
            Log.i("showwarnaprob","delete warna")
            getWarnaByMerk(refMerkk.value)
            setRefWarna(null)
            getStringWarna(null)
        }
    }

    private suspend fun insertWarnaToDao(warna: WarnaTable) {
        withContext(Dispatchers.IO) {
            warnaDao.insertNew(warna)

        }
    }

    private suspend fun deleteWarnaToDao(warna: WarnaTable) {
        withContext(Dispatchers.IO) {
            warnaDao.deleteAnItemWarna(warna.idWarna)
        }
    }

    private suspend fun updateWarnaToDao(kodeWarna: String,
                                         satuan: String,
                                         lastEditedBy: String?,
                                         lastEditedDate:Date,
                                         warnaId:Int) {
        withContext(Dispatchers.IO) {
            //warnaDao.update(warna)

            warnaDao.updateWarna(kodeWarna,satuan,lastEditedBy,lastEditedDate,warnaId)
        }
    }

    fun onAddWarnaFabClick() {
        addWarnaFabM.value = true

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

    fun insertDetailWarna(pcs: Int, isi: Double) {
        viewModelScope.launch {
            _isDetailWarnaLoading.value=true
            val detailWarnaTable = DetailWarnaTable()
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            val ket = "Barang masuk sebanyak $pcs"
            Log.e("InsertDetailWarnaProbs","logged in user ${loggedInUsers}")
            Log.e("InsertDetailWarnaProbs","isi ${isi}")
            Log.e("InsertDetailWarnaProbs","pcs $pcs")
            if (loggedInUsers != null) {
                if (_refWarna.value!=null){
                    //val refMerk_ = getMerkRef()

                    val refMerk__ = getMerkRef()
                    if (refMerk__!=null){

                        detailWarnaTable.warnaRef = _refWarna.value!!
                        detailWarnaTable.detailWarnaLastEditedDate = Date()
                        val roundedValue = BigDecimal(isi).setScale(2, RoundingMode.HALF_EVEN).toDouble()
                        detailWarnaTable.detailWarnaIsi = roundedValue
                        detailWarnaTable.detailWarnaPcs = pcs
                        detailWarnaTable.detailWarnaKet = ket
                        detailWarnaTable.lastEditedBy = loggedInUsers
                        detailWarnaTable.dateIn=Date()
                        detailWarnaTable.user = loggedInUsers
                        val satuan = getSatuan(refWarna.value!!)
                        // val satuan = detailWarnaList.value?.get(0)?.satuan
                        val stringMerk= getStringS(merk.value!!,warna.value!!,roundedValue,satuan,pcs)
                        val detailWarnaTable1 = checkIfIsiExisted(roundedValue, _refWarna.value!!)

                        if (detailWarnaTable1 != null) {
                            detailWarnaTable.detailWarnaRef = detailWarnaTable1.detailWarnaRef
                            detailWarnaTable.createdBy = detailWarnaTable1.createdBy
                            detailWarnaTable.detailWarnaDate = detailWarnaTable1.detailWarnaDate
                            val log = createLog(detailWarnaTable,stringMerk)
                            val barangLog = createBarangLog(detailWarnaTable,log,refMerk__,detailWarnaTable.detailWarnaRef)
                            updateDetailWarnaAndInsertBarangLogAndLog(detailWarnaTable.warnaRef,detailWarnaTable.detailWarnaIsi,detailWarnaTable.detailWarnaPcs,detailWarnaTable.lastEditedBy,detailWarnaTable.detailWarnaLastEditedDate,log,barangLog,ket)
                        } else {
                            detailWarnaTable.detailWarnaRef = UUID.randomUUID().toString()
                            detailWarnaTable.createdBy = loggedInUsers
                            detailWarnaTable.detailWarnaDate = Date()
                            val log = createLog(detailWarnaTable,stringMerk)
                            val barangLog = createBarangLog(detailWarnaTable,log,refMerk__,detailWarnaTable.detailWarnaRef)
                            Log.e("InsertDetailWarnaProbs","detail warna table $detailWarnaTable")
                            Log.e("InsertDetailWarnaProbs","log $log")
                            Log.e("InsertDetailWarnaProbs","baranglog $barangLog")
                            try {
                                insertDetailWarnaAndBarangLogAndLog(detailWarnaTable,log,barangLog)
                            }catch (e:Exception){
                                Log.e("InsertDetailWarnaProbs","exception $e")
                            }

                        }
                        getDetailWarnaByWarnaRef(refWarna.value!!)
                        //getWarnaByMerk(refMerkk.value)
                        isShowOneWarna()

                    }else Toast.makeText(getApplication(), "Pilih kode warna", Toast.LENGTH_SHORT).show()


                } else Toast.makeText(getApplication(), "Pilih kode Merk dan kode warna", Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(getApplication(), userNullString, Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun deleteDetailWarna(detailWarnaModel: DetailWarnaModel){
        viewModelScope.launch {
            if (detailWarnaModel.detailWarnaPcs==0){
                deleteDetailWarna(detailWarnaModel.detailWarnaIsi,detailWarnaModel.warnaRef)
                getDetailWarnaByWarnaRef(refWarna.value!!)
                //getWarnaByMerk(refMerkk.value)
                isShowOneWarna()
            }else{
                Toast.makeText(getApplication(),"Stok tidak kosong, tidak dapat menghapus data",Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun createLog(detailWarnaTable: DetailWarnaTable,stringMerk:String):LogTable{
        val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
        return  LogTable().apply {
            refLog = UUID.randomUUID().toString()
            logTipe = MASUKKELUAR.MASUK
            createdBy = loggedInUsers
            lastEditedBy = loggedInUsers
            userName = loggedInUsers
            logCreatedDate = Date()
            logLastEditedDate = Date()
            merk =stringMerk

        }
    }

    fun getStringS(merk:String?,kodeWarna:String?,isi: Double?,satuan: String?,pcs: Int?):String{
        var s ="${merk} kode ${kodeWarna};  isi ${String.format(Locale.US,"%.2f",isi)} ${satuan}; ${pcs} pcs\n"
        return s
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
        barangLog: BarangLog,
        ket:String,

    ) {
        withContext(Dispatchers.IO){
            dataSourceBarangLog.performUpdateDetailWarnaAndInsertLogAndBarangLogFromDetailWarna(refWarna, detailWarnaIsi, detailWarnaPcs, lastEditedBy, lastEditedDate, logTable, barangLog,ket)
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
    private suspend fun deleteDetailWarna(isi:Double,warnaRef: String){
        withContext(Dispatchers.IO){
            dataSourceDetailWarna.deleteAnItemDetailWarna(isi,warnaRef)
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
