package project.game.abstractengine.entitymanager;

import java.util.ArrayList;
import java.util.List;
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.HashSet;
import java.util.Set;


public class EntityManager {
	
	private List<Entity> entityList;
	private Set<String> entityIDs;
	
	public EntityManager() {
		this.entityList = new ArrayList<>();
		this.entityIDs = new HashSet<>();
		
	}
	
	public boolean addEntity(Entity entity) {
		if (entityIDs.contains(entity.getID())) {
			System.out.println("Duplicate ID: " + entity.getID());
			return false;
		}
		entityList.add(entity);
		entityIDs.add(entity.getID());
		return true;
	}
	
	public void removeEntity(Entity entity) {
		entityList.remove(entity);
		entityIDs.remove(entity.getID());
	}
	
	public List<Entity> getEntities() {
		return entityList;
	}
	
	public void update() {
		for (Entity entity: entityList) {
			entity.update();
		}
		
	}
}
