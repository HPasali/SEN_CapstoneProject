package helpers;
import java.util.regex.Pattern;

//=>This class will be used as a helper class in order to control the entered inputs from the user by calling it on the Login, Register, EditProfileFragment, etc. pages;
public class RegexPatterns {
    //---------Regex Patterns;------------------------
    public static final Pattern EMAIL_PATTERN = Pattern.compile("[a-z0-9.-]+@[a-z]+\\.[a-z]{2,3}"); //username@domain.com
    public static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=_-])(?=\\S+$).{6,16}$"); //min 6 characters are needed to be saved on Firebase Db.
    //'P@ssw1rd' =>Contains at least one uppercase letter (P), one lowercase letter (s), one digit (0), and one special character (@), Does not contain any whitespace characters and meets the length requirement
    // of 6 characters.
    public static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+(?:[0-9]?){6,14}[0-9]$"); //+905555555555
    public static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s'-]+$"); //John Doe, O'Connor, Smith-Jones
    public static final Pattern SURNAME_PATTERN = Pattern.compile("^[A-Za-z\\s'-]+$"); //John Doe, O'Connor, Smith-Jones
    //-------------------------------------------
}
