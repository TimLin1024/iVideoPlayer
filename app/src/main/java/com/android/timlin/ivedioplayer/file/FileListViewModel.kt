package com.android.timlin.ivedioplayer.file

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.android.timlin.ivedioplayer.file.data.FileEntry
import com.android.timlin.ivedioplayer.file.data.VideoFileRepository

/**
 * Created by linjintian on 2019/2/17.
 */
class FileListViewModel(mRepository: VideoFileRepository) : ViewModel() {
    var mFileEntryList: LiveData<List<FileEntry>> = mRepository.getFileEntry()

}