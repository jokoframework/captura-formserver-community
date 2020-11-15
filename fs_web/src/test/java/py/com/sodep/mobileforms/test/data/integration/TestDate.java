package py.com.sodep.mobileforms.test.data.integration;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

import py.com.sodep.mobileforms.utils.TemporalUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestDate {

	public static final class A {
		String name;
		Date d;
		Double db;
		Long c;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Date getD() {
			return d;
		}

		public void setD(Date d) {
			this.d = d;
		}

		public Double getDb() {
			return db;
		}

		public void setDb(Double db) {
			this.db = db;
		}

		public Long getC() {
			return c;
		}

		public void setC(Long c) {
			this.c = c;
		}

	}

	public static void main(String[] args) throws ParseException, JsonGenerationException, JsonMappingException,
			IOException {

		java.text.SimpleDateFormat format = new SimpleDateFormat(TemporalUtils.DATE_TIME_FORMAT);
		java.util.Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
		format.setCalendar(cal);
		java.util.Date date = format.parse("2003-01-25 04:15:30");
		System.out.println(date);
		System.out.println(date.getTime());

		format = new SimpleDateFormat(TemporalUtils.DATE_TIME_FORMAT);
		cal = Calendar.getInstance(new SimpleTimeZone(60 * 60 * 1000, "GMT"));
		format.setCalendar(cal);
		date = format.parse("2003-01-25 05:15:30");
		System.out.println(date);
		System.out.println(date.getTime());

		A a = new A();
		a.name = "Somebody";
		a.d = new Date();
		a.db = new Double(3.39);
		a.c = 3l;

		ObjectMapper mapper = new ObjectMapper();
		String s = mapper.writeValueAsString(a);
		System.out.println(s);
	}
}
