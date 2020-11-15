package py.com.sodep.mobileforms.utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import py.com.sodep.mf.exchange.IllegalStringFormatException;

public class TemporalUtils {
	protected static Logger logger = LoggerFactory
			.getLogger(TemporalUtils.class);

	// TODO how to handle internationalization of dates
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static boolean isLegalDateStringWithFormat(String s, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		return sdf.parse(s, new ParsePosition(0)) != null;
	}

	public static Date shiftDays(Date pFecha, Integer pDays) {
		return shiftDate(pFecha, 0, 0, 0, pDays, 0, 0, false);
	}

	public static Date shiftDate(java.util.Date pFecha, Integer pSegundos,
			Integer pMinutos, Integer pHoras, Integer pDias, Integer pMeses,
			Integer pAnos, Boolean pAtrasar) {
		return shiftDate(pFecha, 0, pSegundos, pMinutos, pHoras, pDias, pMeses,
				pAnos, pAtrasar);
	}

	public static Date shiftDate(java.util.Date pFecha, Integer pMilisegundos,
			Integer pSegundos, Integer pMinutos, Integer pHoras, Integer pDias,
			Integer pMeses, Integer pAnos, Boolean pAtrasar) {
		Date retDate = null;
		if (pFecha != null) {
			Calendar c1 = Calendar.getInstance();
			c1.setTime(pFecha);
			int factorAtraso = pAtrasar ? -1 : 1;

			if (pDias != null) {
				c1.add(Calendar.DATE, pDias.intValue() * factorAtraso);
			}
			if (pMeses != null) {
				c1.add(Calendar.MONTH, pMeses.intValue() * factorAtraso);
			}
			if (pAnos != null) {
				c1.add(Calendar.YEAR, pAnos.intValue() * factorAtraso);
			}

			c1.add(Calendar.SECOND,
					pSegundos != null ? (pSegundos * factorAtraso) : 0);
			c1.add(Calendar.MINUTE,
					pMinutos != null ? (pMinutos * factorAtraso) : 0);
			c1.add(Calendar.HOUR, pHoras != null ? (pHoras * factorAtraso) : 0);
			c1.add(Calendar.MILLISECOND, pMilisegundos);

			return new Date(c1.getTimeInMillis());
		} else {
			logger.error("Date to be shifted should be different from null");
		}
		return retDate;
	}

	public static String formatDate(Date pDate, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String dateString = pDate != null ? sdf.format(pDate) : "<null value>";
		return dateString;
	}

	public static String formatDateTime(Date pDate) {
		return formatDate(pDate, DATE_TIME_FORMAT);
	}

	public static String formatDate(Date pDate) {
		return formatDate(pDate, DATE_FORMAT);
	}
	
	public static Date parseDate(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);

		try {
			return sdf.parse(str);
		} catch (ParseException e) {
			throw new IllegalStringFormatException(str + " is no a valid Date string "
					+ ". The expected type pattern is " + DATE_TIME_FORMAT);
		}
	}


}
