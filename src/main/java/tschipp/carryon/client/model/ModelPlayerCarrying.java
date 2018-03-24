package tschipp.carryon.client.model;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;

public class ModelPlayerCarrying extends ModelPlayer
{

	public ModelPlayerCarrying(float modelSize, boolean smallArmsIn)
	{
		super(modelSize, smallArmsIn);
	}
	
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
	{
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		
		
		
	}

}
