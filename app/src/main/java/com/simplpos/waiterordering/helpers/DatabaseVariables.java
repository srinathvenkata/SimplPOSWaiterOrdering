package com.simplpos.waiterordering.helpers;

import static com.simplpos.waiterordering.SplashScreen.dbHelper;

import android.content.ContentValues;
import android.provider.BaseColumns;
import android.util.Log;

import com.simplpos.waiterordering.MainActivity;
import com.simplpos.waiterordering.SplashScreen;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DatabaseVariables {


    public static final String ITEMLIST_PRINT = "itemslistprint";


    public static Boolean PS_VALUE = false;

    public static String PS_TEXT = "pos_screen";
    public static String PP_TEXT = "print_preferences";
    public static String PL_TEXT = "print_localname";

    public String currencyTypehtml="&#8377;";

    MySQLJDBC mySqlObj = null;
    MySQLJDBC mySqlCrmObj = null;

    public static final String CATEGORY_TABLE = "category_details";
    public static final String CategoryId = "category_id";
    public static final String CategoryDesp = "category_description";

    public static final String DEPARTMENT_TABLE = "department_details";
    public static final String DepartmentID = "department_id";
    public static final String DepartmentDesp = "department_description";
    public static final String CategoryForDepartment = "category_id";
    public static final String FoodstampableForDept = "department_foodstampable";
    public static final String TaxValForDept = "department_totaltax";

    public static final String ADVANCED_TABLE = "employee_advanced_permissions";
    public static final String ADVANCED_EMPLOYEE_ID = "employee_employee_id";
    public static final String ADVANCED_PERMISSIONS = "emp_advanced_permissions";
    public static final String ADVANCED_ASSIGNED_EMP = "assigned_employees";
    public static final String ADVANCED_ASSIGNED_BY = "permission_given_by";

    public static final String STOREWISE_STOCK_TABLE = "storewise_stock";
    public static final String STOREWISE_STOREID = "store_id";
    public static final String STOREWISE_ITEMNO = "item_no";
    public static final String STOREWISE_ITEMNAME = "item_name";
    public static final String STOREWISE_STOCKCOUNT = "stockcount";
    public static final String STOREWISE_EDITBY = "editby";
    public static final String STOREID_ITEMNO = "storeid_itemno";
    public static final String STOREWISE_RACK_NAME = "rack_name";

    public static final String CATEGORY_PRINTER_COMMANDS = "category_printer_commands";
    public static final String PrinterForCategory = "category_printer";
    public static final String TimeForCategoryPrint = "Category_print_time";

    public static final String DEPARTMENT_PRINTER_COMMANDS = "department_printer_commands";
    public static final String PrinterForDept = "department_printer";
    public static final String TimeForDeptPrint = "department_print_time";

    public static final String COMMANDS_PRINTER_TABLE = "printer_commands_table";
    public static final String COMMANDS_PRINTER_NAME = "printer_name";
    public static final String COMMANDS_ITEM_NAME = "item_time";
    public static final String COMMANDS_ITEM_ID = "item_id";
    public static final String COMMANDS_TIME = "print_time";
    public static final String COMMANDS_MESSAGE = "message";
    public static final String COMMANDS_HOLDID = "hold_id";
    public static final String COMMANDS_PRINT_TEXT = "print_text";
    public static final String COMMANDS_INVOICE_ID = "invoice_id";

    public static final String OFFERS_TABLE = "offers";
    public static final String OFFERS_TYPE = "offer_type";
    public static final String OFFERS_START_DATE = "starting_date";
    public static final String OFFERS_END_DATE = "ending_date";
    public static final String OFFERS_CLEARANCE = "clearance_stock";
    public static final String OFFERS_FROM_TIME = "offer_fromtime";
    public static final String OFFERS_TO_TIME = "offer_totime";
    public static final String OFFERS_VALIDITY_DAYS = "offervalidity_days";
    public static final String OFFERS_DEAL_TITLE = "deal_title";

    public static final String OFFER_ITEM_TABLE = "offer_items";
    public static final String OFF_ID = "offer_id";
    public static final String OFF_ITEM_ID = "item_id";
    public static final String OFF_PRICE = "offer_price";
    public static final String OFF_DISCOUNT = "discount_percentage";
    public static final String OFF_CATEGORY = "category";
    public static final String OFF_DEPARTMENT = "department";
    public static final String OFF_FREE_ITEMS = "free_items";
    public static final String OFF_DIS_SYMBOL = "discount_symbol";
    public static final String OFF_MODIFY = "mod_items";


    public static final String UNIQUE_ID = "unique_id";
    public static final String CREATED_DATE = "created_timestamp";
    public static final String MODIFIED_DATE = "modified_timestamp";
    public static final String MODIFIED_IN = "server_local";

    public static final String STOCK_MODIFICATION_TABLE = "stock_modification_history";
    public static final String MDF_ITEM_NO = "item_no";
    public static final String MDF_ITEM_NAME = "item_name";
    public static final String MDF_STOCK_COUNT = "migratestock_count";
    public static final String MDF_EMP_ID = "employee_id";
    public static final String MDF_SENDING_STORE = "sending_store";
    public static final String MDF_RECIVING_STORE = "receiving_store";
    public static final String MDF_NOTES = "notes";
    public static final String MDF_RECIVING_STOCK = "receivingstore_stockcount";
    public static final String MDF_SENDING_STOCK = "sendingstore_stockcount";

    public static final String MERCHANT_TABLE = "merchant_info_table";
    public static final String MERCHANT_NAME = "merchant_name";
    public static final String MERCHANT_ADDRESS = "merchant_address";
    public static final String MERCHANT_ADDRESS2 = "merchant_address2";
    public static final String MERCHANT_PHONE = "merchant_phone";
    public static final String MERCHANT_ZIP = "merchant_zipcode";
    public static final String BUSINESS_TYPE = "business_type";

    public static final String MERCURY_PAY_TABLE = "mercury_pay_table";
    public static final String MERCURY_PRIMARY_URL = "mercury_primary_url";
    public static final String MERCURY_SECONDARY_URL = "mercury_secondary_url";
    public static final String MERCURY_MERCHANT_ID = "mercury_merchant_id";
    public static final String MERCURY_PASSWORD = "mercury_password";

    public static final String ADMIN_TABLE = "admin_details";
    public static final String USERID = "userid";
    public static final String PASSWORD = "password";
    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    public static final String PHONENUMBER = "phonenumber";
    public static final String EMAIL = "email";
    public static final String ADDRESS = "address";
    public static final String SERVER_PASSWORD = "server_password";
    public static final String SECURITY_QUESTION = "security_question";
    public static final String SECURITY_ANSWER = "security_answer";

    public static final String TAX_TABLE = "taxes";
    public static final String TAX_NAME = "taxes_name";
    public static final String TAX_VALUE = "taxes_value";
    public static final String TAX_OVERALL_VALUE = "overalltaxvalue";
    public static final String TAX_ORDER_TYPE = "order_type";
    public static final String TAX_STORE = "storeid";

    public static final String TAX_AMOUNT_TABLE = "taxed_amounts";
    public static final String TAX_AMOUNT_NAME = "tax_name";
    public static final String TAX_AMOUNT_VALUE = "tax_value";
    public static final String TAX_AMOUNT_INVOICE = "invoice_id";

    public static final String CATEGORY_TAX_AMOUNT_TABLE = "category_taxed_amounts";

    public static final String CAT_TAX_TABLE = "category_taxes";
    public static final String CAT_TAX_CAT_ID = "category_id";
    public static final String CAT_TAX_NAME = "taxes_name";
    public static final String CAT_TAX_VALUE = "taxes_value";
    public static final String CAT_TAX_STORE = "storeid";
    public static final String CAT_TAX_ORDER_TYPE = "order_type";

    public static final String VENDOR_TABLE = "vendor";
    public static final String VENDOR_NO = "vendor_no";
    public static final String VENDOR_FIRST_NAME = "vendor_first_name";
    public static final String VENDOR_LAST_NAME = "vendor_last_name";
    public static final String VENDOR_COMPANY_NAME = "vendor_company_name";
    public static final String VENDOR_TERMS = "vendor_terms";
    public static final String VENDOR_TAX_ID = "vendor_tax_id";
    public static final String VENDOR_FAX_NUMBER = "vendor_fax_number";
    public static final String VENDOR_TELEPHONE_NUMBER = "vendor_telephone_number";
    public static final String VENDOR_STREET_ADDRESS = "vendor_street_address";
    public static final String VENDOR_EXTENDED_ADDRESS = "vendor_extended_address";
    public static final String VENDOR_CITY = "vendor_city";
    public static final String VENDOR_STATE = "vendor_state";
    public static final String VENDOR_ZIP_CODE = "vendor_zip_code";
    public static final String VENDOR_COUNTRY = "vendor_country";
    public static final String VENDOR_FLAT_RENT_RATE = "vendor_flat_rent_rate";
    public static final String VENDOR_MIN_ORDER = "vendor_min_order";
    public static final String VENDOR_PO_DELIVERY_METHOD = "vendor_po_delivery_method";
    public static final String VENDOR_COMMISSION_PERCENT = "vendor_commission_percent";
    public static final String VENDOR_BILLABLE_DEPARTMENT = "vendor_billable_department";
    public static final String VENDOR_SOCIAL_SECURITY_NO = "vendor_social_security_no";
    public static final String VENDOR_EMAIL = "vendor_email";
    public static final String VENDOR_WEBSITE = "vendor_website";

    public static final String CREDITCARD_TABLE = "first_data_card";
    public static final String CREDIT_MERCHANT = "merchant_number";
    public static final String CREDIT_TERMINAL = "terminal_number";
    public static final String CREDIT_PRIMARY_URL = "primary_url";
    public static final String CREDIT_SECONDARY_URL = "secondary_url";
    public static final String CREDIT_USERNAME = "username";
    public static final String CREDIT_DEBITCARD = "password";
    public static final String CREDIT_REQUIRE_CVV2 = "clientno";
    public static final String CREDIT_TIME_OUT = "timeout_seconds";
    public static final String CREDIT_PAYMENT_NAME = "payment_processor_name";

    public static final String PaymentProcessorPreferences = "PaymentProcessorPreferences";
    public static final String PaymentProcessorName = "PaymentProcessorName";
    public static final String PaymentProcessSelectvalue = "PaymentProcessSelectvalue";

    public static final String CHECKED_VALUE = "check_value";

    public static final String PAYMENT_TABLE = "payments";
    public static final String PAYMENT_NAME = "payment_name";
    public static final String PAYMENT_VALUE = "taxenable_value";

    public static final String PENDING_QUERIES_TABLE = "pending_data";
    public static final String QUERY_TYPE = "query_type";
    public static final String PENDING_USER_ID = "user_id";
    public static final String PAGE_URL = "page_url";
    public static final String PARAMETERS = "parameters";
    public static final String TABLE_NAME_PENDING = "table_name";
    public static final String CURRENT_TIME_PENDING = "current_time";
    public static final String UNDO_STEP = "undo_step";

    public static final String MISCELLANEOUS_TABLE = "miscellaneous";
    public static final String MISCEL_STORE = "selected_store";
    public static final String MISCEL_UPDATE_LOCAL = "local_time";
    public static final String MISCEL_PAGEURL = "server_url";
    public static final String MISCEL_SERVER_UPDATE_LOCAL = "last_server_update_time";
    public static final String MISCEL_LOCAL_SERVER = "local_server_exists";
    public static final String MISCEL_LOGGEDIN_EMPLOYEE = "loggedin_employee";

    public static final String DEVICE_TABLE = "device_license";
    public static final String DEVICE_LICENSE_NO = "license_number";
    public static final String DEVICE_LICENSE_STATUS = "license_status";
    public static final String DEVICE_EXPTIME_LOCAL = "licenseexptime_local";
    public static final String DEVICE_EXPTIME_SERVER = "licenseexptime_server";
    public static final String DEVICE_TRIAL_STATUS = "trial_status";
    public static final String DEVICE_TRIAL_EXPTIME = "trial_period_exptime";
    public static final String DEVICE_TRIAL_DURATION = "trial_period_duration";
    public static final String DEVICE_COMPANY_ID = "company_id";

    public static final String INVENTORY_TABLE = "inventorytable";
    public static final String INVENTORY_DEPARTMENT = "inventary_department";
    public static final String INVENTORY_ITEM_NO = "inventory_item_no";
    public static final String INVENTORY_ITEM_NAME = "inventary_item_name";
    public static final String INVENTORY_SECOND_DESCRIPTION = "inventary_second_description";
    public static final String INVENTORY_AVG_COST = "inventary_avg_cost";
    public static final String INVENTORY_PRICE_TAX = "inventary_price_tax";
    public static final String INVENTORY_PRICE_CHANGE = "inventary_price_change";
    public static final String INVENTORY_IN_STOCK = "inventary_in_stock";
    public static final String INVENTORY_QUANTITY = "inventary_quantity";
    public static final String INVENTORY_TAXONE = "inventary_taxone";
    public static final String INVENTORY_VENDOR = "inventary_vendor";
    public static final String INVENTORY_TOTAL_TAX = "inventory_total_tax";
    public static final String INVENTORY_NOTES = "inventary_notes";
    public static final String INVENTORY_CATEGORY = "category_id";
    public static final String INVENTORY_ITEMBARCODE = "Item_barcode";
    public static final String INVENTORY_TAKEAWAY = "takeaway_price";
    public static final String INVENTORY_TAKEAWAY_TAX = "takeaway_pricewithtax";
    public static final String INVENTORY_LOCAL_NAME = "local_name";

    public static final String STORE_PRODUCTS_TABLE = "store_products_prices";
    public static final String PRODUCTS_STOREID = "storeid";
    public static final String PRODUCTS_TAKEAWAY = "storetakeaway_price";
    public static final String PRODUCTS_ITEM_NO = "item_no";
    public static final String PRODUCTS_ITEM_NAME = "item_name";
    public static final String PRODUCTS_PRICE = "price";
    public static final String PRODUCTS_DELIVERY_PRICE = "home_delivery_price";
    public static final String PRODUCTS_NOTIFY_LOWSTOCK = "notify_low_stock";
    public static final String PRODUCTS_MIN_STOCKCOUNT = "min_stock_count";
    public static final String STORE_PRODUCT_DISABLE = "disable";

    public static final String GALLERY_TABLE = "product_gallery";
    public static final String GALLERY_ITEM_NO = "inventory_item_no";
    public static final String GALLERY_FULL_IMAGE = "fullsize_image_path";
    public static final String GALLERY_COMPRESS_IMAGE = "compressed_image_path";
    public static final String GALLERY_COMPRESS_IMAGE_2X = "compressed_image_path_2x";
    public static final String GALLERY_SEUENCE_NO = "sequence_no";
    public static final String GALLERY_BASE_URL = "server_base_url";

    public static final String VIDEO_TABLE = "product_videos";
    public static final String VIDEO_ITEM_NO = "item_no";
    public static final String VIDEO_URL = "video_url";
    public static final String VIDEO_EMBED_URL = "video_embed_url";
    public static final String VIDEO_PATH = "video_path";
    public static final String VIDEO_BASE_URL = "server_base_url";

    public static final String PRINTER_TABLE = "printers";
    public static final String PRINTER_TEXT = "header_characters";
    public static final String PRINTER_FONT = "fontsize";
    public static final String PRINTER_NAME = "printname";
    public static final String PRINTER_ID = "print_id";
    public static final String PRINTER_IP = "ipaddress";
    public static final String PRINTER_TYPE = "printer_type";
    public static final String PAPER_WIDTH = "paper_width";
    public static final String FOOTER_TEXT = "footer_text";
    public static final String FOOTER_TEXT_RECEIPT = "footer_characters";

    public static final String CUSTOMERPOS_TABLE = "customerpos_table";
    public static final String CUSTOMERPOS_STORE = "customerpos_store";
    public static final String CUSTOMERPOS_SELECTTABLE = "customerpos_selecttable";
    public static final String CUSTOMERPOS_ORDERTYPE = "customerpos_ordertype";
    public static final String CUSTOMERPOS_USERID = "customerpos_userid";
    public static final String CUSTOMERPOS_PASSWORD = "customerpos_password";
    public static final String CUSTOMERPOS_DECIMAL = "customerpos_decimal";
    public static final String CUSTOMERPOS_SAVEORDER = "customerpos_saveorder";
    public static final String CUSTOMERPOS_WORKON = "customerpos_workon";
    public static final String CUSTOMERPOS_BEFOREORDER = "customerpos_beforeorder";
    public static final String CUSTOMERPOS_MOBILE = "customerpos_mobile";
    public static final String CUSTOMERPOS_PAYMENTMODE = "customerpos_paymentmode";
    public static final String CUSTOMERPOS_PRINTER = "customerpos_printer";

    public static final String STORE_TABLE = "store_details";
    public static final String STORE_NAME = "store_name";
    public static final String STORE_ID = "store_id";
    public static final String STORE_PARENT_ID = "parent_store_id";
    public static final String STORE_EMAIL = "email";
    public static final String STORE_NUMBER = "phonenumber";
    public static final String STORE_STREET = "street";
    public static final String STORE_CITY = "city";
    public static final String STORE_POSTAL = "postal";
    public static final String STORE_COUNTRY = "country";
    public static final String STORE_STATE = "state";
    public static final String STORE_DEFAULTTAX = "defaulttax";
    public static final String STORE_DISCOUNT = "discount";
    public static final String STORE_CURRENCY = "currency";

    public static final String INVOICE_TOTAL_TABLE = "invoice_total_table";
    public static final String INVOICE_ID = "invoice_id";
    public static final String INVOICE_STORE_ID = "store_id";
    public static final String INVOICE_TOTAL_AMT = "total_amt";
    public static final String INVOICE_STATUS = "status";
    public static final String HOLD_STATUS = "hold_status";
    public static final String ORDER_TYPE = "order_type";
    public static final String INVOICE_DELIVERY_STATUS = "delivery_status";
    public static final String INVOICE_DELIVERY_DATE = "order_delivery_date";
    public static final String INVOICE_BOOKED_TABLE = "booked_table";
    public static final String INVOICE_BILL_TS = "bill_ts";


    public static final String INVOICE_HOLD_ID = "holdid";
    public static final String INVOICE_PAYMENT_TYPE = "payment_type";
    public static final String INVOICE_TOTAL_AVG = "total_avgcost";
    public static final String INVOICE_PROFIT = "total_profitt";
    public static final String INVOICE_EMPLOYEE = "employee";
    public static final String INVOICE_CHEQUE_NO = "cheque_no";
    public static final String INVOICE_CUSTOMER = "customer";
    public static final String ID = "_id";

    public static final String INDENT_TOTAL_TABLE = "indent_request";
    public static final String IND_TOTAL_AMOUNT = "total_amt";
    public static final String IND_HOLDID = "holdid";
    public static final String IND_AVGCOST = "total_avgcost";
    public static final String IND_BILL = "bill_ts";
    public static final String IND_DELIVERY_DATE = "order_delivery_date";
    public static final String IND_DELIVERY_STATUS = "delivery_status";

    public static final String INDENT_DELIVERY_TABLE = "indent_delivery";

    public static final String INDENT_ACCEPT_TABLE = "indent_accept";
    public static final String INDENT_ACCEPT_STOREID = "accept_from_store_id";

    public static final String INDENT_ITEMS_TABLE = "indent_req_items";
    public static final String INDENT_QUANTITY = "quantity";
    public static final String INDENT_EXIST_QUANTITY = "existing_qty";
    public static final String INDENT_REQUEST_ID = "indent_req_id";
    public static final String INDENT_STOREID = "store_id";
    public static final String INDENT_EMPLOYEE = "employee";
    public static final String INDENT_STATUS = "status";
    public static final String INDENT_PROCESS_STATUS = "process_status";
    public static final String INDENT_HOLD_STATUS = "hold_status";
    public static final String INDENT_NOTES = "notes";
    public static final String INDENT_ITEM_ID = "item_id";
    public static final String INDENT_ITEM_NAME = "item_name";
    public static final String INDENT_YOUR_COST = "price_you_charge";
    public static final String INDENT_AVG_COST = "avg_cost";

    public static final String INDENT_DELIVERY_ITEM_TABLE = "indent_delivery_items";
    public static final String INDENT_REQ_QTY = "requested_quantity";
    public static final String INDENT_ISSUE_ID = "delivery_issue_id";

    public static final String INDENT_ACCEPT_ITEM_TABLE = "indent_accept_items";
    public static final String INDENT_REL_QTY = "released_quantity";
    public static final String INDENT_STOCK_ID = "accept_stock_id";
    public static final String INDENT_RET_QTY = "returned_quantity";
    public static final String INDENT_WASTAGE = "wastage";
    public static final String INDENT_WASTAGE_NOTES = "wastage_notes";

    public static final String INVOICE_ITEMS_TABLE = "invoice_items_table";
    public static final String INVOICE_ITEM_ID = "item_id";
    public static final String INVOICE_DISCOUNT = "discount";
    public static final String INVOICE_TAX = "total_tax";
    public static final String INVOICE_ITEM_NAME = "item_name";
    public static final String INVOICE_DISCRIPTION = "item_desscription";
    public static final String INVOICE_YOUR_COST = "price_you_charge";
    public static final String INVOICE_AVG_COST = "avg_cost";
    public static final String INVOICE_QUANTITY = "item_quantity";
    public static final String INVOICE_DEPARTMETNT = "in_department";
    public static final String INVOICE_VENDOR = "in_vendor";
    public static final String INVOICE_APPLIEDOFFER_ID = "appliedoffer_id";
    public static final String INVOICE_ITEMS_NOTES = "notes";

    //Log.v("uu", "gggv");
    public static final String EMPLOYEE_TABLE = "employee_table";
    public static final String EMPLOYEE_DEPARTMENT = "employee_department";
    public static final String EMPLOYEE_EMPLOYEE_ID = "employee_employee_id";
    public static final String EMPLOYEE_PASSWORD = "employee_password";
    public static final String EMPLOYEE_DISPLAY_NAME = "employee_display_name";
    public static final String EMPLOYEE_CARD_SWIPE_ID = "employee_card_swipeid";
    public static final String EMPLOYEE_CUSTOMER = "employee_customer";
    public static final String EMPLOYEE_HOURLY_WAGE = "employee_hourly_wage";
    public static final String EMPLOYEE_DISABLE = "employee_disable";
    public static final String EMPLOYEE_CC_TIPS = "employee_cc_tips";
    public static final String EMPLOYEE_ADMIN_CARD = "employee_admin_card";

    public static final String EMP_SERVICE_TABLE = "emp_service_history";
    public static final String EMP_SERVICE_EMPLOYEE_ID = "service_employee_id";
    public static final String EMP_SERVICE_UPDATED_BY_EMPLOYEE_ID = "updated_by_employee";


    public static final String CUSTOMER_EXTENDED_INFO_TABLE = "customer_extended_info_table";
    public static final String CREDIT_CARD_TYPE = "credit_card_type";
    public static final String CREDIT_CARD_NUM = "credit_card_num";
    public static final String EXPIRATION = "expiration";
    public static final String DRIVING_LICENSE = "driving_license";
    public static final String EXP_DATE = "exp_date";
    public static final String CUSTOMER_MOBILE = "customer_mobile";
    public static final String CUSTOMER_FAX = "customer_fax";
    public static final String CUSTOMER_TIN = "tin";

    public static final String CUSTOMER_SHIPPING_TABLE = "customer_shipping_table";
    public static final String SHIPPING_FIRST_NAME = "shipping_first_name";
    public static final String SHIPPING_LAST_NAME = "shipping_last_name";
    public static final String SHIPPING_COMPANY_NAME = "shipping_company_name";
    public static final String SHIPPING_PHONE = "shipping_phone";
    public static final String SHIPPING_STREET = "shipping_street";
    public static final String SHIPPING_EXTENDED = "shipping_extended";
    public static final String SHIPPING_CITY = "shipping_city";
    public static final String SHIPPING_STATE = "shipping_state";
    public static final String SHIPPING_COUNTRY = "shipping_country";
    public static final String SHIPPING_ZIPCODE = "shipping_zipcode";

    public static final String CUSTOMER_STORES_TABLE = "customer_stores_table";
    public static final String STORE_ID_CUSTOMER = "store_id";

    public static final String EMP_PERSONAL_TABLE = "employee_personal";
    public static final String EMP_NAME = "emp_name";
    public static final String EMP_ID = "emp_id";
    public static final String EMP_EMAIL = "emp_email";
    public static final String EMP_PHONE = "emp_phone";
    public static final String EMP_BIRTH = "emp_birth";
    public static final String EMP_ADDRESS = "emp_address";
    public static final String EMP_CITY = "emp_city";
    public static final String EMP_COUNTRY = "emp_country";
    public static final String EMP_STATE = "emp_state";
    public static final String EMP_POSTAL = "emp_postal";

    public static final String EMP_STORE_TABLE = "employee_store";
    public static final String EMP_STORE_NAME = "emp_store_name";
    public static final String EMP_STORE_ID = "emp_store_id";

    public static final String SPILT_LOCAL_TABLE = "spilt_local_table";
    public static final String SPILT_LIST = "spilt_list";
    public static final String SPILT_HOLDID = "spilt_holdid";
    public static final String SPILT_HOLDID_UNIQE = "spilt_hold_uniqe";

    public static final String EMP_PAYROLL_TABLE = "employee_payroll";
    public static final String EMP_FEDERAL = "federal";
    public static final String EMP_AMOUNT = "amount";
    public static final String EMP_STATEA = "statea";
    public static final String EMP_STATEAMOUNT = "stateAmount";
    public static final String EMP_CREDITS = "credits";
    public static final String EMP_FILLINGSTATUS = "filingstatus";
    public static final String EMP_EXEMPT = "exempt";
    public static final String EMP_EXCLUDECHECK = "excludeCheck";

    public static final String EMP_PERMISSIONS_TABLE = "employee_permissions";
    public static final String EMP_INVENTORY = "emp_inventory";
    public static final String EMP_CUSTOMERS = "emp_customers";
    public static final String EMP_REPORTS = "emp_reports";
    public static final String EMP_DISCOUNTS = "emp_discounts";
    public static final String EMP_SETTINGS = "emp_settings";
    public static final String EMP_PRICE = "emp_price_hanges";
    public static final String EMP_EXIT = "emp_allow_exit";
    public static final String EMP_PAYOUTS = "emp_vendor_payouts";
    public static final String EMP_DELETE = "emp_delete_items";
    public static final String EMP_VOID = "emp_void_invoices";
    public static final String EMP_TRANSACTIONS = "emp_transactions";
    public static final String EMP_HOLDPRINTS = "emp_holdprint";
    public static final String EMP_CREDIT = "emp_creditcards";
    public static final String EMP_ENDCASH = "emp_endcash";
    public static final String EMP_STOCK = "emp_stock";

    public static final String PRODUCT_PRINTERS_TABLE = "product_printer_table";
    public static final String PRINTER_VALUE = "printer_value";

    public static final String ORDERING_INFO_TABLE = "ordering_info_table";
    public static final String REORDER_QUANTITY = "reorder_quantity";
    public static final String REORDER_LEVEL = "reorder_level";
    public static final String REORDER_COST = "reorder_cost";
    public static final String VENDERPART_NO = "venderpart_no";
    public static final String COST_PER = "cost_per";
    public static final String CASE_COST = "case_cost";
    public static final String NO_IN_CASE = "no_in_case";
    public static final String TRANSFER_COST_MARKUP = "transfer_cost_markup";
    public static final String ENABLE_MARKUP = "endble_markup";
    public static final String PREFERRED = "preferred";

    public static final String CUSTOMER_TABLE = "customer_table";
    public static final String CUSTOMER_NO = "customer_no";
    public static final String CUSTOMER_FIRST_NAME = "customer_first_name";
    public static final String CUSTOMER_LAST_NAME = "customer_last_name";
    public static final String CUSTOMER_EMAIL = "customer_email";
    public static final String CUSTOMER_NOTES = "customer_notes";


    public static final String CUSTOMER_META_TABLE = "customer_meta";
    public static final String CUSTOMER_ID = "customer_id";
    public static final String CUSTOMER_ATTRIBUTE = "attribute";
    public static final String CUSTOMER_ATTR_VALUE = "value";

    // unique_id VARCHAR(30) NOT NULL, created_timestamp DATETIME DEFAULT NULL, modified_timestamp	DATETIME DEFAULT NULL, server_local VARCHAR(15) DEFAULT 'server', PRIMARY KEY(_id)) ENGINE=InnoDB DEFAULT CHARSET=UTF8
    // 	VARCHAR(30) NOT NULL, unique_id VARCHAR(30) NOT NULL, created_timestamp DATETIME DEFAULT NULL, modified_timestamp	DATETIME DEFAULT NULL, server_local VARCHAR(15) DEFAULT 'server', PRIMARY KEY(_id)) ENGINE=InnoDB DEFAULT CHARSET=UTF8
    public static final String CUSTOMER_ACCOUNT_HISTORY_TABLE = "customer_account_history";
    public static final String CUSTOMER_ACCOUNT_INVOICE_ID = "invoice_id";
    public static final String CUSTOMER_ACCOUNT_TRANSACTION_TYPE = "type";
    public static final String CUSTOMER_ACCOUNT_OPENING_BALANCE = "opening_bal";
    public static final String CUSTOMER_ACCOUNT_AMOUNT = "amount";
    public static final String CUSTOMER_ACCOUNT_CLOSING_BALANCE = "closing_bal";
    public static final String CUSTOMER_ACCOUNT_PAYMENT_STATUS = "payment_status";
    public static final String CUSTOMER_ACCOUNT_REF_NUM = "reference_num";
    public static final String CUSTOMER_ACCOUNT_PAYMENT_MODE = "payment_mode";
    public static final String CUSTOMER_ACCOUNT_CUSTOMER_ID = "customer_no";
    public static final String CUSTOMER_ACCOUNT_EMPLOYEE = "employee";
    public static final String CUSTOMER_ACCOUNT_UNIQUE_ID = "unique_id";

    public static final String ALTERNATE_SKU_TABLE = "alternate_sku";
    public static final String ALTERNATE_SKU_VALUE = "alternate_sku_value";

    public static final String OPTIONAL_INFO_TABLE = "optional_info_table";
    public static final String INVENTORY_MODIFIER_ITEM = "inventary_modifier_item";
    public static final String INVENTORY_COUNT_THIS_ITEM = "inventary_count_this_item";
    public static final String INVENTORY_ALLOW_BUYBACK = "inventary_prompt_quantity";
    public static final String INVENTORY_PROMPT_PRICE = "inventary_prompt_price";
    public static final String INVENTORY_PRINT_ON_RECEIPT = "inventary_print_on_receipt";
    public static final String INVENTORY_COUNT_PARENT_ITEM = "count_parent_item"; // new
    public static final String INVENTORY_COUNT_RAW_MATERIAL = "count_raw_material"; // new

    public static final String INVENTORY_FOODSTAMPABLE = "inventary_foodstampable";
    public static final String INVENTORY_LOCAL_NAMES = "local_names"; // new
    public static final String INVENTORY_MRP = "mrp"; // new
    public static final String INVENTORY_SUB_UNIT = "sub_unit"; // new
    public static final String INVENTORY_SUB_UNIT_PORTIONS = "sub_unit_portions"; // new
    public static final String INVENTORY_BRAND_ID = "brand_id"; // new
    public static final String BONUS_POINTS = "bonus_points";
    public static final String BARCODES = "barcodes";
    public static final String LOCATION = "location";
    public static final String UNIT_SIZE = "unit_size";
    public static final String UNIT_TYPE = "unit_type";
    public static final String HAS_BOTTLE_DEPOSIT = "has_bottle_deposit";
    public static final String BOTTLE_DEPOSIT_VALUE = "bottle_deposit_value";
    public static final String PRODUCT_META_DATA = "product_meta_data";

    public static final String COMMISSION_OPTIONAL_INFO = "commission_optional_info";

    // CREATE TABLE IF NOT EXISTS optional_info_table (

    public static final String PREFERENCES = "preferences";
    public static final String PRE_ATTRIBUTE = "attribute";
    public static final String PRE_VALUE = "value";


    public static final String CARD_PROCESSING_TERMINAL_TABLE = "card_processing_terminal";
    public static final String CARD_PROCESSING_MANUFACTURER = "terminal_manufacturer";
    public static final String CARD_PROCESSING_CONNECTION_TYPE = "terminal_connection_type";
    public static final String CARD_PROCESSING_CONNECTION_ADDRESS = "terminal_connection_address";
    public static final String CARD_PROCESSING_CONNECTION_PORT_NUMBER = "terminal_connection_port_number";
    public static final String CARD_PROCESSING_ENABLE_TIPS = "terminal_connection_enable_tips";
    public static final String CARD_PROCESSING_ENABLE_TAX = "terminal_connection_enable_tax";

    public static final String MODIFIER_TABLE = "modifier_table";
    public static final String MODIFIER_ITEM_NO = "modifier_item_no";
    public static final String MODIFIER_INCLUDED = "price_included";

    public static final String CUSTOMER_GENERAL_INFO_TABLE = "customer_general_info_table";
    public static final String CUSTOMER_COMPANY_NAME = "customer_company_name";
    public static final String CUSTOMER_PRIMARY_PHONE = "customer_primary_phone";
    public static final String CUSTOMER_GENDER = "gender";
    public static final String CUSTOMER_ALTERNATE_PHONE = "customer_alternate_phone";
    public static final String CUSTOMER_STREET1 = "customer_street1";
    public static final String CUSTOMER_STREET2 = "customer_street2";
    public static final String CUSTOMER_STATE = "customer_state";
    public static final String CUSTOMER_CITY = "customer_city";
    public static final String CUSTOMER_COUNTRY = "customer_country";
    public static final String CUSTOMER_ZIPCODE = "customer_zipcode";
    public static final String CUSTOMER_BIRTHDAY = "customer_birthday";
    public static final String CUSTOMER_REWARD_POINTS = "reward_points";
    public static final String CUSTOMER_GENERAL_TIN = "company_tin";

    public static final String SPLIT_INVOICE_TABLE = "split_invoice_table";
    public static final String SPLIT_INVOICE_ID = "invoice_id";
    public static final String SPLIT_PAYMENT_TYPE = "payment_type";
    public static final String SPLIT_AMOUNT = "amount";
    public static final String SPLIT_CHEQUE_NO = "cheque_no";
    public static final String SPLIT_ACCOUNT_NO = "account_no";

    public static final String LOGIN_LOGOUT_TABLE = "login_logout_table";
    public static final String LOGIN_EMPLOYEE_NAME = "login_employee_name";
    public static final String LOGIN_EMPLOYEE_ID = "login_employee_id";
    public static final String LOGIN_TIME = "login_time";
    public static final String LOGOUT_TIME = "logout_time";
    public static final String DIFF_MINUTES = "diff_minutes";
    public static final String DIFF_HOURS = "diff_hours";
    public static final String WAGES = "wages";
    public static final String SESSIONIDVAL = "sessioniduniqueval";

    public static final String TAXED_AMOUNTS_TABLE = "taxed_amounts";
    public static final String TAX_NAME1 = "tax_name";
    public static final String TAX_VALUE1 = "tax_value";
    public static final String INVOICE_ID1 = "invoice_id";

    public static final String TABLE_SETTINGS_TABLE = "tables_setting";
    public static final String SETTINGS_TABLENAME = "table_name";
    public static final String SETTINGS_TABLE_DESCRIPTION = "table_description";
    public static final String SETTINGS_TABLE_IMGPATH = "table_img_path";
    public static final String SETTINGS_TABLE_CAPACITY = "table_capacity";
    public static final String SETTINGS_TABLE_STOREID = "store_id";
    public static final String SETTINGS_TABLE_INVOICEID = "associated_invoice_id";
    public static final String SETTINGS_TABLE_STATUS = "occupancy_status";


    public static final String TABLE_INVENTORY_PRODUCT_IMAGES = "inventory_product_images";
    public static final String INVENTORY_PRODUCT_IMAGE_ITEM_NO = "inventory_item_no";
    public static final String INVENTORY_PRODUCT_IMAGE_FULL_IMAGE = "fullsize_image_path";
    public static final String INVENTORY_PRODUCT_IMAGE_COMPR_IMAGE = "compressed_image_path";
    public static final String INVENTORY_PRODUCT_IMAGE_COMPR_IMAGE_2X = "compressed_image_path_2x";
    public static final String INVENTORY_PRODUCT_IMAGE_TEXT_COLOR = "text_color_code";
    public static final String INVENTORY_PRODUCT_IMAGE_SERVER_URL = "server_base_url";
    public static final String INVENTORY_PRODUCT_IMAGE_BUTTON_COLOR = "button_color_code";
    public static final String INVENTORY_PRODUCT_IMAGE_U_ID = "unique_id";
    public static final String INVENTORY_PRODUCT_IMAGE_CREATED_TIMESTAMP = "created_timestamp";
    public static final String INVENTORY_PRODUCT_IMAGE_MODIFIED_TIMESTAMP = "modified_timestamp";
    public static final String INVENTORY_PRODUCT_IMAGE_SERVER_LOCAL = "server_local";

    public static final String STORE_CATEGORY_TABLE = "store_category";
    public static final String STORE_CATEGORY_STOREID = "store_id";
    public static final String STORE_CATEGORY_ID = "category_id";
    public static final String STORE_CATEGORYID = "store_category_id";

    public static final String REWARD_POINTS_HISTORY_TABLE = "reward_points_history";
    public static final String REWARD_POINTS_HISTORY_INVOICEID = "invoice_id";
    public static final String REWARD_POINTS_HISTORY_CUSTOMERID = "customer_id";
    public static final String REWARD_POINTS_HISTORY_CUSTOMERNAME = "customer_name";
    public static final String REWARD_POINTS_HISTORY_CUSTOMERPHONE = "customer_phone";
    public static final String REWARD_POINTS_HISTORY_REDEEMED = "points_redeemed";// DEFAULT 0
    public static final String REWARD_POINTS_HISTORY_PLATFORM = "platform";
    public static final String REWARD_POINTS_HISTORY_STATUS = "status"; //default 'attained' //'redeemed','attained'
    public static final String REWARD_POINTS_HISTORY_AMOUNT_REDEMPTION = "amount_redemption";// DEFAULT 0

    public static final String ALTERNATE_PLU_TABLE = "alternate_plu";
    public static final String ALTERNATE_PLU_item_no = "item_no";
    public static final String ALTERNATE_PLU_item_name = "item_name";
    public static final String ALTERNATE_PLU_plu_number = "plu_number";
    public static final String ALTERNATE_PLU_plu_barcode = "plu_barcode";
    public static final String ALTERNATE_PLU_avg_cost = "avg_cost";
    public static final String ALTERNATE_PLU_sp = "sp";
    public static final String ALTERNATE_PLU_sp_tax = "sp_tax";
    public static final String ALTERNATE_PLU_mrp = "mrp";
    public static final String ALTERNATE_PLU_taxes = "taxes";
    public static final String ALTERNATE_PLU_notes = "notes";
    public static final String ALTERNATE_PLU_description = "description";
    public static final String ALTERNATE_PLU_unique_id = "unique_id";
    public static final String ALTERNATE_PLU_created_timestamp = "created_timestamp";
    public static final String ALTERNATE_PLU_modified_timestamp = "modified_timestamp";
    public static final String ALTERNATE_PLU_server_local = "server_local";

    public static final String MEMBERSHIP_TYPES_TABLE = "membership_types";
    public static final String MEMBERSHIP_TYPE_MEMBERSHIP_NAME = "membership_name";
    public static final String MEMBERSHIP_TYPE_DESCRIPTION = "membership_description";
    public static final String MEMBERSHIP_TYPE_MEMBERSHIP_ID = "membership_id";
    public static final String MEMBERSHIP_TYPE_LOCAL_NAMES = "local_names";
    public static final String MEMBERSHIP_TYPE_CREATED_BY = "created_by";
    public static final String MEMBERSHIP_TYPE_MODIFIED_BY = "modified_by";
    public static final String MEMBERSHIP_TYPE_unique_id = "unique_id";
    public static final String MEMBERSHIP_TYPE_created_timestamp = "created_timestamp";
    public static final String MEMBERSHIP_TYPE_modified_timestamp = "modified_timestamp";
    public static final String MEMBERSHIP_TYPE_server_local = "server_local";

    public static final String MEMBERSHIP_PLANS_TABLE = "membership_plans";
    public static final String MEMBERSHIP_PLANS_MEMBERSHIP_NAME = "membership_plan_name";
    public static final String MEMBERSHIP_PLANS_PLAN_ID = "membership_plan_id";
    public static final String MEMBERSHIP_PLANS_TYPE_ID = "membership_type_id";
    public static final String MEMBERSHIP_PLANS_LOCAL_NAMES = "local_names";
    public static final String MEMBERSHIP_PLANS_DURATION = "duration";
    public static final String MEMBERSHIP_PLANS_FREE_PERIOD = "free_period";
    public static final String MEMBERSHIP_PLANS_STOREWISE_PRICING = "storewise_pricing";
    public static final String MEMBERSHIP_PLANS_NO_OF_CUSTOMERS = "number_of_customers";
    public static final String MEMBERSHIP_PLANS_CATEGORIES_APPLICABLE = "categories_applicable";
    public static final String MEMBERSHIP_PLANS_DISCOUNT_APPLICABLE = "discount_applicable";
    public static final String MEMBERSHIP_PLANS_LENGTH = "membershipid_length";
    public static final String MEMBERSHIP_PLANS_MAP_ITEM = "map_item_id";
    public static final String MEMBERSHIP_PLANS_CREATED_BY = "created_by";
    public static final String MEMBERSHIP_PLANS_MODIFIED_BY = "modified_by";
    public static final String MEMBERSHIP_PLANS_unique_id = "unique_id";
    public static final String MEMBERSHIP_PLANS_created_timestamp = "created_timestamp";
    public static final String MEMBERSHIP_PLANS_modified_timestamp = "modified_timestamp";
    public static final String MEMBERSHIP_PLANS_server_local = "server_local";


    public static final String COMBO_ITEMS_TABLE = "combo_items";
    public static final String COMBO_ITEMS_PARENT_ITEM_ID = "parent_item_id";
    public static final String COMBO_ITEMS_CHILD_ITEM_ID = "child_item_id";
    public static final String COMBO_ITEMS_CHILD_ITEM_QTY = "qty";
    public static final String COMBO_ITEMS_CHILD_ITEM_DISP_QTY = "display_quantity";
    public static final String COMBO_ITEMS_CHILD_NOTES = "notes";
    public static final String COMBO_ITEMS_IS_PACKAGE = "is_package";
    public static final String COMBO_ITEMS_unique_id = "unique_id";
    public static final String COMBO_ITEMS_created_timestamp = "created_timestamp";
    public static final String COMBO_ITEMS_modified_timestamp = "modified_timestamp";
    public static final String COMBO_ITEMS_server_local = "server_local";

    public static final String CUSTOMER_MEMBERSHIPS_TABLE = "customer_memberships";
    public static final String CUSTOMER_MEMBERSHIPS_CUSTOMER_ID = "customer_id";
    public static final String CUSTOMER_MEMBERSHIPS_PLAN_ID = "membership_plan_id";
    public static final String CUSTOMER_MEMBERSHIPS_ID = "membership_id";
    public static final String CUSTOMER_MEMBERSHIPS_USERS = "membership_users";
    public static final String CUSTOMER_MEMBERSHIPS_VALID_FROM = "valid_from";
    public static final String CUSTOMER_MEMBERSHIPS_VALID_UPTO = "valid_upto";
    public static final String CUSTOMER_MEMBERSHIPS_STATUS = "status";
    public static final String CUSTOMER_MEMBERSHIPS_FIRST_INV_ID = "first_invoice_id";
    public static final String CUSTOMER_MEMBERSHIPS_CREATED_BY = "created_by";
    public static final String CUSTOMER_MEMBERSHIPS_MODIFIED_BY = "modified_by";
    public static final String CUSTOMER_MEMBERSHIPS_unique_id = "unique_id";
    public static final String CUSTOMER_MEMBERSHIPS_created_timestamp = "created_timestamp";
    public static final String CUSTOMER_MEMBERSHIPS_modified_timestamp = "modified_timestamp";
    public static final String CUSTOMER_MEMBERSHIPS_server_local = "server_local";


    public static final String CUSTOMER_MEMBERSHIP_PAYMENTS_TABLE = "customer_membership_payments";
    public static final String CUSTOMER_MEMBERSHIP_PAYMENTS_INV_ID = "invoice_id";
    public static final String CUSTOMER_MEMBERSHIP_PAYMENTS_ID = "membership_id";
    public static final String CUSTOMER_MEMBERSHIP_PAYMENTS_MAP_ITEM_ID = "map_item_id";
    public static final String CUSTOMER_MEMBERSHIP_PAYMENTS_MAP_ITEM_UNIQUE_ID = "map_item_invoice_unique_id";
    public static final String CUSTOMER_MEMBERSHIP_PAYMENTS_VALID_FROM = "valid_from";
    public static final String CUSTOMER_MEMBERSHIP_PAYMENTS_VALID_UPTO = "valid_upto";
    public static final String CUSTOMER_MEMBERSHIP_PAYMENTS_unique_id = "unique_id";
    public static final String CUSTOMER_MEMBERSHIP_PAYMENTS_created_timestamp = "created_timestamp";
    public static final String CUSTOMER_MEMBERSHIP_PAYMENTS_modified_timestamp = "modified_timestamp";
    public static final String CUSTOMER_MEMBERSHIP_PAYMENTS_server_local = "server_local";


    public static final String PACKAGES_PURCHASE_TABLE = "packages_purchase";
    public static final String PACKAGES_PURCHASE_CUSTOMER_ID = "customer_id";
    public static final String PACKAGES_PURCHASE_INVOICE_ID = "invoice_id";
    public static final String PACKAGES_PURCHASE_PRODUCT_ID = "package_product_id";
    public static final String PACKAGES_PURCHASE_INV_UNIQUE_ID = "package_invoice_unique_id";
    public static final String PACKAGES_PURCHASE_ID = "package_id";
    public static final String PACKAGES_PURCHASE_SERVICES_PURCHASED = "products_services_purchased";
    public static final String PACKAGES_PURCHASE_SERVICES_AVAILED = "products_services_availed";
    public static final String PACKAGES_PURCHASE_SERVICES_REMAINING = "products_services_remaining";
    public static final String PACKAGES_PURCHASE_UNIQUE_ID = "unique_id";
    public static final String PACKAGES_PURCHASE_created_timestamp = "created_timestamp";
    public static final String PACKAGES_PURCHASE_modified_timestamp = "modified_timestamp";
    public static final String PACKAGES_PURCHASE_server_local = "server_local";


    public static final String PACKAGES_REDEMPTION_HISTORY_TABLE = "package_redemption_history";
    public static final String PACKAGES_REDEMPTION_HISTORY_INVOICE_ID = "invoice_id";
    public static final String PACKAGES_REDEMPTION_HISTORY_INV_UNIQUE_ID = "product_invoice_unique_id";
    public static final String PACKAGES_REDEMPTION_HISTORY_ID = "package_id";
    public static final String PACKAGES_REDEMPTION_PACKAGE_ID = "package_id";
    public static final String PACKAGES_REDEMPTION_HISTORY_PRODUCT_ID = "product_id";
    public static final String PACKAGES_REDEMPTION_HISTORY_UNIQUE_ID = "unique_id";
    public static final String PACKAGES_REDEMPTION_HISTORY_created_timestamp = "created_timestamp";
    public static final String PACKAGES_REDEMPTION_HISTORY_modified_timestamp = "modified_timestamp";
    public static final String PACKAGES_REDEMPTION_HISTORY_server_local = "server_local";

    /*
    table>
<name>prepaid_card_plans</name>
<createstring>
    TINYTEXT NOT NULL,  VARCHAR(40) NOT NULL, map_item_id VARCHAR(40) NOT NULL, unique_id VARCHAR(30) NOT NULL, created_timestamp DATETIME DEFAULT NULL, modified_timestamp	DATETIME DEFAULT NULL, server_local VARCHAR(40) DEFAULT 'server', UNIQUE(unique_id), PRIMARY KEY(_id)) ENGINE=InnoDB DEFAULT CHARSET=UTF8
</createstring>
</table>
     */
    public static final String PREPAID_CARD_PLANS_TABLE = "prepaid_card_plans";
    public static final String PREPAID_CARD_PLANS_NAME = "prepaidcard_name";
    public static final String PREPAID_CARD_PLANS_PLAN_ID = "prepaidcard_plan_id";
    public static final String PREPAID_CARD_PLANS_AMOUNT_REDEEMABLE = "amount_redeemable";
    public static final String PREPAID_CARD_PLANS_AMOUNT_CHARGED = "amount_charged";
    public static final String PREPAID_CARD_PLANS_CATEGORIES = "categories";
    public static final String PREPAID_CARD_PLANS_VALIDITY_PERIOD = "validity_period";
    public static final String PREPAID_CARD_PLANS_LENGTH = "card_text_length";
    public static final String PREPAID_CARD_PLANS_MAP_ITEM = "map_item_id";
    public static final String PREPAID_CARD_PLANS_unique_id = "unique_id";
    public static final String PREPAID_CARD_PLANS_created_timestamp = "created_timestamp";
    public static final String PREPAID_CARD_PLANS_modified_timestamp = "modified_timestamp";
    public static final String PREPAID_CARD_PLANS_server_local = "server_local";
    /*<table>
<name>prepaid_card_purchase</name>
<createstring>

            </createstring>
</table>*/
    public static final String PREPAID_CARD_PURCHASE_TABLE = "prepaid_card_purchase";
    public static final String PREPAID_CARD_PURCHASE_ID = "prepaid_card_id";
    public static final String PREPAID_CARD_PURCHASE_PLAN_ID = "prepaid_card_plan_id";
    public static final String PREPAID_CARD_PURCHASE_CUSTOMER_ID = "customer_id";
    public static final String PREPAID_CARD_PURCHASE_INVOICE_ID = "invoice_id";
    public static final String PREPAID_CARD_PURCHASE_PRODUCT_ID = "prepaid_product_id";
    public static final String PREPAID_CARD_PURCHASE_PRODUCT_INVOICE_UNIQUE_ID = "prepaid_invoice_unique_id";
    public static final String PREPAID_CARD_PURCHASE_AMOUNT_RECIEVED = "amount_received";
    public static final String PREPAID_CARD_PURCHASE_AMOUNT_REDEEMED = "amount_redeemed";
    public static final String PREPAID_CARD_PURCHASE_AMOUNT_REMAINING = "amount_remaining";
    public static final String PREPAID_CARD_PURCHASE_VALIDITY_EXPIRY = "validity_expiry";
    public static final String PREPAID_CARD_PURCHASE_unique_id = "unique_id";
    public static final String PREPAID_CARD_PURCHASE_created_timestamp = "created_timestamp";
    public static final String PREPAID_CARD_PURCHASE_modified_timestamp = "modified_timestamp";
    public static final String PREPAID_CARD_PURCHASE_server_local = "server_local";
    /*<table>
<name>prepaid_card_redemption_history</name>
<createstring>
      VARCHAR(40) NOT NULL,  VARCHAR(40) NOT NULL, unique_id VARCHAR(30) NOT NULL, created_timestamp DATETIME DEFAULT NULL, modified_timestamp	DATETIME DEFAULT NULL, server_local VARCHAR(40) DEFAULT 'server', UNIQUE(unique_id), PRIMARY KEY(_id)) ENGINE=InnoDB DEFAULT CHARSET=UTF8
            </createstring>
</table>*/

    public static final String PREPAID_CARD_REDEMPTION_TABLE = "prepaid_card_redemption_history";
    public static final String PREPAID_CARD_REDEMPTION_CARD_ID = "prepaidcard_id";
    public static final String PREPAID_CARD_REDEMPTION_INVOICE_ID = "invoice_id";
    public static final String PREPAID_CARD_REDEMPTION_PRODUCT_ID = "product_id";
    public static final String PREPAID_CARD_REDEMPTION_PRODUCT_INVOICE_UNIQUE_ID = "product_invoice_unique_id";
    public static final String PREPAID_CARD_REDEMPTION_OPENING_BALANCE = "opening_balance";
    public static final String PREPAID_CARD_REDEMPTION_AMOUNT = "amount";
    public static final String PREPAID_CARD_REDEMPTION_CLOSING_BALANCE = "closing_balance";
    public static final String PREPAID_CARD_REDEMPTION_unique_id = "unique_id";
    public static final String PREPAID_CARD_REDEMPTION_created_timestamp = "created_timestamp";
    public static final String PREPAID_CARD_REDEMPTION_modified_timestamp = "modified_timestamp";
    public static final String PREPAID_CARD_REDEMPTION_server_local = "server_local";

    public static final String FOOD_PARTNERS_TABLE = "food_partners_settings";
    public static final String FOOD_PARTNERS_STORE_ID = "store_id ";
    public static final String FOOD_PARTNERS_PARTNER_NAME = "partner";
    public static final String FOOD_PARTNERS_ATTRIBUTE = "attribute";
    public static final String FOOD_PARTNERS_VALUE = "value";
    public static final String FOOD_PARTNERS_unique_id = "unique_id";
    public static final String FOOD_PARTNERS_created_timestamp = "created_timestamp";
    public static final String FOOD_PARTNERS_modified_timestamp = "modified_timestamp";
    public static final String FOOD_PARTNERS_server_local = "server_local";

    public static final String RETURN_VOUCHERS_TABLE = "return_vouchers";
    public static final String RETURN_VOUCHER_ID = "invoice_return_voucher_id";
    public static final String RETURN_VOUCHER_TOTAL_AMOUNT = "total_amt";

    public DatabaseVariables()
    {
        mySqlObj = MainActivity.mySqlObj;
        mySqlCrmObj = MainActivity.mySqlCrmObj;
    }
    public void replaceAttributesWithValues(String attribute, String valueOfAttribute) {

        Log.v("Error","SELECT * FROM "+dbHelper.PREFERENCES_TABLE+" WHERE "+dbHelper.PREFERENCES_ATTRIBUTE+"='"+attribute+"'");
        ArrayList<JSONObject> attributeResults = dbHelper.executeRawqueryJSON("SELECT * FROM "+dbHelper.PREFERENCES_TABLE+" WHERE "+dbHelper.PREFERENCES_ATTRIBUTE+"='"+attribute+"'");
        if(attributeResults.size()==1)
        {

            String here = DatabaseHelper.PREFERENCES_ATTRIBUTE + "=?";
            String uniq = attribute;
            ContentValues cv = new ContentValues();

            cv.put(dbHelper.PREFERENCES_ATTRIBUTE, attribute);
            cv.put(dbHelper.PREFERENCES_VALUE, valueOfAttribute);
            dbHelper.executeUpdate(dbHelper.PREFERENCES_TABLE,cv,here, new String[]{uniq});
        }
        else {
            String here = DatabaseHelper.PREFERENCES_ATTRIBUTE + "=?";
            String uniq = attribute;
            dbHelper.executeDelete(dbHelper.PREFERENCES_TABLE,
                    here, new String[]{uniq});

            ContentValues cv = new ContentValues();

            cv.put(dbHelper.PREFERENCES_ATTRIBUTE, attribute);
            cv.put(dbHelper.PREFERENCES_VALUE, valueOfAttribute);
            dbHelper.insertData(cv, dbHelper.PREFERENCES_TABLE);
        }
    }


    public String valueForAttribute(String tableName,String attributeName)
    {
        String returningValue = "";
        MySQLJDBC sqlCrmObj = MainActivity.mySqlCrmObj;
        ArrayList<JSONObject> checkExistingObj = sqlCrmObj.executeRawqueryJSON("SELECT * FROM "+tableName+" WHERE attribute='"+attributeName+"'");
        if(checkExistingObj.size()!=0)
        {

            JSONObject attrRow = checkExistingObj.get(0);
            returningValue = String.valueOf(attrRow.optString("value"));
        }
        return returningValue;
    }
    public String getValueForAttribute(String attribute) {
        String returnValue = "";

        ArrayList<JSONObject> attributeResults = SplashScreen.dbHelper.executeRawqueryJSON("SELECT * FROM "+SplashScreen.dbHelper.PREFERENCES_TABLE+" WHERE "+dbHelper.PREFERENCES_ATTRIBUTE+"='"+attribute+"'");
        if(attributeResults.size()==1)
        {
            JSONObject resultJSON = attributeResults.get(0);
            returnValue = resultJSON.optString(dbHelper.PREFERENCES_VALUE);

        }
        return returnValue;
    }

    public ArrayList<JSONObject> executeRawqueryJSON(String query) {

        ArrayList<JSONObject> results = mySqlObj.executeRawqueryJSON(query);

        return results;
    }

    public void executeExecSQL(String qry) {
        mySqlObj.executeRawquery(qry);
    }


    public String getLicenseKey() {
        String licenseKeyQuery = "SELECT * FROM misc WHERE `keystr`='license_key'";

        ArrayList<JSONObject> licenseKeyDetails = mySqlCrmObj.executeRawqueryJSON(licenseKeyQuery);
        if(licenseKeyDetails.size()>0)
        {
            if( licenseKeyDetails.get(0).optString("valuestr").equals("") ||  licenseKeyDetails.get(0).optString("valuestr")==null){
                return "";
            }else{
                return (String)licenseKeyDetails.get(0).optString("valuestr");
            }
        }else {
            return "";
        }
    }

    public void executeUpdateDD(String tableName, ContentValues contentValues, String here, String[] args) {

//        mySqlObj.executeUpdate(tableName,contentValues,here,args);
    }

    public void executeUpdateToDB(String tableName, JSONObject contentValues, String here, String value) {

        mySqlObj.executeUpdate(tableName,contentValues,here,value);

    }


    public void executeInsertBatch(String tableName, ArrayList<JSONObject> insertingValues) {
        mySqlObj.executeInsertBatch(tableName,insertingValues);
    }

    public void executeInsertToDB(String tableName, JSONObject contentValues) {
        mySqlObj.executeInsert(tableName,contentValues);
    }

    public void executeDeleteInDB(String tablename, String here, String value) {
        mySqlObj.executeDelete(tablename,here,value);
    }

    public void sendToServerServiceData(JSONObject subarr) {
        String timeC = ConstantsAndUtilities.currentTime();
        ConstantsAndUtilities cv = new ConstantsAndUtilities();
        final String datavalforedit = cv.urlEncode(subarr.toString());
        DatabaseVariables dbVar  = new DatabaseVariables();
        String macAddress =  cv.urlEncode("");
        String currentTime = cv.urlEncode(timeC);
        String licenseKey = dbVar.getLicenseKey();

        JSONObject postJsonInfo = new JSONObject();
        try {
            postJsonInfo.put("unique_id",cv.randomValue());

        postJsonInfo.put("pending_data","invoiceInfo.php?javaVersion=true&macaddr="+macAddress+"&license_key="+licenseKey+"&desktop_web_pos=true&androidclient=true&Currentsystemtime="+currentTime+"&data="+datavalforedit);
        postJsonInfo.put("pending_data_json",String.valueOf(subarr));
        postJsonInfo.put("created_timestamp",timeC);
        postJsonInfo.put("modified_timestamp",timeC);
        MainActivity.mySqlCrmObj.executeInsert("pending_data",postJsonInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createTablesIfNotExists()
    {


        String CategoryPrintingCreateQuery = "CREATE TABLE IF NOT EXISTS "+ DatabaseHelper.CATEGORYWISE_PRINTING_TABLE +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                +DatabaseHelper.CATEGORYWISE_PRINTING_CATEGORY_ID + " TEXT,"+ DatabaseHelper.CATEGORYWISE_PRINTING_PRINTER_NAME+" TEXT,"+DatabaseHelper.CREATED_TIME+" TEXT,"+DatabaseHelper.UPDATED_TIME+" TEXT, "+ DatabaseHelper.CATEGORYWISE_PRINTING_PRINTER_ROW_ID +" TEXT )";
        dbHelper.executeExecSQL(CategoryPrintingCreateQuery);

        String usersTableCreateQuery = "CREATE TABLE IF NOT EXISTS "+ DatabaseHelper.PREFERENCES_TABLE +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                +DatabaseHelper.PREFERENCES_ATTRIBUTE + " TEXT,"+ DatabaseHelper.PREFERENCES_VALUE+" TEXT,"+DatabaseHelper.CREATED_TIME+" TEXT,"+DatabaseHelper.UPDATED_TIME+" TEXT)";
        dbHelper.executeExecSQL(usersTableCreateQuery);

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
        dbHelper.executeExecSQL(inventoryDetails);


        String CategoryDetails = "create table if not exists " + DatabaseVariables.CATEGORY_TABLE
                + " ( " + DatabaseVariables.ID + " integer primary key autoincrement, "
                + DatabaseVariables.UNIQUE_ID + " text, " + DatabaseVariables.CREATED_DATE + " text, "
                + DatabaseVariables.MODIFIED_DATE + " text, " + DatabaseVariables.MODIFIED_IN + " text, "
                + DatabaseVariables.CategoryId + " text, " + DatabaseVariables.CategoryDesp + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";

        dbHelper.executeExecSQL(CategoryDetails);

        String DepartmentDetails = "create table if not exists "
                + DatabaseVariables.DEPARTMENT_TABLE + " ( " + DatabaseVariables.ID
                + " integer primary key autoincrement, " + DatabaseVariables.UNIQUE_ID
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.DepartmentID
                + " text, " + DatabaseVariables.DepartmentDesp + " text, " + DatabaseVariables.FoodstampableForDept
                + " text, " + DatabaseVariables.TaxValForDept + " text, " + DatabaseVariables.CHECKED_VALUE
                + " text, " + DatabaseVariables.CategoryForDepartment + " text, local_name text , UNIQUE (unique_id) ON CONFLICT REPLACE);";

        dbHelper.executeExecSQL(DepartmentDetails);


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

        dbHelper.executeExecSQL(ALTERNATE_PLU);

        String Product_Optional_Info_Details = "create table if not exists "
                + DatabaseVariables.OPTIONAL_INFO_TABLE + " ( " + DatabaseVariables.ID
                + " integer primary key autoincrement, " + DatabaseVariables.UNIQUE_ID
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.BONUS_POINTS
                + " text, " + DatabaseVariables.BARCODES + " text, " + DatabaseVariables.LOCATION + " text, "
                + DatabaseVariables.UNIT_SIZE + " text, " + DatabaseVariables.UNIT_TYPE + " text, "
                + DatabaseVariables.HAS_BOTTLE_DEPOSIT + " text, " + DatabaseVariables.BOTTLE_DEPOSIT_VALUE+ " text,"
                + DatabaseVariables.PRODUCT_META_DATA + " text, "
                + DatabaseVariables.INVENTORY_ITEM_NO + " text, " + DatabaseVariables.COMMISSION_OPTIONAL_INFO
                + " text, " + DatabaseVariables.INVENTORY_MODIFIER_ITEM + " text, "
                + DatabaseVariables.INVENTORY_COUNT_THIS_ITEM + " text, "
                + "count_raw_material text, "
                + "count_parent_item text, "
                + DatabaseVariables.INVENTORY_BRAND_ID + " text, "
                + DatabaseVariables.INVENTORY_MRP + " text, "
                + DatabaseVariables.INVENTORY_SUB_UNIT + " text, "
                + DatabaseVariables.INVENTORY_SUB_UNIT_PORTIONS + " text, "
                + DatabaseVariables.INVENTORY_LOCAL_NAMES + " text, "
                + DatabaseVariables.INVENTORY_ALLOW_BUYBACK + " text, " + DatabaseVariables.INVENTORY_PROMPT_PRICE
                + " text, " + DatabaseVariables.INVENTORY_PRINT_ON_RECEIPT + " text, "
                + DatabaseVariables.INVENTORY_FOODSTAMPABLE + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        dbHelper.executeExecSQL(Product_Optional_Info_Details);

        String storeproductspricesdetails = "create table if not exists "
                + DatabaseVariables.STORE_PRODUCTS_TABLE + " ( " + DatabaseVariables.ID
                + " integer primary key autoincrement, " + DatabaseVariables.PRODUCTS_TAKEAWAY
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.PRODUCTS_STOREID
                + " text, " + DatabaseVariables.PRODUCTS_ITEM_NO + " text, " + DatabaseVariables.PRODUCTS_ITEM_NAME
                + " text, " + DatabaseVariables.PRODUCTS_PRICE + " text, " + DatabaseVariables.UNIQUE_ID + " text, " + DatabaseVariables.PRODUCTS_DELIVERY_PRICE
                + " text, " + DatabaseVariables.PRODUCTS_NOTIFY_LOWSTOCK + " text, " + DatabaseVariables.PRODUCTS_MIN_STOCKCOUNT + " text,"+DatabaseVariables.STORE_PRODUCT_DISABLE+" text, UNIQUE (unique_id) ON CONFLICT REPLACE);";
        dbHelper.executeExecSQL(storeproductspricesdetails);


        String TaxDetails = "create table if not exists " + DatabaseVariables.TAX_TABLE + " ( "
                + DatabaseVariables.ID + " integer primary key autoincrement, " + DatabaseVariables.UNIQUE_ID
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.TAX_NAME + " text, " + DatabaseVariables.TAX_OVERALL_VALUE + " text, "
                + DatabaseVariables.TAX_VALUE + " INTEGER, " + DatabaseVariables.TAX_STORE + " text, " + DatabaseVariables.TAX_ORDER_TYPE + " text DEFAULT \"store sale\",  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        dbHelper.executeExecSQL(TaxDetails);

        String CatTaxDetails = "create table if not exists " + DatabaseVariables.CAT_TAX_TABLE + " ( "
                + DatabaseVariables.ID + " integer primary key autoincrement, " + DatabaseVariables.UNIQUE_ID
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.CAT_TAX_NAME + " text, " + DatabaseVariables.CAT_TAX_CAT_ID + " text, "
                + DatabaseVariables.CAT_TAX_VALUE + " INTEGER, " + DatabaseVariables.CAT_TAX_STORE + " text, " + DatabaseVariables.CAT_TAX_ORDER_TYPE + " text DEFAULT \"store sale\",  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        dbHelper.executeExecSQL(CatTaxDetails);


        String inventory_product_images = "CREATE TABLE IF NOT EXISTS inventory_product_images( _id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL," +
                "inventory_item_no text, fullsize_image_path text," +
                "compressed_image_path text, compressed_image_path_2x text," +
                "text_color_code text, server_base_url text," +
                "button_color_code text, unique_id text," +
                "created_timestamp text,	modified_timestamp	text," +
                "server_local text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        dbHelper.executeExecSQL(inventory_product_images);


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
        dbHelper.executeExecSQL(employeeDetails);

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

        dbHelper.executeExecSQL(employeePermissionsDetails);

        String AdvancedDetails = "create table if not exists "
                + DatabaseVariables.ADVANCED_TABLE + " ( " + DatabaseVariables.ID
                + " integer primary key autoincrement, " + DatabaseVariables.UNIQUE_ID
                + " text, " + DatabaseVariables.CREATED_DATE + " text, " + DatabaseVariables.MODIFIED_DATE
                + " text, " + DatabaseVariables.MODIFIED_IN + " text, " + DatabaseVariables.ADVANCED_EMPLOYEE_ID
                + " text, " + DatabaseVariables.ADVANCED_PERMISSIONS + " text, " + DatabaseVariables.ADVANCED_ASSIGNED_EMP
                + " text, " + DatabaseVariables.ADVANCED_ASSIGNED_BY + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";

        dbHelper.executeExecSQL(AdvancedDetails);

        String emp_store = "create table if not exists " + DatabaseVariables.EMP_STORE_TABLE
                + " ( " + BaseColumns._ID
                + " integer primary key autoincrement, " + DatabaseVariables.EMPLOYEE_EMPLOYEE_ID
                + " text, " + DatabaseVariables.EMP_STORE_ID + " text, " + DatabaseVariables.EMP_STORE_NAME
                + " text, " + DatabaseVariables.UNIQUE_ID + " text, " + DatabaseVariables.CREATED_DATE
                + " text, " + DatabaseVariables.MODIFIED_DATE + " text, " + DatabaseVariables.MODIFIED_IN
                + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        dbHelper.executeExecSQL(emp_store);



        String STORE_CATEGORY = "create table if not exists " + DatabaseVariables.STORE_CATEGORY_TABLE
                + " ( " + BaseColumns._ID
                + " integer primary key autoincrement, " + DatabaseVariables.STORE_CATEGORY_STOREID
                + " text, " + DatabaseVariables.STORE_CATEGORY_ID + " text, " + DatabaseVariables.STORE_CATEGORYID
                + " text, " + DatabaseVariables.UNIQUE_ID + " text, " + DatabaseVariables.CREATED_DATE
                + " text, " + DatabaseVariables.MODIFIED_DATE + " text, " + DatabaseVariables.MODIFIED_IN
                + " text,  UNIQUE (unique_id) ON CONFLICT REPLACE);";
        dbHelper.executeExecSQL(STORE_CATEGORY);


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
        dbHelper.executeExecSQL(StoreDetails);


    }
}
