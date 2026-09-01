package com.example.swapapp.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // =====================================================
    // DATABASE
    // =====================================================

    private static final String DATABASE_NAME = "SwapApp.db";

    private static final int DATABASE_VERSION = 5;


    public DatabaseHelper(Context context) {

        super(
                context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }


    // =====================================================
    // CREATE DATABASE
    // =====================================================

    @Override
    public void onCreate(SQLiteDatabase db) {

        // =========================
        // USERS
        // =========================

        db.execSQL(

                "CREATE TABLE IF NOT EXISTS USERS (" +

                        "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        "name TEXT NOT NULL, " +

                        "email TEXT UNIQUE NOT NULL, " +

                        "password TEXT NOT NULL, " +

                        "time_credits INTEGER DEFAULT 20" +

                        ")"
        );


        // =========================
        // SKILLS
        // =========================

        db.execSQL(

                "CREATE TABLE IF NOT EXISTS SKILLS (" +

                        "skill_id INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        "user_id INTEGER NOT NULL, " +

                        "title TEXT NOT NULL, " +

                        "category TEXT, " +

                        "duration_hrs INTEGER, " +

                        "description TEXT, " +

                        "skill_level TEXT, " +

                        "delivery_mode TEXT, " +

                        "FOREIGN KEY(user_id) " +
                        "REFERENCES USERS(user_id)" +

                        ")"
        );


        // =========================
        // TRANSACTIONS / SESSIONS
        // =========================

        db.execSQL(

                "CREATE TABLE IF NOT EXISTS TRANSACTIONS (" +

                        "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        "requester_id INTEGER NOT NULL, " +

                        "provider_id INTEGER NOT NULL, " +

                        "skill_id INTEGER NOT NULL, " +

                        "status TEXT DEFAULT 'Pending', " +

                        "rating INTEGER DEFAULT 0, " +

                        "rated_user_id INTEGER DEFAULT -1, " +

                        "FOREIGN KEY(requester_id) " +
                        "REFERENCES USERS(user_id), " +

                        "FOREIGN KEY(provider_id) " +
                        "REFERENCES USERS(user_id), " +

                        "FOREIGN KEY(skill_id) " +
                        "REFERENCES SKILLS(skill_id)" +

                        ")"
        );
    }


    // =====================================================
    // DATABASE UPGRADE
    // =====================================================

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        db.execSQL(
                "DROP TABLE IF EXISTS TRANSACTIONS"
        );

        db.execSQL(
                "DROP TABLE IF EXISTS SKILLS"
        );

        db.execSQL(
                "DROP TABLE IF EXISTS USERS"
        );

        onCreate(db);
    }


    // =====================================================
    // REGISTER USER
    // =====================================================

    public boolean registerUser(
            String name,
            String email,
            String password) {

        SQLiteDatabase db =
                getWritableDatabase();

        try {

            Cursor existing =
                    db.rawQuery(

                            "SELECT user_id " +
                                    "FROM USERS " +
                                    "WHERE LOWER(email) = LOWER(?)",

                            new String[]{
                                    email.trim()
                            }
                    );


            boolean alreadyExists =
                    existing.moveToFirst();

            existing.close();


            if (alreadyExists) {

                return false;
            }


            db.execSQL(

                    "INSERT INTO USERS " +

                            "(name, email, password, time_credits) " +

                            "VALUES (?, ?, ?, 20)",

                    new Object[]{

                            name.trim(),

                            email.trim(),

                            password
                    }
            );


            return true;


        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    // =====================================================
    // LOGIN USER
    // RETURNS REAL DATABASE USER ID
    // =====================================================

    public int loginUser(
            String email,
            String password) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = null;


        try {

            cursor =
                    db.rawQuery(

                            "SELECT user_id " +

                                    "FROM USERS " +

                                    "WHERE LOWER(email) = LOWER(?) " +

                                    "AND password = ? " +

                                    "LIMIT 1",

                            new String[]{

                                    email.trim(),

                                    password
                            }
                    );


            if (cursor.moveToFirst()) {

                return cursor.getInt(0);
            }


        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (cursor != null) {

                cursor.close();
            }
        }


        return -1;
    }


    // =====================================================
    // CHECK EMAIL EXISTS
    // =====================================================

    public boolean emailExists(
            String email) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(

                        "SELECT user_id " +

                                "FROM USERS " +

                                "WHERE LOWER(email) = LOWER(?) " +

                                "LIMIT 1",

                        new String[]{
                                email.trim()
                        }
                );


        boolean exists =
                cursor.moveToFirst();


        cursor.close();

        return exists;
    }


    // =====================================================
    // GET USER DETAILS
    // COLUMN 0 = name
    // COLUMN 1 = email
    // COLUMN 2 = credits
    // =====================================================

    public Cursor getUserDetails(
            int userId) {

        return getReadableDatabase()
                .rawQuery(

                        "SELECT name, email, time_credits " +

                                "FROM USERS " +

                                "WHERE user_id = ?",

                        new String[]{
                                String.valueOf(userId)
                        }
                );
    }


    // =====================================================
    // UPDATE PROFILE
    // =====================================================

    public boolean updateUserProfile(
            int userId,
            String newName,
            String newEmail) {

        SQLiteDatabase db =
                getWritableDatabase();


        try {

            db.execSQL(

                    "UPDATE USERS " +

                            "SET name = ?, " +

                            "email = ? " +

                            "WHERE user_id = ?",

                    new Object[]{

                            newName.trim(),

                            newEmail.trim(),

                            userId
                    }
            );


            return true;


        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    // =====================================================
    // ADD SKILL
    // =====================================================

    public boolean addSkill(
            int userId,
            String title,
            String category,
            int durationMins,
            String description,
            String skillLevel,
            String deliveryMode) {

        SQLiteDatabase db =
                getWritableDatabase();


        try {

            // User must exist
            Cursor userCursor =
                    db.rawQuery(

                            "SELECT user_id " +

                                    "FROM USERS " +

                                    "WHERE user_id = ?",

                            new String[]{
                                    String.valueOf(userId)
                            }
                    );


            boolean validUser =
                    userCursor.moveToFirst();


            userCursor.close();


            if (!validUser) {

                return false;
            }


            db.execSQL(

                    "INSERT INTO SKILLS " +

                            "(" +

                            "user_id, " +

                            "title, " +

                            "category, " +

                            "duration_hrs, " +

                            "description, " +

                            "skill_level, " +

                            "delivery_mode" +

                            ") " +

                            "VALUES (?, ?, ?, ?, ?, ?, ?)",

                    new Object[]{

                            userId,

                            title.trim(),

                            category,

                            durationMins,

                            description,

                            skillLevel,

                            deliveryMode
                    }
            );


            return true;


        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    // =====================================================
    // DELETE SKILL
    // =====================================================

    public boolean deleteSkill(
            int skillId) {

        SQLiteDatabase db =
                getWritableDatabase();


        try {

            db.execSQL(

                    "DELETE FROM SKILLS " +

                            "WHERE skill_id = ?",

                    new Object[]{
                            skillId
                    }
            );


            return true;


        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    // =====================================================
    // GET USER'S SKILLS
    // =====================================================

    public Cursor getUserSkills(
            int userId) {

        return getReadableDatabase()
                .rawQuery(

                        "SELECT * " +

                                "FROM SKILLS " +

                                "WHERE user_id = ? " +

                                "ORDER BY skill_id DESC",

                        new String[]{
                                String.valueOf(userId)
                        }
                );
    }


    // =====================================================
    // NUMBER OF SKILLS POSTED BY USER
    // =====================================================

    public int getUserSkillCount(
            int userId) {

        Cursor cursor =
                getReadableDatabase()
                        .rawQuery(

                                "SELECT COUNT(*) " +

                                        "FROM SKILLS " +

                                        "WHERE user_id = ?",

                                new String[]{
                                        String.valueOf(userId)
                                }
                        );


        int count = 0;


        if (cursor.moveToFirst()) {

            count =
                    cursor.getInt(0);
        }


        cursor.close();

        return count;
    }


    // =====================================================
    // GET REAL OWNER ID OF SKILL
    // =====================================================

    public int getSkillOwnerId(
            int skillId) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = null;


        try {

            cursor =
                    db.rawQuery(

                            "SELECT user_id " +

                                    "FROM SKILLS " +

                                    "WHERE skill_id = ? " +

                                    "LIMIT 1",

                            new String[]{
                                    String.valueOf(skillId)
                            }
                    );


            if (cursor.moveToFirst()) {

                return cursor.getInt(0);
            }


        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (cursor != null) {

                cursor.close();
            }
        }


        return -1;
    }


    // =====================================================
    // GET NAME OF PERSON WHO POSTED SKILL
    // =====================================================

    public String getSkillOwnerName(
            int skillId) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = null;

        String ownerName =
                "User";


        try {

            String query =

                    "SELECT u.name " +

                            "FROM SKILLS s " +

                            "JOIN USERS u " +

                            "ON s.user_id = u.user_id " +

                            "WHERE s.skill_id = ? " +

                            "LIMIT 1";


            cursor =
                    db.rawQuery(

                            query,

                            new String[]{
                                    String.valueOf(skillId)
                            }
                    );


            if (cursor.moveToFirst()) {

                String name =
                        cursor.getString(0);


                if (name != null &&
                        !name.trim().isEmpty()) {

                    ownerName =
                            name.trim();
                }
            }


        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (cursor != null) {

                cursor.close();
            }
        }


        return ownerName;
    }


    // =====================================================
    // CHECK IF USER ALREADY HAS ACTIVE REQUEST
    // =====================================================

    public boolean hasActiveRequest(
            int requesterId,
            int skillId) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = null;


        try {

            cursor =
                    db.rawQuery(

                            "SELECT transaction_id " +

                                    "FROM TRANSACTIONS " +

                                    "WHERE requester_id = ? " +

                                    "AND skill_id = ? " +

                                    "AND (" +

                                    "status = 'Pending' " +

                                    "OR status = 'Approved'" +

                                    ") " +

                                    "LIMIT 1",

                            new String[]{

                                    String.valueOf(
                                            requesterId
                                    ),

                                    String.valueOf(
                                            skillId
                                    )
                            }
                    );


            return cursor.moveToFirst();


        } catch (Exception e) {

            e.printStackTrace();

            return false;


        } finally {

            if (cursor != null) {

                cursor.close();
            }
        }
    }


    // =====================================================
    // REQUEST SESSION
    // =====================================================

    public boolean requestSession(
            int requesterId,
            int providerId,
            int skillId) {

        SQLiteDatabase db =
                getWritableDatabase();

        db.beginTransaction();


        Cursor skillCursor = null;

        Cursor balanceCursor = null;

        Cursor existingCursor = null;


        try {

            // ==========================================
            // FIND ACTUAL OWNER FROM SKILLS TABLE
            // ==========================================

            skillCursor =
                    db.rawQuery(

                            "SELECT user_id " +

                                    "FROM SKILLS " +

                                    "WHERE skill_id = ?",

                            new String[]{
                                    String.valueOf(skillId)
                            }
                    );


            if (!skillCursor.moveToFirst()) {

                return false;
            }


            int actualProviderId =
                    skillCursor.getInt(0);


            // Use database owner, not passed-in owner
            providerId =
                    actualProviderId;


            // ==========================================
            // CANNOT REQUEST YOUR OWN SKILL
            // ==========================================

            if (requesterId ==
                    providerId) {

                return false;
            }


            // ==========================================
            // BLOCK DUPLICATE ACTIVE REQUEST
            // ==========================================

            existingCursor =
                    db.rawQuery(

                            "SELECT transaction_id " +

                                    "FROM TRANSACTIONS " +

                                    "WHERE requester_id = ? " +

                                    "AND skill_id = ? " +

                                    "AND (" +

                                    "status = 'Pending' " +

                                    "OR status = 'Approved'" +

                                    ") " +

                                    "LIMIT 1",

                            new String[]{

                                    String.valueOf(
                                            requesterId
                                    ),

                                    String.valueOf(
                                            skillId
                                    )
                            }
                    );


            if (existingCursor.moveToFirst()) {

                return false;
            }


            // ==========================================
            // CHECK REQUESTER CREDIT BALANCE
            // ==========================================

            balanceCursor =
                    db.rawQuery(

                            "SELECT time_credits " +

                                    "FROM USERS " +

                                    "WHERE user_id = ?",

                            new String[]{
                                    String.valueOf(
                                            requesterId
                                    )
                            }
                    );


            if (!balanceCursor.moveToFirst()) {

                return false;
            }


            int balance =
                    balanceCursor.getInt(0);


            // Request costs 2 TC
            if (balance < 2) {

                return false;
            }


            // ==========================================
            // REMOVE 2 CREDITS
            // ==========================================

            db.execSQL(

                    "UPDATE USERS " +

                            "SET time_credits = " +
                            "time_credits - 2 " +

                            "WHERE user_id = ?",

                    new Object[]{
                            requesterId
                    }
            );


            // ==========================================
            // CREATE PENDING SESSION
            // ==========================================

            db.execSQL(

                    "INSERT INTO TRANSACTIONS " +

                            "(" +

                            "requester_id, " +

                            "provider_id, " +

                            "skill_id, " +

                            "status" +

                            ") " +

                            "VALUES (?, ?, ?, 'Pending')",

                    new Object[]{

                            requesterId,

                            providerId,

                            skillId
                    }
            );


            db.setTransactionSuccessful();


            return true;


        } catch (Exception e) {

            e.printStackTrace();

            return false;


        } finally {

            if (skillCursor != null) {

                skillCursor.close();
            }


            if (balanceCursor != null) {

                balanceCursor.close();
            }


            if (existingCursor != null) {

                existingCursor.close();
            }


            db.endTransaction();
        }
    }


    // =====================================================
    // ACCEPT SESSION
    // =====================================================

    public boolean acceptSession(
            int sessionId) {

        SQLiteDatabase db =
                getWritableDatabase();


        try {

            Cursor cursor =
                    db.rawQuery(

                            "SELECT status " +

                                    "FROM TRANSACTIONS " +

                                    "WHERE transaction_id = ?",

                            new String[]{
                                    String.valueOf(sessionId)
                            }
                    );


            if (!cursor.moveToFirst()) {

                cursor.close();

                return false;
            }


            String status =
                    cursor.getString(0);


            cursor.close();


            if (!"Pending"
                    .equalsIgnoreCase(status)) {

                return false;
            }


            db.execSQL(

                    "UPDATE TRANSACTIONS " +

                            "SET status = 'Approved' " +

                            "WHERE transaction_id = ?",

                    new Object[]{
                            sessionId
                    }
            );


            return true;


        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    // =====================================================
    // COMPLETE SESSION + SAVE RATING + REWARD PROVIDER
    // =====================================================

    public boolean completeSessionTransaction(
            int sessionId,
            int receiverId,
            int stars) {

        if (stars < 1 ||
                stars > 5) {

            return false;
        }


        SQLiteDatabase db =
                getWritableDatabase();

        db.beginTransaction();


        Cursor cursor = null;


        try {

            cursor =
                    db.rawQuery(

                            "SELECT " +

                                    "status, " +

                                    "rating, " +

                                    "provider_id " +

                                    "FROM TRANSACTIONS " +

                                    "WHERE transaction_id = ?",

                            new String[]{
                                    String.valueOf(
                                            sessionId
                                    )
                            }
                    );


            if (!cursor.moveToFirst()) {

                return false;
            }


            String currentStatus =
                    cursor.getString(0);


            int currentRating =
                    cursor.getInt(1);


            int providerId =
                    cursor.getInt(2);


            // Only approved sessions can complete
            if (!"Approved"
                    .equalsIgnoreCase(
                            currentStatus
                    )) {

                return false;
            }


            // Prevent rating/reward twice
            if (currentRating > 0) {

                return false;
            }


            // Always reward/rate actual provider
            receiverId =
                    providerId;


            // ======================================
            // STAR → CREDIT REWARD
            // ======================================

            int credits;


            switch (stars) {

                case 1:

                    credits = 2;

                    break;


                case 2:

                    credits = 3;

                    break;


                case 3:

                    credits = 5;

                    break;


                case 4:

                    credits = 7;

                    break;


                case 5:

                default:

                    credits = 10;

                    break;
            }


            // ======================================
            // GIVE CREDITS TO PROVIDER
            // ======================================

            db.execSQL(

                    "UPDATE USERS " +

                            "SET time_credits = " +
                            "time_credits + ? " +

                            "WHERE user_id = ?",

                    new Object[]{

                            credits,

                            providerId
                    }
            );


            // ======================================
            // COMPLETE AND STORE RATING
            // ======================================

            db.execSQL(

                    "UPDATE TRANSACTIONS " +

                            "SET " +

                            "status = 'Completed', " +

                            "rating = ?, " +

                            "rated_user_id = ? " +

                            "WHERE transaction_id = ?",

                    new Object[]{

                            stars,

                            providerId,

                            sessionId
                    }
            );


            db.setTransactionSuccessful();


            return true;


        } catch (Exception e) {

            e.printStackTrace();

            return false;


        } finally {

            if (cursor != null) {

                cursor.close();
            }


            db.endTransaction();
        }
    }


    // =====================================================
    // CANCEL SESSION
    // REFUNDS REQUESTER 2 CREDITS
    // =====================================================

    public boolean cancelSession(
            int sessionId) {

        SQLiteDatabase db =
                getWritableDatabase();

        db.beginTransaction();


        Cursor cursor = null;


        try {

            cursor =
                    db.rawQuery(

                            "SELECT " +

                                    "requester_id, " +

                                    "status " +

                                    "FROM TRANSACTIONS " +

                                    "WHERE transaction_id = ?",

                            new String[]{
                                    String.valueOf(
                                            sessionId
                                    )
                            }
                    );


            if (!cursor.moveToFirst()) {

                return false;
            }


            int requesterId =
                    cursor.getInt(0);


            String status =
                    cursor.getString(1);


            if ("Completed"
                    .equalsIgnoreCase(status)) {

                return false;
            }


            if ("Cancelled"
                    .equalsIgnoreCase(status)) {

                return false;
            }


            // ======================================
            // REFUND REQUESTER
            // ======================================

            db.execSQL(

                    "UPDATE USERS " +

                            "SET time_credits = " +
                            "time_credits + 2 " +

                            "WHERE user_id = ?",

                    new Object[]{
                            requesterId
                    }
            );


            // ======================================
            // CANCEL SESSION
            // ======================================

            db.execSQL(

                    "UPDATE TRANSACTIONS " +

                            "SET status = 'Cancelled' " +

                            "WHERE transaction_id = ?",

                    new Object[]{
                            sessionId
                    }
            );


            db.setTransactionSuccessful();


            return true;


        } catch (Exception e) {

            e.printStackTrace();

            return false;


        } finally {

            if (cursor != null) {

                cursor.close();
            }


            db.endTransaction();
        }
    }


    // =====================================================
    // DISPUTE / REPORT ISSUE
    // =====================================================

    public boolean disputeSession(
            int sessionId) {

        SQLiteDatabase db =
                getWritableDatabase();


        try {

            db.execSQL(

                    "UPDATE TRANSACTIONS " +

                            "SET status = 'Disputed' " +

                            "WHERE transaction_id = ?",

                    new Object[]{
                            sessionId
                    }
            );


            return true;


        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    // =====================================================
    // COMPLETED SESSION COUNT
    // =====================================================

    public int getCompletedSessionCount(
            int userId) {

        Cursor cursor =
                getReadableDatabase()
                        .rawQuery(

                                "SELECT COUNT(*) " +

                                        "FROM TRANSACTIONS " +

                                        "WHERE (" +

                                        "requester_id = ? " +

                                        "OR provider_id = ?" +

                                        ") " +

                                        "AND status = 'Completed'",

                                new String[]{

                                        String.valueOf(
                                                userId
                                        ),

                                        String.valueOf(
                                                userId
                                        )
                                }
                        );


        int count = 0;


        if (cursor.moveToFirst()) {

            count =
                    cursor.getInt(0);
        }


        cursor.close();

        return count;
    }


    // =====================================================
    // GET AVERAGE RATING RECEIVED BY USER
    // =====================================================

    public float getAverageRating(
            int userId) {

        Cursor cursor =
                getReadableDatabase()
                        .rawQuery(

                                "SELECT AVG(rating) " +

                                        "FROM TRANSACTIONS " +

                                        "WHERE rated_user_id = ? " +

                                        "AND rating > 0 " +

                                        "AND status = 'Completed'",

                                new String[]{
                                        String.valueOf(
                                                userId
                                        )
                                }
                        );


        float average =
                0;


        if (cursor.moveToFirst()) {

            if (!cursor.isNull(0)) {

                average =
                        cursor.getFloat(0);
            }
        }


        cursor.close();


        return average;
    }


    // =====================================================
    // GET REVIEWS RECEIVED BY USER
    // RETURNS:
    // 0 = rating
    // 1 = reviewer name
    // 2 = skill title
    // =====================================================

    public Cursor getUserReviews(
            int userId) {

        String query =

                "SELECT " +

                        "t.rating, " +

                        "reviewer.name, " +

                        "s.title " +

                        "FROM TRANSACTIONS t " +

                        "JOIN SKILLS s " +

                        "ON t.skill_id = s.skill_id " +

                        "JOIN USERS reviewer " +

                        "ON reviewer.user_id = t.requester_id " +

                        "WHERE t.rated_user_id = ? " +

                        "AND t.rating > 0 " +

                        "AND t.status = 'Completed' " +

                        "ORDER BY t.transaction_id DESC";


        return getReadableDatabase()
                .rawQuery(

                        query,

                        new String[]{
                                String.valueOf(
                                        userId
                                )
                        }
                );
    }


    // =====================================================
    // WALLET TRANSACTIONS
    //
    // COLUMN 0 = transaction id
    // COLUMN 1 = requester id
    // COLUMN 2 = provider id
    // COLUMN 3 = skill
    // COLUMN 4 = partner name
    // COLUMN 5 = rating
    // =====================================================

    public Cursor getWalletTransactions(
            int userId) {

        String query =

                "SELECT " +

                        "t.transaction_id, " +

                        "t.requester_id, " +

                        "t.provider_id, " +

                        "s.title, " +

                        "partner.name, " +

                        "t.rating " +

                        "FROM TRANSACTIONS t " +

                        "JOIN SKILLS s " +

                        "ON t.skill_id = s.skill_id " +

                        "JOIN USERS partner " +

                        "ON partner.user_id = " +

                        "CASE " +

                        "WHEN t.requester_id = ? " +

                        "THEN t.provider_id " +

                        "ELSE t.requester_id " +

                        "END " +

                        "WHERE (" +

                        "t.requester_id = ? " +

                        "OR t.provider_id = ?" +

                        ") " +

                        "AND t.status = 'Completed' " +

                        "ORDER BY t.transaction_id ASC";


        return getReadableDatabase()
                .rawQuery(

                        query,

                        new String[]{

                                String.valueOf(
                                        userId
                                ),

                                String.valueOf(
                                        userId
                                ),

                                String.valueOf(
                                        userId
                                )
                        }
                );
    }


    // =====================================================
    // GET USER NAME
    // =====================================================

    public String getUserName(
            int userId) {

        Cursor cursor =
                getReadableDatabase()
                        .rawQuery(

                                "SELECT name " +

                                        "FROM USERS " +

                                        "WHERE user_id = ?",

                                new String[]{
                                        String.valueOf(
                                                userId
                                        )
                                }
                        );


        String name =
                "User";


        if (cursor.moveToFirst()) {

            String value =
                    cursor.getString(0);


            if (value != null &&
                    !value.trim().isEmpty()) {

                name =
                        value.trim();
            }
        }


        cursor.close();


        return name;
    }


    // =====================================================
    // GET USER BALANCE
    // =====================================================

    public int getUserCredits(
            int userId) {

        Cursor cursor =
                getReadableDatabase()
                        .rawQuery(

                                "SELECT time_credits " +

                                        "FROM USERS " +

                                        "WHERE user_id = ?",

                                new String[]{
                                        String.valueOf(
                                                userId
                                        )
                                }
                        );


        int credits =
                0;


        if (cursor.moveToFirst()) {

            credits =
                    cursor.getInt(0);
        }


        cursor.close();


        return credits;
    }
}