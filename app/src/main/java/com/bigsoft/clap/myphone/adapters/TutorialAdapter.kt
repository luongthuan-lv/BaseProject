package com.bigsoft.clap.myphone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import bzlibs.util.FunctionUtils
import com.bigsoft.clap.myphone.R
import com.bigsoft.clap.myphone.databinding.ItemViewpagerBinding
import com.bigsoft.clap.myphone.models.IntroTutorial
import com.bigsoft.clap.myphone.utils.Utils

class TutorialAdapter(
    private var mContext: Context, private var listTutorial: List<IntroTutorial>
) : RecyclerView.Adapter<TutorialAdapter.ViewHolder>() {

    inner class ViewHolder(var binding: ItemViewpagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            Utils.resizeView(binding.ivPicture, 1080, 810)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.item_viewpager, parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        FunctionUtils.displayImage(
            mContext, holder.binding.ivPicture, listTutorial[position].image
        )
    }

    override fun getItemCount(): Int {
        return listTutorial.size
    }

}