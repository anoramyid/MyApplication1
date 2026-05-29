package com.example.myapplication1.ui.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication1.data.SshConnection
import com.example.myapplication1.data.SupabaseRepository
import com.example.myapplication1.databinding.FragmentTerminalBinding
import com.example.myapplication1.service.SshService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TerminalFragment : Fragment() {

    private var _binding: FragmentTerminalBinding? = null
    private val binding get() = _binding!!
    private val sshService = SshService()
    private val repository = SupabaseRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTerminalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val host = arguments?.getString("host")
        val username = arguments?.getString("username")
        val port = arguments?.getInt("port") ?: 22
        val password = arguments?.getString("password")

        if (!host.isNullOrEmpty() && !username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            lifecycleScope.launch {
                val userId = repository.getCurrentUser()?.id ?: "unknown"
                val connection = SshConnection(
                    user_id = userId,
                    name = host,
                    host = host,
                    username = username,
                    password = password,
                    port = port
                )
                startSshSession(connection, password)
            }
        }

        binding.btnSend.setOnClickListener {
            val cmd = binding.etCommand.text.toString()
            if (cmd.isNotEmpty()) {
                lifecycleScope.launch {
                    sshService.sendCommand(cmd)
                    binding.etCommand.text.clear()
                }
            }
        }
    }

    private fun startSshSession(connection: SshConnection, password: String) {
        lifecycleScope.launch {
            binding.tvTerminalOutput.append("Connecting to ${connection.host}...\n")
            val result = sshService.connect(connection, password)
            if (result.isSuccess) {
                sshService.sendCommand("") 
                listenToTerminalOutput()
            } else {
                val error = result.exceptionOrNull()
                binding.tvTerminalOutput.append("Connection failed: ${error?.message}\n")
            }
        }
    }

    private fun listenToTerminalOutput() {
        lifecycleScope.launch(Dispatchers.IO) {
            val inputStream = sshService.inputStream ?: return@launch
            val buffer = ByteArray(1024)
            var bytesRead: Int
            try {
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val text = String(buffer, 0, bytesRead)
                    val cleanedText = cleanAnsiCodes(text)
                    withContext(Dispatchers.Main) {
                        binding.tvTerminalOutput.append(cleanedText)
                        binding.svTerminal.post {
                            binding.svTerminal.fullScroll(View.FOCUS_DOWN)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvTerminalOutput.append("\n[Disconnected: ${e.message}]\n")
                }
            }
        }
    }

    private fun cleanAnsiCodes(text: String): String {
        return text.replace("\u001B\\[[;\\d]*[A-Za-z]".toRegex(), "")
            .replace("\u001B\\][\\d]*;[^\u0007]*\u0007".toRegex(), "")
            .replace("\r", "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sshService.disconnect()
        _binding = null
    }
}
