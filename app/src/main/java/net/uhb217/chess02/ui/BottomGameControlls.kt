package net.uhb217.chess02.ui

import android.app.Activity
import android.widget.Button
import net.uhb217.chess02.R
class BottomGameControls{
    private val backBtn: Button;
    private val forwardBtn: Button;
    private val resignBtn: Button;
    private val offerDrawBtn: Button;


    constructor(activity: Activity){
        this.backBtn = activity.findViewById(R.id.go_back_btn);
        this.forwardBtn = activity.findViewById(R.id.forward_btn);
        this.resignBtn = activity.findViewById(R.id.resign_btn);
        this.offerDrawBtn = activity.findViewById(R.id.draw_btn);
    }
}