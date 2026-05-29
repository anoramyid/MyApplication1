package com.example.myapplication1.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication1.R
import com.example.myapplication1.data.SshConnection
import com.example.myapplication1.data.SupabaseRepository
import com.example.myapplication1.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val repository = SupabaseRepository()
    private lateinit var adapter: ConnectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ConnectionAdapter(emptyList()) { connection ->
            val bundle = Bundle().apply {
                putString("host", connection.host)
                putString("username", connection.username)
                putInt("port", connection.port)
                putString("password", connection.password)
            }
            findNavController().navigate(R.id.action_dashboard_to_terminal, bundle)
        }

        binding.rvConnections.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConnections.adapter = adapter

        binding.fabAdd.setOnClickListener {
            showAddConnectionDialog()
        }

        loadConnections()
    }

    private fun showAddConnectionDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_connection, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etHost = dialogView.findViewById<EditText>(R.id.etHost)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val etPort = dialogView.findViewById<EditText>(R.id.etPort)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Connection")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString()
                val host = etHost.text.toString()
                val user = etUsername.text.toString()
                val pass = etPassword.text.toString()
                val port = etPort.text.toString().toIntOrNull() ?: 22

                if (name.isNotEmpty() && host.isNotEmpty()) {
                    saveConnection(name, host, user, pass, port)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveConnection(name: String, host: String, user: String, pass: String, port: Int) {
        lifecycleScope.launch {
            try {
                val userId = repository.getCurrentUser()?.id
                if (userId != null) {
                    val newConn = SshConnection(
                        user_id = userId,
                        name = name,
                        host = host,
                        username = user,
                        password = pass,
                        port = port
                    )
                    repository.addConnection(newConn)
                    loadConnections()
                    Toast.makeText(requireContext(), "Connection added", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadConnections() {
        lifecycleScope.launch {
            try {
                val connections = repository.getConnections()
                adapter.updateData(connections)
            } catch (e: Exception) {
                // Silent error or log
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
