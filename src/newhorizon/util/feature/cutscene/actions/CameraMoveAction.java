package newhorizon.util.feature.cutscene.actions;

import arc.Core;
import arc.math.Mathf;
import arc.scene.actions.TemporalAction;
import newhorizon.util.feature.cutscene.UIActions;

public class CameraMoveAction extends TemporalAction{
	public float startX, startY;
	public float endX, endY;
	
	
	
	@Override
	protected void begin(){
		if(UIActions.disabled() || (Mathf.equal(startX, endX) && Mathf.equal(startY, endY))) return;
		startX = Core.camera.position.x;
		startY = Core.camera.position.y;
	}
	
	@Override
	protected void update(float percent){
		if(!UIActions.disabled())Core.camera.position.set(startX + (endX - startX) * percent, startY + (endY - startY) * percent);
	}
	
	@Override
	public void reset(){
		super.reset();
	}
	
	public void setPosition(float x, float y){
		endX = x;
		endY = y;
	}
	
	public float getX(){
		return endX;
	}
	
	public void setX(float x){
		endX = x;
	}
	
	public float getY(){
		return endY;
	}
	
	public void setY(float y){
		endY = y;
	}
}
