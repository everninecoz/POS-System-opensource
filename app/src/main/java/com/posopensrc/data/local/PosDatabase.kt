package com.posopensrc.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.posopensrc.core.utils.AppConstants
import com.posopensrc.core.security.PasswordUtils
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
import com.posopensrc.data.local.entity.ActivityLogEntity
import com.posopensrc.data.local.entity.CategoryEntity
import com.posopensrc.data.local.entity.CustomerEntity
import com.posopensrc.data.local.entity.DiscountEntity
import com.posopensrc.data.local.entity.ProductEntity
import com.posopensrc.data.local.entity.SettingsEntity
import com.posopensrc.data.local.entity.ShiftEntity
import com.posopensrc.data.local.entity.StockOpnameEntity
import com.posopensrc.data.local.entity.StockOpnameItemEntity
import com.posopensrc.data.local.entity.TransactionEntity
import com.posopensrc.data.local.entity.TransactionItemEntity
import com.posopensrc.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        SettingsEntity::class,
        ActivityLogEntity::class,
        CustomerEntity::class,
        ShiftEntity::class,
        DiscountEntity::class,
        StockOpnameEntity::class,
        StockOpnameItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transactionItemDao(): TransactionItemDao
    abstract fun settingsDao(): SettingsDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun customerDao(): CustomerDao
    abstract fun shiftDao(): ShiftDao
    abstract fun discountDao(): DiscountDao
    abstract fun stockOpnameDao(): StockOpnameDao

    companion object {
        @Volatile
        private var INSTANCE: PosDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create customers table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS customers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        phone TEXT,
                        email TEXT,
                        address TEXT,
                        total_purchases REAL NOT NULL DEFAULT 0.0,
                        purchase_count INTEGER NOT NULL DEFAULT 0,
                        loyalty_points INTEGER NOT NULL DEFAULT 0,
                        notes TEXT,
                        is_active INTEGER NOT NULL DEFAULT 1,
                        created_at INTEGER NOT NULL DEFAULT 0,
                        updated_at INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_customers_phone ON customers (phone)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_customers_name ON customers (name)")

                // Create shifts table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS shifts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id INTEGER NOT NULL,
                        opening_balance REAL NOT NULL DEFAULT 0.0,
                        closing_balance REAL,
                        actual_balance REAL,
                        total_sales REAL NOT NULL DEFAULT 0.0,
                        total_transactions INTEGER NOT NULL DEFAULT 0,
                        is_open INTEGER NOT NULL DEFAULT 1,
                        opened_at INTEGER NOT NULL DEFAULT 0,
                        closed_at INTEGER,
                        notes TEXT,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_shifts_user_id ON shifts (user_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_shifts_is_open ON shifts (is_open)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_shifts_opened_at ON shifts (opened_at)")

                // Create transaction_items table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS transaction_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        transaction_id INTEGER NOT NULL,
                        product_id INTEGER NOT NULL,
                        product_name TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        price REAL NOT NULL,
                        cost_price REAL NOT NULL DEFAULT 0.0,
                        subtotal REAL NOT NULL,
                        is_voided INTEGER NOT NULL DEFAULT 0,
                        void_reason TEXT,
                        created_at INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
                        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_items_transaction_id ON transaction_items (transaction_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transaction_items_product_id ON transaction_items (product_id)")

                // Add new columns to transactions table
                db.execSQL("ALTER TABLE transactions ADD COLUMN is_voided INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN void_reason TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN voided_at INTEGER")
                db.execSQL("ALTER TABLE transactions ADD COLUMN customer_id INTEGER")
                db.execSQL("ALTER TABLE transactions ADD COLUMN shift_id INTEGER")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create discounts table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS discounts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        code TEXT,
                        description TEXT,
                        discount_type TEXT NOT NULL,
                        discount_value REAL NOT NULL,
                        min_purchase REAL NOT NULL DEFAULT 0.0,
                        max_discount REAL,
                        buy_quantity INTEGER,
                        get_quantity INTEGER,
                        product_id INTEGER,
                        category_id INTEGER,
                        usage_limit INTEGER,
                        usage_count INTEGER NOT NULL DEFAULT 0,
                        is_active INTEGER NOT NULL DEFAULT 1,
                        valid_from INTEGER NOT NULL DEFAULT 0,
                        valid_until INTEGER NOT NULL DEFAULT 9223372036854775807,
                        created_at INTEGER NOT NULL DEFAULT 0,
                        updated_at INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_discounts_code ON discounts (code)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_discounts_is_active ON discounts (is_active)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_discounts_valid_until ON discounts (valid_until)")

                // Create stock_opnames table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS stock_opnames (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        user_id INTEGER NOT NULL,
                        notes TEXT,
                        total_items INTEGER NOT NULL DEFAULT 0,
                        items_with_difference INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_opnames_user_id ON stock_opnames (user_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_opnames_created_at ON stock_opnames (created_at)")

                // Create stock_opname_items table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS stock_opname_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        opname_id INTEGER NOT NULL,
                        product_id INTEGER NOT NULL,
                        product_name TEXT NOT NULL,
                        system_stock INTEGER NOT NULL,
                        physical_stock INTEGER NOT NULL,
                        difference INTEGER NOT NULL,
                        notes TEXT,
                        created_at INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (opname_id) REFERENCES stock_opnames(id) ON DELETE CASCADE,
                        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_opname_items_opname_id ON stock_opname_items (opname_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_opname_items_product_id ON stock_opname_items (product_id)")
            }
        }

        fun getDatabase(context: Context): PosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PosDatabase::class.java,
                    AppConstants.DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }

        private suspend fun populateDatabase(database: PosDatabase) {
            val now = System.currentTimeMillis()

            // Create default admin user
            val adminPinHash = PasswordUtils.hashPin(AppConstants.DEFAULT_ADMIN_PIN)
            val adminPasswordHash = PasswordUtils.hashPassword(AppConstants.DEFAULT_ADMIN_PASSWORD)
            database.userDao().insertUser(
                UserEntity(
                    username = AppConstants.DEFAULT_ADMIN_USERNAME,
                    pinHash = adminPinHash,
                    passwordHash = adminPasswordHash,
                    fullName = AppConstants.DEFAULT_ADMIN_NAME,
                    role = AppConstants.ROLE_ADMIN,
                    createdAt = now,
                    updatedAt = now
                )
            )

            // Create default categories
            database.categoryDao().insertCategories(
                listOf(
                    CategoryEntity(name = "Makanan", icon = "🍜", sortOrder = 1),
                    CategoryEntity(name = "Minuman", icon = "☕", sortOrder = 2),
                    CategoryEntity(name = "Snack", icon = "🍪", sortOrder = 3),
                    CategoryEntity(name = "Rokok", icon = "🚬", sortOrder = 4),
                    CategoryEntity(name = "Kebutuhan", icon = "🧴", sortOrder = 5)
                )
            )

            // Create default settings
            database.settingsDao().insertSettings(
                SettingsEntity(
                    storeName = "Warung Saya",
                    taxPercentage = AppConstants.DEFAULT_TAX_PERCENTAGE,
                    language = "id",
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }
}
