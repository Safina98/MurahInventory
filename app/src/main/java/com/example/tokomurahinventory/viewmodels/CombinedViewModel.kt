package com.example.tokomurahinventory.viewmodels


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.model.WarnaModel
import com.example.tokomurahinventory.models.WarnaTable
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.userNullString
import com.example.tokomurahinventory.utils.viewerNotAuthorized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import java.util.UUID

class CombinedViewModel(
    val merkDao: MerkDao,
    val warnaDao: WarnaDao,
    val refMerk: String?,
    val loggedInUser: String,
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

    init {
        if (refMerk != null) {
            //getWarnaByMerk()
        }
        getAllMerkTable()
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
        canUserPerformAction(context, UserAction.INSERT) { canPerform ->
            if (canPerform) {
                addMerkFabM.value = true
            } else {
                Toast.makeText(context, viewerNotAuthorized, Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun onAddMerkFabClicked() { addMerkFabM.value = false }
    fun onNavigateToWarna(refMerk: String) { navigateToWarnaM.value = refMerk }
    @SuppressLint("NullSafeMutableLiveData")
    fun onNavigatedToWarna() { navigateToWarnaM.value = null }

    // Warna functions
    fun getWarnaByMerk(refMerk: String) {
        uiScope.launch {
            val list = withContext(Dispatchers.IO) {
                warnaDao.getWarnaWithTotalPcsList(refMerk!!)
            }
            _allWarnaByMerk.value = list
            _unFilteredWarna.value = list
            Log.i("SplitFragmetProbs","getWarnaByMerk $list")
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

    fun onAddWarnaFabClick() { addWarnaFabM.value = true }
    fun onAddWarnaFabClicked() { addWarnaFabM.value = false }
    fun onNavigateToDetailWarna(refMerk: String) { navigateToDetailWarnaM.value = refMerk }
    @SuppressLint("NullSafeMutableLiveData")
    fun onNavigatedToDetailWarna() { navigateToDetailWarnaM.value = null }



    fun onMerkLongClick(v: View): Boolean { return true }
    fun onWarnaLongClick(v: View): Boolean { return true }

    @SuppressLint("NullSafeMutableLiveData")
    fun onNavigatetedToWarna(){ navigateToWarnaM.value = null }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
