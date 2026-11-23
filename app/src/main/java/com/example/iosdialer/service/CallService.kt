package com.example.iosdialer.service

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.example.iosdialer.CallActivity

class CallService : InCallService() {

    companion object {
        var currentCall: Call? = null
        var callListener: ((Call?) -> Unit)? = null
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        currentCall = call
        callListener?.invoke(call)

        // Call connect hote hi custom iOS call screen open karein
        val intent = Intent(this, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        currentCall = null
        callListener?.invoke(null)
    }
}
