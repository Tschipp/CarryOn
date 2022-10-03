package tschipp.carryon.common.helper;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class KeyboardCallbackWrapper
{
	GLFWKeyCallback oldCallback;

	public void setup(Minecraft mc)
	{
		this.oldCallback = GLFW.glfwSetKeyCallback(mc.getWindow().getWindow(), this::keyCallback);
	}

	private void keyCallback(long window, int key, int scancode, int action, int mods)
	{
		KeyPressedEvent event = new KeyPressedEvent(key, scancode);
		MinecraftForge.EVENT_BUS.post(event);

		if (event.isCanceled())
			return;

		if (this.oldCallback != null)
			this.oldCallback.invoke(window, key, scancode, action, mods);
	}

	@Cancelable
	public static class KeyPressedEvent extends Event
	{
		public int key;
		public int scancode;

		public KeyPressedEvent(int key, int scancode)
		{
			this.key = key;
			this.scancode = scancode;
		}

	}
}