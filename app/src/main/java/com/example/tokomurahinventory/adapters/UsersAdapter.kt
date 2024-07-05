package com.example.tokomurahinventory.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tokomurahinventory.databinding.ItemListMerkBinding
import com.example.tokomurahinventory.databinding.ItemListUsersBinding
import com.example.tokomurahinventory.models.UsersTable


class UsersAdapter(
    private val usersClickListener: UsersClickListener,
    private val usersLongListener: UsersLongListener,
    private val updateUsersClickListener: UpdateUsersClickListener,
    private val deleteUsersClickListener: DeleteUsersClickListener
) : ListAdapter<UsersTable, UsersAdapter.MyViewHolder>(UsersStockDiffCallback()) {

    class MyViewHolder private constructor(val binding: ItemListUsersBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UsersTable, clickListener: UsersClickListener, longListener: UsersLongListener,updateUsersClickListener: UpdateUsersClickListener,deleteUsersClickListener: DeleteUsersClickListener) {
            binding.users = item
            binding.updateClickListemer = updateUsersClickListener
            binding.deleteClickListener = deleteUsersClickListener
          //  binding.clickListemer = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemListUsersBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position), usersClickListener, usersLongListener,updateUsersClickListener,deleteUsersClickListener)
    }
}

class UsersStockDiffCallback: DiffUtil.ItemCallback<UsersTable>(){
    override fun areItemsTheSame(oldItem: UsersTable, newItem: UsersTable): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: UsersTable, newItem: UsersTable): Boolean {
        return oldItem == newItem
    }
}
class UsersClickListener(val clickListener: (users: UsersTable) -> Unit) {
    fun onClick(Users: UsersTable) = clickListener(Users)
}
class  UsersLongListener(val longListener: (users: UsersTable) -> Unit){
    fun onLongClick(v: View, users: UsersTable): Boolean {
        //logic goes here
        longListener(users)
        return true}
}
class UpdateUsersClickListener(val clickListener: (users: UsersTable) -> Unit) {
    fun onClick(Users: UsersTable) = clickListener(Users)
}
class DeleteUsersClickListener(val clickListener: (users: UsersTable) -> Unit) {
    fun onClick(Users: UsersTable) = clickListener(Users)
}