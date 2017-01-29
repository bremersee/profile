package org.bremersee.profile.test.model;

import junit.framework.TestCase;
import org.bremersee.profile.model.AbstractUserDto;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * @author Christian Bremer
 */
public class CommonUserDtoTest {

    private AbstractUserDto dto;

    @Before
    public void init() {
        dto = new AbstractUserDto() {
        };
        // format yyyy-MM-dd
        dto.setDateOfBirthString("1960-03-14");
        dto.setPreferredLocaleString("en_GB");
        dto.setPreferredTimeZoneID("Europe/Berlin");
    }

    @Test
    public void getDateOfBirth() throws Exception {
        System.out.println("Testing 'getDateOfBirth' on AbstractUserDto ...");
        Date birthday = dto.getDateOfBirth();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTime(birthday);
        TestCase.assertEquals(1960, cal.get(Calendar.YEAR));
        TestCase.assertEquals(2, cal.get(Calendar.MONTH));
        TestCase.assertEquals(14, cal.get(Calendar.DAY_OF_MONTH));
        System.out.println("Testing 'getDateOfBirth' on AbstractUserDto ... DONE!");
    }

    @Test
    public void setDateOfBirth() throws Exception {
        System.out.println("Testing 'setDateOfBirth' on AbstractUserDto ...");
        Date backup = dto.getDateOfBirth();

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.YEAR, 1975);
        cal.set(Calendar.MONTH, 4);
        cal.set(Calendar.DAY_OF_MONTH, 27);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        dto.setDateOfBirth(cal.getTime());

        TestCase.assertEquals("1975-05-27", dto.getDateOfBirthString());

        dto.setDateOfBirth(backup);
        System.out.println("Testing 'setDateOfBirth' on AbstractUserDto ... DONE!");
    }

    @Test
    public void getPreferredLocale() throws Exception {
        System.out.println("Testing 'getPreferredLocale' on AbstractUserDto ...");
        TestCase.assertEquals("en_GB", dto.getPreferredLocale().toString());
        System.out.println("Testing 'getPreferredLocale' on AbstractUserDto ... DONE!");

    }

    @Test
    public void setPreferredLocale() throws Exception {
        System.out.println("Testing 'setPreferredLocale' on AbstractUserDto ...");
        String backup = dto.getPreferredLocaleString();

        Locale locale = new Locale("de", "CH");
        dto.setPreferredLocale(locale);
        TestCase.assertEquals("de_CH", dto.getPreferredLocale().toString());

        dto.setPreferredLocaleString(backup);
        System.out.println("Testing 'setPreferredLocale' on AbstractUserDto ... DONE!");
    }

    @Test
    public void getPreferredLanguage() throws Exception {
        System.out.println("Testing 'getPreferredLanguage' on AbstractUserDto ...");
        TestCase.assertEquals("en", dto.getPreferredLanguage());
        System.out.println("Testing 'getPreferredLanguage' on AbstractUserDto ... DONE!");
    }

    @Test
    public void setPreferredLanguage() throws Exception {
        System.out.println("Testing 'setPreferredLanguage' on AbstractUserDto ...");
        String backup = dto.getPreferredLocaleString();

        dto.setPreferredLanguage("fr");
        TestCase.assertEquals("fr_GB", dto.getPreferredLocaleString());

        dto.setPreferredLocaleString(backup);
        System.out.println("Testing 'setPreferredLanguage' on AbstractUserDto ... DONE!");
    }

    @Test
    public void getPreferredCountry() throws Exception {
        System.out.println("Testing 'getPreferredCountry' on AbstractUserDto ...");
        TestCase.assertEquals("GB", dto.getPreferredCountry());
        System.out.println("Testing 'getPreferredCountry' on AbstractUserDto ... DONE!");
    }

    @Test
    public void setPreferredCountry() throws Exception {
        System.out.println("Testing 'setPreferredCountry' on AbstractUserDto ...");
        String backup = dto.getPreferredLocaleString();

        dto.setPreferredCountry("FR");
        TestCase.assertEquals("en_FR", dto.getPreferredLocaleString());

        dto.setPreferredLocaleString(backup);
        System.out.println("Testing 'setPreferredCountry' on AbstractUserDto ... DONE!");
    }

    @Test
    public void getPreferredTimeZone() throws Exception {
        System.out.println("Testing 'getPreferredTimeZone' on AbstractUserDto ...");
        TestCase.assertEquals("Europe/Berlin", dto.getPreferredTimeZone().getID());
        System.out.println("Testing 'getPreferredTimeZone' on AbstractUserDto ... DONE!");
    }

    @Test
    public void setPreferredTimeZone() throws Exception {
        System.out.println("Testing 'setPreferredTimeZone' on AbstractUserDto ...");
        String backup = dto.getPreferredTimeZoneID();

        dto.setPreferredTimeZone(TimeZone.getTimeZone("GMT"));
        TestCase.assertEquals("GMT", dto.getPreferredTimeZoneID());

        dto.setPreferredTimeZoneID(backup);
        System.out.println("Testing 'setPreferredTimeZone' on AbstractUserDto ... DONE!");
    }

}