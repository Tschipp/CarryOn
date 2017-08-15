package tschipp.carryon.common.helper;

import tschipp.carryon.CarryOn;

public class InvalidConfigException extends Exception
{

	public InvalidConfigException(String cause)
	{
		super(cause);
	}

	public void printException()
	{
		CarryOn.LOGGER.error(this.getMessage());
		for (int i = 0; i < this.getStackTrace().length; i++)
		{
			StackTraceElement element = this.getStackTrace()[i];
			CarryOn.LOGGER.error(element.toString());
			
			if(i >= 10)
			{
				CarryOn.LOGGER.error((this.getStackTrace().length - 10) + " more...");
				break;
			}
		}
		
		CarryOn.LOGGER.info("");
	}
	
}
