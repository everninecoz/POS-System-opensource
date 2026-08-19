package com.posopensrc.core.di

import android.content.Context
import com.posopensrc.data.local.PosDatabase
import com.posopensrc.data.local.dao.ActivityLogDao
import com.posopensrc.data.local.dao.CategoryDao
import com.posopensrc.data.local.dao.CustomerDao
import com.posopensrc.data.local.dao.DiscountDao
import com.posopensrc.data.local.dao.ProductDao
import com.posopensrc.data.local.dao.SettingsDao
import com.posopensrc.data.local.dao.ShiftDao
import com.posopensrc.data.local.dao.StockOpnameDao
import com.posopensrc.data.local.dao.TransactionDao
import com.posopensrc.data.local.dao.TransactionItemDao
import com.posopensrc.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PosDatabase {
        return PosDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(database: PosDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideProductDao(database: PosDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideCategoryDao(database: PosDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideTransactionDao(database: PosDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideTransactionItemDao(database: PosDatabase): TransactionItemDao {
        return database.transactionItemDao()
    }

    @Provides
    fun provideSettingsDao(database: PosDatabase): SettingsDao {
        return database.settingsDao()
    }

    @Provides
    fun provideActivityLogDao(database: PosDatabase): ActivityLogDao {
        return database.activityLogDao()
    }

    @Provides
    fun provideCustomerDao(database: PosDatabase): CustomerDao {
        return database.customerDao()
    }

    @Provides
    fun provideShiftDao(database: PosDatabase): ShiftDao {
        return database.shiftDao()
    }

    @Provides
    fun provideDiscountDao(database: PosDatabase): DiscountDao {
        return database.discountDao()
    }

    @Provides
    fun provideStockOpnameDao(database: PosDatabase): StockOpnameDao {
        return database.stockOpnameDao()
    }
}
