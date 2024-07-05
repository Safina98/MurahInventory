package com.example.tokomurahinventory.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tokomurahinventory.databinding.ItemListDetailWarnaBinding
import com.example.tokomurahinventory.databinding.ItemListWarnaBinding
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.WarnaTable


class DetailWarnaAdapter(
    private val detailWarnaClickListener: DetailWarnaClickListener,
    private val detailWarnaLongListener: DetailWarnaLongListener,
    private val updateDetailWarnaClickListener: UpdateDetailWarnaClickListener,
    private val deleteDetailWarnaClickListener: DeleteDetailWarnaClickListener
) : ListAdapter<DetailWarnaTable, DetailWarnaAdapter.MyViewHolder>(DetailWarnaStockDiffCallback()){

    class MyViewHolder private constructor(val binding: ItemListDetailWarnaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetailWarnaTable, clickListener: DetailWarnaClickListener, longListener: DetailWarnaLongListener,updateDetailWarnaClickListener: UpdateDetailWarnaClickListener,deleteDetailWarnaClickListener: DeleteDetailWarnaClickListener) {
            binding.detailWarna = item
            binding.updateClickListemer = updateDetailWarnaClickListener
            binding.deleteClickListemer = deleteDetailWarnaClickListener
            //binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemListDetailWarnaBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), detailWarnaClickListener, detailWarnaLongListener,updateDetailWarnaClickListener,deleteDetailWarnaClickListener)
    }
}

class DetailWarnaStockDiffCallback: DiffUtil.ItemCallback<DetailWarnaTable>(){
    override fun areItemsTheSame(oldItem: DetailWarnaTable, newItem: DetailWarnaTable): Boolean {
        return oldItem.id== newItem.id
    }
    override fun areContentsTheSame(oldItem: DetailWarnaTable, newItem: DetailWarnaTable): Boolean {
        return oldItem == newItem
    }
}
class DetailWarnaClickListener(val clickListener: (warna: DetailWarnaTable) -> Unit) {
    fun onClick(Warna: DetailWarnaTable) = clickListener(Warna)
}
class  DetailWarnaLongListener(val longListener: (Warna: DetailWarnaTable) -> Unit){
    fun onLongClick(v: View, Warna: DetailWarnaTable): Boolean {
        longListener(Warna)
        return true}
}
class UpdateDetailWarnaClickListener(val clickListener: (warna: DetailWarnaTable) -> Unit) {
    fun onClick(warna: DetailWarnaTable) = clickListener(warna)
}
class DeleteDetailWarnaClickListener(val clickListener: (warna: DetailWarnaTable) -> Unit) {
    fun onClick(warna: DetailWarnaTable) = clickListener(warna)
}