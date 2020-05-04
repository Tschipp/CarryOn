package tschipp.carryon.common.helper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * Utility methods for reflection.
 *
 * @author Choonster
 */
public class ReflectionUtil
{
	
	public static MethodHandle findMethod(final Class<?> clazz, final String methodName, @Nullable final String methodObfName, final Class<?>... parameterTypes)
	{
		final Method method = ReflectionHelper.findMethod(clazz, methodName, methodObfName, parameterTypes);
		try
		{
			return MethodHandles.lookup().unreflect(method);
		}
		catch (IllegalAccessException e)
		{
			throw new ReflectionHelper.UnableToFindMethodException(e);
		}
	}

	
	public static MethodHandle findFieldGetter(Class<?> clazz, String... fieldNames)
	{
		final Field field = ReflectionHelper.findField(clazz, fieldNames);

		try
		{
			return MethodHandles.lookup().unreflectGetter(field);
		}
		catch (IllegalAccessException e)
		{
			throw new ReflectionHelper.UnableToAccessFieldException(fieldNames, e);
		}
	}

	
	public static MethodHandle findFieldSetter(Class<?> clazz, String... fieldNames)
	{
		final Field field = ReflectionHelper.findField(clazz, fieldNames);

		try
		{
			return MethodHandles.lookup().unreflectSetter(field);
		}
		catch (IllegalAccessException e)
		{
			throw new ReflectionHelper.UnableToAccessFieldException(fieldNames, e);
		}
	}
}