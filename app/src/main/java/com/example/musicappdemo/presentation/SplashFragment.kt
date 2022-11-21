package com.example.musicappdemo.presentation

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.musicappdemo.R
import com.example.musicappdemo.databinding.FragmentSplashBinding
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashFragment : Fragment(R.layout.fragment_splash) {
    private lateinit var binding: FragmentSplashBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            binding.textVer.text = "Version number: " + requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionCode.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        lifecycleScope.launch {
            delay(3000)
            val action = SplashFragmentDirections.actionSplashFragmentToMusicFragment()
            view.findNavController().navigate(action)
        }
    }

    override fun onPause() {
        lifecycleScope.cancel()
        super.onPause()
    }
}