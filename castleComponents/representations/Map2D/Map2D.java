package castleComponents.representations.Map2D;

import java.util.List;

import castleComponents.Entity;
import castleComponents.objects.Vector2;

public class Map2D {
	
	public Map2D(){}
	
	public Vector2 getPositionOfEntity(Entity e){
		//Find entity and return its position
		//Otherwise return the NULL vector2
		
		
		return null;
	}
	
	//This should return states
	public boolean moveTo(Entity e, Vector2 pos){
		//Move an entity to a particular location
		//If pos is out of bounds 
		return true;
	}
	
	public Outcome moveToWithVelocity(Entity e, Vector2 pod, Vector2 vel){
		
	}
	
	public boolean isRoad(Vector2:pos){
		
	}
	
	public boolean isPark(Vector2:pos){
		
	}
	public boolean isNoGo(Vector2:pos){
		
	}
	public int countEntitiesInRange(Vector2 pos, Vector2 range){
		
	}
	public int countEntitiesInRange(Vector2 pos, int range){
		
	}
	
	public boolean isType(String typeName){
		
	}
	
	public boolean addMapSection(Map2D map){
		
	}
	
	public Map2D getMapSection(Vector2 v){
					
	}
	
	public List<Entity> getEntitiesAtPos(Vector2 pos){
		
	}
	
	public boolean addEntity(Entity e){
		
	}
	
	public Park getParkAtPos(Vector2 pos){
		
	}
	
	public boolean entityParking(Entity e, Vector2 pos){
		
	}

}

enum Outcome {
	OUT_OF_BOUNDS, INVALID;
}
