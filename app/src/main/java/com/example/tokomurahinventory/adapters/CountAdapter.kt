package com.example.tokomurahinventory.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tokomurahinventory.databinding.ItemListBarangLogNewBinding
import com.example.tokomurahinventory.models.CountModel
import com.example.tokomurahinventory.viewmodels.LogViewModel
import java.util.Locale

class CountAdapter(
    private val clickListener: AddNetClickListener,
    private val deleteNetListener: DeleteNetClickListener,
    private val barangLogMerkClickListener: BarangLogMerkClickListener,
    private val barangLogKodeClickListener: BarangLogKodeClickListener,
    private val barangLogIsiClickListener: BarangLogIsiClickListener,
    private val barangLogPcsClickListener: BarangLogPcsClickListener,
    private val viewModel: LogViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<CountModel, CountAdapter.ViewHolder>(CountAdapterDiffCallback()) {

    class ViewHolder private constructor(private val binding: ItemListBarangLogNewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: CountModel, position: Int,
            clickListener: AddNetClickListener,
            deleteNetListener: DeleteNetClickListener,
            barangLogMerkClickListener: BarangLogMerkClickListener,
            barangLogKodeClickListener: BarangLogKodeClickListener,
            barangLogIsiClickListener: BarangLogIsiClickListener,
            barangLogPcsClickListener: BarangLogPcsClickListener,
            viewModel: LogViewModel,
            lifecycleOwner: LifecycleOwner
        ) {
            binding.item = item
            binding.clickListener = clickListener
            binding.delteListener = deleteNetListener
            binding.merkListener = barangLogMerkClickListener
            binding.kodeListener = barangLogKodeClickListener
            binding.isiistener = barangLogIsiClickListener
            binding.pcsListener = barangLogPcsClickListener
            binding.position = position
            binding.viewModel = viewModel
            binding.lifecycleOwner = lifecycleOwner
            binding.inputIsi.text= String.format(Locale.US,"%.2f", item.isi)
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemListBarangLogNewBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position, clickListener, deleteNetListener, barangLogMerkClickListener, barangLogKodeClickListener, barangLogIsiClickListener,barangLogPcsClickListener,viewModel, lifecycleOwner)
    }
}
class CountAdapterDiffCallback : DiffUtil.ItemCallback<CountModel>() {
    override fun areItemsTheSame(oldItem: CountModel, newItem: CountModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CountModel, newItem: CountModel): Boolean {
        return oldItem == newItem
    }
}
class AddNetClickListener(val clickListener: (countModel: CountModel, position: Int) -> Unit) {
    fun onAddNetClick(countModel: CountModel, position: Int) {
        clickListener(countModel, position)
    }
}
class DeleteNetClickListener(val clickListener: (countModel: CountModel, position: Int) -> Unit) {
    fun onDeleteNetClick(countModel: CountModel, position: Int) {
        clickListener(countModel, position)
    }
}
class BarangLogMerkClickListener(val clickListener: (countModel: CountModel, position: Int) -> Unit) {
    fun onAddNetClick(countModel: CountModel, position: Int) {
        clickListener(countModel, position)
    }
}
class BarangLogKodeClickListener(val clickListener: (countModel: CountModel, position: Int) -> Unit) {
    fun onAddNetClick(countModel: CountModel, position: Int) {
        clickListener(countModel, position)
    }
}
class BarangLogIsiClickListener(val clickListener: (countModel: CountModel, position: Int) -> Unit) {
    fun onAddNetClick(countModel: CountModel, position: Int) {
        clickListener(countModel, position)
    }
}
class BarangLogPcsClickListener(val clickListener: (countModel: CountModel, position: Int) -> Unit) {
    fun onAddNetClick(countModel: CountModel, position: Int) {
        clickListener(countModel, position)
    }
}
