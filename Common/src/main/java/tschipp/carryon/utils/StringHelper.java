package tschipp.carryon.utils;

public class StringHelper
{
	public static boolean matchesWildcards(String str, String[] wildcards)
	{
		for(String w : wildcards)
		{
			if(!str.contains(w))
				return false;
			int i = str.indexOf(w);
			str = str.substring(i + w.length());
		}

		return true;
	}

}
