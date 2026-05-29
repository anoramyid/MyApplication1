package com.example.myapplication1.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication1.R
import com.example.myapplication1.data.SupabaseRepository
import com.example.myapplication1.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private val repository = SupabaseRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        repository.signIn(email, password)
                        findNavController().navigate(R.id.action_login_to_dashboard)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        repository.signUp(email, password)
                        Toast.makeText(requireContext(), "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
