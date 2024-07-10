package com.example.tokomurahinventory.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tokomurahinventory.databinding.ItemListLogBinding
import com.example.tokomurahinventory.models.LogTable



class LogAdapter(
    private val LogClickListener: LogClickListener,
    private val LogLongListener: LogLongListener,
    private val logDeleteListener: LogDeleteListener,
) : ListAdapter<LogTable, LogAdapter.MyViewHolder>(LogStockDiffCallback()) {

    class MyViewHolder private constructor(val binding: ItemListLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LogTable, clickListener: LogClickListener, longListener: LogLongListener,logDeleteListener: LogDeleteListener) {
            binding.log = item
            binding.clickListemer = clickListener
            binding.deleteListener = logDeleteListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemListLogBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), LogClickListener, LogLongListener,logDeleteListener)
    }
}

class LogStockDiffCallback: DiffUtil.ItemCallback<LogTable>(){
    override fun areItemsTheSame(oldItem: LogTable, newItem: LogTable): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: LogTable, newItem: LogTable): Boolean {
        return oldItem == newItem
    }
}

class LogClickListener(val clickListener: (log: LogTable) -> Unit) {
    fun onClick(log: LogTable) = clickListener(log)
}
class LogDeleteListener(val clickListener: (log: LogTable) -> Unit) {
    fun onClick(log: LogTable) = clickListener(log)
}
class  LogLongListener(val longListener: (log: LogTable) -> Unit){
    fun onLongClick(v: View, log: LogTable): Boolean {
        //logic goes here
        longListener(log)
        return true}
}