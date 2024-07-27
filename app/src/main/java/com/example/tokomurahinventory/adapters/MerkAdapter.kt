package com.example.tokomurahinventory.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tokomurahinventory.databinding.ItemListMerkBinding
import com.example.tokomurahinventory.models.MerkTable

class MerkAdapter(
    private val merkClickListener: MerkClickListener,
    private val merkLongListener: MerkLongListener,
    private val updateMerkClickListener: UpdateMerkClickListener,
    private val deleteMerkClickListener: DeleteMerkClickListener
) : ListAdapter<MerkTable, MerkAdapter.MyViewHolder>(MerkStockDiffCallback()) {

    class MyViewHolder private constructor(val binding: ItemListMerkBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MerkTable, clickListener: MerkClickListener, longListener: MerkLongListener,updateMerkClickListener: UpdateMerkClickListener,deleteMerkClickListener: DeleteMerkClickListener) {
            binding.merk = item
            binding.clickListemer = clickListener
            binding.updateCLickListemer = updateMerkClickListener
            binding.deleteClickListemer= deleteMerkClickListener
            binding.longClickListemer = longListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemListMerkBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), merkClickListener, merkLongListener,updateMerkClickListener,deleteMerkClickListener)
    }
}

class MerkStockDiffCallback: DiffUtil.ItemCallback<MerkTable>(){
    override fun areItemsTheSame(oldItem: MerkTable, newItem: MerkTable): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: MerkTable, newItem: MerkTable): Boolean {
        return oldItem == newItem
    }
}
class MerkClickListener(val clickListener: (Merk_id: MerkTable) -> Unit) {
    fun onClick(Merk:MerkTable) = clickListener(Merk)
}
class  MerkLongListener(val longListener: (Merk: MerkTable) -> Unit){
    fun onLongClick(v: View, Merk: MerkTable): Boolean {
        //logic goes here
        longListener(Merk)
        return true}
}
class UpdateMerkClickListener(val clickListener: (merk: MerkTable) -> Unit) {
    fun onClick(Merk:MerkTable) = clickListener(Merk)
}
class DeleteMerkClickListener(val clickListener: (merk: MerkTable) -> Unit) {
    fun onClick(Merk:MerkTable) = clickListener(Merk)
}