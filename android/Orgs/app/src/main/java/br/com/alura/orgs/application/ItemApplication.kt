package br.com.alura.orgs.application

import android.app.Application
import br.com.alura.orgs.model.source.ItemRoomDatabase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ItemApplication : Application() /*{
    @Inject
    lateinit var database: ItemRoomDatabase
}*/