package com.example.tokomurahinventory.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tokomurahinventory.databinding.ItemListBarangLogBinding
import com.example.tokomurahinventory.databinding.ItemListBarangLogNewBinding
import com.example.tokomurahinventory.models.CountModel
import com.example.tokomurahinventory.viewmodels.LogViewModel

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
        // Add a function to set up TextWatcher

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

    class CountAdapterDiffCallback : DiffUtil.ItemCallback<CountModel>() {
        override fun areItemsTheSame(oldItem: CountModel, newItem: CountModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CountModel, newItem: CountModel): Boolean {
            return oldItem == newItem
        }
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
/*
        // Add a function to set up TextWatcher
        private fun setupTextWatchers(position: Int) {
            binding.inputIsi.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Update the ViewModel when input_count changes
                    val count1 = s.toString().toDoubleOrNull() ?: 0.0
                    binding.viewModel?.updateIsi(position, count1)
                }
                override fun afterTextChanged(s: Editable?) {}
            })
            binding.inputMerk.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Update the ViewModel when input_count changes
                    val count1 = s.toString()
                    binding.viewModel?.updateMerk(position, count1)
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            binding.inputKode.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Update the ViewModel when input_net changes
                    val net = s.toString()
                    binding.viewModel?.updateKode(position, net)
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            binding.inputPcs.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Update the ViewModel when input_net changes
                    val net = s.toString().toIntOrNull() ?: 0
                    binding.viewModel?.updatePcs(position, net)
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

         */
