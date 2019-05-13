package com.uottawa.thebench.utils;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.uottawa.thebench.model.Admin;
import com.uottawa.thebench.model.BasicUser;
import com.uottawa.thebench.model.Chore;
import com.uottawa.thebench.model.Household;
import com.uottawa.thebench.model.Resource;
import com.uottawa.thebench.model.User;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class for handling all SQLite related operations used across the app. Includes creating the
 * tables and storing, retrieving, updating and deleting the data.
 *
 * Note: In SQLite, images for avatars are stored using BLOB data type, boolean uses INTEGER data
 * type (0 or 1) and dates use INTEGER (date.getTime).
 *
 * Help for storing images in SQLLite from:
 * https://stackoverflow.com/a/11790199/2341126
 */
public class DBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "choresDB.db";

    // Household table and columns
    private static final String TABLE_HOUSEHOLD = "household";
    private static final String COLUMN_HOUSEHOLD_ID = "_id";
    private static final String COLUMN_HOUSEHOLD_NAME = "name";

    // Users table and columns
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USERS_ID = "_id";
    private static final String COLUMN_USERS_NAME = "name";
    private static final String COLUMN_USERS_AVATAR = "avatar";
    private static final String COLUMN_USERS_IS_ADMIN = "isAdmin";
    private static final String COLUMN_USERS_PASSWORD = "password";
    private static final String COLUMN_USERS_POINTS = "points";

    // Chores table and columns
    private static final String TABLE_CHORES = "chores";
    private static final String COLUMN_CHORES_ID = "_id";
    private static final String COLUMN_CHORES_NAME = "name";
    private static final String COLUMN_CHORES_DESCRIPTION = "description";
    private static final String COLUMN_CHORES_DEADLINE = "deadline";
    private static final String COLUMN_CHORES_IS_ALL_DAY = "isAllDay";
    private static final String COLUMN_CHORES_POINTS = "points";
    private static final String COLUMN_CHORES_STATUS = "status";
    private static final String COLUMN_CHORES_CREATED_BY_USER_ID = "createdByUserId";
    private static final String COLUMN_CHORES_ASSIGNED_TO_USER_ID = "assignedToUserId";

    // Resources table and columns
    private static final String TABLE_RESOURCES = "resources";
    private static final String COLUMN_RESOURCES_ID = "_id";
    private static final String COLUMN_RESOURCES_NAME = "name";
    private static final String COLUMN_RESOURCES_CHECKED = "checked";
    private static final String COLUMN_RESOURCES_CHORE_ID = "choreId";

    public static final int DEFAULT_CHORES_COUNT = 20;

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    /**
     * Create the tables.
     *
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_HOUSEHOLD_TABLE = "CREATE TABLE " + TABLE_HOUSEHOLD + "("
                + COLUMN_HOUSEHOLD_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_HOUSEHOLD_NAME + " TEXT"
                + ")";
        sqLiteDatabase.execSQL(CREATE_HOUSEHOLD_TABLE);

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USERS_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_USERS_NAME + " TEXT,"
                + COLUMN_USERS_AVATAR + " BLOB,"
                + COLUMN_USERS_IS_ADMIN + " INTEGER,"
                + COLUMN_USERS_PASSWORD + " TEXT,"
                + COLUMN_USERS_POINTS + " INTEGER"
                + ")";
        sqLiteDatabase.execSQL(CREATE_USERS_TABLE);

        String CREATE_CHORES_TABLE = "CREATE TABLE " + TABLE_CHORES + "("
                + COLUMN_CHORES_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_CHORES_NAME + " TEXT,"
                + COLUMN_CHORES_DESCRIPTION + " TEXT,"
                + COLUMN_CHORES_DEADLINE + " INTEGER,"
                + COLUMN_CHORES_IS_ALL_DAY + " INTEGER,"
                + COLUMN_CHORES_POINTS + " INTEGER,"
                + COLUMN_CHORES_STATUS + " TEXT,"
                + COLUMN_CHORES_CREATED_BY_USER_ID + " INTEGER,"
                + COLUMN_CHORES_ASSIGNED_TO_USER_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_CHORES_CREATED_BY_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_USERS_ID + ") ON DELETE SET NULL,"
                + "FOREIGN KEY(" + COLUMN_CHORES_ASSIGNED_TO_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_USERS_ID + ") ON DELETE SET NULL"
                + ")";
        sqLiteDatabase.execSQL(CREATE_CHORES_TABLE);

        String CREATE_RESOURCES_TABLE = "CREATE TABLE " + TABLE_RESOURCES + "("
                + COLUMN_RESOURCES_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_RESOURCES_NAME + " TEXT,"
                + COLUMN_RESOURCES_CHECKED + " INTEGER,"
                + COLUMN_RESOURCES_CHORE_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_RESOURCES_CHORE_ID + ") REFERENCES "
                + TABLE_CHORES + "(" + COLUMN_CHORES_ID + ") ON DELETE CASCADE"
                + ")";
        sqLiteDatabase.execSQL(CREATE_RESOURCES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_HOUSEHOLD);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CHORES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_RESOURCES);
    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Add a record for the household (should be only one).
     *
     * @param household Household object
     */
    public void addHousehold(Household household) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_HOUSEHOLD_NAME, household.getName());
        db.insert(TABLE_HOUSEHOLD, null, contentValues);
        db.close();
    }

    /**
     * Update the household record.
     *
     * @param household Household object
     * @return True if updated, false otherwise
     */
    public boolean updateHousehold(Household household) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_HOUSEHOLD_NAME, household.getName());
        boolean updated = db.update(TABLE_HOUSEHOLD, contentValues, COLUMN_HOUSEHOLD_ID + "=" + household.getId(), null) > 0;
        db.close();
        return updated;
    }

    /**
     * Get the household object.
     *
     * @return Household object
     */
    public Household getHousehold() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_HOUSEHOLD + " LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        Household household = new Household(-1, "");

        if (cursor.moveToFirst()) {
            household.setId(Integer.parseInt(cursor.getString(0)));
            household.setName(cursor.getString(1));
            cursor.close();
        }
        else {
            household = null;
        }
        db.close();
        return household;
    }

    /**
     * Add a record for a new user.
     *
     * @param user New user
     * @return Id of the new user
     */
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USERS_NAME, user.getName());
        if (user.getAvatar() != null) {
            // Store the avatar as a BLOB
            contentValues.put(COLUMN_USERS_AVATAR, getBytes(user.getAvatar()));
        }

        // If the user is an admin, include the password
        if (user.isAdmin()) {
            contentValues.put(COLUMN_USERS_IS_ADMIN, 1);
            contentValues.put(COLUMN_USERS_PASSWORD, ((Admin)user).getPassword());
        }
        else {
            contentValues.put(COLUMN_USERS_IS_ADMIN, 0);
        }
        contentValues.put(COLUMN_USERS_POINTS, user.getPoints());
        long userId = db.insert(TABLE_USERS, null, contentValues);
        db.close();
        return userId;
    }

    /**
     * Update a user record.
     *
     * @param user User to be updated
     * @return True if updated, false otherwise
     */
    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_USERS_NAME, user.getName());
        if (user.getAvatar() != null) {
            contentValues.put(COLUMN_USERS_AVATAR, getBytes(user.getAvatar()));
        }

        if (user.isAdmin()) {
            contentValues.put(COLUMN_USERS_IS_ADMIN, 1);
            contentValues.put(COLUMN_USERS_PASSWORD, ((Admin)user).getPassword());
        }
        else {
            contentValues.put(COLUMN_USERS_IS_ADMIN, 0);
        }
        contentValues.put(COLUMN_USERS_POINTS, user.getPoints());
        boolean updated = db.update(TABLE_USERS, contentValues, COLUMN_USERS_ID + "=" + user.getId(), null) > 0;
        db.close();
        return updated;
    }

    /**
     * Delete a user record.
     *
     * @param user User to delete
     * @return True if deleted, false otherwise
     */
    public boolean deleteUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean deleted = db.delete(TABLE_USERS, COLUMN_USERS_ID + "=" + user.getId(), null) > 0;
        db.close();
        return deleted;
    }

    /**
     * Find a user for a given id.
     *
     * @param userId User id
     * @return  User object
     */
    public User findUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = findUser(db, userId);
        db.close();
        return user;
    }

    /**
     * Private method to find a user given a database object and a user id.
     *
     * @param db SQLiteDatabase object
     * @param userId User id
     * @return User object
     */
    private User findUser(SQLiteDatabase db, int userId) {
        String query = "Select * FROM " + TABLE_USERS + " WHERE " +
                COLUMN_USERS_ID + " = " + userId;
        Cursor cursor = db.rawQuery(query, null);

        User user;

        if (cursor.moveToFirst()) {
            byte[] avatar = cursor.getBlob(2);
            Bitmap bitmap = null;
            if (avatar != null) {
                // Decode the BLOB field into a bitmap
                bitmap = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
            }
            boolean isAdmin = Integer.parseInt(cursor.getString(3)) == 1;
            // Create Admin or BasicUser object
            if (isAdmin) {
                user = new Admin(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        bitmap,
                        null,
                        cursor.getString(4),
                        Integer.parseInt(cursor.getString(5))
                );
            }
            else {
                user = new BasicUser(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        bitmap,
                        null,
                        Integer.parseInt(cursor.getString(5))
                );
            }
            cursor.close();
        }
        else {
            user = null;
        }
        return user;
    }

    /**
     * Private method for finding a user in a list of users, given the id.
     * @param userId User id
     * @param users List of users in the household
     * @return User object
     */
    private User findUser(int userId, List<User> users) {
        for (User user : users) {
            if (userId == user.getId()) {
                return user;
            }
        }
        return null;
    }

    public User findUserByName(String name) {
        List<User> users = getUsers();
        for (User user : users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Get a list of all users in the household.
     *
     * @return List of users
     */
    public List<User> getUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<User> users = getUsers(db);
        db.close();
        return users;
    }

    /**
     * Private method for getting all the users in the household, ordered by admins first, then
     * alphabetically.
     *
     * @param db SQLiteDatabase object
     * @return List of all users
     */
    private List<User> getUsers(SQLiteDatabase db) {
        String query = "SELECT * FROM " + TABLE_USERS + " ORDER BY " + COLUMN_USERS_IS_ADMIN
                + " DESC, " + COLUMN_USERS_NAME;
        Cursor cursor = db.rawQuery(query, null);

        List<User> users = new ArrayList<>();

        while (cursor.moveToNext()) {
            byte[] avatar = cursor.getBlob(2);
            Bitmap bitmap = null;
            if (avatar != null) {
                bitmap = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
            }
            boolean isAdmin = Integer.parseInt(cursor.getString(3)) == 1;
            if (isAdmin) {
                User user = new Admin(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        bitmap,
                        null,
                        cursor.getString(4),
                        Integer.parseInt(cursor.getString(5))
                );
                users.add(user);
            }
            else {
                User user = new BasicUser(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        bitmap,
                        null,
                        Integer.parseInt(cursor.getString(5))
                );
                users.add(user);
            }
        }
        cursor.close();
        return users;
    }

    /**
     * Add a chore record. Also add its resources if there are any.
     *
     * @param chore New chore
     * @return Id of the new chore
     */
    public long addChore(Chore chore) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CHORES_NAME, chore.getName());
        contentValues.put(COLUMN_CHORES_DESCRIPTION, chore.getDescription());
        contentValues.put(COLUMN_CHORES_DEADLINE, chore.getDeadline().getTime());
        contentValues.put(COLUMN_CHORES_IS_ALL_DAY, chore.isAllDay() ? 1 : 0);
        contentValues.put(COLUMN_CHORES_POINTS, chore.getPoints());
        contentValues.put(COLUMN_CHORES_STATUS, chore.getStatus().name());
        if (chore.hasCreator()) {
            contentValues.put(COLUMN_CHORES_CREATED_BY_USER_ID, chore.getCreator().getId());
        }
        if (chore.hasAssignee()) {
            contentValues.put(COLUMN_CHORES_ASSIGNED_TO_USER_ID, chore.getAssignee().getId());
        }
        long choreId = db.insert(TABLE_CHORES, null, contentValues);

        if (choreId > 0 && chore.hasResources()) {
            for (Resource resource : chore.getResources()) {
                ContentValues contentValuesResource = new ContentValues();
                contentValuesResource.put(COLUMN_RESOURCES_NAME, resource.getName());
                contentValuesResource.put(COLUMN_RESOURCES_CHECKED, 0);
                contentValuesResource.put(COLUMN_RESOURCES_CHORE_ID, choreId);
                db.insert(TABLE_RESOURCES, null, contentValuesResource);
            }
        }

        db.close();
        return choreId;
    }

    /**
     * Update an existing chore record, and its resources.
     *
     * @param chore Chore object
     * @return True if updated, false otherwise
     */
    public boolean updateChore(Chore chore) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CHORES_NAME, chore.getName());
        contentValues.put(COLUMN_CHORES_DESCRIPTION, chore.getDescription());
        contentValues.put(COLUMN_CHORES_DEADLINE, chore.getDeadline().getTime());
        contentValues.put(COLUMN_CHORES_IS_ALL_DAY, chore.isAllDay() ? 1 : 0);
        contentValues.put(COLUMN_CHORES_POINTS, chore.getPoints());
        contentValues.put(COLUMN_CHORES_STATUS, chore.getStatus().name());
        contentValues.put(COLUMN_CHORES_CREATED_BY_USER_ID, chore.getCreator().getId());
        if (chore.hasAssignee()) {
            contentValues.put(COLUMN_CHORES_ASSIGNED_TO_USER_ID, chore.getAssignee().getId());
        }
        if (chore.hasResources()) {
            for (Resource resource : chore.getResources()) {
                ContentValues contentValuesResource = new ContentValues();
                contentValuesResource.put(COLUMN_RESOURCES_NAME, resource.getName());
                contentValuesResource.put(COLUMN_RESOURCES_CHECKED, resource.isChecked() ? 1 : 0);
                db.update(TABLE_RESOURCES, contentValuesResource, COLUMN_RESOURCES_ID + "=" + resource.getId(), null);
            }
        }
        boolean updated = db.update(TABLE_CHORES, contentValues, COLUMN_CHORES_ID + "=" + chore.getId(), null) > 0;
        db.close();
        return updated;
    }

    /**
     * Delete a chore record.
     *
     * @param chore Chore object
     * @return True if deleted, false otherwise
     */
    public boolean deleteChore(Chore chore) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean deleted = db.delete(TABLE_CHORES, COLUMN_CHORES_ID + "=" + chore.getId(), null) > 0;
        return deleted;
    }

    /**
     * Delete all chore records.
     */
    public void deleteAllChores() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CHORES);
    }

    /**
     * Find a chore given an id.
     *
     * @param choreId Chore id
     * @return Chore object
     */
    public Chore findChore(int choreId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "Select * FROM " + TABLE_CHORES + " WHERE " +
                COLUMN_CHORES_ID + " = " + choreId;
        Cursor cursor = db.rawQuery(query, null);

        Chore chore;

        if (cursor.moveToFirst()) {
            User creator = null;
            User assignee = null;
            if (!cursor.isNull(7)) {
                creator = findUser(db, Integer.parseInt(cursor.getString(7)));
            }
            if (!cursor.isNull(8)) {
                assignee = findUser(db, Integer.parseInt(cursor.getString(8)));
            }

            chore = new Chore(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    cursor.getString(2),
                    new Date(cursor.getLong(3)),
                    Integer.parseInt(cursor.getString(4)) == 1,
                    Integer.parseInt(cursor.getString(5)),
                    Chore.Status.valueOf(cursor.getString(6)),
                    Chore.RecurrencePattern.NONE,
                    null,
                    creator
            );
            chore.setAssignee(assignee);
            chore.setResources(getResources(db, chore));
            cursor.close();
        }
        else {
            chore = null;
        }
        db.close();
        return chore;
    }

    /**
     * Get active chores for all users.
     *
     * @return List of chores (returns a max of DEFAULT_CHORES_COUNT chores)
     */
    public List<Chore> getChores() {
        return getChores(null, Chore.Status.ACTIVE, DEFAULT_CHORES_COUNT, 0);
    }

    /**
     * Get chores for either all users or a given user, and filtered by status.
     *
     * @param user User to get chores for, leave null if for all users
     * @param status Status of chores to retrieve
     * @return List of chores (returns a max of DEFAULT_CHORES_COUNT chores)
     */
    public List<Chore> getChores(User user, Chore.Status status) {
        return getChores(user, status, DEFAULT_CHORES_COUNT, 0);
    }

    /**
     * Get chores filtered by one or all users, status, count and offset. Ordered by the earliest
     * deadline (if status is completed order by deadline that is most in the future).
     *
     * @param user User to get chores for, leave null if for all users
     * @param status Status of chores to retrieve
     * @param count Number of chores to retrieve
     * @param offset Offset
     * @return List of chores
     */
    public List<Chore> getChores(User user, Chore.Status status, int count, int offset) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CHORES + " WHERE "
                + (user != null ? COLUMN_CHORES_ASSIGNED_TO_USER_ID + " = " + user.getId() + " AND " : "")
                + COLUMN_CHORES_STATUS + " = \"" + status.name() + "\""
                + " ORDER BY " + COLUMN_CHORES_DEADLINE + (status == Chore.Status.COMPLETED ? " DESC" : "")
                + "," + COLUMN_CHORES_NAME
                + " LIMIT " + count + " OFFSET " + offset;
        Cursor cursor = db.rawQuery(query, null);

        List<Chore> chores = new ArrayList<>();
        List<User> users = getUsers(db); // getting users this way so that avatars aren't constantly fetched

        while (cursor.moveToNext()) {
            Chore chore = new Chore(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    cursor.getString(2),
                    new Date(cursor.getLong(3)),
                    Integer.parseInt(cursor.getString(4)) == 1,
                    Integer.parseInt(cursor.getString(5)),
                    Chore.Status.valueOf(cursor.getString(6)),
                    Chore.RecurrencePattern.NONE,
                    null,
                    null
            );
            if (!cursor.isNull(7)) {
                chore.setCreator(findUser(Integer.parseInt(cursor.getString(7)), users));
            }
            if (!cursor.isNull(8)) {
                chore.setAssignee(findUser(Integer.parseInt(cursor.getString(8)), users));
            }
            chore.setResources(getResources(db, chore));
            chores.add(chore);
        }
        cursor.close();
        db.close();
        return chores;
    }

    /**
     * Add a resource record.
     *
     * @param resource Resource object (should include reference to chore object)
     * @return Id of new resource
     */
    public long addResource(Resource resource) {
        if (resource.getChore() == null) {
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_RESOURCES_NAME, resource.getName());
        contentValues.put(COLUMN_RESOURCES_CHECKED, resource.isChecked() ? 1 : 0);
        contentValues.put(COLUMN_RESOURCES_CHORE_ID, resource.getChore().getId());
        long choreId = db.insert(TABLE_RESOURCES, null, contentValues);

        db.close();
        return choreId;
    }

    /**
     * Update a resource record.
     *
     * @param resource Resource object
     * @return True if updated, false otherwise
     */
    public boolean updateResource(Resource resource) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_RESOURCES_NAME, resource.getName());
        contentValues.put(COLUMN_RESOURCES_CHECKED, resource.isChecked() ? 1 : 0);
        boolean updated = db.update(TABLE_RESOURCES, contentValues, COLUMN_RESOURCES_ID + "=" + resource.getId(), null) > 0;
        db.close();
        return updated;
    }

    /**
     * Delete a resource record.
     *
     * @param resource Resource object
     * @return True if deleted, false otherwise
     */
    public boolean deleteResource(Resource resource) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean deleted = db.delete(TABLE_RESOURCES, COLUMN_RESOURCES_ID + "=" + resource.getId(), null) > 0;
        return deleted;
    }

    /**
     * Get all resources belonging to a chore.
     *
     * @param choreId Chore id
     * @return List of resources
     */
//    public List<Resource> getResources(int choreId) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        List<Resource> resources = getResources(db, choreId);
//        db.close();
//        return resources;
//    }

    /**
     * Private method for getting resources belonging to a chore.
     *
     * @param db SQLiteDatabase object
     * @param chore Chore object
     * @return List of resources
     */
    private List<Resource> getResources(SQLiteDatabase db, Chore chore) {
        String query = "SELECT * FROM " + TABLE_RESOURCES + " WHERE "
                + COLUMN_RESOURCES_CHORE_ID + " = " + chore.getId()
                + " ORDER BY " + COLUMN_RESOURCES_NAME;
        Cursor cursor = db.rawQuery(query, null);

        List<Resource> resources = new ArrayList<>();

        while (cursor.moveToNext()) {
            Resource resource = new Resource(
                    Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    Integer.parseInt(cursor.getString(2)) == 1,
                    chore
            );
            resources.add(resource);
        }
        cursor.close();
        return  resources;
    }

    /**
     * Convert from bitmap to byte array
     *
     * @param bitmap Bitmap object
     * @return byte array
     */
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

}
