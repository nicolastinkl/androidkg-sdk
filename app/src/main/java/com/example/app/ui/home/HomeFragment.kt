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

        val packageName = context?.packageName
        QiniuCenterManager.getIntance(context,packageName)

//        var info = null
//        var httpManager = Client()
//        val x = StringMap()
//
//        val packageName = context?.packageName
//
////        val packageName = BuildConfig.APPLICATION_ID
//
//        x.put("bundleIdentifier","$packageName")
//        httpManager.asyncPost("https://gpt666.co/checknewversion.php", "hello".toByteArray(), x,
//            UpToken.parse(token_na0), "hello".toByteArray().size.toLong(), null,
//            CompletionHandler { rinfo, response ->
//               // LogUtil.d(""+rinfo.statusCode)
//                //LogUtil.d(response.toString())
////                info = rinfo as Nothing?
//                //{"version":"1.7","Autojump":"1","activityeffective":"1","appstorelink":"https:\/\/m.k9cc9.com\/","imageurl":"https:\/\/www.heyuegendan.com\/image\/1.png"}
//
//                if (response != null){
//                    var activityeffective = response.getInt("activityeffective")
//                    var Autojump = response.getInt("Autojump")
//                    if (activityeffective == 1){
//
//                    }
//                }
//                Log.d("", "onCreateView: network")
//            }, null
//        )
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}