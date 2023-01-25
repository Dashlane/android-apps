package com.dashlane.ui.menu

import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class MenuModule {
    @Singleton
    @Binds
    abstract fun bindMenuPresenter(impl: MenuPresenter): MenuDef.IPresenter
}