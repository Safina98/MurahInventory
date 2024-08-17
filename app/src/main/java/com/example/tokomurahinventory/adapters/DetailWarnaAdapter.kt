package com.example.tokomurahinventory.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tokomurahinventory.databinding.ItemListDetailWarnaBinding
import com.example.tokomurahinventory.models.model.DetailWarnaModel
import java.util.Locale


class DetailWarnaAdapter(
    private val detailWarnaClickListener: DetailWarnaClickListener,
    private val detailWarnaLongListener: DetailWarnaLongListener,
    private val updateDetailWarnaClickListener: UpdateDetailWarnaClickListener,
    private val deleteDetailWarnaClickListener: DeleteDetailWarnaClickListener
) : ListAdapter<DetailWarnaModel, DetailWarnaAdapter.MyViewHolder>(DetailWarnaStockDiffCallback()){

    class MyViewHolder private constructor(val binding: ItemListDetailWarnaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetailWarnaModel, clickListener: DetailWarnaClickListener, longListener: DetailWarnaLongListener,updateDetailWarnaClickListener: UpdateDetailWarnaClickListener,deleteDetailWarnaClickListener: DeleteDetailWarnaClickListener) {
            binding.detailWarna = item
            binding.updateClickListemer = updateDetailWarnaClickListener
            binding.deleteClickListener = deleteDetailWarnaClickListener
            binding.longClickListener =longListener
            binding.textView4.text = String.format(Locale.US,"%.2f", item.detailWarnaIsi)
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

class DetailWarnaStockDiffCallback: DiffUtil.ItemCallback<DetailWarnaModel>(){
    override fun areItemsTheSame(oldItem: DetailWarnaModel, newItem: DetailWarnaModel): Boolean {
        return oldItem.detailWarnaIsi== newItem.detailWarnaIsi
    }
    override fun areContentsTheSame(oldItem: DetailWarnaModel, newItem: DetailWarnaModel): Boolean {
        return oldItem == newItem
    }
}
class DetailWarnaClickListener(val clickListener: (warna: DetailWarnaModel) -> Unit) {
    fun onClick(Warna: DetailWarnaModel) = clickListener(Warna)
}
class DetailWarnaLongListener(val longListener: (Warna: DetailWarnaModel) -> Unit){
    fun onLongClick(v: View, Warna: DetailWarnaModel): Boolean {
        longListener(Warna)
        return true}
}
class UpdateDetailWarnaClickListener(val clickListener: (warna: DetailWarnaModel) -> Unit) {
    fun onClick(warna: DetailWarnaModel) = clickListener(warna)
}
class DeleteDetailWarnaClickListener(val clickListener: (warna: DetailWarnaModel) -> Unit) {
    fun onClick(warna: DetailWarnaModel) = clickListener(warna)
}