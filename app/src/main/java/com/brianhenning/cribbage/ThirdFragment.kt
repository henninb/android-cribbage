package com.brianhenning.cribbage

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.brianhenning.cribbage.databinding.ThirdFragmentBinding
import com.brianhenning.cribbage.R
import com.perimeterx.mobile_sdk.PerimeterX

class ThirdFragment : Fragment() {
    private var _binding: ThirdFragmentBinding? = null
    private val binding get() = _binding!!
    lateinit var mywebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ThirdFragmentBinding.inflate(inflater, container, false)
        //mywebView = findViewById(R.id.thirdFragment)
        mywebView = binding.webView
        mywebView.webViewClient = WebViewClient()
        mywebView.apply {
            loadUrl("https://www.bhenning.com/")
            settings.javaScriptEnabled = true
            settings.safeBrowsingEnabled = true
        }
        //PerimeterX.setupWebView(mywebView, this)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
