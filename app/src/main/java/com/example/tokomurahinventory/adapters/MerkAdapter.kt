package com.example.tokomurahinventory.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tokomurahinventory.databinding.ItemListMerkBinding
import com.example.tokomurahinventory.models.MerkDummyModel

class MerkAdapter(
    private val merkClickListener: MerkClickListener,
    private val merkLongListener: MerkLongListener
) : ListAdapter<MerkDummyModel, MerkAdapter.MyViewHolder>(MerkStockDiffCallback()) {

    class MyViewHolder private constructor(val binding: ItemListMerkBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MerkDummyModel, clickListener: MerkClickListener, longListener: MerkLongListener) {
            binding.merk = item
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
        holder.bind(getItem(position), merkClickListener, merkLongListener)
    }
}

class MerkStockDiffCallback: DiffUtil.ItemCallback<MerkDummyModel>(){
    override fun areItemsTheSame(oldItem: MerkDummyModel, newItem: MerkDummyModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MerkDummyModel, newItem: MerkDummyModel): Boolean {
        return oldItem == newItem
    }

}
class MerkClickListener(val clickListener: (Merk_id: MerkDummyModel) -> Unit) {
    fun onClick(Merk:MerkDummyModel) = clickListener(Merk)

}
class  MerkLongListener(val longListener: (Merk: MerkDummyModel) -> Unit){
    fun onLongClick(v: View, Merk: MerkDummyModel): Boolean {
        //logic goes here

        longListener(Merk)

        return true}
}