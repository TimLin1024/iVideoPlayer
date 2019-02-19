package com.android.timlin.ivedioplayer

import com.android.timlin.ivedioplayer.list.file.FileViewModelFactory
import com.android.timlin.ivedioplayer.list.file.data.FileDetector
import com.android.timlin.ivedioplayer.list.file.data.FileRepository

/**
 * Created by linjintian on 2019/2/18.
 */
object InjectorUtils {
    fun provideRepository(): FileRepository? {
        val videoFileDetector = FileDetector
        return FileRepository.getInstance(videoFileDetector)
    }

    fun provideFileViewModelFactory(): FileViewModelFactory {
        val repository = provideRepository()
        return FileViewModelFactory(repository!!)
    }


}