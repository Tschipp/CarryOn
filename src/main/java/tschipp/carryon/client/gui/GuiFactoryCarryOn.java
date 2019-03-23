//package tschipp.carryon.client.gui;
//
//import java.util.Set;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiScreen;
//import net.minecraftforge.fml.client.IModGuiFactory;
//
//public class GuiFactoryCarryOn implements IModGuiFactory
//{
//    @Override
//    public void initialize(Minecraft minecraftInstance) {
//        // Do nothing
//    }
//
//    /*
//    @Override
//    public Class<? extends GuiScreen> mainConfigGuiClass() {
//        return GuiConfigCarryOn.class;
//    } */
//
//    @Override
//    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
//        return null;
//    }
//
//    /*
//    @Override
//    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
//        return null;
//    } */
//
//   
//	@Override
//	public boolean hasConfigGui()
//	{
//		return true;
//	}
//
//	@Override
//	public GuiScreen createConfigGui(GuiScreen parentScreen)
//	{
//
//		return new GuiConfigCarryOn(parentScreen);
//	}
//	
//
//
//}