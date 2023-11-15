package com.duanstar.locationfaker.fake_location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StopFakeLocationBroadcast : BroadcastReceiver() {

    @Inject lateinit var stateMachine: FakeLocationStateMachine

    override fun onReceive(context: Context, intent: Intent) {
        stateMachine.off()
    }
}
