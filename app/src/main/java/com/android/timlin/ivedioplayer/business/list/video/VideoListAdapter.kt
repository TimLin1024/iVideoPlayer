package com.android.timlin.ivedioplayer.business.list.video

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_video.view.*
import java.util.*


/**
 * Created by linjintian on 2019/2/19.
 */
class VideoListAdapter : RecyclerView.Adapter<VideoListAdapter.VideoFileViewHolder>() {
    private var mVideoEntryList: List<VideoEntry> = Collections.emptyList()
    var mOnItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoFileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(com.android.timlin.ivedioplayer.R.layout.item_video, parent, false)
        return VideoFileViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mVideoEntryList.size
    }

    override fun onBindViewHolder(holder: VideoFileViewHolder, position: Int) {
        holder.bindView(mVideoEntryList[position])
    }

    fun swapList(videoEntryList: List<VideoEntry>) {
        mVideoEntryList = videoEntryList
        notifyDataSetChanged()
    }

    inner class VideoFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener { mOnItemClickListener?.onItemClick(adapterPosition, mVideoEntryList[adapterPosition]) }
        }

        fun bindView(videoEntry: VideoEntry) {
//            GlideApp.with(itemView.context)
//                    .load(videoEntry.thumbnail)
//                    .into(itemView.mIvVideoPreview)
//            val thumbnail = ThumbnailUtils.createVideoThumbnail(videoEntry.path, MediaStore.Images.Thumbnails.MINI_KIND)
            itemView.mIvVideoPreview.setImageBitmap(videoEntry.thumbnail)
            itemView.mTvName.text = videoEntry.name
            itemView.mTvTime.text = videoEntry.time
            //TODO "预览"

        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, videoEntry: VideoEntry)
    }
}