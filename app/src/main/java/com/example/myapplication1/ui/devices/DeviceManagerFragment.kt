package com.example.myapplication1.ui.devices

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication1.R
import com.example.myapplication1.data.ConnectionGroup
import com.example.myapplication1.data.SshConnection
import com.example.myapplication1.data.SupabaseRepository
import com.example.myapplication1.databinding.FragmentDashboardBinding
import com.example.myapplication1.ui.dashboard.ConnectionAdapter
import kotlinx.coroutines.launch

class DeviceManagerFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val repository = SupabaseRepository()
    private lateinit var adapter: ConnectionAdapter
    private var groups: List<ConnectionGroup> = emptyList()

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

        // Setup menu for Import/Add Group
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_dashboard, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add_group -> {
                        showAddGroupDialog()
                        true
                    }
                    R.id.action_edit_group -> {
                        showEditGroupDialog()
                        true
                    }
                    R.id.action_import -> {
                        showImportDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        adapter = ConnectionAdapter(
            connections = emptyList(),
            groups = emptyList(),
            onConnectClick = { /* Management mode - click can be edit or nothing */ 
                showOptionsDialog(it)
            },
            onLongClick = { connection ->
                showOptionsDialog(connection)
            }
        )

        binding.rvConnections.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConnections.adapter = adapter
        binding.tvHeaderTitle.text = "Manage Devices"

        binding.fabAdd.setOnClickListener {
            showAddConnectionDialog()
        }

        loadGroups()
        loadConnections()
    }

    private fun loadGroups() {
        lifecycleScope.launch {
            try {
                groups = repository.getGroups()
            } catch (e: Exception) {}
        }
    }

    private fun loadConnections() {
        lifecycleScope.launch {
            try {
                val connections = repository.getConnections()
                adapter.updateData(connections, groups)
            } catch (e: Exception) {}
        }
    }

    private fun showOptionsDialog(connection: SshConnection) {
        val options = arrayOf("Edit", "Clone", "Delete")
        AlertDialog.Builder(requireContext())
            .setTitle(connection.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditConnectionDialog(connection)
                    1 -> cloneConnection(connection)
                    2 -> showDeleteConfirmDialog(connection)
                }
            }
            .show()
    }

    private fun cloneConnection(connection: SshConnection) {
        val cloned = connection.copy(id = null, name = "${connection.name} (Clone)")
        saveConnection(cloned.name, cloned.host, cloned.username, cloned.password, cloned.port, cloned.groupId)
    }

    private fun showEditConnectionDialog(connection: SshConnection) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_connection, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etHost = dialogView.findViewById<EditText>(R.id.etHost)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val etPort = dialogView.findViewById<EditText>(R.id.etPort)
        val spGroup = dialogView.findViewById<Spinner>(R.id.spGroup)

        setupGroupSpinner(spGroup, connection.groupId)

        etName.setText(connection.name)
        etHost.setText(connection.host)
        etUsername.setText(connection.username)
        etPassword.setText(connection.password)
        etPort.setText(connection.port.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Connection")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val selectedGroupIndex = spGroup.selectedItemPosition
                val groupId = if (selectedGroupIndex > 0) groups[selectedGroupIndex - 1].id else null
                
                val updatedConn = connection.copy(
                    name = etName.text.toString(),
                    host = etHost.text.toString(),
                    username = etUsername.text.toString(),
                    password = etPassword.text.toString(),
                    port = etPort.text.toString().toIntOrNull() ?: 22,
                    groupId = groupId
                )
                updateConnection(updatedConn)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupGroupSpinner(spinner: Spinner, selectedGroupId: String? = null) {
        val groupNames = mutableListOf("No Group")
        groupNames.addAll(groups.map { it.name })
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, groupNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        if (selectedGroupId != null) {
            val index = groups.indexOfFirst { it.id == selectedGroupId }
            if (index != -1) spinner.setSelection(index + 1)
        }
    }

    private fun showDeleteConfirmDialog(connection: SshConnection) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Connection")
            .setMessage("Delete ${connection.name}?")
            .setPositiveButton("Delete") { _, _ -> connection.id?.let { deleteConnection(it) } }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateConnection(connection: SshConnection) {
        lifecycleScope.launch {
            try {
                repository.updateConnection(connection)
                loadConnections()
            } catch (e: Exception) {}
        }
    }

    private fun deleteConnection(id: String) {
        lifecycleScope.launch {
            try {
                repository.deleteConnection(id)
                loadConnections()
            } catch (e: Exception) {}
        }
    }

    private fun showAddConnectionDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_connection, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etHost = dialogView.findViewById<EditText>(R.id.etHost)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val etPort = dialogView.findViewById<EditText>(R.id.etPort)
        val spGroup = dialogView.findViewById<Spinner>(R.id.spGroup)

        setupGroupSpinner(spGroup)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Connection")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val selectedGroupIndex = spGroup.selectedItemPosition
                val groupId = if (selectedGroupIndex > 0) groups[selectedGroupIndex - 1].id else null
                saveConnection(etName.text.toString(), etHost.text.toString(), etUsername.text.toString(), 
                    etPassword.text.toString(), etPort.text.toString().toIntOrNull() ?: 22, groupId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveConnection(name: String, host: String, user: String, pass: String, port: Int, groupId: String? = null) {
        lifecycleScope.launch {
            try {
                val userId = repository.getCurrentUser()?.id
                if (userId != null) {
                    val newConn = SshConnection(user_id = userId, name = name, host = host, username = user, password = pass, port = port, groupId = groupId)
                    repository.addConnection(newConn)
                    loadConnections()
                }
            } catch (e: Exception) {}
        }
    }

    private fun showAddGroupDialog() {
        val etGroupName = EditText(requireContext()).apply { hint = "Group Name" }
        AlertDialog.Builder(requireContext())
            .setTitle("Add New Group")
            .setView(etGroupName)
            .setPositiveButton("Save") { _, _ -> saveGroup(etGroupName.text.toString()) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveGroup(name: String) {
        lifecycleScope.launch {
            try {
                val userId = repository.getCurrentUser()?.id
                if (userId != null) {
                    repository.addGroup(ConnectionGroup(user_id = userId, name = name))
                    loadGroups()
                }
            } catch (e: Exception) {}
        }
    }

    private fun showEditGroupDialog() {
        if (groups.isEmpty()) return
        val groupNames = groups.map { it.name }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Select Group to Edit")
            .setItems(groupNames) { _, which ->
                val selectedGroup = groups[which]
                val etGroupName = EditText(requireContext()).apply { setText(selectedGroup.name) }
                AlertDialog.Builder(requireContext())
                    .setTitle("Edit Group Name")
                    .setView(etGroupName)
                    .setPositiveButton("Update") { _, _ ->
                        lifecycleScope.launch {
                            repository.updateGroup(selectedGroup.copy(name = etGroupName.text.toString()))
                            loadGroups()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }.show()
    }

    private fun showImportDialog() {
        val etCsv = EditText(requireContext()).apply { hint = "name,host,username,password,port,group"; minLines = 5; gravity = Gravity.TOP }
        AlertDialog.Builder(requireContext())
            .setTitle("Import CSV")
            .setView(etCsv)
            .setPositiveButton("Import") { _, _ -> importCsvConnections(etCsv.text.toString()) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun importCsvConnections(csvStr: String) {
        lifecycleScope.launch {
            try {
                val userId = repository.getCurrentUser()?.id ?: return@launch
                csvStr.lines().forEach { line ->
                    if (line.isBlank()) return@forEach
                    val parts = line.split(",").map { it.trim() }
                    if (parts.size >= 5) {
                        val groupId = if (parts.size >= 6) groups.find { it.name.equals(parts[5], true) }?.id else null
                        repository.addConnection(SshConnection(user_id = userId, name = parts[0], host = parts[1], username = parts[2], password = parts[3], port = parts[4].toIntOrNull() ?: 22, groupId = groupId))
                    }
                }
                loadConnections()
            } catch (e: Exception) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
