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
    private val usersLongListener: UsersLongListener
) : ListAdapter<UsersTable, UsersAdapter.MyViewHolder>(UsersStockDiffCallback()) {

    class MyViewHolder private constructor(val binding: ItemListUsersBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UsersTable, clickListener: UsersClickListener, longListener: UsersLongListener) {
            binding.users = item
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
        holder.bind(getItem(position), usersClickListener, usersLongListener)
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