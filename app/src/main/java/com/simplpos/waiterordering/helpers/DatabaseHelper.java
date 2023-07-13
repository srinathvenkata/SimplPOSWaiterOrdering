package com.simplpos.waiterordering.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "simplpos_waiter_db";
    private String TAG = "DbHelperTag";
    public static final String PREFERENCES_TABLE = "preferences";
    public static final String PREFERENCES_ATTRIBUTE = "attribute";
    public static final String PREFERENCES_VALUE = "value";
    public static final String CREATED_TIME = "created_time";
    public static final String UPDATED_TIME = "updated_time";


    public static final String CATEGORYWISE_PRINTING_TABLE = "categorywise_printing";
    public static final String CATEGORYWISE_PRINTING_CATEGORY_ID = "category_id";
    public static final String CATEGORYWISE_PRINTING_PRINTER_ROW_ID = "category_printer_row_id";
    public static final String CATEGORYWISE_PRINTING_PRINTER_NAME = "category_printer_name";

    SQLiteDatabase db= null;
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        db= this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String usersTableCreateQuery = "CREATE TABLE IF NOT EXISTS "+ PREFERENCES_TABLE +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                +PREFERENCES_ATTRIBUTE + " TEXT,"+ PREFERENCES_VALUE+" TEXT,"+CREATED_TIME+" TEXT,"+UPDATED_TIME+" TEXT)";
        sqLiteDatabase.execSQL(usersTableCreateQuery);

        String CategoryPrintingCreateQuery = "CREATE TABLE IF NOT EXISTS "+ CATEGORYWISE_PRINTING_TABLE +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                +CATEGORYWISE_PRINTING_CATEGORY_ID + " TEXT,"+ CATEGORYWISE_PRINTING_PRINTER_NAME+" TEXT,"+CREATED_TIME+" TEXT,"+UPDATED_TIME+" TEXT, "+ CATEGORYWISE_PRINTING_PRINTER_ROW_ID +" TEXT )";
        sqLiteDatabase.execSQL(CategoryPrintingCreateQuery);

        String inventoryDetails = "create table if not exists "
                + DatabaseVariables.INVENTORY_TABLE + " ( " + DatabaseVariables.ID
                + " integer primary key autoincrement, " + DatabaseVariables.INVENTORY_ITEM_NAME
                + " text, " + DatabaseVariables.INVENTORY_DEPARTMENT + " text, "
                + " is_combo_item text, "
                + "parent_item_qty text, "
                + DatabaseVariables.INVENTORY_ITEM_NO + " text, " + DatabaseVariables.INVENTORY_AVG_COST
                + " text, " + DatabaseVariables.INVENTORY_IN_STOCK + " text, "
                + DatabaseVariables.INVENTORY_PRICE_CHANGE + " text, " + DatabaseVariables.INVENTORY_PRICE_TAX
                + " text, " + DatabaseVariables.INVENTORY_QUANTITY + " text, " + DatabaseVariables.INVENTORY_TAXONE
                + " text, " + DatabaseVariables.INVENTORY_VENDOR + " text, " + DatabaseVariables.CHECKED_VALUE
                + " text, " + DatabaseVariables.INVENTORY_SECOND_DESCRIPTION + " text, "
                + DatabaseVariables.INVENTORY_CATEGORY + " text, " + DatabaseVariables.UNIQUE_ID + " text, "
                + " parent_item_no text, "
                + DatabaseVariables.INVENTORY_TAKEAWAY + " text, " + DatabaseVariables.INVENTORY_TAKEAWAY_TAX + " text, "
                + DatabaseVariables.INVENTORY_TOTAL_TAX + " text, " + DatabaseVariables.CREATED_DATE + " text, "
                + DatabaseVariables.MODIFIED_DATE + " text, " + DatabaseVariables.INVENTORY_NOTES + " text, " + DatabaseVariables.INVENTORY_ITEMBARCODE + " text, "
                + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.INVENTORY_LOCAL_NAME + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(inventoryDetails);


        String CategoryDetails = "create table if not exists " + DatabaseVariables.CATEGORY_TABLE
                + " ( " + DatabaseVariables.ID + " integer primary key autoincrement, "
                + DatabaseVariables.UNIQUE_ID + " text, " + DatabaseVariables.CREATED_DATE + " text, "
                + DatabaseVariables.MODIFIED_DATE + " text, " + DatabaseVariables.MODIFIED_IN + " text, "
                + DatabaseVariables.CategoryId + " text, " + DatabaseVariables.CategoryDesp + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(CategoryDetails);

        String DepartmentDetails = "create table if not exists "
                + DatabaseVariables.DEPARTMENT_TABLE + " ( " + DatabaseVariables.ID
                + " integer primary key autoincrement, " + DatabaseVariables.UNIQUE_ID
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.DepartmentID
                + " text, " + DatabaseVariables.DepartmentDesp + " text, " + DatabaseVariables.FoodstampableForDept
                + " text, " + DatabaseVariables.TaxValForDept + " text, " + DatabaseVariables.CHECKED_VALUE
                + " text, " + DatabaseVariables.CategoryForDepartment + " text, local_name text , UNIQUE (unique_id) ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(DepartmentDetails);


        String ALTERNATE_PLU = "create table if not exists " + DatabaseVariables.ALTERNATE_PLU_TABLE + " "
                + "( _id integer primary key autoincrement," + DatabaseVariables.ALTERNATE_PLU_item_no + " text,"
                + DatabaseVariables.ALTERNATE_PLU_item_name + " text,"
                + DatabaseVariables.ALTERNATE_PLU_plu_number + " text,"
                + DatabaseVariables.ALTERNATE_PLU_plu_barcode + " text,"
                + DatabaseVariables.ALTERNATE_PLU_avg_cost + " text,"
                + DatabaseVariables.ALTERNATE_PLU_sp + " text,"
                + DatabaseVariables.ALTERNATE_PLU_sp_tax + " text,"
                + DatabaseVariables.ALTERNATE_PLU_mrp + " text,"
                + DatabaseVariables.ALTERNATE_PLU_taxes + " text,"
                + DatabaseVariables.ALTERNATE_PLU_notes + " text,"
                + DatabaseVariables.ALTERNATE_PLU_description + " text,"
                + DatabaseVariables.ALTERNATE_PLU_unique_id + " text,"
                + DatabaseVariables.ALTERNATE_PLU_created_timestamp + " text,"
                + DatabaseVariables.ALTERNATE_PLU_modified_timestamp + " text,"
                + DatabaseVariables.ALTERNATE_PLU_server_local + " text);";

        sqLiteDatabase.execSQL(ALTERNATE_PLU);

        String Product_Optional_Info_Details = "create table if not exists "
                + DatabaseVariables.OPTIONAL_INFO_TABLE + " ( " + DatabaseVariables.ID
                + " integer primary key autoincrement, " + DatabaseVariables.UNIQUE_ID
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.BONUS_POINTS
                + " text, " + DatabaseVariables.BARCODES + " text, " + DatabaseVariables.LOCATION + " text, "
                + DatabaseVariables.UNIT_SIZE + " text, " + DatabaseVariables.UNIT_TYPE + " text, "
                + DatabaseVariables.INVENTORY_ITEM_NO + " text, " + DatabaseVariables.COMMISSION_OPTIONAL_INFO
                + " text, " + DatabaseVariables.INVENTORY_MODIFIER_ITEM + " text, "
                + DatabaseVariables.INVENTORY_COUNT_THIS_ITEM + " text, "
                + "count_raw_material text, "
                + "count_parent_item text, "
                + DatabaseVariables.INVENTORY_BRAND_ID + " text, "
                + DatabaseVariables.INVENTORY_MRP + " text, "
                + DatabaseVariables.INVENTORY_SUB_UNIT + " text, "
                + DatabaseVariables.INVENTORY_SUB_UNIT_PORTIONS + " text, "
                + DatabaseVariables.HAS_BOTTLE_DEPOSIT + " text, " + DatabaseVariables.BOTTLE_DEPOSIT_VALUE+ " text,"
                + DatabaseVariables.PRODUCT_META_DATA + " text, "
                + DatabaseVariables.INVENTORY_LOCAL_NAMES + " text, "
                + DatabaseVariables.INVENTORY_ALLOW_BUYBACK + " text, " + DatabaseVariables.INVENTORY_PROMPT_PRICE
                + " text, " + DatabaseVariables.INVENTORY_PRINT_ON_RECEIPT + " text, "
                + DatabaseVariables.INVENTORY_FOODSTAMPABLE + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";
                sqLiteDatabase.execSQL(Product_Optional_Info_Details);

        String storeproductspricesdetails = "create table if not exists "
                + DatabaseVariables.STORE_PRODUCTS_TABLE + " ( " + DatabaseVariables.ID
                + " integer primary key autoincrement, " + DatabaseVariables.PRODUCTS_TAKEAWAY
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.PRODUCTS_STOREID
                + " text, " + DatabaseVariables.PRODUCTS_ITEM_NO + " text, " + DatabaseVariables.PRODUCTS_ITEM_NAME
                + " text, " + DatabaseVariables.PRODUCTS_PRICE + " text, " + DatabaseVariables.UNIQUE_ID + " text, " + DatabaseVariables.PRODUCTS_DELIVERY_PRICE
                + " text, " + DatabaseVariables.PRODUCTS_NOTIFY_LOWSTOCK + " text, " + DatabaseVariables.PRODUCTS_MIN_STOCKCOUNT + " text,"+DatabaseVariables.STORE_PRODUCT_DISABLE+" text, UNIQUE (unique_id) ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(storeproductspricesdetails);


        String TaxDetails = "create table if not exists " + DatabaseVariables.TAX_TABLE + " ( "
                + DatabaseVariables.ID + " integer primary key autoincrement, " + DatabaseVariables.UNIQUE_ID
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.TAX_NAME + " text, " + DatabaseVariables.TAX_OVERALL_VALUE + " text, "
                + DatabaseVariables.TAX_VALUE + " INTEGER, " + DatabaseVariables.TAX_STORE + " text, " + DatabaseVariables.TAX_ORDER_TYPE + " text DEFAULT \"store sale\",  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(TaxDetails);

        String CatTaxDetails = "create table if not exists " + DatabaseVariables.CAT_TAX_TABLE + " ( "
                + DatabaseVariables.ID + " integer primary key autoincrement, " + DatabaseVariables.UNIQUE_ID
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.CAT_TAX_NAME + " text, " + DatabaseVariables.CAT_TAX_CAT_ID + " text, "
                + DatabaseVariables.CAT_TAX_VALUE + " INTEGER, " + DatabaseVariables.CAT_TAX_STORE + " text, " + DatabaseVariables.CAT_TAX_ORDER_TYPE + " text DEFAULT \"store sale\",  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(CatTaxDetails);


        String inventory_product_images = "CREATE TABLE IF NOT EXISTS inventory_product_images( _id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL," +
                "inventory_item_no text, fullsize_image_path text," +
                "compressed_image_path text, compressed_image_path_2x text," +
                "text_color_code text, server_base_url text," +
                "button_color_code text, unique_id text," +
                "created_timestamp text,	modified_timestamp	text," +
                "server_local text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(inventory_product_images);


        String employeeDetails = "create table if not exists " + DatabaseVariables.EMPLOYEE_TABLE
                + " ( " + BaseColumns._ID
                + " integer primary key autoincrement, " + DatabaseVariables.UNIQUE_ID
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.EMPLOYEE_DEPARTMENT
                + " text, " + DatabaseVariables.EMPLOYEE_EMPLOYEE_ID + " text, "
                + DatabaseVariables.EMPLOYEE_PASSWORD + " text, " + DatabaseVariables.EMPLOYEE_DISPLAY_NAME
                + " text, " + DatabaseVariables.EMPLOYEE_CARD_SWIPE_ID + " text, "
                + DatabaseVariables.EMPLOYEE_CUSTOMER + " text, " + DatabaseVariables.EMPLOYEE_HOURLY_WAGE
                + " text, " + DatabaseVariables.EMPLOYEE_DISABLE + " text, " + DatabaseVariables.EMPLOYEE_CC_TIPS + " text, "
                + DatabaseVariables.EMPLOYEE_ADMIN_CARD + " text, " + DatabaseVariables.SECURITY_QUESTION
                + " text, " + DatabaseVariables.SECURITY_ANSWER + " text, " + DatabaseVariables.SERVER_PASSWORD
                + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(employeeDetails);

        String employeePermissionsDetails = "create table if not exists "
                + DatabaseVariables.EMP_PERMISSIONS_TABLE + " ( " + BaseColumns._ID
                + " integer primary key autoincrement, " + DatabaseVariables.UNIQUE_ID
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.EMP_INVENTORY
                + " text, " + DatabaseVariables.EMPLOYEE_EMPLOYEE_ID + " text, " + DatabaseVariables.EMP_CUSTOMERS
                + " text, " + DatabaseVariables.EMP_REPORTS + " text, " + DatabaseVariables.EMP_DISCOUNTS
                + " text, " + DatabaseVariables.EMP_SETTINGS + " text, " + DatabaseVariables.EMP_PRICE + " text, "
                + DatabaseVariables.EMP_EXIT + " text, " + DatabaseVariables.EMP_PAYOUTS + " text, " + DatabaseVariables.EMP_DELETE
                + " text, " + DatabaseVariables.EMP_VOID + " text, " + DatabaseVariables.EMP_TRANSACTIONS
                + " text, " + DatabaseVariables.EMP_HOLDPRINTS + " text, " + DatabaseVariables.EMP_CREDIT
                + " text, " + DatabaseVariables.EMP_ENDCASH + " text, " + DatabaseVariables.EMP_STOCK + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(employeePermissionsDetails);

        String AdvancedDetails = "create table if not exists "
                + DatabaseVariables.ADVANCED_TABLE + " ( " + DatabaseVariables.ID
                + " integer primary key autoincrement, " + DatabaseVariables.UNIQUE_ID
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.ADVANCED_EMPLOYEE_ID
                + " text, " + DatabaseVariables.ADVANCED_PERMISSIONS + " text, " + DatabaseVariables.ADVANCED_ASSIGNED_EMP
                + " text, " + DatabaseVariables.ADVANCED_ASSIGNED_BY + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(AdvancedDetails);

        String emp_store = "create table if not exists " + DatabaseVariables.EMP_STORE_TABLE
                + " ( " + BaseColumns._ID
                + " integer primary key autoincrement, " + DatabaseVariables.EMPLOYEE_EMPLOYEE_ID
                + " text, " + DatabaseVariables.EMP_STORE_ID + " text, " + DatabaseVariables.EMP_STORE_NAME
                + " text, " + DatabaseVariables.UNIQUE_ID + " text, " + DatabaseVariables.CREATED_DATE
                + " text, " + DatabaseVariables.MODIFIED_DATE + " text, " + DatabaseVariables.MODIFIED_IN
                + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(emp_store);



        String STORE_CATEGORY = "create table if not exists " + DatabaseVariables.STORE_CATEGORY_TABLE
                + " ( " + BaseColumns._ID
                + " integer primary key autoincrement, " + DatabaseVariables.STORE_CATEGORY_STOREID
                + " text, " + DatabaseVariables.STORE_CATEGORY_ID + " text, " + DatabaseVariables.STORE_CATEGORYID
                + " text, " + DatabaseVariables.UNIQUE_ID + " text, " + DatabaseVariables.CREATED_DATE
                + " text, " + DatabaseVariables.MODIFIED_DATE + " text, " + DatabaseVariables.MODIFIED_IN
                + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(STORE_CATEGORY);


        String StoreDetails = "create table if not exists " + DatabaseVariables.STORE_TABLE
                + " ( " + BaseColumns._ID
                + " integer primary key autoincrement, " + DatabaseVariables.STORE_NAME
                + " text, " + DatabaseVariables.STORE_ID + " text, " + DatabaseVariables.STORE_PARENT_ID + " text, " + DatabaseVariables.STORE_EMAIL + " text, "
                + DatabaseVariables.STORE_NUMBER + " text, " + DatabaseVariables.STORE_STREET + " text, "
                + DatabaseVariables.STORE_CITY + " text, " + DatabaseVariables.STORE_POSTAL + " text, "
                + DatabaseVariables.STORE_COUNTRY + " text, " + DatabaseVariables.STORE_STATE + " text, "
                + DatabaseVariables.STORE_DEFAULTTAX + " text, " + DatabaseVariables.STORE_DISCOUNT + " text, "
                + DatabaseVariables.STORE_CURRENCY + " text, " + DatabaseVariables.UNIQUE_ID + " text, "
                + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE + " text, "
                + DatabaseVariables.MODIFIED_IN + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(StoreDetails);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+PREFERENCES_TABLE);
    }
    public boolean insertData(ContentValues contentValues, String tableName)
    {
//        SQLiteDatabase db= this.getWritableDatabase();

        long result = db.insert(tableName,null,contentValues);
        return  ( (result == -1) ? false : true);
    }

    public void executeExecSQL(String query) {
        SQLiteDatabase database= db;
        try {
//            database= this.getWritableDatabase();

//                    database.beginTransaction();
            database.setMaximumSize(1073741824);
//                    query = addSlashes(query);
            database.execSQL(query);
//                    database.setTransactionSuccessful();
//                    database.endTransaction();

        }catch (Exception exp){
            exp.printStackTrace();
        }finally {
            /*if(database!=null && database.isOpen())
            { database.close();}*/
        }
    }

    public boolean executeUpdate(String table, ContentValues contentValues, String where, String[] args) {

        long result = db.update(table,contentValues,where,args);
        return  ( (result == -1) ? false : true);



    }


    public boolean executeDelete(String table ,String where, String[] args) {

        long result = db.delete(table,where,args) ;// db.update(table,contentValues,where,args);
        return  ( (result == -1) ? false : true);



    }
    public ArrayList<JSONObject> executeRawqueryJSON(String query) {
        Cursor mCursor=null;
        JSONArray resultJSON=new JSONArray();
        boolean enteredloop = false;
        ArrayList<JSONObject> mArrayList = new ArrayList<JSONObject>();
        SQLiteDatabase database= null;

//            do {
        try {
            database= this.getWritableDatabase();
//                    database.beginTransaction();
            mCursor = database.rawQuery(query, null);
            int columnCount = 0;
            if(mCursor!=null) {
                mCursor.moveToFirst();
                while (mCursor.isAfterLast() == false) {
                    int totalColumn = mCursor.getColumnCount();
                    JSONObject rowObject = new JSONObject();
                    for (int i = 0; i < totalColumn; i++) {
                        if (mCursor.getColumnName(i) != null) {
                            try {
                                rowObject.put(mCursor.getColumnName(i),
                                        mCursor.getString(i));
                            } catch (Exception e) {
                                Log.v(TAG, e.getMessage());
                            }
                        }
                    }
                    mArrayList.add(columnCount,rowObject);
                    columnCount++;
                    resultJSON.put(rowObject);
                    mCursor.moveToNext();
                }
            }

//                    database.setTransactionSuccessful();
//                    database.endTransaction();

        } catch (Exception e) {
            e.printStackTrace();



        } finally {
            if(mCursor!=null) {
                mCursor.close();
            }
                /*if(database!=null && database.isOpen())
                { database.close();}*/
        }

        // } while (exceptionHandled == false && exceptionCount <= 100);
        return mArrayList;

    }

}
