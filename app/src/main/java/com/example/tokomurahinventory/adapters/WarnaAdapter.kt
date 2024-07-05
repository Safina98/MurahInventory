package com.example.tokomurahinventory.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tokomurahinventory.databinding.ItemListWarnaBinding
import com.example.tokomurahinventory.models.model.WarnaModel


class WarnaAdapter (private val warnaClickListener: WarnaClickListener,
                     private val warnaLongListener: WarnaLongListener,
                    private val updateWarnaClickListener: UpdateWarnaClickListener,
                    private val deleteWarnaClickListener: DeleteWarnaClickListener
) : ListAdapter<WarnaModel, WarnaAdapter.MyViewHolder>(WarnaStockDiffCallback()){
    class MyViewHolder private constructor(val binding: ItemListWarnaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WarnaModel, clickListener: WarnaClickListener, longListener: WarnaLongListener,updateWarnaClickListener: UpdateWarnaClickListener,deleteWarnaClickListener: DeleteWarnaClickListener) {
            binding.warna = item
            binding.clickListener = clickListener
            binding.updateClickListemer = updateWarnaClickListener
            binding.deleteClickListener = deleteWarnaClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =ItemListWarnaBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), warnaClickListener, warnaLongListener, updateWarnaClickListener,deleteWarnaClickListener)
    }
}



class WarnaStockDiffCallback: DiffUtil.ItemCallback<WarnaModel>(){
    override fun areItemsTheSame(oldItem: WarnaModel, newItem: WarnaModel): Boolean {
        return oldItem.idWarna== newItem.idWarna
    }
    override fun areContentsTheSame(oldItem: WarnaModel, newItem: WarnaModel): Boolean {
        return oldItem == newItem
    }
}
class WarnaClickListener(val clickListener: (warna: WarnaModel) -> Unit) {
    fun onClick(Warna: WarnaModel) = clickListener(Warna)
}
class  WarnaLongListener(val longListener: (Warna: WarnaModel) -> Unit){
    fun onLongClick(v: View, Warna: WarnaModel): Boolean {
        longListener(Warna)

        return true}
}
class UpdateWarnaClickListener(val clickListener: (warna: WarnaModel) -> Unit) {
    fun onClick(Warna: WarnaModel) = clickListener(Warna)
}
class DeleteWarnaClickListener(val clickListener: (warna: WarnaModel) -> Unit) {
    fun onClick(Warna: WarnaModel) = clickListener(Warna)
}