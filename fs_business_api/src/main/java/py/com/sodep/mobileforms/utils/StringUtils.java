package py.com.sodep.mobileforms.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.FontMetrics;
import java.text.BreakIterator;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.SwingUtilities;

public class StringUtils {

	private static Logger logger = LoggerFactory.getLogger(StringUtils.class);


	public static boolean isEmpty(String string) {
		if ((string == null) || (string.trim().length() == 0)) {
			return true;
		}
		return false;
	}

	public static String addQuote(String s) {
		return " \"" + s + "\" ";
	}

	public static String addQuote(Object v) {
		return addQuote((v != null) ? v.toString() : null);
	}

	public static String getStackTraceAsString(Throwable e) {
		StringBuffer buff = new StringBuffer();
		if(e != null) {
			StackTraceElement elements[] = e.getStackTrace();
			for (StackTraceElement st : elements) {
				buff.append(st.getClassName()).append("-").append(st.getMethodName()).append("(Line ")
						.append(st.getLineNumber()).append(")").append("\n");
			}
		} else {
			logger.error("Throwable is null!");
		}
		return buff.toString();
	}

	/**
	 * Searches for a string inside an array. If the array or the string are
	 * null, the method return false
	 * 
	 * @param string
	 *            a not null String
	 * @param array
	 *            a not null array
	 * @return true if the string is equals at least one element in the array.
	 *         False otherwise.
	 */
	public static boolean isStringContained(String string, String[] array) {

		if (string == null) {
			// A null string is not useful in this case
			return false;
		}
		if (array == null) {
			// String can't be contained in a empty array
			return false;
		}
		for (String element : array) {
			if (string.equals(element)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkStringContains(String fullString, String searchedString) {
		int index1 = fullString.toLowerCase().indexOf(searchedString.toLowerCase());
		if (index1 != -1) {
			return true;
		} else {
			return false;
		}
	}

	public static String getOrdinalFor(int value) {
		int tenRemainder = value % 10;
		switch (tenRemainder) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}

	public static String removeExtensionFromFilename(String filename) {
		CharSequence separator = new String(".");
		if (!filename.contains(separator)) {
			// filename does not contain any valid extension
			return filename;
		}

		String withoutExtension = filename.substring(0, filename.lastIndexOf('.'));
		return withoutExtension;

	}

	public static String addExtensionToFilename(String filename, String extension) {
		String withoutExtension = removeExtensionFromFilename(filename);
		return withoutExtension + "." + extension;
	}

	/*
	 * Commented because it depends on common-lang (danicricco: I couldn't find
	 * the maven dependency) public static String getRandomPassword(int count,
	 * boolean letters, boolean numbers) { return
	 * RandomStringUtils.random(count, letters, numbers); }
	 */

	/**
	 * Returns an HTML string wrapped to specified width and number of lines.
	 * 
	 * @param fm
	 *            Font metrics
	 * @param text
	 *            The text to be wrapped
	 * @param maxNumberLines
	 *            Maximum number of lines that the text can occupy
	 * @param width
	 *            Maximum width that the text can occupy
	 * @return
	 */
	public static String wrapText(FontMetrics fm, String text, int maxNumberLines, int width) {
		int containerWidth = width;

		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(text);

		StringBuffer trial = new StringBuffer();
		// StringBuffer real = new StringBuffer("<html>");

		StringBuffer real = new StringBuffer("");
		real.append("<html>");
		int counter = 1;

		int start = boundary.first();
		for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {

			String word = text.substring(start, end);

			int nextEnd = boundary.next();
			if (nextEnd != BreakIterator.DONE) {
				String nextWord = text.substring(end, nextEnd);
				// FIXME: check for all the punctuation symbols, not just these.
				if (nextWord.equals(".") || nextWord.equals(",") || nextWord.equals(":") || nextWord.equals(";")) {
					word = text.substring(start, nextEnd);
					end++;

				} else {
					boundary.previous();
				}
			}

			trial.append(word);

			// Compute the width of the string using a font with the specified
			// "metrics" (sizes).
			int trialWidth = SwingUtilities.computeStringWidth(fm, trial.toString());
			if (trialWidth > containerWidth) {
				trial = new StringBuffer(word);

				if (counter >= maxNumberLines) {
					return real.toString();
				}

				real.append("<br>");
				counter++;

			}

			real.append(word);
		}
		real.append("</html>");
		// real.append("</html>");

		// label.setText(real.toString());
		return real.toString();
	}

	/**
	 * Truncate the string if s.length is greater than length
	 * */
	public static String truncate(String s, Integer length) {
		if (s != null) {
			if (s.length() > length) {
				return s.substring(0, length);
			}
		}
		return s;
	}

	public static String trimWithCharList(String str, char[] list) {
		if (isEmpty(str) || list == null || list.length == 0) {
			return str;
		}

		char charStr = list[0];
		String replaceRegex = "^\\" + charStr + "|\\" + charStr + "$";

		if (list.length > 1) {
			for (int i = 1; i < list.length; i++) {
				charStr = list[i];
				replaceRegex += "|^\\" + charStr + " |\\" + charStr + "$";
			}
		}

		return str.replaceAll(replaceRegex, "");
	}

	/**
	 * Receives a collection of of objects and will execute the method
	 * {@link #toString()} of each object, separate them with the separator, and
	 * return the result as a String
	 * 
	 * @param coll
	 * @param separator
	 * @return
	 */
	public static String toStringWithSeparator(Collection<?> coll, String quote, String separator) {
		StringBuilder s = new StringBuilder();
		Iterator<?> iterator = coll.iterator();
		boolean isFirst = true;
		while (iterator.hasNext()) {
			if (!isFirst) {
				s.append(separator);
			}
			if (quote != null) {
				s.append(quote);
			}
			s.append(iterator.next().toString());
			if (quote != null) {
				s.append(quote);
			}
			isFirst = false;
		}

		return s.toString();
	}

	public static String toStringWithSeparator(Collection<?> coll, String separator) {
		return toStringWithSeparator(coll, null, separator);
	}
}