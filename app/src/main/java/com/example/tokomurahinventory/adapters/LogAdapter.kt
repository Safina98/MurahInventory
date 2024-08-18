package com.example.tokomurahinventory.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tokomurahinventory.databinding.ItemListLogBinding
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.utils.DATE_FORMAT


class LogAdapter(
    private val logClickListener: LogClickListener,
    private val logLongListener: LogLongListener,
    private val logDeleteListener: LogDeleteListener,
    private val isTipeVisible:Boolean
) : ListAdapter<LogTable, LogAdapter.MyViewHolder>(LogStockDiffCallback()) {

    class MyViewHolder private constructor(val binding: ItemListLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LogTable, clickListener: LogClickListener, longListener: LogLongListener, logDeleteListener: LogDeleteListener,isTipeVisible: Boolean) {
            binding.log = item
            val formattedDate = DATE_FORMAT.format(item.logLastEditedDate)
            binding.txtDate.text = formattedDate
            binding.clickListemer = clickListener
            binding.deleteListener = logDeleteListener
            if (!isTipeVisible){
                binding.txtTipe.visibility=View.GONE
            }
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
        holder.bind(getItem(position), logClickListener, logLongListener,logDeleteListener,isTipeVisible)
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