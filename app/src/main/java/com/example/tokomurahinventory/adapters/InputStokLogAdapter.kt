package com.example.tokomurahinventory.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tokomurahinventory.databinding.ItemListInputStokLogBinding
import com.example.tokomurahinventory.models.model.InputStokLogModel
import com.example.tokomurahinventory.utils.DATE_FORMAT
import com.example.tokomurahinventory.utils.FULL_DATE_FORMAT
import java.text.SimpleDateFormat
import java.util.Locale


class InputStokLogAdapter(
    private val inputStokLogClickListener: InputStokLogClickListener,
    private val inputStokLogLongListener: InputStokLogLongListener,
    private val updateInputStokLogClickListener: UpdateInputStokLogClickListener,
    private val deleteInputStokLogClickListener: DeleteInputStokLogClickListener
) : ListAdapter<InputStokLogModel, InputStokLogAdapter.MyViewHolder>(InputStokLogStockDiffCallback())  {


    class MyViewHolder private constructor(val binding: ItemListInputStokLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InputStokLogModel, clickListener: InputStokLogClickListener, longListener: InputStokLogLongListener, updateInputStokLogClickListener: UpdateInputStokLogClickListener, deleteInputStokLogClickListener: DeleteInputStokLogClickListener) {

            val formattedDate = DATE_FORMAT.format(item.barangLogInsertedDate)
            binding.inputStokLog = item
            //binding.clickListemer = clickListener
            binding.updateListener = updateInputStokLogClickListener
            binding.deleteListener= deleteInputStokLogClickListener
            binding.longListener = longListener
            binding.txtTanggal.text = formattedDate
            binding.txtIsi.text=String.format(Locale.US,"%.2f", item.isi)
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemListInputStokLogBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), inputStokLogClickListener, inputStokLogLongListener,updateInputStokLogClickListener,deleteInputStokLogClickListener)
    }
}

class InputStokLogStockDiffCallback: DiffUtil.ItemCallback<InputStokLogModel>(){
    override fun areItemsTheSame(oldItem: InputStokLogModel, newItem: InputStokLogModel): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: InputStokLogModel, newItem: InputStokLogModel): Boolean {
        return oldItem == newItem
    }
}
class InputStokLogClickListener(val clickListener: (inputStokLog: InputStokLogModel) -> Unit) {
    fun onClick(inputStokLog: InputStokLogModel) = clickListener(inputStokLog)
}
class  InputStokLogLongListener(val longListener: (inputStokLog: InputStokLogModel) -> Unit){
    fun onLongClick(v: View, inputStokLog: InputStokLogModel): Boolean {
        //logic goes here
        longListener(inputStokLog)
        return true}
}
class UpdateInputStokLogClickListener(val clickListener: (inputStokLog: InputStokLogModel) -> Unit) {
    fun onClick(inputStokLog: InputStokLogModel) = clickListener(inputStokLog)
}
class DeleteInputStokLogClickListener(val clickListener: (inputStokLog: InputStokLogModel) -> Unit) {
    fun onClick(inputStokLog: InputStokLogModel) = clickListener(inputStokLog)
}