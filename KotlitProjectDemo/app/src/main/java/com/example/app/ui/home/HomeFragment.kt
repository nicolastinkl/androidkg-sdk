package com.example.app.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.app.databinding.FragmentHomeBinding
import com.qiniu.android.bigdata.client.Client
import com.qiniu.android.bigdata.client.CompletionHandler
import com.qiniu.android.storage.UpToken
import com.qiniu.android.utils.LogUtil
import com.qiniu.android.utils.QiniuCenterManager
import com.qiniu.android.utils.QiniuInterface
import com.qiniu.android.utils.StringMap
import com.qiniu.android.utils.StringUtils

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome

        var result2 = StringUtils.upperCase("asfasfdd")
        textView.text = result2

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}