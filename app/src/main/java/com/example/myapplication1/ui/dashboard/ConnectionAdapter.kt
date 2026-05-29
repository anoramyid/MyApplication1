package com.example.myapplication1.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication1.R
import com.example.myapplication1.data.SshConnection

class ConnectionAdapter(
    private var connections: List<SshConnection>,
    private val onClick: (SshConnection) -> Unit
) : RecyclerView.Adapter<ConnectionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(android.R.id.text1)
        val tvHost: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val connection = connections[position]
        holder.tvName.text = connection.name
        holder.tvHost.text = "${connection.username}@${connection.host}:${connection.port}"
        holder.itemView.setOnClickListener { onClick(connection) }
    }

    override fun getItemCount() = connections.size

    fun updateData(newConnections: List<SshConnection>) {
        connections = newConnections
        notifyDataSetChanged()
    }
}
