package com.uottawa.thebench.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import com.uottawa.thebench.model.Chore;
import com.uottawa.thebench.model.User;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

/**
 * Class for utility functions used across the app.
 */
public class Utils {

    /*
    * Code from:
    * https://stackoverflow.com/a/15537470/2341126
    * By: diesel
    * */
    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * https://stackoverflow.com/a/9563438/2341126
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * https://stackoverflow.com/a/9563438/2341126
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static void createRandomChores(Context context, List<User> users) {
        if (context == null || users == null || users.size() == 0) {
            return;
        }
        final String[] choreNames = {"Clean garage","Wash car","Do dishes","Get groceries","Change tires","Clean room","Fix the computer","Dust the living room","Clean the fridge","Vacuum","Take ou the trash","Clean the bathrooms","Clean the dishwasher","Deep clean the couches","Mow the lawn","Shovel the snow","Do laundry","Organize the pantry","Mop the floor","Take out the compost","Clear the gutters","Pick up the mail","Clean the outside walls","Water the plants","Feed the fish","Clean the shower","Plant a tree","Rake the leaves","Buy Christmas wrapping","Clean the tub","Put chlorine in the pool","Clean the pool","Buy propane for the BBQ","Find the car keys","Wash the dog","Wash the cat"};
        //final int[] months = {Calendar.JANUARY,Calendar.FEBRUARY,Calendar.MARCH,Calendar.APRIL,Calendar.MAY,Calendar.JUNE,Calendar.JULY,Calendar.AUGUST,Calendar.SEPTEMBER,Calendar.OCTOBER,Calendar.NOVEMBER,Calendar.DECEMBER};
        final int[] months = {Calendar.NOVEMBER,Calendar.DECEMBER};
        final int[] minutes = {0,15,30,45};
        final Chore.Status[] otherStatuses = {Chore.Status.POSTPONED, Chore.Status.COMPLETED};

        DBHandler dbHandler = new DBHandler(context);
        dbHandler.deleteAllChores();

        List<Chore> chores = new ArrayList<>();

//        for (int n = 0; n < 10; n++)
        for (String choreName : choreNames) {
            Chore chore = new Chore(
                    -1,
                    choreName,
                    choreName,
                    new GregorianCalendar(2017, months[randInt(0,months.length - 1)], randInt(0,30), randInt(0,23), minutes[randInt(0,minutes.length - 1)]).getTime(),
                    randInt(0,2) == 0,
                    randInt(5,100),
                    Chore.Status.ACTIVE,
                    Chore.RecurrencePattern.NONE,
                    null,
                    users.get(randInt(0,users.size()-1))
            );
            chore.setAssignee(users.get(randInt(0,users.size()-1)));

            DateTime deadline = new DateTime(chore.getDeadline());
            if (LocalDateTime.now().compareTo(new LocalDateTime(deadline)) > 0) { // past date
                chore.setStatus(otherStatuses[randInt(0,otherStatuses.length - 1)]);
            }

            // add random resources
            for (int i = 1; i <= randInt(1, 6); i++) {
                chore.addResource(-1,"Resource " + i,false);
            }

            dbHandler.addChore(chore);
        }
    }

    /**
     * https://stackoverflow.com/a/363692/2341126
     *
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((max - min) + 1) + min;
    }

    /**
     * Get the deadline string given the date and if it's all day or not.
     *
     * @param deadline DateTime object
     * @param isAllDay Boolean
     * @return String
     */
    public static String getDeadlineString(DateTime deadline, boolean isAllDay) {
        String str = "Deadline: ";
        if (isToday(deadline)) {
            str += "Today";
        }
        else if (isTomorrow(deadline)) {
            str += "Tomorrow";
        }
        else if (isYesterday(deadline)) {
            str += "Yesterday";
        }
        else if (!isPast(deadline) && isBeforeNextWeek(deadline)) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("EEEE");
            str += dateTimeFormatter.print(deadline);
        }
        else {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MMM d");
            str += dateTimeFormatter.print(deadline);
        }

        if (!isAllDay) {
            str += " at ";
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("h:mm a");
            str += dateTimeFormatter.print(deadline);
        }

        return str;
    }

    /**
     * https://stackoverflow.com/a/42521090/2341126
     * @param time
     * @return
     */
    public static boolean isToday(DateTime time) {
        return LocalDate.now().compareTo(new LocalDate(time)) == 0;
    }

    /**
     * https://stackoverflow.com/a/42521090/2341126
     * @param time
     * @return
     */
    public static boolean isTomorrow(DateTime time) {
        return LocalDate.now().plusDays(1).compareTo(new LocalDate(time)) == 0;
    }

    /**
     * https://stackoverflow.com/a/42521090/2341126
     * @param time
     * @return
     */
    public static boolean isYesterday(DateTime time) {
        return LocalDate.now().minusDays(1).compareTo(new LocalDate(time)) == 0;
    }

    /**
     * Check if a date is in the past.
     *
     * @param time
     * @return True if in the past, false otherwise
     */
    public static boolean isPast(DateTime time) {
        return LocalDateTime.now().compareTo(new LocalDateTime(time)) > 0;
    }

    /**
     * Check if a date is before the next week (before 7 days from now).
     *
     * @param time
     * @return True if before next week, false otherwise
     */
    public static boolean isBeforeNextWeek(DateTime time) {
        return LocalDate.now().plusWeeks(1).compareTo(new LocalDate(time)) > 0;
    }

}
