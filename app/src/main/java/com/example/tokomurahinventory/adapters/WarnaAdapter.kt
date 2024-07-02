package com.example.tokomurahinventory.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tokomurahinventory.databinding.ItemListWarnaBinding
import com.example.tokomurahinventory.models.WarnaTable


class WarnaAdapter (private val warnaClickListener: WarnaClickListener,
                     private val warnaLongListener: WarnaLongListener
) : ListAdapter<WarnaTable, WarnaAdapter.MyViewHolder>(WarnaStockDiffCallback()){
    class MyViewHolder private constructor(val binding: ItemListWarnaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WarnaTable, clickListener: WarnaClickListener, longListener: WarnaLongListener) {
            binding.warna = item
            binding.clickListener = clickListener
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
        holder.bind(getItem(position), warnaClickListener, warnaLongListener)
    }
}



class WarnaStockDiffCallback: DiffUtil.ItemCallback<WarnaTable>(){
    override fun areItemsTheSame(oldItem: WarnaTable, newItem: WarnaTable): Boolean {
        return oldItem.idWarna== newItem.idWarna
    }

    override fun areContentsTheSame(oldItem: WarnaTable, newItem: WarnaTable): Boolean {
        return oldItem == newItem
    }

}
class WarnaClickListener(val clickListener: (warna: WarnaTable) -> Unit) {
    fun onClick(Warna: WarnaTable) = clickListener(Warna)

}
class  WarnaLongListener(val longListener: (Warna: WarnaTable) -> Unit){
    fun onLongClick(v: View, Warna: WarnaTable): Boolean {
        longListener(Warna)

        return true}
}